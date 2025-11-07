package com.moepus.biomespy.biome;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class LazyBiomeNoiseChecker {
    private final DensityFunction.SinglePointContext ctx;
    private Long temperature;
    private Long humidity;
    private Long continentalness;
    private Long erosion;
    private Long weirdness;

    public LazyBiomeNoiseChecker(int x, int z) {
        this.ctx = new DensityFunction.SinglePointContext(x, 0, z);
    }

    private long getTemperature(Climate.Sampler sampler) {
        if (temperature == null)
            temperature = Climate.quantizeCoord((float) sampler.temperature().compute(ctx));
        return temperature;
    }

    private long getHumidity(Climate.Sampler sampler) {
        if (humidity == null)
            humidity = Climate.quantizeCoord((float) sampler.humidity().compute(ctx));
        return humidity;
    }

    private long getContinentalness(Climate.Sampler sampler) {
        if (continentalness == null)
            continentalness = Climate.quantizeCoord((float) sampler.continentalness().compute(ctx));
        return continentalness;
    }

    private long getErosion(Climate.Sampler sampler) {
        if (erosion == null)
            erosion = Climate.quantizeCoord((float) sampler.erosion().compute(ctx));
        return erosion;
    }

    private long getWeirdness(Climate.Sampler sampler) {
        if (weirdness == null)
            weirdness = Climate.quantizeCoord((float) sampler.weirdness().compute(ctx));
        return weirdness;
    }

    public boolean matches(Climate.Sampler sampler, BiomeEnvelope env) {
        if (env.impossible) return false;
        for (BiomeNoiseCheckState.NoiseType type : BiomeNoiseCheckState.NoiseType.values()) {
            if (!checkSingleNoise(sampler, env, type)) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(Climate.Sampler sampler, BiomeEnvelope env, BiomeNoiseCheckState state) {
        if (env.impossible) return false;

        BiomeNoiseCheckState.NoiseType priority = state.getLastFilteredNoise();
        if (priority != BiomeNoiseCheckState.NoiseType.NONE) {
            if (!checkSingleNoise(sampler, env, priority)) {
                state.setLastFilteredNoise(priority);
                return false;
            }
        }

        for (BiomeNoiseCheckState.NoiseType type : BiomeNoiseCheckState.NoiseType.values()) {
            if (type == priority) continue;
            if (!checkSingleNoise(sampler, env, type)) {
                state.setLastFilteredNoise(type);
                return false;
            }
        }

        return true;
    }

    private boolean checkSingleNoise(Climate.Sampler sampler, BiomeEnvelope env,
                                     BiomeNoiseCheckState.NoiseType type) {
        switch (type) {
            case TEMPERATURE -> {
                if (env.tMin <= env.tMax && env.tMin + 20000 > env.tMax) {
                    long v = getTemperature(sampler);
                    if (v < env.tMin || v > env.tMax) return false;
                }
            }
            case HUMIDITY -> {
                if (env.hMin <= env.hMax && env.hMin + 20000 > env.hMax) {
                    long v = getHumidity(sampler);
                    if (v < env.hMin || v > env.hMax) return false;
                }
            }
            case CONTINENTALNESS -> {
                if (env.cMin <= env.cMax && env.cMin + 20000 > env.cMax) {
                    long v = getContinentalness(sampler);
                    if (v < env.cMin || v > env.cMax) return false;
                }
            }
            case EROSION -> {
                if (env.eMin <= env.eMax && env.eMin + 20000 > env.eMax) {
                    long v = getErosion(sampler);
                    if (v < env.eMin || v > env.eMax) return false;
                }
            }
            case WEIRDNESS -> {
                if (env.wMin <= env.wMax && env.wMin + 20000 > env.wMax) {
                    long v = getWeirdness(sampler);
                    if (v < env.wMin || v > env.wMax) return false;
                }
            }
            default -> {
            }
        }
        return true;
    }

    public Climate.TargetPoint toTargetPoint(Climate.Sampler sampler, long depth) {
        return new Climate.TargetPoint(
                getTemperature(sampler),
                getHumidity(sampler),
                getContinentalness(sampler),
                getErosion(sampler),
                depth,
                getWeirdness(sampler)
        );
    }
}
