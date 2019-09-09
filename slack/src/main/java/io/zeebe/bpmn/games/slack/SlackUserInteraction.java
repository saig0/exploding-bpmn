package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.BlockElement;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackUserInteraction implements GameInteraction {

  private final ScheduledExecutorService executorService =
      Executors.newSingleThreadScheduledExecutor();
  @Autowired private SlackSession session;
  @Autowired private MethodsClient methodsClient;

  @Override
  public CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards) {

    final var channelId = session.getChannelId(player);

    final var blocks = new ArrayList<LayoutBlock>();

    final var block1 =
        SectionBlock.builder()
            .text(MarkdownTextObject.builder().text("Choose one of the cards to play").build())
            .build();
    blocks.add(block1);

    final var cardButtons = new ArrayList<BlockElement>();

    final var passButton =
        ButtonElement.builder()
            .text(PlainTextObject.builder().text("PASS").build())
            .value("pass")
            .actionId("pass")
            .style("primary")
            .build();

    cardButtons.add(passButton);

    final List<Card> nonPlayableCards = new ArrayList<>(handCards);

    final Map<CardType, List<Card>> catCards =
        handCards.stream()
            .filter(card -> card.getType().isCatCard())
            .collect(Collectors.groupingBy(Card::getType));

    final List<Card> feralCats = catCards.getOrDefault(CardType.FERAL_CAT, List.of());

    final List<Card> nonFeralCats =
        catCards.entrySet().stream()
            .filter(e -> e.getKey() != CardType.FERAL_CAT)
            .map(e -> e.getValue().get(0))
            .collect(Collectors.toList());

    if (feralCats.size() == 1) {
      final var feralCat = feralCats.get(0);
      nonFeralCats.forEach(cat -> cardButtons.add(buildActionButton(feralCat, cat)));

      nonPlayableCards.removeAll(feralCats);
      nonPlayableCards.removeAll(nonFeralCats);
    }

    final List<List<Card>> twoSameCatCards =
        catCards.entrySet().stream()
            .filter(e -> e.getValue().size() >= 2)
            .map(e -> e.getValue())
            .collect(Collectors.toList());

    twoSameCatCards.forEach(cats -> cardButtons.add(buildActionButton(cats.get(0), cats.get(1))));
    twoSameCatCards.forEach(nonPlayableCards::removeAll);

    final List<Card> actionCards =
        handCards.stream()
            .filter(card -> card.getType() != CardType.NOPE && card.getType() != CardType.DEFUSE)
            .filter(card -> !card.getType().isCatCard())
            .collect(Collectors.toList());

    actionCards.forEach(card -> cardButtons.add(buildActionButton(card)));
    nonPlayableCards.removeAll(actionCards);

    final var block2 = ActionsBlock.builder().elements(cardButtons).build();
    blocks.add(block2);

    if (!nonPlayableCards.isEmpty()) {

      final var block3 =
          SectionBlock.builder()
              .text(
                  MarkdownTextObject.builder()
                      .text(
                          String.format(
                              "Not playable cards: %s",
                              nonPlayableCards.stream()
                                  .map(card -> String.format("*%s*", card.getType().name()))
                                  .collect(Collectors.joining(", "))))
                      .build())
              .build();

      blocks.add(block3);
    }

    try {

      final var resp = methodsClient.chatPostMessage(req -> req.channel(channelId).blocks(blocks));

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }

    final var future = new CompletableFuture<List<Card>>();

    session.putPendingAction(
        channelId,
        action -> {
          final String actionValue = action.getValue();

          final List<Card> cards;
          if (actionValue.equals("pass")) {
            cards = List.of();

          } else {
            cards =
                Arrays.stream(actionValue.split("\\+"))
                    .map(
                        value -> {
                          final var cardIdAndType = value.split("-");
                          final var cardId = cardIdAndType[0];
                          final var cardType = cardIdAndType[1];

                          return new Card(Integer.parseInt(cardId), CardType.valueOf(cardType));
                        })
                    .collect(Collectors.toList());
          }

          future.complete(cards);

          return ActionResponse.builder()
              .responseType("ephemeral")
              // .text(String.format("Action '%s' has been accepted.", action.getActionId()))
              .deleteOriginal(true)
              // .replaceOriginal(false)
              .build();
        });

    return future;
  }

  @Override
  public CompletableFuture<Boolean> nopeThePlayedCard(String player) {
    final var channelId = session.getChannelId(player);

    final var block1 =
        SectionBlock.builder()
            .text(MarkdownTextObject.builder().text("Do you want to NOPE the card?").build())
            .build();

    final var nopeButton =
        ButtonElement.builder()
            .text(PlainTextObject.builder().text("NOPE!").build())
            .value("nope")
            .actionId("nope")
            .style("primary")
            .build();

    final var skipButton =
        ButtonElement.builder()
            .text(PlainTextObject.builder().text("Skip").build())
            .value("skip")
            .actionId("skip")
            .build();

    final var block2 = ActionsBlock.builder().elements(List.of(nopeButton, skipButton)).build();

    final var future = new CompletableFuture<Boolean>();

    try {

      final var resp =
          methodsClient.chatPostMessage(
              req -> req.channel(channelId).blocks(List.of(block1, block2)));

      final var messageTs = resp.getTs();

      executorService.schedule(
          () -> removeMessage(future, channelId, messageTs), 5, TimeUnit.SECONDS);

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }

    session.putPendingAction(
        channelId,
        action -> {
          final String actionValue = action.getValue();

          if (actionValue.equals("nope")) {
            future.complete(true);

          } else {
            future.complete(false);
          }

          return ActionResponse.builder().responseType("ephemeral").deleteOriginal(true).build();
        });

    return future;
  }

  private void removeMessage(
      CompletableFuture<Boolean> future, String channelId, String messageTs) {

    if (future.isDone()) {
      return;
    }

    try {
      methodsClient.chatDelete(req -> req.channel(channelId).ts(messageTs));
    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ButtonElement buildActionButton(Card card) {
    final var text = PlainTextObject.builder().emoji(true).text(card.getType().name()).build();
    final var value = card.getId() + "-" + card.getType();

    return ButtonElement.builder().text(text).value(value).actionId("play-card-" + value).build();
  }

  private ButtonElement buildActionButton(Card card1, Card card2) {
    final var text =
        PlainTextObject.builder()
            .text(String.format("%s & %s", card1.getType().name(), card2.getType().name()))
            .build();

    final var value =
        String.format(
            "%d-%s+%d-%s", card1.getId(), card1.getType(), card2.getId(), card2.getType());

    return ButtonElement.builder().text(text).value(value).actionId("play-card-" + value).build();
  }
}
