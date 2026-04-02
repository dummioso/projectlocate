package com.projectlocate.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.projectlocate.scanner.StructureScanner;
import com.projectlocate.structure.StructureDefinition;
import com.projectlocate.structure.StructureDefinitions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Registers the /clocate command with tab completion and structure scanning.
 */
public class ClocateCommand {

	public static void register(
		CommandDispatcher<ServerCommandSource> dispatcher,
		CommandRegistryAccess registryAccess,
		CommandManager.RegistrationEnvironment environment
	) {
		dispatcher.register(
			CommandManager.literal("clocate")
				.requires(source -> source.hasPermissionLevel(2))
				.then(
					CommandManager.argument("structure", StringArgumentType.word())
						.suggests(ClocateCommand::suggestStructures)
						.executes(ClocateCommand::execute)
				)
		);
	}

	/**
	 * Provides tab completion for structure names.
	 */
	private static CompletableFuture<Suggestions> suggestStructures(
		CommandContext<ServerCommandSource> context,
		SuggestionsBuilder builder
	) {
		String remaining = builder.getRemaining().toLowerCase();
		for (String name : StructureDefinitions.getSortedNames()) {
			if (name.startsWith(remaining)) {
				builder.suggest(name);
			}
		}
		return builder.buildFuture();
	}

	/**
	 * Executes the /clocate command.
	 */
	private static int execute(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		String structureName = StringArgumentType.getString(context, "structure");

		// Validate structure name
		Optional<StructureDefinition> structureOpt = StructureDefinitions.getByName(structureName);
		if (structureOpt.isEmpty()) {
			source.sendError(Text.literal(
				"Unknown structure: " + structureName + ". Supported: "
				+ String.join(", ", StructureDefinitions.getSortedNames())
			).formatted(Formatting.RED));
			return 0;
		}

		StructureDefinition structure = structureOpt.get();

		// Get the executing entity (must be a player)
		ServerPlayerEntity player = source.getPlayer();
		if (player == null) {
			source.sendError(Text.literal("This command can only be run by a player.").formatted(Formatting.RED));
			return 0;
		}

		Vec3d playerPos = player.getPos();

		// Run scan on the server thread
		source.getServer().executeSync(() -> {
			StructureScanner.ScanResult result = StructureScanner.scan(
				source.getWorld(),
				playerPos,
				structure
			);

			if (result != null) {
				BlockPos pos = result.position();
				String direction = StructureScanner.getDirection(playerPos, pos);

				// Primary result message
				source.sendFeedback(() -> Text.literal(
					structure.displayName() + " at "
					+ pos.getX() + " " + pos.getY() + " " + pos.getZ()
				).formatted(Formatting.GREEN), false);

				// Distance and direction info
				source.sendFeedback(() -> Text.literal(
					String.format("%.1f blocks away (%s)", result.distanceFromPlayer(), direction)
				).formatted(Formatting.GRAY), false);

				// Debug info with score
				source.sendFeedback(() -> Text.literal(
					"[confidence score: " + result.score() + "]"
				).formatted(Formatting.DARK_GRAY), false);
			} else {
				source.sendFeedback(() -> Text.literal(
					"No " + structure.displayName() + " found within 8 chunks."
				).formatted(Formatting.YELLOW), false);
			}
		});

		return 1;
	}
}
