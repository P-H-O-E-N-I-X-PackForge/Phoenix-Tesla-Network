package net.phoenix_tesla_network.tesla.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.registry.registrate.entry.GTRecipeTypeEntry;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;
import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixTeslaRecipeTypes {

    public static GTRecipeTypeEntry TESLA_TOWER;

    public static GTRecipeTypeEntry TESLA_RELAY;

    public static void init() {
        TESLA_TOWER = REGISTRATE.recipeType("phoenix_tesla_tower", MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT))
                .register();

        TESLA_RELAY = REGISTRATE.recipeType("phoenix_tesla_relay", MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .setEUIO(IO.IN)
                .UI(builder -> builder.setProgressBar(GTGuiTextures.PROGRESS_EXTRACT))
                .register();
    }
}
