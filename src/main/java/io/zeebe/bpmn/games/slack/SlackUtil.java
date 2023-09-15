package io.zeebe.bpmn.games.slack;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SlackUtil {

  private static final Map<CardType, String> cardEmojis =
      Map.ofEntries(
          Map.entry(CardType.EXPLODING, ":bpmn-error-throw-event-red:"),
          Map.entry(CardType.DEFUSE, ":bpmn-error-catch-event-green:"),
          Map.entry(CardType.ATTACK, ":bpmn-parallel-gateway-purple:"),
          Map.entry(CardType.SEE_THE_FUTURE, ":bpmn-timer-catch-event-blue:"),
          Map.entry(CardType.ALTER_THE_FUTURE, ":bpmn-compensation-throw-event-blue:"),
          Map.entry(CardType.DRAW_FROM_BOTTOM, ":bpmn-escalation-throw-event-blue:"),
          Map.entry(CardType.SHUFFLE, ":bpmn-complex-gateway-blue:"),
          Map.entry(CardType.SKIP, ":bpmn-link-throw-event-blue:"),
          Map.entry(CardType.NOPE, ":bpmn-cancel-throw-event-yellow:"),
          Map.entry(CardType.FAVOR, ":bpmn-receive-task:"),
          Map.entry(CardType.FERAL_CAT, ":bpmn-none-task:"),
          Map.entry(CardType.CAT_1, ":bpmn-user-task:"),
          Map.entry(CardType.CAT_2, ":bpmn-manual-task:"),
          Map.entry(CardType.CAT_3, ":bpmn-service-task:"),
          Map.entry(CardType.CAT_4, ":bpmn-script-task:"),
          Map.entry(CardType.CAT_5, ":bpmn-business-rule-task:"),
          Map.entry(CardType.ATOMIC, ":bpmn-termination-end-event:"));

  public static String formatCard(Card card) {
    final var type = card.getType();
    return formatCardType(type);
  }

  public static String formatButtonCards(List<Card> cards) {

    final String emojis =
        cards.stream()
            .map(
                card -> {
                  final var type = card.getType();
                  return cardEmojis.get(type);
                })
            .collect(Collectors.joining(" & "));

    final String names =
        cards.stream()
            .map(
                card -> {
                  final var type = card.getType();
                  return type.name().toLowerCase().replaceAll("_", " ");
                })
            .collect(Collectors.joining(", ", "(", ")"));

    return emojis + " " + names;
  }

  public static String formatCardType(CardType type) {
    return Optional.ofNullable(cardEmojis.get(type))
        .map(
            e -> {
              final var typeName = type.name().toLowerCase().replaceAll("_", " ");
              return e + String.format(" _(%s)_", typeName);
            })
        .orElse(String.format("*%s*", type));
  }

  public static String formatCardTypePlain(CardType type) {
    return Optional.ofNullable(cardEmojis.get(type))
        .map(
            e -> {
              final var typeName = type.name().toLowerCase().replaceAll("_", " ");
              return e + String.format(" (%s)", typeName);
            })
        .orElse(String.format("%s", type));
  }

  public static String formatCards(List<Card> cards) {
    return cards.stream().map(SlackUtil::formatCard).collect(Collectors.joining(", "));
  }

  public static String formatPlayer(String userId) {
    if (!isBot(userId)) {
      return String.format("<@%s>", userId);
    } else {
      return String.format(":robot_face: _%s_", userId);
    }
  }

  public static boolean isBot(String userId) {
    return userId.startsWith("bot_");
  }
}
