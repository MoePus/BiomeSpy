package com.moepus.biomespy.compat.alexscaves;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record AlexCaveBiomeSourceExtendedInfo(long lastSampledWorldSeed, ResourceKey<Level> lastSampledDimension) {
    public AlexCaveBiomeSourceExtendedInfo(long lastSampledWorldSeed, ResourceKey<Level> lastSampledDimension) {
        this.lastSampledWorldSeed = lastSampledWorldSeed;
        this.lastSampledDimension = lastSampledDimension;
    }

    public long lastSampledWorldSeed() {
        return this.lastSampledWorldSeed;
    }

    public ResourceKey<Level> lastSampledDimension() {
        return this.lastSampledDimension;
    }
}
