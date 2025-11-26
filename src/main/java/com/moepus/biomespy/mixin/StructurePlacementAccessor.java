package com.moepus.biomespy.mixin;

import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(StructurePlacement.class)
public interface StructurePlacementAccessor {
    @Accessor
    StructurePlacement.FrequencyReductionMethod getFrequencyReductionMethod();
    @Accessor
    float getFrequency();
    @Accessor
    int getSalt();
    @Accessor
    Optional<StructurePlacement.ExclusionZone> getExclusionZone();
}
