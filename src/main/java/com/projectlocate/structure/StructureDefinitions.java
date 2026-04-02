package com.projectlocate.structure;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines all supported structures and their block signatures.
 * Each structure has a display name, a list of signatures with weights,
 * and a minimum score threshold to confirm detection.
 */
public class StructureDefinitions {

	private StructureDefinitions() {}

	/**
	 * All structure definitions keyed by their command name.
	 */
	public static final Map<String, StructureDefinition> DEFINITIONS = Map.ofEntries(
		Map.entry("trial_chambers", new StructureDefinition(
			"Trial Chambers",
			List.of(
				StructureSignature.strong("minecraft:trial_spawner", 10),
				StructureSignature.strong("minecraft:trial_vault", 8),
				StructureSignature.of("minecraft:copper_bulb", 3),
				StructureSignature.of("minecraft:tuff_bricks", 2),
				StructureSignature.of("minecraft:chiseled_tuff", 3)
			),
			8
		)),

		Map.entry("ancient_city", new StructureDefinition(
			"Ancient City",
			List.of(
				StructureSignature.strong("minecraft:reinforced_deepslate", 10),
				StructureSignature.strong("minecraft:sculk_shrieker", 8),
				StructureSignature.of("minecraft:sculk_catalyst", 4)
			),
			8
		)),

		Map.entry("desert_pyramid", new StructureDefinition(
			"Desert Pyramid",
			List.of(
				StructureSignature.strong("minecraft:chiseled_sandstone", 6),
				StructureSignature.of("minecraft:cut_sandstone", 2),
				StructureSignature.of("minecraft:blue_terracotta", 5)
			),
			7
		)),

		Map.entry("ocean_monument", new StructureDefinition(
			"Ocean Monument",
			List.of(
				StructureSignature.of("minecraft:sea_lantern", 3),
				StructureSignature.of("minecraft:dark_prismarine", 3),
				StructureSignature.of("minecraft:prismarine_bricks", 4)
			),
			7
		)),

		Map.entry("stronghold", new StructureDefinition(
			"Stronghold",
			List.of(
				StructureSignature.strong("minecraft:end_portal_frame", 15)
			),
			10
		)),

		Map.entry("nether_fortress", new StructureDefinition(
			"Nether Fortress",
			List.of(
				StructureSignature.of("minecraft:nether_brick_fence", 4),
				StructureSignature.of("minecraft:nether_bricks", 2)
			),
			5
		)),

		Map.entry("bastion_remnant", new StructureDefinition(
			"Bastion Remnant",
			List.of(
				StructureSignature.strong("minecraft:gilded_blackstone", 8),
				StructureSignature.of("minecraft:polished_blackstone_bricks", 3),
				StructureSignature.of("minecraft:blackstone", 1)
			),
			7
		)),

		Map.entry("pillager_outpost", new StructureDefinition(
			"Pillager Outpost",
			List.of(
				StructureSignature.of("minecraft:white_banner", 5),
				StructureSignature.of("minecraft:dark_oak_fence", 3)
			),
			5
		)),

		Map.entry("mineshaft", new StructureDefinition(
			"Mineshaft",
			List.of(
				StructureSignature.of("minecraft:rail", 2),
				StructureSignature.of("minecraft:cobweb", 2),
				StructureSignature.of("minecraft:oak_planks", 1),
				StructureSignature.of("minecraft:oak_fence", 2)
			),
			5
		)),

		Map.entry("trail_ruins", new StructureDefinition(
			"Trail Ruins",
			List.of(
				StructureSignature.strong("minecraft:suspicious_gravel", 8),
				StructureSignature.of("minecraft:mud_bricks", 3),
				StructureSignature.of("minecraft:terracotta", 1)
			),
			7
		))
	);

	/**
	 * All supported structure command names for tab completion.
	 */
	public static final Set<String> SUPPORTED_NAMES = DEFINITIONS.keySet();

	/**
	 * Look up a structure definition by its command name.
	 */
	public static Optional<StructureDefinition> getByName(String name) {
		return Optional.ofNullable(DEFINITIONS.get(name.toLowerCase()));
	}

	/**
	 * Returns all supported structure names sorted alphabetically.
	 */
	public static List<String> getSortedNames() {
		return SUPPORTED_NAMES.stream().sorted().collect(Collectors.toList());
	}
}
