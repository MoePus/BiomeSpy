package com.moepus.biomespy.compat.terrablender;

import net.minecraftforge.fml.ModList;

public class TerrablenderCompat {
    public static boolean TERRABLENDER_INSTALLED = false;

    static {
        TERRABLENDER_INSTALLED = ModList.get().isLoaded("terrablender");
    }
}
