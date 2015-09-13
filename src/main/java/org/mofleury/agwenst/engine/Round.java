package org.mofleury.agwenst.engine;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mofleury.agwenst.domain.live.EngagedCard;
import org.mofleury.agwenst.domain.live.Row;
import org.mofleury.agwenst.domain.still.Card;
import org.mofleury.agwenst.domain.still.CardType;
import org.mofleury.agwenst.domain.still.Player;

import lombok.Getter;

@Getter
public class Round {

	private static final Comparator<Card> SPECIAL_CARD_SORTER = Comparator.comparing(c -> c.getEffect()
			.map(e -> e.getPriority())
			.orElse(0));

	private final List<Player> players;

	private final Map<Player, Map<Integer, Row>> rows;

	private final Map<Player, Boolean> passed;

	private Player currentPlayer;

	private final List<Card> specialCards;

	public Round(List<Player> players, Player currentPlayer) {
		this.players = players;
		this.currentPlayer = currentPlayer;

		specialCards = new ArrayList<>();
		rows = buildRows();
		passed = players.stream()
				.collect(toMap(p -> p, p -> false));

	}

	private Map<Player, Map<Integer, Row>> buildRows() {
		return players.stream()
				.collect(toMap(p -> p, p -> range(1, 4).boxed()
						.collect(Collectors.toMap(rid -> rid, rid -> new Row()))));
	}

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
			int total = rs.values()
					.stream()
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

	public void playCard(Card card) {
		if (card.getType()
				.equals(CardType.UNIT)) {
			rows.get(currentPlayer)
					.get(card.getTargetRow())
					.getCards()
					.add(new EngagedCard(card));
		} else {
			specialCards.add(card);
		}

		adjustCardStrengths();
	}

	private void adjustCardStrengths() {

		// reset values
		rows.forEach((p, rows) -> rows.forEach((rid, row) -> row.getCards()
				.forEach(c -> c.setCurrentValue(c.getCard()
						.getValue()))));

		// readjust values
		specialCards.stream()
				.sorted(SPECIAL_CARD_SORTER)
				.forEach(spc -> {
					rows.forEach((p, rows) -> rows.forEach((rid, row) -> row.getCards()
							.forEach(c -> applySpecialCard(spc, rid, c))));
				});

	}

	private void applySpecialCard(Card spc, Integer rowId, EngagedCard c) {
		if (spc.getTargetRow() == rowId) {
			spc.getEffect()
					.ifPresent(e -> {
						int newValue = e.apply(c.getCurrentValue());
						c.setCurrentValue(newValue);
					});
		}
	}

}
