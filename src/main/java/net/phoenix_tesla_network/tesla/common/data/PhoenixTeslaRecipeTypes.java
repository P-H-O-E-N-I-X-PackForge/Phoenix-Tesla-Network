package net.phoenix_tesla_network.tesla.common.data;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;

import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MULTIBLOCK;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.register;

public class PhoenixTeslaRecipeTypes {

    public static GTRecipeType TESLA_TOWER;

    public static GTRecipeType TESLA_RELAY;

    public static void init() {
        TESLA_TOWER = register("phoenix_tesla_tower", MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setEUIO(IO.IN);

        TESLA_RELAY = register("phoenix_tesla_relay", MULTIBLOCK)
                .setMaxIOSize(1, 1, 1, 1)
                .setSlotOverlay(false, false, GuiTextures.BOX_OVERLAY)
                .setProgressBar(GuiTextures.PROGRESS_BAR_EXTRACT, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
                .setEUIO(IO.IN);
    }
}
