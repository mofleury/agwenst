package org.mofleury.agwenst.console;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mofleury.agwenst.domain.live.Row;
import org.mofleury.agwenst.domain.still.Player;
import org.mofleury.agwenst.engine.Game;

import jline.console.ConsoleReader;

public class ConsoleUI {

	private final ConsoleReader console;

	private Game game;
	private Player currentPlayer;

	private boolean cancelRequested = false;

	public ConsoleUI() throws IOException {
		console = new ConsoleReader();
	}

	public void run(Game game) {

		this.game = game;
		this.currentPlayer = game.getPlayer1();

		try {
			console.println("Welcome! type 'help' for directions");
			console.flush();

			while (!cancelRequested) {
				String input = console.readLine();
				if (input == null) {
					// exit requested
					break;
				}
				Optional<Command> c = Command.forName(input);
				if (c.isPresent()) {
					c.get()
							.execute(console, this);
				} else {
					console.println("Don't know anything about '" + input + "'");
				}
				console.flush();
			}

			console.println("Thanks for playing!");
			console.flush();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void quit() {
		cancelRequested = true;
	}

	public void displayHand() throws IOException {
		console.println("-------------------------");
		console.println("" + game.getHands()
				.get(currentPlayer)
				.getCards());
		console.println("-------------------------");
	}

	public void displayField() throws IOException {

		Player player1 = game.getPlayer1();
		Player player2 = game.getPlayer2();
		Map<Player, List<Row>> rows = game.getRows();

		console.println("---------------------------");

		console.println("" + rows.get(player2)
				.get(2));
		console.println("" + rows.get(player2)
				.get(1));
		console.println("" + rows.get(player2)
				.get(0));

		console.println("---------------------------");

		console.println("" + rows.get(player1)
				.get(0));
		console.println("" + rows.get(player1)
				.get(1));
		console.println("" + rows.get(player1)
				.get(2));

		console.println("---------------------------");

	}
}
