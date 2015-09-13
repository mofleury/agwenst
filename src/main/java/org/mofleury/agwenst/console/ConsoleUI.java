package org.mofleury.agwenst.console;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.iterate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

		public void clearScreen() {
			try {
				delegate.clearScreen();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final Comparator<? super Card> HAND_SORTER = Comparator.comparing(Card::getType)
			.reversed()
			.thenComparing(Card::getValue)
			.thenComparing(Comparator.comparingInt(Card::getTargetRow))
			.thenComparing(Card::getName);

	private final ConsoleReader console;
	private final ExceptionCatchingConsole out;

	private Game game;

	private boolean cancelRequested = false;

	public ConsoleUI() throws IOException {
		console = new ConsoleReader();
		out = new ExceptionCatchingConsole(console);

		console.addCompleter(new StringsCompleter(Arrays.stream(Command.values())
				.map(Command::getName)
				.collect(toList())));

	}

	public void run(Game game) {

		this.game = game;

		try {
			console.println("Welcome! type 'help' for directions");

			displayField();

			console.flush();

			while (!cancelRequested) {

				console.setPrompt(game.getRound()
						.getCurrentPlayer()
						.getName() + " > ");

				String input = console.readLine();
				if (input == null) {
					// exit requested
					break;
				} else if (input.trim()
						.isEmpty()) {
					continue;
				}
				Optional<Command> c = Command.forInput(input.trim());
				if (c.isPresent()) {
					c.get()
							.execute(console, this, input);
				} else {
					console.println("Don't know what to do with '" + input + "'");
				}

				if (game.gameOver()) {
					console.print("Game Complete, ");
					if (game.getWinner()
							.isPresent()) {
						console.println("the winner is " + game.getWinner()
								.get()
								.getName() + "!");
					} else {
						console.print("it's a draw!");
					}
					break;
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

	public void displayHand() {
		out.println("-------------------------");
		indexedPlayerHand().forEach((i, c) -> {
			out.println(i + " - " + formatCard(c));
		});
		out.println("-------------------------");
	}

	private String formatCard(Card c) {
		switch (c.getType()) {
		case SPECIAL:
			return "(" + c.getTargetRow() + ", " + c.getEffect()
					.map(e -> e.getLabel())
					.orElse("?") + ") " + c.getName();
		case UNIT:
			return "(" + c.getTargetRow() + ", " + c.getValue() + ") " + c.getName();
		}
		throw new IllegalArgumentException("Unknown card type " + c.getType());
	}

	private Map<Integer, Card> indexedPlayerHand() {
		AtomicInteger index = new AtomicInteger(0);

		return game.getHands()
				.get(game.getRound()
						.getCurrentPlayer())
				.getCards()
				.stream()
				.sorted(HAND_SORTER)
				.collect(Collectors.toMap(c -> index.incrementAndGet(), c -> c));
	}

	public void playCard(int cardIndex) throws IndexOutOfBoundsException {

		Card card = indexedPlayerHand().get(cardIndex);
		if (card == null) {
			throw new IndexOutOfBoundsException("No card at index " + cardIndex);
		}
		game.playCard(card);

		swapPlayers();
	}

	private void swapPlayers() {

		out.clearScreen();

		displayField();

	}

	public void displayField() {

		Map<Player, Map<Integer, Row>> rows = game.getRound()
				.getRows();

		Map<Player, Integer> scores = game.getRound()
				.computeScores();

		Player currentPlayer = game.getRound()
				.getCurrentPlayer();
		Player otherPlayer = game.getRound()
				.getOtherPlayer();

		out.println("----------------------------");

		String specialCards = game.getRound()
				.getSpecialCards()
				.stream()
				.map(spc -> formatCard(spc))
				.collect(joining("\n"));

		out.println(specialCards);

		out.println("----------------------------");
		out.println(playerStatus(otherPlayer));
		out.println("----------( " + scores.get(otherPlayer) + " )-------------");

		iterate(Game.ROW_COUNT, i -> i - 1).limit(Game.ROW_COUNT)
				.forEach(r -> {
					printRow(otherPlayer, rows, r);
				});

		out.println("----------------------------");

		iterate(1, i -> i + 1).limit(Game.ROW_COUNT)
				.forEach(r -> {
					printRow(currentPlayer, rows, r);
				});

		out.println("----------( " + scores.get(currentPlayer) + " )-------------");

		out.println(playerStatus(currentPlayer));
	}

	private String playerStatus(Player p) {
		return p.getName() + " " + game.getVictories()
				.get(p) + " <"
				+ game.getHands()
						.get(p)
						.getCards()
						.size()
				+ ">" + (game.getRound()
						.hasPassed(p) ? " passed" : "");
	}

	private void printRow(Player player, Map<Player, Map<Integer, Row>> rows, int r) {
		out.print(" " + r + " |");

		String effects = game.getRound()
				.getSpecialCards()
				.stream()
				.filter(spc -> spc.getTargetRow() == r)
				.filter(spc -> spc.getEffect()
						.isPresent())
				.map(spc -> spc.getEffect()
						.get()
						.getLabel())
				.collect(joining());

		out.print(String.format("%1$1s", effects) + "|");

		rows.get(player)
				.get(r)
				.getCards()
				.forEach(c -> out.print(c.getCard()
						.getName() + "(" + c.getCurrentValue() + ") "));
		out.println("");
	}

	public void displayStatus() {
		out.println("Victories: ");
		game.getPlayers()
				.stream()
				.forEach(p -> {
					out.println(p.getName() + " : " + game.getVictories()
							.get(p)
							.intValue());
				});
	}

	public void pass() {
		game.pass();
		out.clearScreen();
		displayField();
	}
}
