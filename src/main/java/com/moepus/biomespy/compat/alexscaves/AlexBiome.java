package com.moepus.biomespy.compat.alexscaves;

import com.github.alexmodguy.alexscaves.server.config.BiomeGenerationConfig;
import com.github.alexmodguy.alexscaves.server.config.BiomeGenerationNoiseCondition;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRarity;
import com.github.alexmodguy.alexscaves.server.level.biome.BiomeSourceAccessor;
import com.github.alexmodguy.alexscaves.server.misc.VoronoiGenerator;
import com.moepus.biomespy.biome.BiomeEnvelope;
import com.moepus.biomespy.biome.BiomeEnvelopeSelector;
import com.moepus.biomespy.mixin.compat.alexscaves.BiomeGenerationNoiseConditionAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;

public class AlexBiome {
    public static AlexsCavesBiomeSourceExtendedInfo getExtendedInfo(MultiNoiseBiomeSource biomeSource) {
        try {
            Field f = biomeSource.getClass().getDeclaredField("lastSampledWorldSeed");
            f.setAccessible(true);
            long seed = (Long) f.get(biomeSource);
            Field f2 = biomeSource.getClass().getDeclaredField("lastSampledDimension");
            f2.setAccessible(true);
            ResourceKey<Level> dimension = (ResourceKey) f2.get(biomeSource);
            return new AlexsCavesBiomeSourceExtendedInfo(seed, dimension);
        } catch (Exception e) {
            return null;
        }
    }

    public record AlexsCavesData(AlexsCavesBiomeSourceExtendedInfo extendedInfo,
                                 Map<Integer, AlexBiomeInfo> envelopeMap,
                                 Map<Long, Optional<Holder<Biome>>> cachedBiomes) {
    }

    public record AlexBiomeInfo(Holder<Biome> biome, BiomeEnvelope biomeEnvelope, long dMin, long dMax,
                                int distanceFromSpawn) {
    }

    public static void initAlexsCavesData(BiomeEnvelopeSelector envelopeMap, Collection<Holder<Biome>> biomes,
                                          Climate.ParameterList<Holder<Biome>> parameters, MultiNoiseBiomeSource biomeSource) {
        AlexsCavesBiomeSourceExtendedInfo extendedInfo = getExtendedInfo(biomeSource);
        if (extendedInfo == null) {
            return;
        }
        ResourceKey<Level> dimension = extendedInfo.lastSampledDimension();
        Map<Integer, AlexBiomeInfo> alexsCavesEnvelopeMap = new HashMap<>();
        for (Map.Entry<ResourceKey<Biome>, BiomeGenerationNoiseCondition> condition : BiomeGenerationConfig.BIOMES.entrySet()) {
            if (!(condition.getValue()).isDisabledCompletely()) {
                Holder<Biome> biomeHolder = ((BiomeSourceAccessor) biomeSource).getResourceKeyMap().get(condition.getKey());
                BiomeGenerationNoiseConditionAccessor conditionAccessor = (BiomeGenerationNoiseConditionAccessor) (condition.getValue());
                if (dimension != null && !conditionAccessor.getDimensions().contains(dimension.location().toString())) {
                    continue;
                }
                int rarityOffset = (condition.getValue()).getRarityOffset();

                BiomeEnvelope envelope = new BiomeEnvelope();
                float[] continentalness = conditionAccessor.getContinentalness();
                float[] erosion = conditionAccessor.getErosion();
                float[] humidity = conditionAccessor.getHumidity();
                float[] temperature = conditionAccessor.getTemperature();
                float[] weirdness = conditionAccessor.getWeirdness();

                if (continentalness != null && continentalness.length >= 2) {
                    envelope.cMin = Climate.quantizeCoord(continentalness[0]);
                    envelope.cMax = Climate.quantizeCoord(continentalness[1]);
                }
                if (erosion != null && erosion.length >= 2) {
                    envelope.eMin = Climate.quantizeCoord(erosion[0]);
                    envelope.eMax = Climate.quantizeCoord(erosion[1]);
                }
                if (weirdness != null && weirdness.length >= 2) {
                    envelope.wMin = Climate.quantizeCoord(weirdness[0]);
                    envelope.wMax = Climate.quantizeCoord(weirdness[1]);
                }
                if (humidity != null && humidity.length >= 2) {
                    envelope.hMin = Climate.quantizeCoord(humidity[0]);
                    envelope.hMax = Climate.quantizeCoord(humidity[1]);
                }
                if (temperature != null && temperature.length >= 2) {
                    envelope.tMin = Climate.quantizeCoord(temperature[0]);
                    envelope.tMax = Climate.quantizeCoord(temperature[1]);
                }

                float[] depth = conditionAccessor.getDepth();
                long dMin = Long.MAX_VALUE;
                long dMax = Long.MIN_VALUE;
                if (depth != null && depth.length >= 2) {
                    dMin = Climate.quantizeCoord(depth[0]);
                    dMax = Climate.quantizeCoord(depth[1]);
                }
                AlexBiomeInfo verticalEnvelope = new AlexBiomeInfo(
                        biomeHolder, envelope, dMin, dMax, conditionAccessor.getDistanceFromSpawn());
                alexsCavesEnvelopeMap.put(rarityOffset, verticalEnvelope);
            }
        }
        envelopeMap.setPlatformData(AlexsCavesData.class, new AlexsCavesData(extendedInfo, alexsCavesEnvelopeMap, new HashMap<>()));
    }

    private static boolean isFarEnoughFromSpawn(int x, int z, double dist) {
        return x * x + z * z >= dist * dist;
    }

    public static boolean isAlexsCavesChunk(BiomeEnvelopeSelector envelopeSelector, int x, int z) {
        if (!(envelopeSelector.getPlatformData(AlexsCavesData.class) instanceof AlexsCavesData data))
            return false;
        AlexsCavesBiomeSourceExtendedInfo extendedInfo = data.extendedInfo;
        VoronoiGenerator.VoronoiInfo voronoiInfo = ACBiomeRarity.getRareBiomeInfoForQuad(extendedInfo.lastSampledWorldSeed(), QuartPos.fromBlock(x), QuartPos.fromBlock(z));
        return voronoiInfo != null;
    }


    public static Holder<Biome> getAlexsCavesBiome(BiomeEnvelopeSelector envelopeSelector, Climate.Sampler sampler, int x, int y, int z) {
        if (!(envelopeSelector.getPlatformData(AlexsCavesData.class) instanceof AlexsCavesData data))
            return null;
        Map<Integer, AlexBiomeInfo> envelopeMap = data.envelopeMap;
        AlexsCavesBiomeSourceExtendedInfo extendedInfo = data.extendedInfo;
        VoronoiGenerator.VoronoiInfo voronoiInfo = ACBiomeRarity.getRareBiomeInfoForQuad(extendedInfo.lastSampledWorldSeed(), QuartPos.fromBlock(x), QuartPos.fromBlock(z));
        if (voronoiInfo == null) return null;
        Vec3 rareBiomeCenter = ACBiomeRarity.getRareBiomeCenter(voronoiInfo);
        if (rareBiomeCenter == null) return null;

        int foundRarityOffset = ACBiomeRarity.getRareBiomeOffsetId(voronoiInfo);
        AlexBiomeInfo biomeInfo = envelopeMap.get(foundRarityOffset);
        if (biomeInfo == null) return null;

        if (!isFarEnoughFromSpawn(x, z, biomeInfo.distanceFromSpawn))
            return null;

        DensityFunction.SinglePointContext ctx = new DensityFunction.SinglePointContext(
                QuartPos.toBlock((int) Math.floor(rareBiomeCenter.x)), y, QuartPos.toBlock((int) Math.floor(rareBiomeCenter.z)));

        long depth = Climate.quantizeCoord((float) sampler.depth().compute(ctx));
        if (depth < biomeInfo.dMin || depth > biomeInfo.dMax) {
            return null;
        }

        long blockPos = (long) ctx.blockX() << 32 | (ctx.blockZ() & 0xFFFFFFFFL);
        return data.cachedBiomes.computeIfAbsent(blockPos,
                pos -> biomeInfo.biomeEnvelope.matches(sampler, ctx) ? Optional.of(biomeInfo.biome) : Optional.empty()).orElse(null);
    }
}
