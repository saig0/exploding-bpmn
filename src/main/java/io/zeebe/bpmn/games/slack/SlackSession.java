package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import io.zeebe.bpmn.games.GameListener.Context;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackSession {

  private static final Logger LOG = LoggerFactory.getLogger(SlackSession.class);

  private final Map<String, String> channelIdByUserId = new HashMap<>();
  private final Map<String, GameInfo> games = new HashMap<>();

  private final Map<String, Function<BlockActionPayload.Action, ActionResponse>> pendingActions =
      new HashMap<>();

  @Autowired private MethodsClient methodsClient;

  public String getChannelId(String userId) {
    return channelIdByUserId.computeIfAbsent(userId, this::resolveChannelId);
  }

  private String resolveChannelId(String userId) {
    try {
      final var response = methodsClient.conversationsOpen(r -> r.users(List.of(userId)));

      return response.getChannel().getId();

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getUserIdsOfGame(Context context) {
    return Optional.ofNullable(games.get(context.getKey()))
        .map(GameInfo::getUserIds)
        .orElse(context.getUserIds());
  }

  public String getGameChannelId(Context context) {
    return Optional.ofNullable(games.get(context.getKey()))
        .map(GameInfo::getChannelId)
        .orElse(context.getChannelId());
  }

  public void putGame(String key, String channelId, List<String> userIds) {
    games.put(key, new GameInfo(channelId, userIds));
  }

  public void removeGame(String key) {
    games.remove(key);
  }

  public void putPendingAction(
      String channelId, Function<BlockActionPayload.Action, ActionResponse> action) {
    pendingActions.put(channelId, action);
  }

  public Optional<Function<BlockActionPayload.Action, ActionResponse>> getPendingAction(
      String channelId) {
    return Optional.ofNullable(pendingActions.get(channelId));
  }

  public void removePendingAction(String channelId) {
    pendingActions.remove(channelId);
  }

  public static class GameInfo {

    private final String channelId;
    private final List<String> userIds;

    public GameInfo(String channelId, List<String> userIds) {
      this.channelId = channelId;
      this.userIds = userIds;
    }

    public List<String> getUserIds() {
      return userIds;
    }

    public String getChannelId() {
      return channelId;
    }
  }
}
