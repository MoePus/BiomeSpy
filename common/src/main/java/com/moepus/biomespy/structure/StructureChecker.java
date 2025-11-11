package com.moepus.biomespy.structure;

import com.moepus.biomespy.biome.BiomeEnvelopeSelector;
import com.moepus.biomespy.biome.LazyBiomeNoiseChecker;
import com.moepus.biomespy.mixin.StructureCheckAccessor;
import com.moepus.biomespy.mixin.StructureManagerAccessor;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Map;

public class StructureChecker {
    private static boolean tryAddReference(StructureManager pStructureManager, StructureStart pStructureStart) {
        if (pStructureStart.canBeReferenced()) {
            pStructureManager.addReference(pStructureStart);
            return true;
        }
        return false;
    }

    private static StructureCheckResult checkStructureInfo(Object2IntMap<Structure> pStructureChunks, Structure pStructure, boolean pSkipKnownStructures) {
        int i = pStructureChunks.getOrDefault(pStructure, -1);
        return i == -1 || pSkipKnownStructures && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
    }

    public static Pair<BlockPos, Holder<Structure>>
    getStructureGeneratingAt(Map<Holder<Structure>, BiomeEnvelopeSelector> pStructureHoldersSet, LevelReader pLevel,
                             StructureManager pStructureManager, boolean pSkipKnownStructures, StructurePlacement pPlacement,
                             ChunkPos pChunkPos, Climate.ParameterList<Holder<Biome>> parameters) {
        StructureCheckAccessor structureCheckAccessor = (StructureCheckAccessor) (((StructureManagerAccessor) pStructureManager).getStructureCheck());
        if (!pPlacement.applyAdditionalChunkRestrictions(pChunkPos.x, pChunkPos.z, structureCheckAccessor.getSeed()))
            return null;

        Climate.Sampler sampler = structureCheckAccessor.getRandomState().sampler();
        Object2IntMap<Structure> structureChunkMap = structureCheckAccessor.getLoadedChunks().get(pChunkPos.toLong());
        int x = pChunkPos.getMinBlockX();
        int z = pChunkPos.getMinBlockZ();

        for (var entry : pStructureHoldersSet.entrySet()) {
            Holder<Structure> holder = entry.getKey();

            StructureCheckResult structurecheckresult;
            if (structureChunkMap != null) {
                structurecheckresult = checkStructureInfo(structureChunkMap, holder.value(), pSkipKnownStructures);
            } else {
                LazyBiomeNoiseChecker biomeChecker = new LazyBiomeNoiseChecker(entry.getValue(), parameters, x, z);
                if (!biomeChecker.matches(sampler))
                    continue;

                structurecheckresult = pStructureManager.checkStructurePresence(pChunkPos, holder.value(), pPlacement, pSkipKnownStructures);
            }
            if (structurecheckresult == StructureCheckResult.START_NOT_PRESENT) continue;

            if (!pSkipKnownStructures && structurecheckresult == StructureCheckResult.START_PRESENT) {
                return Pair.of(pPlacement.getLocatePos(pChunkPos), holder);
            }

            // SkipKnownStructures || StructureCheckResult.CHUNK_LOAD_NEEDED
            ChunkAccess chunkaccess = pLevel.getChunk(pChunkPos.x, pChunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structurestart = pStructureManager.getStartForStructure(SectionPos.bottomOf(chunkaccess), holder.value(), chunkaccess);
            if (structurestart != null && structurestart.isValid() && (!pSkipKnownStructures || tryAddReference(pStructureManager, structurestart))) {
                return Pair.of(pPlacement.getLocatePos(structurestart.getChunkPos()), holder);
            }
        }

        return null;
    }
}
