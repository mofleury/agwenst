package org.mofleury.agwenst.engine;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mofleury.agwenst.domain.live.EngagedCard;
import org.mofleury.agwenst.domain.live.Row;
import org.mofleury.agwenst.domain.still.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Round {

	private final List<Player> players;

	private final Map<Player, List<Row>> rows;

	private final Map<Player, Boolean> passed;

	private Player currentPlayer;

	public boolean hasPassed(Player p) {
		return passed.get(p) == true;
	}

	public void swapPlayerIfPossible() {
		if (passed.get(getOtherPlayer()) == false) {
			currentPlayer = getOtherPlayer();
		}
	}

	public Player getOtherPlayer() {
		return players.stream()
				.filter(p -> p != currentPlayer)
				.findAny()
				.get();
	}

	public void pass() {
		passed.put(currentPlayer, true);

		swapPlayerIfPossible();
	}

	public boolean hasEnded() {
		return passed.values()
				.stream()
				.allMatch(passed -> passed == true);
	}

	public Map<Player, Integer> computeScores() {

		Map<Player, Integer> scores = new HashMap<>();
		rows.forEach((p, rs) -> {
			int total = rs.stream()
					.map(r -> r.getCards()
							.stream()
							.mapToInt(EngagedCard::getCurrentValue)
							.sum())
					.mapToInt(Integer::intValue)
					.sum();
			scores.put(p, total);
		});

		return scores;
	}

	public Optional<Player> findWinner() {
		return computeScores().entrySet()
				.stream()
				.max(Comparator.comparing(e -> e.getValue()))
				.map(e -> e.getKey());
	}
}
