package io.zeebe.bpmn.games.bot;

import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class SimpleBot implements GameInteraction {

  @Override
  public CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards) {
    return CompletableFuture.completedFuture(selectCards(handCards));
  }

  @Override
  public CompletableFuture<Boolean> nopeThePlayedCard(String player) {
    final var wantToNope = ThreadLocalRandom.current().nextDouble() > 0.5;

    return CompletableFuture.completedFuture(wantToNope);
  }

  private List<Card> selectCards(List<Card> handCards) {

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

    return twoSameCatCards.or(() -> feralAndOtherCat).or(() -> actionCard).orElse(List.of());
  }

  @Override
  public CompletableFuture<List<Card>> alterTheFuture(String player, List<Card> cards) {

    final var alteredFuture = new ArrayList<>(cards);
    Collections.shuffle(alteredFuture);

    return CompletableFuture.completedFuture(alteredFuture);
  }

  @Override
  public CompletableFuture<String> selectPlayer(String player, List<String> otherPlayers) {

     final var index = ThreadLocalRandom.current().nextInt(0, otherPlayers.size());
     final var otherPlayer = otherPlayers.get(index);

    return CompletableFuture.completedFuture(otherPlayer);
  }
}
