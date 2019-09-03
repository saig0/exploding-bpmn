package io.zeebe.bpmn.games.model;

import java.util.List;

public class GameState {

  private List<Card> deck;
  private List<Card> discardPile;
  private List<Player> players;

  public List<Card> getDeck() {
    return deck;
  }

  public void setDeck(List<Card> deck) {
    this.deck = deck;
  }

  public List<Card> getDiscardPile() {
    return discardPile;
  }

  public void setDiscardPile(List<Card> discardPile) {
    this.discardPile = discardPile;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void setPlayers(List<Player> players) {
    this.players = players;
  }
}
