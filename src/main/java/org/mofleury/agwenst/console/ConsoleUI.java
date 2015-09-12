package org.mofleury.agwenst.console;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.iterate;
import static java.util.stream.IntStream.range;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mofleury.agwenst.domain.live.Row;
import org.mofleury.agwenst.domain.still.Card;
import org.mofleury.agwenst.domain.still.Player;
import org.mofleury.agwenst.engine.Game;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import lombok.RequiredArgsConstructor;

public class ConsoleUI {

	@RequiredArgsConstructor
	private static class ExceptionCatchingConsole {
		private final ConsoleReader delegate;

		public void println(String s) {
			try {
				delegate.println(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void print(String s) {
			try {
				delegate.print(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void println() {
			try {
				delegate.println();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final Comparator<? super Card> HAND_SORTER = Comparator.comparingInt(Card::getValue)
			.thenComparing(Card::getName);

	private final ConsoleReader console;
	private final ExceptionCatchingConsole out;

	private Game game;
	private Player currentPlayer;

	private boolean cancelRequested = false;

	public ConsoleUI() throws IOException {
		console = new ConsoleReader();
		out = new ExceptionCatchingConsole(console);

		console.addCompleter(new StringsCompleter(Arrays.stream(Command.values())
				.map(Command::getName)
				.collect(toList())));

		console.setPrompt(">");
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
				} else if (input.trim()
						.isEmpty()) {
					continue;
				}
				Optional<Command> c = Command.forName(input.trim());
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
		out.println("-------------------------");
		game.getHands()
				.get(currentPlayer)
				.getCards()
				.stream()
				.sorted(HAND_SORTER)
				.forEach(c -> {
					out.println(c.getName() + "(" + c.getValue() + ")");
				});
		out.println("-------------------------");
	}

	public void displayField() throws IOException {

		Player player1 = game.getPlayer1();
		Player player2 = game.getPlayer2();
		Map<Player, List<Row>> rows = game.getRows();

		out.println("---------------------------");

		iterate(Game.ROW_COUNT - 1, i -> i - 1).limit(Game.ROW_COUNT)
				.forEach(r -> {
					rows.get(player2)
							.get(r)
							.getCards()
							.forEach(c -> out.print(c.getCard()
									.getName() + "(" + c.getCurrentValue() + ")"));
				});


		out.println();
		out.println("---------------------------");


		range(0, Game.ROW_COUNT).forEach(r -> {
			rows.get(player1)
					.get(r)
					.getCards()
					.forEach(c -> out.print(c.getCard()
							.getName() + "(" + c.getCurrentValue() + ") "));
		});

		out.println();
		out.println("---------------------------");

	}
}
