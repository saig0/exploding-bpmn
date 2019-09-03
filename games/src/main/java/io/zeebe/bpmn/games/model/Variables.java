package io.zeebe.bpmn.games.model;

import io.zeebe.client.api.response.ActivatedJob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Variables {

  private final Map<String, Object> resultVariables = new HashMap<>();
  private Map<String, Object> jobVariables;

  public Variables(ActivatedJob job) {
    wrap(job);
  }

  public Variables wrap(ActivatedJob job) {
    jobVariables = job.getVariablesAsMap();
    resultVariables.clear();
    return this;
  }

  public Map<String, Object> getResultVariables() {
    return resultVariables;
  }

  public List<Card> getDeck() {
    return (List<Card>) jobVariables.get("deck");
  }

  public Variables putDeck(List<Card> deck) {
    resultVariables.put("deck", deck);
    return this;
  }

  public List<Card> getDiscardPile() {
    return (List<Card>) jobVariables.get("discardPile");
  }

  public Variables putDiscardPile(List<Card> discardPile) {
    resultVariables.put("discardPile", discardPile);
    return this;
  }

  public Map<String,List<Card>> getPlayers() {
    return (Map<String,List<Card>>) jobVariables.get("players");
  }

  public Variables putPlayers(Map<String,List<Card>> players) {
    resultVariables.put("players", players);
    return this;
  }

  public int getRound() {
    return (Integer) jobVariables.get("round");
  }

  public Variables putRound(int round) {
    resultVariables.put("round", round);
    return this;
  }

  public int getTurns() {
    return (Integer) jobVariables.get("turns");
  }

  public Variables putTurns(int turns) {
    resultVariables.put("turns", turns);
    return this;
  }

  public String getCorrelationKey() {
    return (String) jobVariables.get("correlationKey");
  }

  public Variables putCorrelationKey(String correlationKey) {
    resultVariables.put("correlationKey", correlationKey);
    return this;
  }

  public List<String> getPlayerNames() {
    return (List<String>) jobVariables.get("playerNames");
  }

  public Variables putPlayerNames(List<String> playerNames) {
    resultVariables.put("playerNames", playerNames);
    return this;
  }

}
