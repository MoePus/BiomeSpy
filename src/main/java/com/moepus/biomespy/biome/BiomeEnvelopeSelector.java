package com.moepus.biomespy.biome;

import com.moepus.biomespy.compat.alexscaves.AlexBiome;
import com.moepus.biomespy.compat.alexscaves.AlexsCavesCompat;
import com.moepus.biomespy.compat.terrablender.IParameterListExtendedInfo;
import com.moepus.biomespy.compat.terrablender.TerrablenderCompat;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import terrablender.worldgen.IExtendedParameterList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BiomeEnvelopeSelector {
    private final Map<Integer, BiomeEnvelope> envelopeMap;
    private final Map<Class<?>, Object> platformData = new HashMap<>();

    public BiomeEnvelopeSelector(Collection<Holder<Biome>> biomes, Climate.ParameterList<Holder<Biome>> parameters, MultiNoiseBiomeSource biomeSource) {
        this.envelopeMap = new HashMap<>();
        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            ((IParameterListExtendedInfo) parameters).biomeSpy$visitAllEnvelopes((index, map) -> {
                BiomeEnvelope combinedEnvelope = new BiomeEnvelope();
                combinedEnvelope.impossible = true;
                for (var entry : map.entrySet()) {
                    if (biomes.contains(entry.getKey())) {
                        combinedEnvelope.impossible = false;
                        combinedEnvelope.add(entry.getValue());
                    }
                }
                this.envelopeMap.put(index, combinedEnvelope);
            });
        } else {
            BiomeEnvelope combinedEnvelope = new BiomeEnvelope();
            combinedEnvelope.impossible = true;
            for (var pair : parameters.values()) {
                if (biomes.contains(pair.getSecond())) {
                    combinedEnvelope.impossible = false;
                    combinedEnvelope.add(pair.getFirst());
                }
            }
            this.envelopeMap.put(0, combinedEnvelope);
        }
        if (AlexsCavesCompat.ALEXS_CAVES_INSTALLED) {
            AlexBiome.initAlexsCavesData(this, biomes, parameters, biomeSource);
        }
    }

    public BiomeEnvelope getEnvelope(Climate.ParameterList<Holder<Biome>> parameters, int qx, int qy, int qz) {
        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            int uniqueness = ((IExtendedParameterList<?>) parameters).getUniqueness(qx, qy, qz);
            return envelopeMap.get(uniqueness);
        }
        return envelopeMap.get(0);
    }

    public <T> void setPlatformData(Class<?> key, T data) {
        platformData.put(key, data);
    }

    public Object getPlatformData(Class<?> key) {
        return platformData.get(key);
    }
}
