package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SelectAction implements JobHandler {

  private final GameListener listener;

  public SelectAction(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var player = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(player);

    final List<Card> cardsToPlay = selectCardsToPlay(hand);

    variables
        .putCards(cardsToPlay)
        .putLastPlayedCards(cardsToPlay);

    if (cardsToPlay.isEmpty()) {
      variables.putAction("pass");
      listener.playerPassed(GameContext.of(job), player);

    } else {

      final var card = cardsToPlay.get(0).getType();
      final var action = card.isCatCard() ? "cat-pair" : card.name().toLowerCase();

      variables.putAction(action);
      listener.cardsPlayed(GameContext.of(job), player, cardsToPlay);
    }

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }

  private List<Card> selectCardsToPlay(List<Card> handCards) {

    final boolean playCard = ThreadLocalRandom.current().nextDouble() < 0.25;
    if (!playCard) {
      // pass this turn
      return List.of();
    }

    final Map<CardType, List<Card>> catCards =
        handCards.stream()
            .filter(card -> card.getType().isCatCard())
            .collect(Collectors.groupingBy(Card::getType));

    final var twoSameCatCards =
        catCards.entrySet().stream()
            .filter(e -> e.getValue().size() >= 2)
            .map(e -> e.getValue().subList(0, 2))
            .findFirst();

    final var catCard =
        catCards.keySet().stream()
            .filter(card -> card != CardType.FERAL_CAT)
            .findFirst()
            .map(type -> catCards.get(type).get(0));

    final var feralCatCard =
        Optional.ofNullable(catCards.get(CardType.FERAL_CAT)).map(cards -> cards.get(0));

    final var feralAndOtherCat =
        Optional.of(catCards)
            .filter(cards -> cards.size() >= 2)
            .filter(cards -> cards.containsKey(CardType.FERAL_CAT))
            .map(cards -> List.of(feralCatCard.get(), catCard.get()));

    final var actionCards =
        handCards.stream()
            .filter(card -> card.getType() != CardType.NOPE && card.getType() != CardType.DEFUSE)
            .filter(card -> !card.getType().isCatCard())
            .collect(Collectors.toList());

    final var actionCard =
        Optional.of(actionCards)
            .filter(cards -> !cards.isEmpty())
            .map(
                cards -> {
                  final var cardIdx = ThreadLocalRandom.current().nextInt(0, cards.size());
                  return List.of(cards.get(cardIdx));
                });

    return twoSameCatCards
        .or(() -> feralAndOtherCat)
        .or(() -> actionCard)
        .orElse(List.of());
  }
}
