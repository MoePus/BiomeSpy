package com.moepus.biomespy.biome;

import com.moepus.biomespy.compat.terrablender.IParameterListExtendedInfo;
import com.moepus.biomespy.compat.terrablender.TerrablenderCompat;
import com.moepus.biomespy.platform.Services;
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

    public BiomeEnvelopeSelector(Collection<Holder<Biome>> biomes, Climate.ParameterList<Holder<Biome>> parameters, MultiNoiseBiomeSource biomeSource) {
        this.envelopeMap = new HashMap<>();
        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            ((IParameterListExtendedInfo) parameters).biomeSpy$visitAllEnvelopes((index, map) -> {
                BiomeEnvelope combinedEnvelope = new BiomeEnvelope();
                for (var entry : map.entrySet()) {
                    if (biomes.contains(entry.getKey()))
                        combinedEnvelope.add(entry.getValue());
                }
                this.envelopeMap.put(index, combinedEnvelope);
            });
        } else {
            BiomeEnvelope combinedEnvelope = new BiomeEnvelope();
            for (var pair : parameters.values()) {
                if (biomes.contains(pair.getSecond())) {
                    combinedEnvelope.add(pair.getFirst());
                }
            }
            this.envelopeMap.put(0, combinedEnvelope);
        }
        Services.PLATFORM.initPlatformSpecificBiomeEnvelope(this.envelopeMap, biomes, parameters, biomeSource);
    }

    public BiomeEnvelope getEnvelope(Climate.ParameterList<Holder<Biome>> parameters, int qx, int qy, int qz) {
        BiomeEnvelope platformEnvelope = Services.PLATFORM.getPlatformSpecificBiomeEnvelope(parameters, qx, qy, qz);
        if (platformEnvelope != null) {
            return platformEnvelope;
        }
        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            int uniqueness = ((IExtendedParameterList<?>) parameters).getUniqueness(qx, qy, qz);
            return envelopeMap.get(uniqueness);
        }
        return envelopeMap.get(0);
    }
}
