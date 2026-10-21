package net.phoenix_tesla_network.tesla.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;

/**
 * Soft link to materials owned by PhoenixCore: resolved by id at recipe time, falling back to a vanilla GT material
 * when PhoenixCore is not installed.
 */
public final class CoreMaterials {

    private CoreMaterials() {}

    public static Material get(String path, Material fallback) {
        Material found = GTRegistries.MATERIALS.get(ResourceLocation.fromNamespaceAndPath("phoenixcore", path));
        return found != null ? found : fallback;
    }

    public static Material aurumSteel() {
        return get("aurum_steel", GTMaterials.Steel);
    }

    public static Material aluminfrost() {
        return get("aluminfrost", GTMaterials.Aluminium);
    }

    public static Material frostReinforcedStainedSteel() {
        return get("frost_reinforced_stained_steel", GTMaterials.StainlessSteel);
    }

    public static Material sourceImbuedTitanium() {
        return get("source_imbued_titanium", GTMaterials.Titanium);
    }

    public static Material voidTouchedTungstenSteel() {
        return get("void_touched_tungsten_steel", GTMaterials.TungstenSteel);
    }
}
