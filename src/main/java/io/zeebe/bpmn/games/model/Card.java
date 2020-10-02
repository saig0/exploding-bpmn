package io.zeebe.bpmn.games.model;

import java.util.Objects;

public class Card {

  private int id;
  private CardType type;

  Card() {}

  public Card(int id, CardType type) {
    this.id = id;
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public CardType getType() {
    return type;
  }

  public void setType(CardType type) {
    this.type = type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Card card = (Card) o;
    return id == card.id && type == card.type;
  }

  @Override
  public String toString() {
    return "{" + "id=" + id + ", type=" + type + '}';
  }
}
