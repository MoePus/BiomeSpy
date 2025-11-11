package com.moepus.biomespy.compat.alexscaves;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.lang.reflect.Field;

public class BiomeUtil {
    public static AlexCaveBiomeSourceExtendedInfo getExtendedInfo(MultiNoiseBiomeSource biomeSource) {
        try {
            Field f = biomeSource.getClass().getDeclaredField("lastSampledWorldSeed");
            f.setAccessible(true);
            long seed = (Long)f.get(biomeSource);
            Field f2 = biomeSource.getClass().getDeclaredField("lastSampledDimension");
            f2.setAccessible(true);
            ResourceKey<Level> dimension = (ResourceKey)f2.get(biomeSource);
            return new AlexCaveBiomeSourceExtendedInfo(seed, dimension);
        } catch (Exception var6) {
            return null;
        }
    }
}
