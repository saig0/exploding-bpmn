package io.zeebe.bpmn.games.deck;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.GameState;
import io.zeebe.bpmn.games.model.Player;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BuildDeck implements JobHandler {

  private final GameListener listener;

  private final Map<CardType, List<Integer>> actionCards =
      Map.of(
          CardType.ATTACK, List.of(4, 7, 11),
          CardType.SKIP, List.of(4, 6, 10),
          CardType.SEE_THE_FUTURE, List.of(3, 3, 6),
          CardType.ALTER_THE_FUTURE, List.of(2, 4, 6),
          CardType.SHUFFLE, List.of(2, 4, 6),
          CardType.DRAW_FROM_BOTTOM, List.of(3, 4, 7),
          CardType.FAVOR, List.of(2, 4, 6),
          CardType.NOPE, List.of(4, 6, 10),
          CardType.DEFUSE, List.of(3, 7, 10));

  private final Map<CardType, List<Integer>> catCards =
      Map.of(
          CardType.FERAL_CAT, List.of(2, 4, 6),
          CardType.CAT_1, List.of(3, 4, 7),
          CardType.CAT_2, List.of(3, 4, 7),
          CardType.CAT_3, List.of(3, 4, 7),
          CardType.CAT_4, List.of(3, 4, 7),
          CardType.CAT_5, List.of(3, 4, 7));

  public BuildDeck(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

    final List<String> playerNames = (List<String>) job.getVariablesAsMap().get("playerNames");
    final var playerCount = playerNames.size();

    final List<Card> deck = buildDeck(playerCount);

    final var players = dealHandCards(playerNames, deck);

    Collections.shuffle(deck);

    final var gameState = new GameState();
    gameState.setDeck(deck);
    gameState.setDiscardPile(List.of());

    listener.newGameStarted(gameState);

    jobClient.newCompleteCommand(job.getKey()).variables(Map.of(
        "deck", deck,
        "discardPile", List.of(),
        "players", players)
    ).send().join();
  }

  private List<Card> buildDeck(int playerCount) {
    int times;
    if (playerCount < 4) {
      times = 0;
    } else if (playerCount < 8) {
      times = 1;
    } else {
      times = 2;
    }

    final var cards = new HashMap<CardType, List<Integer>>();
    cards.putAll(actionCards);
    cards.putAll(catCards);

    final var cardId = new AtomicInteger(1);

    final var deck =
        cards.entrySet().stream()
            .flatMap(
                entry -> Collections.nCopies(entry.getValue().get(times), entry.getKey()).stream())
            .map(cardType -> new Card(cardId.getAndIncrement(), cardType))
            .collect(Collectors.toList());

    Collections.nCopies(playerCount - 1, CardType.EXPLODING)
        .forEach(
            cardType -> {
              final var card = new Card(cardId.getAndIncrement(), cardType);
              deck.add(card);
            });

    return deck;
  }

  private Map<String, List<Card>> dealHandCards(List<String> playerNames, List<Card> deck) {

    final var playerCards =
        deck.stream()
            .filter(
                card -> card.getType() != CardType.DEFUSE && card.getType() != CardType.EXPLODING)
            .collect(Collectors.toList());

    Collections.shuffle(playerCards);

    return playerNames
        .stream()
        .collect(Collectors.toMap(name -> name, name -> {
          var handCards =
              IntStream.range(0, 7)
                  .mapToObj(i -> playerCards.remove(0))
                  .collect(Collectors.toList());

          deck.stream()
              .filter(card -> card.getType() == CardType.DEFUSE)
              .findFirst()
              .ifPresent(handCards::add);

          deck.removeAll(handCards);

          return handCards;
        }));
  }
}
