package net.phoenix_tesla_network.tesla;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;
import net.phoenix_tesla_network.tesla.common.data.recipe.*;

@GTAddon(PhoenixTeslaNetwork.MOD_ID)
public class PhoenixTeslaGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return PhoenixTeslaNetwork.PHOENIX_TESLA_REGISTRATE;
    }

    @Override
    public void addRecipes(RecipeOutput provider) {
        TeslaHatchRecipes.init(provider);
        TeslaMultiAmpHatchRecipes.init(provider);
        CustomComponetRecipes.init(provider);
        WirelessChargerRecipes.init(provider);
    }
}
