package org.mofleury.agwenst.engine;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.mofleury.agwenst.domain.live.Deck;
import org.mofleury.agwenst.domain.live.Hand;
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

	private final List<Player> players;
	private final List<Player> shuffledPlayers;

	private final Map<Player, Deck> decks;
	private final Map<Player, Hand> hands;

	private Round round;

	public Game(long seed, List<Player> players, Map<Player, InitialDeck> initialDecks) {
		rand = new Random(seed);

		this.players = new ArrayList<>(players);

		victories = players.stream()
				.collect(toMap(p -> p, p -> new AtomicInteger(0)));
		roundCounter = new AtomicInteger(0);

		decks = players.stream()
				.collect(toMap(p -> p, p -> new Deck()));

		hands = players.stream()
				.collect(toMap(p -> p, p -> new Hand()));

		prepareDecksAndHands(initialDecks);

		shuffledPlayers = new ArrayList<>(players);
		Collections.shuffle(shuffledPlayers, rand);

		newRound();
	}

	private void newRound() {
		round = new Round(players, shuffledPlayers.get(roundCounter.get() % 2));
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

		round.playCard(card);

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

		if (!gameOver()) {
			newRound();
		}
	}

	public Optional<Player> getWinner() {
		return victories.entrySet()
				.stream()
				.filter(e -> e.getValue()
						.get() == 2)
				.map(e -> e.getKey())
				.findAny();
	}

	public boolean gameOver() {
		return getWinner().isPresent() || (roundCounter.get() == 3);
	}
}
