package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.BlockElement;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackUserInteraction implements GameInteraction {

  @Autowired private SlackSession session;

  @Autowired private MethodsClient methodsClient;

  @Autowired
  private ResponseSender responseSender;


  @Override
  public CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards) {

    final var channelId = session.getChannelId(player);

    final var block1 =
        SectionBlock.builder()
            .text(MarkdownTextObject.builder().text("Choose one of the cards to play").build())
            .build();

    final var passButton = ButtonElement.builder()
        .text(PlainTextObject.builder().text("PASS").build())
        .value("pass")
        .actionId("pass")
        .style("primary")
        .build();

    final List<BlockElement> cardButtons =
        handCards.stream()
            .map(
                card -> {
                  final var text =
                      PlainTextObject.builder().emoji(true).text(card.getType().name()).build();
                  final var value = card.getId() + "-" + card.getType();

                  return ButtonElement.builder()
                      .text(text)
                      .value(value)
                      .actionId("play-card-" + value)
                      .build();
                })
            .collect(Collectors.toList());

    cardButtons.add(0, passButton);

    final var block2 = ActionsBlock.builder().elements(cardButtons).build();

    try {

      final var resp = methodsClient
          .chatPostMessage(req -> req.channel(channelId).blocks(List.of(block1, block2)));



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

            final var cardIdAndType = actionValue.split("-");
            final var cardId = cardIdAndType[0];
            final var cardType = cardIdAndType[1];
            final var card = new Card(Integer.parseInt(cardId), CardType.valueOf(cardType));

            cards = List.of(card);
          }

          future.complete(cards);

          return ActionResponse.builder()
              .responseType("ephemeral")
              //.text(String.format("Action '%s' has been accepted.", action.getActionId()))
              .deleteOriginal(true)
              //.replaceOriginal(false)
              .build();
        });

    return future;
  }
}
