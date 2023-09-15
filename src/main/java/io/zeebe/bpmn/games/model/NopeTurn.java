package io.zeebe.bpmn.games.model;

import java.util.List;

public class NopeTurn {

  private String nopePlayer;
  private String currentPlayer;
  private String nextPlayer;
  private int playerCount;

  private List<Card> playedCards;

  private int deckSize;

  private List<Card> handCards;

  public static NopeTurn of(Variables variables) {
    final var nopeTurn = new NopeTurn();

    final int playerCount = variables.getPlayerCount();
    final String nopePlayer = variables.getNopePlayer();
    final String nextPlayer = variables.getPlayerNames().get(variables.getNextPlayerIndex());
    final List<Card> handCards = variables.getPlayers().get(nopePlayer);

    nopeTurn.nopePlayer = nopePlayer;
    nopeTurn.currentPlayer = variables.getNextPlayer();
    nopeTurn.nextPlayer = nextPlayer;
    nopeTurn.playerCount = playerCount;
    nopeTurn.playedCards = variables.getLastPlayedCards();
    nopeTurn.deckSize = variables.getDeck().size();
    nopeTurn.handCards = handCards;

    return nopeTurn;
  }

  public String getNopePlayer() {
    return nopePlayer;
  }

  public String getCurrentPlayer() {
    return currentPlayer;
  }

  public String getNextPlayer() {
    return nextPlayer;
  }

  public int getPlayerCount() {
    return playerCount;
  }

  public List<Card> getPlayedCards() {
    return playedCards;
  }

  public int getDeckSize() {
    return deckSize;
  }

  public List<Card> getHandCards() {
    return handCards;
  }
}
