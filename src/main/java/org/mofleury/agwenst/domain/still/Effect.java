package org.mofleury.agwenst.domain.still;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Effect {
	SAP("x") {
		@Override
		public int apply(int currentValue) {
			return 1;
		}
	};
	private final String label;

	public abstract int apply(int currentValue);

	public int getPriority(){
		return ordinal();
	}
}
