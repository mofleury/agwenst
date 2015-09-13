package org.mofleury.agwenst;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.mofleury.agwenst.domain.still.CardType.UNIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mofleury.agwenst.domain.still.Card;
import org.mofleury.agwenst.domain.still.InitialDeck;

public class DeckBuilder {

	public InitialDeck simpleDeck(int min, int max) {
		return new InitialDeck(range(min, max + 1).mapToObj(i -> new Card(UNIT, "c" + i, i, 1, Optional.empty()))
				.collect(toList()));
	}

	public InitialDeck balancedDeck() {
		List<Card> cards = new ArrayList<>();
		range(1, 4).forEach(r -> {
			range(1, 4).forEach(i -> cards.add(new Card(UNIT, "c1", 1, r, Optional.empty())));
			range(1, 3).forEach(i -> cards.add(new Card(UNIT, "c2", 2, r, Optional.empty())));
			range(1, 2).forEach(i -> cards.add(new Card(UNIT, "c3", 3, r, Optional.empty())));
			range(1, 2).forEach(i -> cards.add(new Card(UNIT, "c5", 5, r, Optional.empty())));
			range(1, 1).forEach(i -> cards.add(new Card(UNIT, "c8", 8, r, Optional.empty())));
		});

		return new InitialDeck(cards);

	}
}
