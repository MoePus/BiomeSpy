package com.moepus.biomespy.compat.terrablender;

import com.moepus.biomespy.biome.BiomeEnvelope;
import com.moepus.biomespy.biome.BiomeNoiseCheckState;
import com.moepus.biomespy.biome.LazyBiomeNoiseChecker;
import com.moepus.biomespy.mixin.MultiNoiseBiomeSourceAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import terrablender.worldgen.IExtendedParameterList;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class BiomeFinder {
    public static boolean shouldUseThis(MultiNoiseBiomeSource biomeSource) {
        var parameters = ((MultiNoiseBiomeSourceAccessor) biomeSource).invokeParameters();
        return ((IExtendedParameterList<?>) parameters).isInitialized();
    }

    public static Pair<BlockPos, Holder<Biome>> findClosestBiome3d(@NotNull BlockPos pos, int radius, int horizontalStep,
                                                                   int verticalStep, Predicate<Holder<Biome>> biomePredicate,
                                                                   Climate.Sampler sampler, LevelReader level,
                                                                   MultiNoiseBiomeSource biomeSource) {
        List<Holder<Biome>> biomes = biomeSource.possibleBiomes().stream().filter(biomePredicate).toList();
        if (biomes.isEmpty()) return null;

        Set<Holder<Biome>> set = Set.copyOf(biomes);
        var parameters = ((MultiNoiseBiomeSourceAccessor) biomeSource).invokeParameters();
        BiomeNoiseCheckState noiseCheckState = new BiomeNoiseCheckState();

        int i = Math.floorDiv(radius * 2, horizontalStep);
        int[] heights = Mth.outFromOrigin(pos.getY(), level.getMinBuildHeight() + 1, level.getMaxBuildHeight(), verticalStep).toArray();
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, i, Direction.EAST, Direction.SOUTH)) {
            int x = pos.getX() + blockpos$mutableblockpos.getX() * horizontalStep;
            int z = pos.getZ() + blockpos$mutableblockpos.getZ() * horizontalStep;
            int qx = QuartPos.fromBlock(x);
            int qz = QuartPos.fromBlock(z);
            BiomeEnvelope biomeEnvelope = BiomeEnvelope.of(set, biomeSource, qx, qz);
            LazyBiomeNoiseChecker biomeChecker = new LazyBiomeNoiseChecker(x, z);
            if (!biomeChecker.matches(sampler, biomeEnvelope, noiseCheckState))
                continue;
            for (int y : heights) {
                DensityFunction.SinglePointContext verticalContext = new DensityFunction.SinglePointContext(x, y, z);
                long d = Climate.quantizeCoord((float) sampler.depth().compute(verticalContext));
                Climate.TargetPoint climate = biomeChecker.toTargetPoint(sampler, d);
                Holder<Biome> holder = (Holder<Biome>) ((IExtendedParameterList<?>) parameters)
                        .findValuePositional(climate, qx, QuartPos.fromBlock(y), qz);
                if (set.contains(holder)) {
                    return Pair.of(new BlockPos(x, y, z), holder);
                }
            }
        }
        return null;
    }
}