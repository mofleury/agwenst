package org.mofleury.agwenst.console;

import java.io.IOException;
import java.util.Optional;

import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {

	HELP("help", "Displays available commands") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui) throws IOException {
			console.println("available commands:");
			for (Command c : values()) {
				console.print("\t");
				console.print(c.getName());
				console.print(" : ");
				console.print(c.getDescription());
				console.println();
			}
		}
	},
	DIPLAY_FIELD("field", "Displays game field") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui) throws IOException {
			ui.displayField();
		}
	},
	DISPLAY_HAND("hand", "Displays your hand") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui) throws IOException {
			ui.displayHand();
		}
	},
	EXIT("exit", "Exits the game") {
		@Override
		public void execute(ConsoleReader console, ConsoleUI ui) throws IOException {
			ui.quit();
		}
	};

	private final String name;
	private final String description;

	public abstract void execute(ConsoleReader console, ConsoleUI ui) throws IOException;

	public static Optional<Command> forName(String name) {
		for (Command c : values()) {
			if (c.name.equals(name)) {
				return Optional.of(c);
			}
		}
		return Optional.empty();
	}
}
