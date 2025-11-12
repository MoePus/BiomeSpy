package com.moepus.biomespy.compat.alexscaves;

import net.minecraftforge.fml.ModList;

public class AlexsCavesCompat {
    public static boolean ALEXS_CAVES_INSTALLED = false;

    static {
        ALEXS_CAVES_INSTALLED = ModList.get().isLoaded("alexscaves");
    }
}
