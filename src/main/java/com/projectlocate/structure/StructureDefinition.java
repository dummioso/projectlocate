package com.projectlocate.structure;

import java.util.List;

/**
 * A complete structure definition containing its display name,
 * block signatures, and the minimum score required to confirm detection.
 */
public record StructureDefinition(
	String displayName,
	List<StructureSignature> signatures,
	int minScore
) {
	/**
	 * Returns true if the given score meets the detection threshold.
	 */
	public boolean meetsThreshold(int score) {
		return score >= minScore;
	}
}
