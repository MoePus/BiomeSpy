package com.moepus.biomespy.mixin.compat.alexscaves;

import com.github.alexmodguy.alexscaves.server.config.BiomeGenerationNoiseCondition;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(
        value = {BiomeGenerationNoiseCondition.class},
        remap = false
)
public interface BiomeGenerationNoiseConditionAccessor {
    @Accessor
    int getDistanceFromSpawn();

    @Accessor
    float[] getContinentalness();

    @Accessor
    float[] getErosion();

    @Accessor
    float[] getHumidity();

    @Accessor
    float[] getTemperature();

    @Accessor
    float[] getWeirdness();

    @Accessor
    float[] getDepth();

    @Accessor
    List<String> getDimensions();
}
