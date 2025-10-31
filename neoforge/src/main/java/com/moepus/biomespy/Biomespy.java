package com.moepus.biomespy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class Biomespy {
    public Biomespy(IEventBus eventBus) {
        BiomeSpyCommon.init();
    }
}
