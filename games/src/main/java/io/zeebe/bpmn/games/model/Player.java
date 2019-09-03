package io.zeebe.bpmn.games.model;

import java.util.List;

public class Player {

  private String name;

  private List<Card> handCards;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Card> getHandCards() {
    return handCards;
  }

  public void setHandCards(List<Card> handCards) {
    this.handCards = handCards;
  }
}
