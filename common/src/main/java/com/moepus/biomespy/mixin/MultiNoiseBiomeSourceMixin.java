package com.moepus.biomespy.mixin;

import com.moepus.biomespy.BiomeEnvelope;
import com.moepus.biomespy.compat.terrablender.BiomeFinder;
import com.moepus.biomespy.platform.Services;
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

@Mixin(value = MultiNoiseBiomeSource.class, remap = false)
public abstract class MultiNoiseBiomeSourceMixin extends BiomeSource {
    @Shadow
    protected abstract Climate.ParameterList<Holder<Biome>> parameters();

    @Shadow
    public abstract Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint);

    @Override
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(@NotNull BlockPos pos, int radius, int horizontalStep, int verticalStep, Predicate<Holder<Biome>> biomePredicate, Climate.Sampler sampler, LevelReader level) {
        if (Services.PLATFORM.isModLoaded("terrablender")) {
            if (BiomeFinder.shouldUseThis((MultiNoiseBiomeSource) (Object) this)) {
                return BiomeFinder.findClosestBiome3d(pos, radius, horizontalStep, verticalStep, biomePredicate, sampler, level, (MultiNoiseBiomeSource) (Object) this);
            }
        }

        Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(biomePredicate).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) return null;
        BiomeEnvelope biomeEnvelope = new BiomeEnvelope();
        for (Pair<Climate.ParameterPoint, Holder<Biome>> p : this.parameters().values()) {
            Holder<Biome> biome = p.getSecond();
            if (!set.contains(biome)) continue;
            biomeEnvelope.add(p.getFirst());
        }
        int i = Math.floorDiv(radius * 2, horizontalStep);
        int[] heights = Mth.outFromOrigin(pos.getY(), level.getMinBuildHeight() + 1, level.getMaxBuildHeight(), verticalStep).toArray();
        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, i, Direction.EAST, Direction.SOUTH)) {
            int x = pos.getX() + blockpos$mutableblockpos.getX() * horizontalStep;
            int z = pos.getZ() + blockpos$mutableblockpos.getZ() * horizontalStep;
            DensityFunction.SinglePointContext horizontalContext = new DensityFunction.SinglePointContext(x, 0, z);
            long t = Climate.quantizeCoord((float) sampler.temperature().compute(horizontalContext));
            if (t < biomeEnvelope.tMin || t > biomeEnvelope.tMax) continue;
            long h = Climate.quantizeCoord((float) sampler.humidity().compute(horizontalContext));
            if (h < biomeEnvelope.hMin || h > biomeEnvelope.hMax) continue;
            long w = Climate.quantizeCoord((float) sampler.weirdness().compute(horizontalContext));
            if (w < biomeEnvelope.wMin || w > biomeEnvelope.wMax) continue;
            long c = Climate.quantizeCoord((float) sampler.continentalness().compute(horizontalContext));
            if (c < biomeEnvelope.cMin || c > biomeEnvelope.cMax) continue;
            long e = Climate.quantizeCoord((float) sampler.erosion().compute(horizontalContext));
            if (e < biomeEnvelope.eMin || e > biomeEnvelope.eMax) continue;
            for (int y : heights) {
                DensityFunction.SinglePointContext verticalContext = new DensityFunction.SinglePointContext(x, y, z);
                long d = Climate.quantizeCoord((float) sampler.depth().compute(verticalContext));
                Climate.TargetPoint climate = new Climate.TargetPoint(t, h, c, e, d, w);
                Holder<Biome> holder = this.getNoiseBiome(climate);
                if (set.contains(holder)) {
                    return Pair.of(new BlockPos(x, y, z), holder);
                }
            }
        }
        return null;
    }
}