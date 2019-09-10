package io.zeebe.bpmn.games.slack;

import io.zeebe.bpmn.games.model.Card;
import java.util.List;
import java.util.stream.Collectors;

public class SlackUtil {

  public static String formatCard(Card card) {
    return "*" + card.getType().name() + "*";
  }

  public static String formatCards(List<Card> cards) {
    return cards.stream().map(SlackUtil::formatCard).collect(Collectors.joining(", "));
  }

  public static String formatPlayer(String userId) {
    if (!isBot(userId)) {
      return String.format("<@%s>", userId);
    } else {
      return String.format("_%s_", userId);
    }
  }

  public static boolean isBot(String userId) {
    return userId.startsWith("bot_");
  }

}
