package io.zeebe.bpmn.games.model;

public enum CardType {

  // special
  EXPLODING,
  DEFUSE,

  // actions
  ATTACK,
  SKIP,
  SEE_THE_FUTURE,
  ALTER_THE_FUTURE,
  SHUFFLE,
  DRAW_FROM_BOTTOM,
  FAVOR,
  NOPE,

  // cats
  FERAL_CAT,
  CAT_1,
  CAT_2,
  CAT_3,
  CAT_4,
  CAT_5;

  public boolean isCatCard() {
    return name().contains("CAT");
  }
}
