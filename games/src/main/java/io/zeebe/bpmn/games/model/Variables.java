package io.zeebe.bpmn.games.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.zeebe.client.api.response.ActivatedJob;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Variables {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Map<String, Object> resultVariables = new HashMap<>();
  private final Map<String, Object> jobVariables;

  private Variables(Map<String, Object> jobVariables) {
    this.jobVariables = jobVariables;
  }

  public static Variables from(ActivatedJob job) {
    return new Variables(job.getVariablesAsMap());
  }

  public Map<String, Object> getResultVariables() {
    return resultVariables;
  }

  private Card asCard(Map serializedCard) {
    final var id = Optional.ofNullable((Integer) serializedCard.get("id")).orElseThrow();
    final var type = Optional.ofNullable((String) serializedCard.get("type"))
        .map(CardType::valueOf)
        .orElseThrow();
    return new Card(id, type);
  }

  private List<Card> asCardList(List<Map> serializedCards) {
    return serializedCards.stream().map(this::asCard).collect(Collectors.toList());
  }

  public List<Card> getDeck() {
    return asCardList((List<Map>) jobVariables.get("deck"));
  }

  public Variables putDeck(List<Card> deck) {
    resultVariables.put("deck", deck);
    return this;
  }

  public List<Card> getDiscardPile() {
    return asCardList((List<Map>) jobVariables.get("discardPile"));
  }

  public Variables putDiscardPile(List<Card> discardPile) {
    resultVariables.put("discardPile", discardPile);
    return this;
  }

  public Map<String, List<Card>> getPlayers() {
    final var map = (Map<String, List>) jobVariables.get("players");
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            e -> e.getKey(),
            e -> asCardList(e.getValue())));
  }

  public Variables putPlayers(Map<String, List<Card>> players) {
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
    resultVariables.put("turnArray", new int[turns]); // used for multi-instance
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

  public String getNextPlayer() {
    return (String) jobVariables.get("nextPlayer");
  }

  public Variables putNextPlayer(String nextPlayer) {
    resultVariables.put("nextPlayer", nextPlayer);
    return this;
  }

  public List<Card> getCards() {
    return asCardList((List<Map>) jobVariables.get("cards"));
  }

  public Variables putCards(List<Card> cards) {
    resultVariables.put("cards", cards);
    return this;
  }

  public String getAction() {
    return (String) jobVariables.get("action");
  }

  public Variables putAction(String action) {
    resultVariables.put("action", action);
    return this;
  }

  public Card getCard() {
    return asCard((Map) jobVariables.get("card"));
  }

  public Variables putCard(Card card) {
    resultVariables.put("card", card);
    return this;
  }

  public String getOtherPlayer() {
    return (String) jobVariables.get("otherPlayer");
  }

  public Variables putOtherPlayer(String otherPlayer) {
    resultVariables.put("otherPlayer", otherPlayer);
    return this;
  }

  public String getNopePlayer() {
    return (String) jobVariables.get("nopePlayer");
  }

  public Variables putNopePlayer(String nopePlayer) {
    resultVariables.put("nopePlayer", nopePlayer);
    return this;
  }

  public boolean hasDefuseCard() {
    return (boolean) resultVariables.get("hasDefuse");
  }

  public Variables putHasDefuse(boolean hasDefuseCard) {
    resultVariables.put("hasDefuse", hasDefuseCard);
    return this;
  }

  public int getPlayerCount() {
    return (Integer) jobVariables.get("playerCount");
  }

  public Variables putPlayerCount(int playerCount) {
    resultVariables.put("playerCount", playerCount);
    return this;
  }

}
