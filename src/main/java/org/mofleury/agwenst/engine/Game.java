package org.mofleury.agwenst.engine;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

	private final Player player1;
	private final Player player2;
	private final List<Player> players;

	private final Map<Player, Deck> decks;
	private final Map<Player, Hand> hands;

	private final Map<Player, List<Row>> rows;

	public Game(long seed, Player player1, Player player2, Map<Player, InitialDeck> initialDecks) {
		rand = new Random(seed);

		this.player1 = player1;
		this.player2 = player2;
		players = Arrays.asList(player1, player2);

		decks = players.stream()
				.collect(toMap(p -> p, p -> new Deck()));

		hands = players.stream()
				.collect(toMap(p -> p, p -> new Hand()));

		prepareDecksAndHands(initialDecks);

		rows = players.stream()
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

	public void playCard(Player player, Card card, int targetRow) {
		boolean found = hands.get(player)
				.getCards()
				.remove(card);
		if (!found) {
			throw new IllegalStateException("Player " + player + "does not have card " + card + " in his hand");
		}
		rows.get(player)
				.get(targetRow)
				.getCards()
				.add(new EngagedCard(card));
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

}
