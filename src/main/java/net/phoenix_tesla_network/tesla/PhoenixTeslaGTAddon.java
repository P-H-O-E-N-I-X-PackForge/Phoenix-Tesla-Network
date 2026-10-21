package net.phoenix_tesla_network.tesla;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;

@GTAddon(PhoenixTeslaNetwork.MOD_ID)
public class PhoenixTeslaGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return PhoenixTeslaNetwork.PHOENIX_TESLA_REGISTRATE;
    }

    @Override
    public void addRecipes(RecipeOutput provider) {
        // TODO: hatch/charger recipes (ported with the rest of the content)
    }
}
