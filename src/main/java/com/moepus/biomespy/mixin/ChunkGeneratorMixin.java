package com.moepus.biomespy.mixin;

import com.moepus.biomespy.biome.BiomeEnvelopeSelector;
import com.moepus.biomespy.structure.StructureChecker;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Shadow
    @Final
    protected BiomeSource biomeSource;

    /**
     * @author MoePus
     * @reason Optimize structure generation checks
     */
    @Inject(method = "getNearestGeneratedStructure(Ljava/util/Set;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/StructureManager;IIIZJLnet/minecraft/world/level/levelgen/structure/placement/RandomSpreadStructurePlacement;)Lcom/mojang/datafixers/util/Pair;", at = @At("HEAD"), cancellable = true)
    private static void getNearestGeneratedStructure(
            Set<Holder<Structure>> pStructureHoldersSet, LevelReader pLevel, StructureManager pStructureManager, int pX, int pY, int pZ, boolean pSkipKnownStructures, long pSeed, RandomSpreadStructurePlacement pSpreadPlacement, CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir
    ) {
        BiomeSource biomeSource = ((StructureCheckAccessor) (((StructureManagerAccessor) pStructureManager).getStructureCheck())).getBiomeSource();
        if (!(biomeSource instanceof MultiNoiseBiomeSource)) return;
        cir.cancel();

        int spacing = pSpreadPlacement.spacing();

        var parameters = ((MultiNoiseBiomeSourceAccessor) biomeSource).invokeParameters();
        Map<Holder<Structure>, BiomeEnvelopeSelector> structureBiome = new HashMap<>();
        for (Holder<Structure> structure : pStructureHoldersSet) {
            structureBiome.put(structure, new BiomeEnvelopeSelector(structure.value().biomes().stream().toList(), parameters, (MultiNoiseBiomeSource)biomeSource));
        }

        for (int j = -pZ; j <= pZ; j++) {
            boolean flag = j == -pZ || j == pZ;

            for (int k = -pZ; k <= pZ; k++) {
                boolean flag1 = k == -pZ || k == pZ;

                if (!flag && !flag1) continue;

                int regionX = pX + spacing * j;
                int regionZ = pY + spacing * k;
                ChunkPos chunkpos = pSpreadPlacement.getPotentialStructureChunk(pSeed, regionX, regionZ);

                Pair<BlockPos, Holder<Structure>> pair = StructureChecker.getStructureGeneratingAt(structureBiome, pLevel,
                        pStructureManager, pSkipKnownStructures, pSpreadPlacement, chunkpos, parameters);
                if (pair == null) continue;

                cir.setReturnValue(pair);
                return;
            }
        }

        cir.setReturnValue(null);
    }
}
