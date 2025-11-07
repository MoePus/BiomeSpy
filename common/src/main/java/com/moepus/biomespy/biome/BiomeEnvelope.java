package com.moepus.biomespy.biome;

import com.moepus.biomespy.compat.terrablender.IParameterListExtendedInfo;
import com.moepus.biomespy.compat.terrablender.TerrablenderCompat;
import com.moepus.biomespy.mixin.MultiNoiseBiomeSourceAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import terrablender.worldgen.IExtendedParameterList;

import java.util.Collection;
import java.util.List;

public final class BiomeEnvelope {
    public boolean impossible = false;
    public long tMin = Long.MAX_VALUE, tMax = Long.MIN_VALUE;
    public long hMin = Long.MAX_VALUE, hMax = Long.MIN_VALUE;
    public long cMin = Long.MAX_VALUE, cMax = Long.MIN_VALUE;
    public long eMin = Long.MAX_VALUE, eMax = Long.MIN_VALUE;
    public long wMin = Long.MAX_VALUE, wMax = Long.MIN_VALUE;

    public void add(Climate.ParameterPoint pp) {
        var T = pp.temperature(); tMin = Math.min(tMin, T.min()); tMax = Math.max(tMax, T.max());
        var H = pp.humidity();    hMin = Math.min(hMin, H.min()); hMax = Math.max(hMax, H.max());
        var C = pp.continentalness(); cMin = Math.min(cMin, C.min()); cMax = Math.max(cMax, C.max());
        var E = pp.erosion();     eMin = Math.min(eMin, E.min()); eMax = Math.max(eMax, E.max());
        var W = pp.weirdness();   wMin = Math.min(wMin, W.min()); wMax = Math.max(wMax, W.max());
    }

    public void add(BiomeEnvelope other) {
        tMin = Math.min(tMin, other.tMin); tMax = Math.max(tMax, other.tMax);
        hMin = Math.min(hMin, other.hMin); hMax = Math.max(hMax, other.hMax);
        cMin = Math.min(cMin, other.cMin); cMax = Math.max(cMax, other.cMax);
        eMin = Math.min(eMin, other.eMin); eMax = Math.max(eMax, other.eMax);
        wMin = Math.min(wMin, other.wMin); wMax = Math.max(wMax, other.wMax);
    }

    @Override
    public String toString() {
        if (impossible) return "BiomeEnvelope{impossible}";
        return "BiomeEnvelope{" +
                "T=[" + tMin + "," + tMax + "]," +
                "H=[" + hMin + "," + hMax + "]," +
                "C=[" + cMin + "," + cMax + "]," +
                "E=[" + eMin + "," + eMax + "]," +
                "W=[" + wMin + "," + wMax + "]" +
                '}';
    }

    public static BiomeEnvelope of(Collection<Holder<Biome>> biomes, MultiNoiseBiomeSource biomeSource, int qx, int qz) {
        var parameters = ((MultiNoiseBiomeSourceAccessor) biomeSource).invokeParameters();

        if (TerrablenderCompat.TERRABLENDER_INSTALLED) {
            int uniqueness = ((IExtendedParameterList<?>) parameters).getUniqueness(qx, 0, qz);
            List<Holder<Biome>> biomesList = biomes instanceof List<Holder<Biome>> list ? list : biomes.stream().toList();
            BiomeEnvelope envelope = ((IParameterListExtendedInfo) parameters).biomeSpy$getEnvelopeForBiomes(biomesList, uniqueness);
            if (envelope != null) {
                return envelope;
            }
            return new BiomeEnvelope();
        }

        BiomeEnvelope biomeEnvelope = new BiomeEnvelope();
        for (Pair<Climate.ParameterPoint, Holder<Biome>> p : parameters.values()) {
            Holder<Biome> biome = p.getSecond();
            if (!biomes.contains(biome)) continue;
            biomeEnvelope.add(p.getFirst());
        }
        return biomeEnvelope;
    }

    public void update(Collection<Holder<Biome>> biomes, MultiNoiseBiomeSource biomeSource, int qx, int qz) {
        if (!TerrablenderCompat.TERRABLENDER_INSTALLED) {
            return;
        }

        var parameters = ((MultiNoiseBiomeSourceAccessor) biomeSource).invokeParameters();
        int uniqueness = ((IExtendedParameterList<?>) parameters).getUniqueness(qx, 0, qz);
        List<Holder<Biome>> biomesList = biomes instanceof List<Holder<Biome>> list ? list : biomes.stream().toList();
        BiomeEnvelope other = ((IParameterListExtendedInfo) parameters).biomeSpy$getEnvelopeForBiomes(biomesList, uniqueness);

        this.impossible = other.impossible;
        this.tMin = other.tMin; this.tMax = other.tMax;
        this.hMin = other.hMin; this.hMax = other.hMax;
        this.cMin = other.cMin; this.cMax = other.cMax;
        this.eMin = other.eMin; this.eMax = other.eMax;
        this.wMin = other.wMin; this.wMax = other.wMax;
    }
}
