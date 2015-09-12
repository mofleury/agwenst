package org.mofleury.agwenst.domain.still;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
public class Card {
	private final String name;
	private final int value;
}
