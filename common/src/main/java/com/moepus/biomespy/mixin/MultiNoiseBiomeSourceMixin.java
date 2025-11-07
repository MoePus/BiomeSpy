package com.moepus.biomespy.mixin;

import com.moepus.biomespy.biome.BiomeEnvelope;
import com.moepus.biomespy.biome.BiomeNoiseCheckState;
import com.moepus.biomespy.biome.LazyBiomeNoiseChecker;
import com.moepus.biomespy.compat.terrablender.BiomeFinder;
import com.moepus.biomespy.compat.terrablender.TerrablenderCompat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mixin(value = MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin extends BiomeSource {
    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Shadow
    public abstract Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint);

    @Override
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(@NotNull BlockPos pos, int radius, int horizontalStep, int verticalStep, Predicate<Holder<Biome>> biomePredicate, Climate.Sampler sampler, LevelReader level) {
        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            if (BiomeFinder.shouldUseThis((MultiNoiseBiomeSource) (Object) this)) {
                return BiomeFinder.findClosestBiome3d(pos, radius, horizontalStep, verticalStep, biomePredicate, sampler, level, (MultiNoiseBiomeSource) (Object) this);
            }
        }

        Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(biomePredicate).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) return null;
        BiomeEnvelope biomeEnvelope = BiomeEnvelope.of(set, (MultiNoiseBiomeSource) (Object) this, 0, 0);
        BiomeNoiseCheckState noiseCheckState = new BiomeNoiseCheckState();

        int i = Math.floorDiv(radius * 2, horizontalStep);
        int[] heights = Mth.outFromOrigin(pos.getY(), level.getMinBuildHeight() + 1, level.getMaxBuildHeight(), verticalStep).toArray();
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, i, Direction.EAST, Direction.SOUTH)) {
            int x = pos.getX() + blockpos$mutableblockpos.getX() * horizontalStep;
            int z = pos.getZ() + blockpos$mutableblockpos.getZ() * horizontalStep;
            LazyBiomeNoiseChecker biomeChecker = new LazyBiomeNoiseChecker(x, z);
            if (!biomeChecker.matches(sampler, biomeEnvelope, noiseCheckState))
                continue;
            for (int y : heights) {
                DensityFunction.SinglePointContext verticalContext = new DensityFunction.SinglePointContext(x, y, z);
                long d = Climate.quantizeCoord((float) sampler.depth().compute(verticalContext));
                Climate.TargetPoint climate = biomeChecker.toTargetPoint(sampler, d);
                Holder<Biome> holder = this.getNoiseBiome(climate);
                if (set.contains(holder)) {
                    return Pair.of(new BlockPos(x, y, z), holder);
                }
            }
        }
        return null;
    }
}