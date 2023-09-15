package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.bot.DmnBasedBot;
import io.zeebe.bpmn.games.bot.GameBot;
import io.zeebe.bpmn.games.bot.RandomBot;
import io.zeebe.bpmn.games.model.Card;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.zeebe.bpmn.games.model.NopeTurn;
import io.zeebe.bpmn.games.model.PlayerTurn;
import io.zeebe.bpmn.games.model.PlayersOverview;
import io.zeebe.bpmn.games.slack.SlackUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AppConfig {

  private static final String RANDOM_BOT_PREFIX = "bot_random";
  private static final String DMN_BOT_PREFIX = "bot_dmn";

  @Autowired private UserInteraction user;

  @Autowired private RandomBot randomBot;

  @Autowired private DmnBasedBot dmnBasedBot;

  @Value("${default-bot}")
  private String defaultBot;

  @Primary
  @Bean
  public GameInteraction interactionDelegate() {

    final Function<String, GameInteraction> delegate =
        player -> {
          if (SlackUtil.isBot(player)) {
            return getGameBot(player);
          } else {
            return user;
          }
        };

    return new GameInteraction() {
      @Override
      public CompletableFuture<List<Card>> selectCardsToPlay(PlayerTurn playerTurn) {
        return delegate.apply(playerTurn.getCurrentPlayer()).selectCardsToPlay(playerTurn);
      }

      @Override
      public CompletableFuture<Boolean> nopeThePlayedCard(NopeTurn nopeTurn) {
        return delegate.apply(nopeTurn.getNopePlayer()).nopeThePlayedCard(nopeTurn);
      }

      @Override
      public CompletableFuture<List<Card>> alterTheFuture(PlayerTurn playerTurn, List<Card> cards) {
        return delegate.apply(playerTurn.getCurrentPlayer()).alterTheFuture(playerTurn, cards);
      }

      @Override
      public CompletableFuture<String> selectPlayer(String player, PlayersOverview playersOverview) {
        return delegate.apply(player).selectPlayer(player, playersOverview);
      }

      @Override
      public CompletableFuture<Card> selectCardToGive(String player, List<Card> handCards) {
        return delegate.apply(player).selectCardToGive(player, handCards);
      }

      @Override
      public CompletableFuture<Integer> selectPositionToInsertExplodingCard(PlayerTurn playerTurn, Card card) {
        return delegate.apply(playerTurn.getCurrentPlayer()).selectPositionToInsertExplodingCard(playerTurn, card);
      }
    };
  }

  private GameBot getGameBot(String botName) {
    if (botName.startsWith(RANDOM_BOT_PREFIX)) {
      return randomBot;

    } else if (botName.startsWith(DMN_BOT_PREFIX)) {
      return dmnBasedBot;

    } else if (defaultBot.equals("random")) {
      return randomBot;

    } else {
      // DMN-based bot is the default
      return dmnBasedBot;
    }
  }

}
