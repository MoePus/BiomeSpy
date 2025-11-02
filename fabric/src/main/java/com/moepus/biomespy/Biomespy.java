package com.moepus.biomespy;

import net.fabricmc.api.ModInitializer;

public class Biomespy implements ModInitializer {

    @Override
    public void onInitialize() {
        BiomeSpyCommon.init();
    }
}
