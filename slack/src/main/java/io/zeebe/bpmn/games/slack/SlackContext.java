package io.zeebe.bpmn.games.slack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlackContext {

  private static final Logger LOG = LoggerFactory.getLogger(SlackContext.class);

  private final Map<String, UserInfo> users = new HashMap<>();
  private final Map<Long, GameInfo> games = new HashMap<>();

  public void putUser(UserInfo info) {
    users.put(info.userId, info);

    LOG.debug("put user {}", info);
  }

  public UserInfo getUser(String userId) {
    return users.get(userId);
  }

  public void putGame(GameInfo info) {
    games.put(info.getKey(), info);

    LOG.debug("put game {}", info);
  }

  public GameInfo getGame(Long key) {
    return games.get(key);
  }

  public static class UserInfo {

    private final String userId;
    private final String userName;
    private final String channelId;

    public UserInfo(String userId, String userName, String channelId) {
      this.userId = userId;
      this.userName = userName;
      this.channelId = channelId;
    }

    public String getUserId() {
      return userId;
    }

    public String getUserName() {
      return userName;
    }

    public String getChannelId() {
      return channelId;
    }
  }

  public static class GameInfo {

    private final long key;
    private final List<UserInfo> players;

    public GameInfo(long key, List<UserInfo> players) {
      this.key = key;
      this.players = players;
    }

    public long getKey() {
      return key;
    }

    public List<UserInfo> getPlayers() {
      return players;
    }
  }
}
