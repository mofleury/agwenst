package org.mofleury.agwenst.engine;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	private Round round;

	public Game(long seed, Player player1, Player player2, Map<Player, InitialDeck> initialDecks) {
		rand = new Random(seed);

		this.player1 = player1;
		this.player2 = player2;

		players = Arrays.asList(player1, player2);

		victories = players.stream()
				.collect(toMap(p -> p, p -> new AtomicInteger(0)));
		roundCounter = new AtomicInteger(0);

		winner = Optional.empty();

		decks = players.stream()
				.collect(toMap(p -> p, p -> new Deck()));

		hands = players.stream()
				.collect(toMap(p -> p, p -> new Hand()));

		prepareDecksAndHands(initialDecks);

		newRound();
	}

	private void newRound() {
		round = new Round(players, buildRows(), players.stream()
				.collect(toMap(p -> p, p -> false)), rand.nextDouble() < 0.5 ? player1 : player2);
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
		boolean found = hands.get(round.getCurrentPlayer())
				.getCards()
				.remove(card);
		if (!found) {
			throw new IllegalStateException(
					"Player " + round.getCurrentPlayer() + "does not have card " + card + " in his hand");
		}

		round.getRows()
				.get(round.getCurrentPlayer())
				.get(card.getTargetRow() - 1)
				.getCards()
				.add(new EngagedCard(card));

		if (hands.get(round.getCurrentPlayer())
				.getCards()
				.isEmpty()) {
			pass();
		} else {
			round.swapPlayerIfPossible();
		}
	}

	public void pass() {
		round.pass();
		if (round.hasEnded()) {
			endRound();
		}
	}

	private void endRound() {

		roundCounter.incrementAndGet();

		Optional<Player> roundWinner = round.findWinner();

		roundWinner.ifPresent(p -> {
			victories.get(p)
					.incrementAndGet();
		});

		if ((roundCounter.get() == 3) || victories.values()
				.stream()
				.anyMatch(v -> v.get() == 2)) {
			this.winner = roundWinner;
		} else {
			newRound();
		}
	}

	public boolean gameOver() {
		return winner.isPresent() || (roundCounter.get() == 3);
	}
}
