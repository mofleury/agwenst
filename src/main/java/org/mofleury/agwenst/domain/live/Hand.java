package org.mofleury.agwenst.domain.live;

import java.util.ArrayList;
import java.util.Collection;

import org.mofleury.agwenst.domain.still.Card;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Hand {
	private final Collection<Card> cards = new ArrayList<>();
}
