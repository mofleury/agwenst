package org.mofleury.agwenst.domain.still;

import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InitialDeck {

	private final Collection<Card> cards;
}
