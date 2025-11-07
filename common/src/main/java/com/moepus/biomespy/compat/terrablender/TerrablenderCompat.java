package com.moepus.biomespy.compat.terrablender;

import com.moepus.biomespy.platform.Services;

public class TerrablenderCompat {
    public static boolean TERRABLENDER_INSTALLED = false;

    static {
        TERRABLENDER_INSTALLED = Services.PLATFORM.isModLoaded("terrablender");
    }
}
