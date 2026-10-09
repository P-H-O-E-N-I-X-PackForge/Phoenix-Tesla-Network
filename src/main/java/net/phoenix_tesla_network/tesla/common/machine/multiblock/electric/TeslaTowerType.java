package net.phoenix_tesla_network.tesla.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum TeslaTowerType {

    BASIC("tesla_tower_basic", "Basic Tesla Tower"),
    ADVANCED("tesla_tower_advanced", "Advanced Tesla Tower"),

    ULTIMATE("tesla_tower", "Tesla Tower");

    private final String registryName;
    private final String displayName;

    TeslaTowerType(String registryName, String displayName) {
        this.registryName = registryName;
        this.displayName = displayName;
    }

    public double spireBackOffset() {
        return this == BASIC ? 3.5 : 5.5;
    }

    public double linkAnchorHeight() {
        return this == BASIC ? 26.5 : 50.5;
    }

    public float ringRadius() {
        return this == BASIC ? 3.8f : 6.5f;
    }

    public float ringDrop() {
        return this == BASIC ? 1.4f : 2.5f;
    }

    public float[] ringHeights() {
        return this == BASIC ? new float[] { 3.0f, 7.5f, 12.0f } : new float[] { 5.5f, 14.5f, 22.5f };
    }

    public String registryName() {
        return registryName;
    }

    public String displayName() {
        return displayName;
    }

    public ResourceLocation id() {
        return PhoenixTeslaNetwork.id(registryName);
    }

    public PhoenixTeslaConfigs.TowerProfile profile() {
        var towers = PhoenixTeslaConfigs.get().towers;
        return switch (this) {
            case BASIC -> towers.tier1Basic;
            case ADVANCED -> towers.tier2Advanced;
            case ULTIMATE -> towers.tier3Ultimate;
        };
    }

    public static @Nullable TeslaTowerType fromId(ResourceLocation id) {
        if (id == null || !PhoenixTeslaNetwork.MOD_ID.equals(id.getNamespace())) return null;
        for (TeslaTowerType type : values()) {
            if (type.registryName.equals(id.getPath())) return type;
        }
        return null;
    }

    public static @Nullable TeslaTowerType fromId(String path) {
        for (TeslaTowerType type : values()) {
            if (type.registryName.equals(path)) return type;
        }
        return null;
    }

    public static String tierName(int tier) {
        int clamped = Math.max(0, Math.min(GTValues.VN.length - 1, tier));
        return GTValues.VN[clamped];
    }

    public List<Component> describeLimits() {
        var p = profile();
        Component range;
        if (p.infiniteRange) {
            range = Component.literal(p.crossDimension ? "Infinite range, all dimensions" :
                    "Infinite range, same dimension only");
        } else {
            range = Component.literal(p.rangeBlocks + " block range" +
                    (p.crossDimension ? ", reaches other dimensions" : ", same dimension only") +
                    " (extend with Tesla Range Extenders)");
        }
        return List.of(
                range,
                Component.literal("Hatches: " + tierName(p.minHatchTier) + " - " + tierName(p.maxHatchTier)),
                Component.literal("Tesla Batteries: " + tierName(p.minBatteryTier) + " - " +
                        tierName(p.maxBatteryTier)));
    }
}
