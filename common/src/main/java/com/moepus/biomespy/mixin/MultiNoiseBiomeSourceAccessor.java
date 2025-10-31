package com.moepus.biomespy.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = MultiNoiseBiomeSource.class, remap = false)
public interface MultiNoiseBiomeSourceAccessor {
    @Invoker
    Climate.ParameterList<Holder<Biome>> invokeParameters();
}
