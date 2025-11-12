package com.moepus.biomespy.biome;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class BiomeEnvelope {
    public boolean impossible = false;
    public long tMin = Long.MAX_VALUE, tMax = Long.MIN_VALUE;
    public long hMin = Long.MAX_VALUE, hMax = Long.MIN_VALUE;
    public long cMin = Long.MAX_VALUE, cMax = Long.MIN_VALUE;
    public long eMin = Long.MAX_VALUE, eMax = Long.MIN_VALUE;
    public long wMin = Long.MAX_VALUE, wMax = Long.MIN_VALUE;

    public void add(Climate.ParameterPoint pp) {
        var T = pp.temperature();
        tMin = Math.min(tMin, T.min());
        tMax = Math.max(tMax, T.max());
        var H = pp.humidity();
        hMin = Math.min(hMin, H.min());
        hMax = Math.max(hMax, H.max());
        var C = pp.continentalness();
        cMin = Math.min(cMin, C.min());
        cMax = Math.max(cMax, C.max());
        var E = pp.erosion();
        eMin = Math.min(eMin, E.min());
        eMax = Math.max(eMax, E.max());
        var W = pp.weirdness();
        wMin = Math.min(wMin, W.min());
        wMax = Math.max(wMax, W.max());
    }

    public void add(BiomeEnvelope other) {
        tMin = Math.min(tMin, other.tMin);
        tMax = Math.max(tMax, other.tMax);
        hMin = Math.min(hMin, other.hMin);
        hMax = Math.max(hMax, other.hMax);
        cMin = Math.min(cMin, other.cMin);
        cMax = Math.max(cMax, other.cMax);
        eMin = Math.min(eMin, other.eMin);
        eMax = Math.max(eMax, other.eMax);
        wMin = Math.min(wMin, other.wMin);
        wMax = Math.max(wMax, other.wMax);
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

    public boolean matches(Climate.Sampler sampler, DensityFunction.SinglePointContext ctx) {
        if (tMin <= tMax && tMin + 20000 > tMax) {
            long t = Climate.quantizeCoord((float) sampler.temperature().compute(ctx));
            if (t < tMin || t > tMax) return false;
        }
        if (hMin <= hMax && hMin + 20000 > hMax) {
            long h = Climate.quantizeCoord((float) sampler.humidity().compute(ctx));
            if (h < hMin || h > hMax) return false;
        }
        if (wMin <= wMax && wMin + 20000 > wMax) {
            long w = Climate.quantizeCoord((float) sampler.weirdness().compute(ctx));
            if (w < wMin || w > wMax) return false;
        }
        if (cMin <= cMax && cMin + 20000 > cMax) {
            long c = Climate.quantizeCoord((float) sampler.continentalness().compute(ctx));
            if (c < cMin || c > cMax) return false;
        }
        if (eMin <= eMax && eMin + 20000 > eMax) {
            long e = Climate.quantizeCoord((float) sampler.erosion().compute(ctx));
            if (e < eMin || e > eMax) return false;
        }
        return true;
    }
}
