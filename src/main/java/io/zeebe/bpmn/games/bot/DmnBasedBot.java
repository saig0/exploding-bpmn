/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.zeebe.bpmn.games.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.NopeTurn;
import io.zeebe.bpmn.games.model.PlayerTurn;
import io.zeebe.bpmn.games.model.PlayersOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.Map.entry;

@Component
public final class DmnBasedBot implements GameBot {

  private static final Logger LOGGER = LoggerFactory.getLogger(DmnBasedBot.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String SELECT_CARDS_TO_PLAY_DECISION = "select_cards_to_play";
  private static final String NOPE_PLAYED_CARDS_DECISION = "nope_played_cards";
  private static final String ALTER_THE_FUTURE_DECISION = "alter_the_future";
  private static final String SELECT_PLAYER_DECISION = "select_player";
  private static final String SELECT_CARD_TO_GIVE_DECISION = "select_card_to_give";
  private static final String POSITION_OF_EXPLODING_CARD_DECISION = "position_of_exploding_card";

  private static final TypeReference<List<Card>> CARDS_DECISION_OUTPUT = new TypeReference<>() {};
  private static final TypeReference<Card> CARD_DECISION_OUTPUT = new TypeReference<>() {};
  private static final TypeReference<Boolean> BOOLEAN_DECISION_OUTPUT = new TypeReference<>() {};
  private static final TypeReference<String> STRING_DECISION_OUTPUT = new TypeReference<>() {};
  private static final TypeReference<Integer> NUMBER_DECISION_OUTPUT = new TypeReference<>() {};

  private final ZeebeClient zeebeClient;
  private final BotMemoryAccess memoryAccess;

  public DmnBasedBot(ZeebeClient zeebeClient, BotMemoryAccess memoryAccess) {
    this.zeebeClient = zeebeClient;
    this.memoryAccess = memoryAccess;
  }

  private <T> CompletableFuture<T> evaluateDecision(
      String decisionId, Map<String, Object> variables, TypeReference<T> decisionOutputType) {
    return zeebeClient
        .newEvaluateDecisionCommand()
        .decisionId(decisionId)
        .variables(variables)
        .send()
        .toCompletableFuture()
        .thenApply(
            response -> {
              String decisionOutput = response.getDecisionOutput();
              try {
                return objectMapper.readValue(decisionOutput, decisionOutputType);
              } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize decision output", e);
              }
            });
  }

  @Override
  public CompletableFuture<List<Card>> selectCardsToPlay(PlayerTurn playerTurn) {

    final var variables =
        new HashMap<>(
            Map.ofEntries(
                entry("hand_cards", playerTurn.getHandCards()),
                entry("deck_size", playerTurn.getDeckSize()),
                entry("player_count", playerTurn.getPlayerCount()),
                entry("remaining_turns", playerTurn.getRemainingTurns()),
                entry("discard_pile", playerTurn.getDiscardPile())));

    final Card nextCard =
        memoryAccess
            .getNextCard(playerTurn.getGameKey(), playerTurn.getCurrentPlayer())
            .orElse(null);
    variables.put("next_card", nextCard);

    LOGGER.debug(
        "{} should select card. The next card is {}", playerTurn.getCurrentPlayer(), nextCard);

    return evaluateDecision(SELECT_CARDS_TO_PLAY_DECISION, variables, CARDS_DECISION_OUTPUT);
  }

  @Override
  public CompletableFuture<Boolean> nopeThePlayedCard(NopeTurn nopeTurn) {

    final var variables =
        Map.ofEntries(
            entry("nope_player", nopeTurn.getNopePlayer()),
            entry("current_player", nopeTurn.getCurrentPlayer()),
            entry("next_player", nopeTurn.getNextPlayer()),
            entry("played_cards", nopeTurn.getPlayedCards()),
            entry("deck_size", nopeTurn.getDeckSize()),
            entry("player_count", nopeTurn.getPlayerCount()));

    return evaluateDecision(NOPE_PLAYED_CARDS_DECISION, variables, BOOLEAN_DECISION_OUTPUT);
  }

  @Override
  public CompletableFuture<List<Card>> alterTheFuture(
      final PlayerTurn playerTurn, final List<Card> cards) {

    final Map<String, Object> variables =
        Map.ofEntries(
            entry("cards", cards), entry("remaining_turns", playerTurn.getRemainingTurns()));

    return evaluateDecision(ALTER_THE_FUTURE_DECISION, variables, CARDS_DECISION_OUTPUT);
  }

  @Override
  public CompletableFuture<String> selectPlayer(
      final String player, final PlayersOverview playersOverview) {

    final Map<String, Object> variables =
        Map.ofEntries(entry("players", playersOverview.getPlayers()));

    return evaluateDecision(SELECT_PLAYER_DECISION, variables, STRING_DECISION_OUTPUT);
  }

  @Override
  public CompletableFuture<Card> selectCardToGive(final String player, final List<Card> handCards) {

    final Map<String, Object> variables = Map.ofEntries(entry("hand_cards", handCards));

    return evaluateDecision(SELECT_CARD_TO_GIVE_DECISION, variables, CARD_DECISION_OUTPUT);
  }

  @Override
  public CompletableFuture<Integer> selectPositionToInsertExplodingCard(
      PlayerTurn playerTurn, Card card) {

    final Map<String, Object> variables =
        Map.ofEntries(
            entry("remaining_turns", playerTurn.getRemainingTurns()),
            entry("deck_size", playerTurn.getDeckSize()),
            entry("player_count", playerTurn.getPlayerCount()));

    return evaluateDecision(POSITION_OF_EXPLODING_CARD_DECISION, variables, NUMBER_DECISION_OUTPUT);
  }
}
