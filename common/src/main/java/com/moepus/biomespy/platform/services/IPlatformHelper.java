package com.moepus.biomespy.platform.services;

import com.moepus.biomespy.biome.BiomeEnvelope;
import com.moepus.biomespy.biome.BiomeEnvelopeSelector;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.Collection;
import java.util.Map;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Checks if a mod with the given id is currently loading.
     *
     * @param modId The mod to check if it is loading.
     * @return True if the mod is loading, false otherwise.
     */
    boolean hasLoadingMod(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    default void initPlatformSpecificBiomeEnvelope(BiomeEnvelopeSelector envelopeMap, Collection<Holder<Biome>> biomes,
                                                   Climate.ParameterList<Holder<Biome>> parameters, MultiNoiseBiomeSource biomeSource) {

    }

    default BiomeEnvelope getPlatformSpecificBiomeEnvelope(BiomeEnvelopeSelector envelopeMap, Climate.ParameterList<Holder<Biome>> parameters, int qx, int qy, int qz) {
        return null;
    }
}
