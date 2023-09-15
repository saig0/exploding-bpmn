package io.zeebe.bpmn.games.bot;

import io.zeebe.bpmn.games.model.Card;
import java.util.Optional;

public interface BotMemoryAccess {

  Optional<Card> getNextCard(String gameKey, String player);
}
