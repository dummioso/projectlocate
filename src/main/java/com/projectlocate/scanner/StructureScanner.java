package com.projectlocate.scanner;

import com.projectlocate.structure.StructureDefinition;
import com.projectlocate.structure.StructureSignature;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Scans loaded chunks around a player to detect structures
 * using heuristic block signature matching with confidence scoring.
 */
public class StructureScanner {

	private static final int SCAN_RADIUS_CHUNKS = 8;

	// Y-range to scan: from bottom of world to a reasonable upper bound
	// Most structures generate between Y=-64 and Y=200
	private static final int MIN_SCAN_Y = -64;
	private static final int MAX_SCAN_Y = 200;

	// Sample every N blocks to improve performance
	// Scanning every block is too slow; sampling every 4 blocks gives good coverage
	private static final int SAMPLING_STEP = 4;

	/**
	 * Result of a structure scan.
	 */
	public record ScanResult(
		BlockPos position,
		String structureName,
		int score,
		double distanceFromPlayer
	) {}

	/**
	 * Scans loaded chunks around the player for the specified structure.
	 * Returns the first match found, or null if nothing meets the threshold.
	 */
	@Nullable
	public static ScanResult scan(
		ServerWorld world,
		Vec3d playerPos,
		StructureDefinition structure
	) {
		ChunkPos playerChunk = new ChunkPos(
			BlockPos.ofFloored(playerPos)
		);

		// Build a priority queue of chunks ordered by distance from player
		// so we find the closest structure first
		Queue<ChunkScanTask> chunkQueue = new PriorityQueue<>();

		for (int dx = -SCAN_RADIUS_CHUNKS; dx <= SCAN_RADIUS_CHUNKS; dx++) {
			for (int dz = -SCAN_RADIUS_CHUNKS; dz <= SCAN_RADIUS_CHUNKS; dz++) {
				int chunkX = playerChunk.x + dx;
				int chunkZ = playerChunk.z + dz;
				ChunkPos pos = new ChunkPos(chunkX, chunkZ);

				// Only scan loaded chunks
				if (!world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
					continue;
				}

				// Priority is distance squared (closer chunks first)
				double distSq = dx * dx + dz * dz;
				chunkQueue.add(new ChunkScanTask(pos, distSq));
			}
		}

		// Process chunks from closest to farthest
		while (!chunkQueue.isEmpty()) {
			ChunkScanTask task = chunkQueue.poll();
			ScanResult result = scanChunk(world, task.chunkPos(), structure, playerPos);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Scans a single chunk for structure signatures.
	 * Returns a result if the score meets the threshold.
	 */
	@Nullable
	private static ScanResult scanChunk(
		ServerWorld world,
		ChunkPos chunkPos,
		StructureDefinition structure,
		Vec3d playerPos
	) {
		WorldChunk chunk = world.getChunk(chunkPos.x, chunkPos.z);
		if (chunk == null) {
			return null;
		}

		// Track score and best position for this chunk
		int totalScore = 0;
		BlockPos bestPos = null;
		int bestPosScore = 0;

		// Map to track which signature types we've found in this chunk
		// This prevents over-counting the same block type
		Map<Identifier, Integer> signatureCounts = new HashMap<>();
		Map<Identifier, BlockPos> signaturePositions = new HashMap<>();

		int chunkMinX = chunkPos.getStartX();
		int chunkMinZ = chunkPos.getStartZ();

		// Scan blocks with sampling for performance
		for (int x = 0; x < 16; x += SAMPLING_STEP) {
			for (int z = 0; z < 16; z += SAMPLING_STEP) {
				for (int y = MIN_SCAN_Y; y <= MAX_SCAN_Y; y += SAMPLING_STEP) {
					BlockPos pos = new BlockPos(chunkMinX + x, y, chunkMinZ + z);
					BlockState state = chunk.getBlockState(pos);
					Block block = state.getBlock();
					Identifier blockId = Registries.BLOCK.getId(block);

					// Check if this block matches any signature
					for (StructureSignature sig : structure.signatures()) {
						if (sig.blockId().equals(blockId)) {
							signatureCounts.merge(blockId, 1, Integer::sum);
							signaturePositions.putIfAbsent(blockId, pos);

							// Weight is applied once per unique block type found,
							// but we add a small bonus for multiple occurrences
							int count = signatureCounts.get(blockId);
							int contribution = sig.weight();
							if (count > 1) {
								contribution += (count - 1); // Small bonus for multiples
							}
							totalScore += sig.isHighConfidence() ? sig.weight() : 1;

							// Track the position of the highest-confidence block
							if (sig.isHighConfidence() && sig.weight() > bestPosScore) {
								bestPosScore = sig.weight();
								bestPos = pos;
							}
						}
					}
				}
			}
		}

		// Check if we meet the threshold
		if (structure.meetsThreshold(totalScore) && bestPos != null) {
			double distance = playerPos.distanceTo(Vec3d.ofCenter(bestPos));
			return new ScanResult(
				bestPos,
				structure.displayName(),
				totalScore,
				distance
			);
		}

		return null;
	}

	/**
	 * Calculates the cardinal direction from the player to the target position.
	 */
	public static String getDirection(Vec3d from, BlockPos to) {
		double dx = to.getX() - from.x;
		double dz = to.getZ() - from.z;

		if (Math.abs(dx) > Math.abs(dz)) {
			return dx > 0 ? "East" : "West";
		} else {
			return dz > 0 ? "South" : "North";
		}
	}

	/**
	 * Internal record for ordering chunk scan tasks by distance.
	 */
	private record ChunkScanTask(ChunkPos chunkPos, double distanceSq)
		implements Comparable<ChunkScanTask> {
		@Override
		public int compareTo(ChunkScanTask other) {
			return Double.compare(this.distanceSq, other.distanceSq);
		}
	}
}
