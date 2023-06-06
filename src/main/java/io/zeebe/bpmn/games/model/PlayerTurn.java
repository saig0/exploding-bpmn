package io.zeebe.bpmn.games.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerTurn {

  private String currentPlayer;
  private List<String> nextPlayers;
  private int playerCount;

  private List<Card> handCards;
  private int remainingTurns;

  private int deckSize;
  private List<Card> discardPile;

  private String gameKey;

  public static PlayerTurn of(Variables variables) {
    final var playerTurn = new PlayerTurn();
    final String currentPlayer = variables.getNextPlayer();
    final List<String> playerNames = variables.getPlayerNames();
    final List<String> nextPlayers =
        Stream.concat(playerNames.stream(), playerNames.stream())
            .collect(Collectors.toList())
            .subList(
                variables.getNextPlayerIndex(),
                variables.getNextPlayerIndex() + variables.getPlayerCount() - 1);

    playerTurn.currentPlayer = currentPlayer;
    playerTurn.nextPlayers = nextPlayers;
    playerTurn.playerCount = variables.getPlayerCount();
    playerTurn.handCards = variables.getPlayers().get(currentPlayer);
    playerTurn.remainingTurns = variables.getTurns();
    playerTurn.deckSize = variables.getDeck().size();
    playerTurn.discardPile = variables.getDiscardPile();
    playerTurn.gameKey = variables.getGameKey();

    return playerTurn;
  }

  public String getCurrentPlayer() {
    return currentPlayer;
  }

  public List<String> getNextPlayers() {
    return nextPlayers;
  }

  public int getPlayerCount() {
    return playerCount;
  }

  public List<Card> getHandCards() {
    return handCards;
  }

  public int getRemainingTurns() {
    return remainingTurns;
  }

  public int getDeckSize() {
    return deckSize;
  }

  public List<Card> getDiscardPile() {
    return discardPile;
  }

  public String getGameKey() {
    return gameKey;
  }
}
