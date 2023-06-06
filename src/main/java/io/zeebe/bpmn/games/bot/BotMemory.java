package io.zeebe.bpmn.games.bot;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BotMemory {

  private final Map<String, Memory> memoryByPlayer = new HashMap<>();

  private Memory getMemoryOfPlayer(String player) {
    return memoryByPlayer.computeIfAbsent(player, p -> new Memory());
  }

  public void rememberFuture(String player, List<Card> cards) {
    getMemoryOfPlayer(player).putFuture(cards);
  }

  public void forgetAll() {
    memoryByPlayer.clear();
  }

  public void removeNextCard() {
    memoryByPlayer.forEach((player, memory) -> memory.removeNext());
  }

  public void rememberCard(String player, Card card, int cardIndex) {
    getMemoryOfPlayer(player).putCard(card, cardIndex);
  }

  public Optional<Card> getNextCard(String player) {
    return getMemoryOfPlayer(player).getNextCard();
  }

  public void forgetNextCards(int size) {
    memoryByPlayer.forEach((player, memory) -> memory.forgetNextCards(size));
  }

  private static class Memory {

    private static final Card NULL_CARD = new Card(0, CardType.NOPE);

    private final List<Card> future = new ArrayList<>();

    public void putFuture(List<Card> cards) {
      cards.forEach(
          card -> {
            if (!future.isEmpty()) {
              future.remove(0);
            }
          });

      for (int i = 0; i < cards.size(); i++) {
        final var card = cards.get(i);
        future.add(i, card);
      }
    }

    public void removeNext() {
      if (!future.isEmpty()) {
        future.remove(0);
      }
    }

    public void forgetNextCards(int size) {
      putFuture(IntStream.range(0, size).mapToObj(i -> NULL_CARD).collect(Collectors.toList()));
    }

    public void putCard(Card card, int cardIndex) {
      IntStream.range(future.size(), cardIndex)
          .forEach(
              i -> {
                future.add(null);
              });

      if (future.size() > cardIndex) {
        future.remove(cardIndex);
        future.add(cardIndex, card);
      } else {
        future.add(card);
      }
    }

    public Optional<Card> getNextCard() {
      if (future.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(future.get(0)).filter(f -> !f.equals(NULL_CARD));
      }
    }
  }
}
