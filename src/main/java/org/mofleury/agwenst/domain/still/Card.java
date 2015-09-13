package org.mofleury.agwenst.domain.still;

import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Card {

	private final CardType type;

	private final String name;
	private final int value;

	private final int targetRow;
	private final Optional<Effect> effect;
}
