package org.mofleury.agwenst.domain.live;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Row {

	private EngagedCard modifier;
	private final List<EngagedCard> cards = new ArrayList<>();
}
