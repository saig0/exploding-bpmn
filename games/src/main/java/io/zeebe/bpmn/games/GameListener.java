package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.GameState;
import io.zeebe.bpmn.games.model.Player;

public interface GameListener {

  void newGameStarted(GameState state);

  void nextPlayerSelected(String player, int turns);
}
