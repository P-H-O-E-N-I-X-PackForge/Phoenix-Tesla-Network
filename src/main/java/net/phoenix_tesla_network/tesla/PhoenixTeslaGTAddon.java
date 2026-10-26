package net.phoenix_tesla_network.tesla;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.data.recipe.EquipmentFoundryRecipeHelper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Ingredient;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixModules;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;

@GTAddon(PhoenixTeslaNetwork.MOD_ID)
public class PhoenixTeslaGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return PhoenixTeslaNetwork.PHOENIX_TESLA_REGISTRATE;
    }

    /**
     * Equipment Foundry recipes: GTM decides what can be installed from these (equipment ingredient + module item
     * ingredient + module), not from item names. Each module installs only on its own Phoenix suite piece.
     */
    @Override
    public void addRecipes(RecipeOutput provider) {
        Ingredient helmet = Ingredient.of(PhoenixTeslaItems.PHOENIX_HELMET.get());
        Ingredient chest = Ingredient.of(PhoenixTeslaItems.PHOENIX_CHESTPLATE.get());
        Ingredient legs = Ingredient.of(PhoenixTeslaItems.PHOENIX_LEGGINGS.get());
        Ingredient boots = Ingredient.of(PhoenixTeslaItems.PHOENIX_BOOTS.get());

        // Chestplate
        foundry(provider, "tesla_link", chest, PhoenixTeslaItems.TESLA_LINK_MODULE.get(), PhoenixModules.TESLA_LINK);
        foundry(provider, "phoenix_rebirth", chest, PhoenixTeslaItems.PHOENIX_REBIRTH_MODULE.get(),
                PhoenixModules.REBIRTH);
        foundry(provider, "phoenix_wing", chest, PhoenixTeslaItems.PHOENIX_WING_MODULE.get(), PhoenixModules.WING);
        foundry(provider, "phoenix_naphtha", chest, PhoenixTeslaItems.PHOENIX_NAPHTHA_MODULE.get(),
                PhoenixModules.NAPHTHA);
        foundry(provider, "phoenix_mender", chest, PhoenixTeslaItems.PHOENIX_MENDER_MODULE.get(),
                PhoenixModules.MENDER);
        // Helmet
        foundry(provider, "phoenix_purifier", helmet, PhoenixTeslaItems.PHOENIX_PURIFIER_MODULE.get(),
                PhoenixModules.PURIFIER);
        foundry(provider, "phoenix_lore_scanner", helmet, PhoenixTeslaItems.PHOENIX_LORE_SCANNER_MODULE.get(),
                PhoenixModules.LORE_SCANNER);
        // Leggings
        foundry(provider, "phoenix_sprint", legs, PhoenixTeslaItems.PHOENIX_SPRINT_MODULE.get(),
                PhoenixModules.SPRINT);
        foundry(provider, "phoenix_magnet", legs, PhoenixTeslaItems.PHOENIX_MAGNET_MODULE.get(),
                PhoenixModules.MAGNET);
        // Boots
        foundry(provider, "phoenix_jump", boots, PhoenixTeslaItems.PHOENIX_JUMP_MODULE.get(), PhoenixModules.JUMP);
        foundry(provider, "phoenix_step", boots, PhoenixTeslaItems.PHOENIX_STEP_MODULE.get(), PhoenixModules.STEP);
        foundry(provider, "phoenix_tesla_discharge", boots,
                PhoenixTeslaItems.PHOENIX_TESLA_DISCHARGE_MODULE.get(), PhoenixModules.DISCHARGE);
    }

    private static void foundry(RecipeOutput provider, String name, Ingredient equipment,
                                net.minecraft.world.level.ItemLike moduleItem,
                                com.tterrag.registrate.util.entry.RegistryEntry<com.gregtechceu.gtceu.api.item.module.ItemModule, ? extends com.gregtechceu.gtceu.api.item.module.ItemModule> module) {
        EquipmentFoundryRecipeHelper.addEquipmentFoundryRecipe(provider, PhoenixTeslaNetwork.id(name), equipment,
                Ingredient.of(moduleItem), module);
    }
}
