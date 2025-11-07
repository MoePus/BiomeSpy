package com.moepus.biomespy.biome;

public class BiomeNoiseCheckState {
    public enum NoiseType {
        TEMPERATURE, HUMIDITY, WEIRDNESS, CONTINENTALNESS, EROSION, NONE
    }

    private NoiseType lastFilteredNoise = NoiseType.NONE;

    public NoiseType getLastFilteredNoise() {
        return lastFilteredNoise;
    }

    public void setLastFilteredNoise(NoiseType type) {
        this.lastFilteredNoise = type;
    }

    public void clear() {
        this.lastFilteredNoise = NoiseType.NONE;
    }
}
