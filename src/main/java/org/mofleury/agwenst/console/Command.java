package org.mofleury.agwenst.console;

import java.io.IOException;
import java.util.Optional;

import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {

	HELP("help", "", "Displays available commands", 0) {
		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			console.println("available commands:");
			for (Command c : values()) {
				console.print("\t");
				console.print(c.getName());
				console.print(" " + c.getUsagePostfix());
				console.print(" : ");
				console.print(c.getDescription());
				console.println();
			}
		}
	},
	DIPLAY_FIELD("field", "", "Displays game field", 0) {
		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.displayField();
		}
	},
	DISPLAY_HAND("hand", "", "Displays your hand", 0) {
		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.displayHand();
		}
	},
	PLAY_CARD("play", "<card index>", "Plays a card", 1) {

		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {

			int cardIndex = Integer.valueOf(arguments[0]);

			try {
				ui.playCard(cardIndex);
			} catch (IndexOutOfBoundsException e) {
				console.println("invalid row or card id");
			}
		}
	},
	PASS("pass", "", "Pass for this round", 0) {
		@Override
		void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.pass();
		}
	},
	STATUS("status", "", "Displays global status", 0) {
		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.displayStatus();
		}
	},
	EXIT("exit", "", "Exits the game", 0) {
		@Override
		public void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.quit();
		}
	};

	private final String name;
	private final String usagePostfix;
	private final String description;
	private final int argCount;

	abstract void doExecute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException;

	public void execute(ConsoleReader console, ConsoleUI ui, String input) throws IOException {
		doExecute(console, ui, extractArguments(input));
	}

	public static Optional<Command> forInput(String input) {
		for (Command c : values()) {
			if (input.startsWith(c.getName())) {
				String[] arguments = extractArguments(input);
				if (arguments.length == c.getArgCount()) {
					return Optional.of(c);
				}
			}
		}
		return Optional.empty();
	}

	public static String[] extractArguments(String input) {

		int spaceIndex = input.indexOf(' ');
		if (spaceIndex > 0) {
			String remaining = input.substring(spaceIndex);
			return remaining.trim()
					.split("\\s+");
		}
		return new String[] {};

	}
}
