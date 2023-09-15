package io.zeebe.bpmn.games.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayersOverview {

  private List<Player> players;

  public static PlayersOverview of(Variables variables) {
    final var playersOverview = new PlayersOverview();

    final String currentPlayer = variables.getNextPlayer();
    final Map<String, List<Card>> otherPlayers = new HashMap<>(variables.getPlayers());
    otherPlayers.remove(currentPlayer);

    playersOverview.players =
        otherPlayers.entrySet().stream()
            .map(
                player -> {
                  final String playerName = player.getKey();
                  final List<Card> handCards = player.getValue();
                  return new Player(playerName, handCards.size());
                })
            .collect(Collectors.toList());

    return playersOverview;
  }

  public List<Player> getPlayers() {
    return players;
  }

  @Override
  public String toString() {
    return "PlayersOverview{" + "players=" + players + '}';
  }

  public static class Player {

    private String name;
    private int handcards_count;

    public Player(String name, int handcardsCount) {
      this.name = name;
      handcards_count = handcardsCount;
    }

    public String getName() {
      return name;
    }

    public int getHandcards_count() {
      return handcards_count;
    }

    @Override
    public String toString() {
      return "Player{" + "name='" + name + '\'' + ", handcards_count=" + handcards_count + '}';
    }
  }
}
