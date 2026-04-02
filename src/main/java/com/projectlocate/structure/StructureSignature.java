package com.projectlocate.structure;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Represents a single block signature used to detect a structure.
 * Each signature has a weight indicating how strongly its presence
 * suggests the structure is nearby.
 */
public record StructureSignature(
	Identifier blockId,
	int weight,
	boolean isHighConfidence
) {
	/**
	 * Creates a signature with default weight of 1.
	 */
	public static StructureSignature of(String blockId, int weight) {
		return new StructureSignature(
			Identifier.tryParse(blockId),
			weight,
			weight >= 5
		);
	}

	/**
	 * Creates a high-confidence signature (weight >= 5 marks it as high confidence).
	 */
	public static StructureSignature strong(String blockId, int weight) {
		return new StructureSignature(
			Identifier.tryParse(blockId),
			weight,
			true
		);
	}
}
