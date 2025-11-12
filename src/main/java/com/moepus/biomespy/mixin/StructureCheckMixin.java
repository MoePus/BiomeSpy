package com.moepus.biomespy.mixin;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = StructureCheck.class, remap = false)
public class StructureCheckMixin {
    @Shadow
    @Final
    private Map<Structure, Long2BooleanMap> featureChecks;

    @Shadow
    @Final
    private BiomeSource biomeSource;

    @Shadow
    @Final
    private RandomState randomState;

    @Inject(method = "checkStart", at = @At("HEAD"), cancellable = true)
    private void fastbiomelookup$testInject(ChunkPos chunkPos, Structure structure, boolean skipKnownStructures, CallbackInfoReturnable<StructureCheckResult> cir) {
        this.featureChecks.clear();
    }
}
