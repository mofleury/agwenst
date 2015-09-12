package org.mofleury.agwenst;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.of;

import java.util.Map;

import org.mofleury.agwenst.domain.still.Card;
import org.mofleury.agwenst.domain.still.InitialDeck;
import org.mofleury.agwenst.domain.still.Player;
import org.mofleury.agwenst.engine.Game;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		Player player1 = new Player("Jack");
		Player player2 = new Player("Johnes");

		InitialDeck defaultDeck = new InitialDeck(range(1, 21).mapToObj(i -> new Card("c" + i, i))
				.collect(toList()));

		Map<Player, InitialDeck> initialDecks = of(player1, player2).collect(toMap(p -> p, p -> defaultDeck));

		Game game = new Game(1, player1, player2, initialDecks);

		game.displayField();

		game.displayHand(player1);
	}
}
