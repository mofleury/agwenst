package org.mofleury.agwenst.console;

import java.io.IOException;
import java.util.Optional;

import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {

	HELP("help", "", "Displays available commands") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
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
	DIPLAY_FIELD("field", "", "Displays game field") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.displayField();
		}
	},
	DISPLAY_HAND("hand", "", "Displays your hand") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.displayHand();
		}
	},
	PLAY_CARD("play", "<card index> <target row>", "Plays a card") {

		@Override
		public void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {

			if (arguments.length != 2) {
				console.println("Cannot understand arguments " + arguments);
			}
			int cardIndex = Integer.valueOf(arguments[0]);
			int targetRow = Integer.valueOf(arguments[1]);

			ui.playCard(cardIndex, targetRow);
		}
	},
	EXIT("exit", "", "Exits the game") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException {
			ui.quit();
		}
	};

	private final String name;
	private final String usagePostfix;
	private final String description;

	public abstract void execute(ConsoleReader console, ConsoleUI ui, String[] arguments) throws IOException;

	public static Optional<Command> forInput(String name) {
		for (Command c : values()) {
			if (name.startsWith(c.getName())) {
				return Optional.of(c);
			}
		}
		return Optional.empty();
	}

	public static String[] extractArguments(String input){

		int spaceIndex = input.indexOf(' ');
		if(spaceIndex > 0){
			String remaining = input.substring(spaceIndex);
			return remaining.trim().split("\\s+");
		}
		return new String[]{};

	}
}
