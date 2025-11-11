package com.moepus.biomespy.compat.terrablender;

import com.moepus.biomespy.biome.BiomeEnvelope;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import terrablender.api.RegionType;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IParameterListExtendedInfo {
    void biomeSpy$gatherExtendedInfo(RegistryAccess registryAccess, RegionType regionType, long seed);

    BiomeEnvelope biomeSpy$getEnvelopeForBiomes(List<Holder<Biome>> biomes, int regionIndex);

    void biomeSpy$visitAllEnvelopes(BiConsumer<Integer, HashMap<Holder<Biome>, BiomeEnvelope>> visitor);
}
