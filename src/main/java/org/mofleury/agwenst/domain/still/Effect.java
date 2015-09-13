package org.mofleury.agwenst.domain.still;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Effect {
	SAP("x");
	private final String label;
}
