package io.zeebe.bpmn.games.slack;

import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GamesApplication;
import io.zeebe.bpmn.games.bot.SimpleBot;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Autowired private ZeebeClient zeebeClient;

  @Autowired private SlackGameStateNotifier gameStateNotifier;

  @Autowired private SlackUserInteraction userInteraction;

  @Bean
  public GamesApplication games() {
    return new GamesApplication(zeebeClient, gameStateNotifier, interactionDelegate());
  }

  @Bean
  public GameInteraction interactionDelegate() {
    final var user = userInteraction;
    final var bot = new SimpleBot();

    final Function<String, GameInteraction> delegate =
        player -> {
          if (SlackUtil.isBot(player)) {
            return bot;
          } else {
            return user;
          }
        };

    return new GameInteraction() {
      @Override
      public CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards, int deckSize) {
        return delegate.apply(player).selectCardsToPlay(player, handCards, deckSize);
      }

      @Override
      public CompletableFuture<Boolean> nopeThePlayedCard(String player) {
        return delegate.apply(player).nopeThePlayedCard(player);
      }

      @Override
      public CompletableFuture<List<Card>> alterTheFuture(String player, List<Card> cards) {
        return delegate.apply(player).alterTheFuture(player, cards);
      }

      @Override
      public CompletableFuture<String> selectPlayer(String player, List<String> otherPlayers) {
        return delegate.apply(player).selectPlayer(player, otherPlayers);
      }

      @Override
      public CompletableFuture<Card> selectCardToGive(String player, List<Card> handCards) {
        return delegate.apply(player).selectCardToGive(player, handCards);
      }

      @Override
      public CompletableFuture<Integer> selectPositionToInsertCard(
          String player, Card card, int deckSize) {
        return delegate.apply(player).selectPositionToInsertCard(player, card, deckSize);
      }
    };
  }
}
