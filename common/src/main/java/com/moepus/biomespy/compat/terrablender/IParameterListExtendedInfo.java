package com.moepus.biomespy.compat.terrablender;

import com.moepus.biomespy.BiomeEnvelope;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import terrablender.api.RegionType;

import java.util.List;

public interface IParameterListExtendedInfo {
    void biomeSpy$gatherExtendedInfo(RegistryAccess registryAccess, RegionType regionType, long seed);

    BiomeEnvelope biomeSpy$getEnvelopeForBiomes(List<Holder<Biome>> biomes, int regionIndex);
}
