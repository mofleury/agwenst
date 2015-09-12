package org.mofleury.agwenst.domain.live;

import org.mofleury.agwenst.domain.still.Card;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EngagedCard {
	private final Card card;
	private int currentValue;

	public EngagedCard(Card card) {
		this.card = card;
		this.currentValue = card.getValue();
	}
}
