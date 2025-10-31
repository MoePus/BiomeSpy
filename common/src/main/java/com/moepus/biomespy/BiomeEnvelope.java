package com.moepus.biomespy;

import net.minecraft.world.level.biome.Climate;

public final class BiomeEnvelope {
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
}
