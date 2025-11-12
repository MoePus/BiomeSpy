package com.moepus.biomespy.compat.terrablender;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import terrablender.worldgen.IExtendedParameterList;

public class TerraBiome {
    public static Holder<Biome> getNoiseBiome(Climate.ParameterList<Holder<Biome>> parameters, Climate.TargetPoint climate, int x, int y, int z) {
        return (Holder<Biome>)(((IExtendedParameterList) parameters).
                findValuePositional(climate, QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)));
    }

    public static int getUniqueness(Climate.ParameterList<Holder<Biome>> parameters, int qx, int qy, int qz) {
        return ((IExtendedParameterList<?>) parameters).getUniqueness(qx, qy, qz);
    }
}