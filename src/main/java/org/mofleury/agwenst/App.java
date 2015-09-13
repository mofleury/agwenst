package org.mofleury.agwenst;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

import java.io.IOException;
import java.util.Map;

import org.mofleury.agwenst.console.ConsoleUI;
import org.mofleury.agwenst.domain.still.InitialDeck;
import org.mofleury.agwenst.domain.still.Player;
import org.mofleury.agwenst.engine.Game;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws IOException {

		Player player1 = new Player("Jack");
		Player player2 = new Player("Johnes");

		Game game = initSampleGame(player1, player2);

		ConsoleUI ui = new ConsoleUI();

		ui.run(game);

	}

	private static Game initSampleGame(Player player1, Player player2) throws IOException {

		InitialDeck defaultDeck = new DeckBuilder().balancedDeck();

		Map<Player, InitialDeck> initialDecks = of(player1, player2).collect(toMap(p -> p, p -> defaultDeck));

		return new Game(System.currentTimeMillis(), player1, player2, initialDecks);

	}
}
