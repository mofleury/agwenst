package org.mofleury.agwenst.engine;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.mofleury.agwenst.domain.live.Deck;
import org.mofleury.agwenst.domain.live.EngagedCard;
import org.mofleury.agwenst.domain.live.Hand;
import org.mofleury.agwenst.domain.live.Row;
import org.mofleury.agwenst.domain.still.Card;
import org.mofleury.agwenst.domain.still.InitialDeck;
import org.mofleury.agwenst.domain.still.Player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Game {

	public final static int ROW_COUNT = 3;
	public final static int INITIAL_HAND_SIZE = 8;

	private final Random rand;

	private final Map<Player, AtomicInteger> victories;
	private final AtomicInteger roundCounter;
	private Optional<Player> winner;

	private final Player player1;
	private final Player player2;
	private final List<Player> players;

	private final Map<Player, Deck> decks;
	private final Map<Player, Hand> hands;

	private final Map<Player, List<Row>> rows;

	private Player currentPlayer;
	private final Map<Player, Boolean> passed;

	public Game(long seed, Player player1, Player player2, Map<Player, InitialDeck> initialDecks) {
		rand = new Random(seed);

		this.player1 = player1;
		this.player2 = player2;

		currentPlayer = rand.nextDouble() < 0.5 ? player1 : player2;

		players = Arrays.asList(player1, player2);

		victories = players.stream()
				.collect(toMap(p -> p, p -> new AtomicInteger(0)));
		roundCounter = new AtomicInteger(0);
		winner = Optional.empty();

		passed = players.stream()
				.collect(toMap(p -> p, p -> false));

		decks = players.stream()
				.collect(toMap(p -> p, p -> new Deck()));

		hands = players.stream()
				.collect(toMap(p -> p, p -> new Hand()));

		prepareDecksAndHands(initialDecks);

		rows = buildRows();

	}

	private Map<Player, List<Row>> buildRows() {
		return players.stream()
				.collect(toMap(p -> p, p -> range(0, 3).mapToObj(i -> new Row())
						.collect(toList())));
	}

	private void prepareDecksAndHands(Map<Player, InitialDeck> initialDecks) {
		players.forEach(p -> {
			List<Card> candidates = new ArrayList<>(initialDecks.get(p)
					.getCards());
			Collections.shuffle(candidates, rand);
			hands.get(p)
					.getCards()
					.addAll(candidates.stream()
							.limit(INITIAL_HAND_SIZE)
							.collect(toList()));

			decks.get(p)
					.getCards()
					.addAll(candidates.stream()
							.skip(INITIAL_HAND_SIZE)
							.collect(toList()));
		});
	}

	public void playCard(Card card) {
		boolean found = hands.get(currentPlayer)
				.getCards()
				.remove(card);
		if (!found) {
			throw new IllegalStateException("Player " + currentPlayer + "does not have card " + card + " in his hand");
		}

		rows.get(currentPlayer)
				.get(card.getTargetRow() - 1)
				.getCards()
				.add(new EngagedCard(card));

		if (hands.get(currentPlayer)
				.getCards()
				.isEmpty()) {
			pass();
		} else {
			swapPlayerIfNeeded();
		}
	}

	private void swapPlayerIfNeeded() {
		if (passed.get(getOtherPlayer()) == false) {
			currentPlayer = getOtherPlayer();
		}
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

	public Player getOtherPlayer() {
		if (currentPlayer == player1) {
			return player2;
		} else {
			return player1;
		}
	}

	public void pass() {
		passed.put(currentPlayer, true);

		if (passed.values()
				.stream()
				.allMatch(passed -> passed == true)) {
			endRound();
		}

		swapPlayerIfNeeded();
	}

	private void endRound() {

		roundCounter.incrementAndGet();

		Optional<Player> roundWinner = computeScores().entrySet()
				.stream()
				.max(Comparator.comparing(e -> e.getValue()))
				.map(e -> e.getKey());

		roundWinner.ifPresent(p -> {
			victories.get(p)
					.incrementAndGet();
		});

		if ((roundCounter.get() == 3) || victories.values()
				.stream()
				.anyMatch(v -> v.get() == 2)) {
			this.winner = roundWinner;
		} else {
			rows.clear();
			rows.putAll(buildRows());

			passed.replaceAll((p, b) -> false);
		}
	}

	public boolean gameOver() {
		return winner.isPresent() || (roundCounter.get() == 3);
	}
}
