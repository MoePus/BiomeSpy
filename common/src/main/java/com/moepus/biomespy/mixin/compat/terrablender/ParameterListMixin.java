package com.moepus.biomespy.mixin.compat.terrablender;

import com.moepus.biomespy.BiomeEnvelope;
import com.moepus.biomespy.compat.terrablender.IParameterListExtendedInfo;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import terrablender.api.Region;
import terrablender.api.RegionType;
import terrablender.api.Regions;
import terrablender.worldgen.IExtendedParameterList;

import java.util.*;

@Mixin(value = Climate.ParameterList.class, remap = false)
public abstract class ParameterListMixin<T> implements IParameterListExtendedInfo, IExtendedParameterList<T> {
    @Shadow
    @Final
    private List<Pair<Climate.ParameterPoint, T>> values;

    @Unique
    private boolean biomeSpy$initialized = false;

    @Unique
    private HashMap<T, BiomeEnvelope> biomeSpy$uniqueTrees[];

    @Override
    public void biomeSpy$gatherExtendedInfo(RegistryAccess registryAccess, RegionType regionType, long seed) {
        if (biomeSpy$initialized) return;
        biomeSpy$initialized = true;

        biomeSpy$uniqueTrees = new HashMap[Regions.getCount(regionType)];
        Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        for (Region region : Regions.get(regionType)) {
            int regionIndex = Regions.getIndex(regionType, region.getName());
            if (regionIndex == 0) {
                HashMap<T, BiomeEnvelope> biomeEnvelopeHashMap = new HashMap<>();
                for (Pair<Climate.ParameterPoint, T> p : this.values) {
                    T biome = p.getSecond();
                    biomeEnvelopeHashMap.computeIfAbsent(biome, k -> new BiomeEnvelope()).add(p.getFirst());
                }
                this.biomeSpy$uniqueTrees[0] = biomeEnvelopeHashMap;
            } else {
                HashMap<T, BiomeEnvelope> biomeEnvelopeHashMap = new HashMap<>();
                region.addBiomes(biomeRegistry, (pair) -> {
                    Optional<Holder.Reference<Biome>> biome = biomeRegistry.getHolder(pair.getSecond());
                    if (biome.isPresent()) {
                        Holder<Biome> biomeHolder = biome.get();
                        biomeEnvelopeHashMap.computeIfAbsent((T) biomeHolder, k -> new BiomeEnvelope()).add(pair.getFirst());
                    }
                });
                this.biomeSpy$uniqueTrees[regionIndex] = biomeEnvelopeHashMap;
            }
        }
    }

    @Override
    public BiomeEnvelope biomeSpy$getEnvelopeForBiomes(List<Holder<Biome>> biomes, int regionIndex) {
        BiomeEnvelope combinedEnvelope = new BiomeEnvelope();
        HashMap<T, BiomeEnvelope> biomeEnvelopeHashMap = this.biomeSpy$uniqueTrees[regionIndex];
        for (Holder<Biome> biome : biomes) {
            BiomeEnvelope envelope = biomeEnvelopeHashMap.get((T) biome);
            if (envelope != null) {
                combinedEnvelope.add(envelope);
            }
        }
        return combinedEnvelope;
    }

    public Climate.ParameterList<T> clone() {
        try {
            return (Climate.ParameterList)super.clone();
        } catch (CloneNotSupportedException var2) {
            throw new AssertionError();
        }
    }
}
