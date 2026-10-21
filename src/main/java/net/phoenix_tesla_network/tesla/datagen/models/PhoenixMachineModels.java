package net.phoenix_tesla_network.tesla.datagen.models;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;
import net.phoenix_tesla_network.tesla.common.block.TeslaBatteryBlock;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.CHARGER_STATE;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.SIDED_SIDED_OVERLAY_MODEL;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.tieredHullTextures;

public class PhoenixMachineModels {

    public static final ResourceLocation WIRELESS_CHARGER_IDLE = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_front");
    public static final ResourceLocation WIRELESS_CHARGER_RUNNING = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_front_active");
    public static final ResourceLocation WIRELESS_CHARGER_RUNNING_EMISSIVE = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_front_active_emissive");
    public static final ResourceLocation WIRELESS_CHARGER_FINISHED = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_front");
    public static final ResourceLocation WIRELESS_CHARGER_FINISHED_EMISSIVE = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_front_emissive");
    public static final ResourceLocation WIRELESS_CHARGER_TOP = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_top");
    public static final ResourceLocation WIRELESS_CHARGER_TOP_ACTIVE = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_top_active");
    public static final ResourceLocation WIRELESS_CHARGER_TOP_ACTIVE_EMISSIVE = PhoenixTeslaNetwork
            .id("block/machine/part/wireless_charger/overlay_top_active_emissive");

    public static MachineBuilder.ModelInitializer createWirelessChargerModel() {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(renderState -> {
                BatteryBufferMachine.State state = renderState.getValue(CHARGER_STATE);

                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(SIDED_SIDED_OVERLAY_MODEL));
                tieredHullTextures(model, builder.getOwner().getTier());

                switch (state) {
                    case IDLE -> {
                        model.texture("overlay_front", WIRELESS_CHARGER_IDLE);
                        model.texture("overlay_top", WIRELESS_CHARGER_TOP);
                        model.texture("overlay_top", WIRELESS_CHARGER_TOP);
                    }
                    case RUNNING -> {
                        model.texture("overlay_front", WIRELESS_CHARGER_RUNNING);
                        model.texture("overlay_front_emissive", WIRELESS_CHARGER_RUNNING_EMISSIVE);
                        model.texture("overlay_top", WIRELESS_CHARGER_TOP_ACTIVE);
                        model.texture("overlay_top_emissive", WIRELESS_CHARGER_TOP_ACTIVE_EMISSIVE);
                    }
                    case FINISHED -> {
                        model.texture("overlay_front", WIRELESS_CHARGER_FINISHED);
                        model.texture("overlay_front_emissive", WIRELESS_CHARGER_FINISHED_EMISSIVE);
                        model.texture("overlay_top", WIRELESS_CHARGER_TOP);
                    }

                }
                return model;
            });
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, TeslaBatteryBlock>, RegistrateBlockstateProvider> createTeslaBlockModel(ITeslaBattery batteryData) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop(ctx.getName(),
                    PhoenixTeslaNetwork.id("block/casings/batteries/" + batteryData.getBatteryName() + "/side"),
                    PhoenixTeslaNetwork.id("block/casings/batteries/" + batteryData.getBatteryName() + "/top"),
                    PhoenixTeslaNetwork.id("block/casings/batteries/" + batteryData.getBatteryName() + "/top")));
        };
    }

    public static MachineBuilder.ModelInitializer createOverlayCasingMachineModel(
                                                                                  String overlayName,
                                                                                  String casingTexturePath) {
        return (ctx, prov, builder) -> {
            builder.forAllStatesModels(state -> {
                BlockModelBuilder model = prov.models().nested()
                        .parent(prov.models().getExistingFile(
                                GTCEu.id("block/overlay/2_layer/front_emissive")));
                casingTextures(model, casingTexturePath);

                model.texture("overlay",
                        PhoenixTeslaNetwork.id("block/overlay/machine/" + overlayName + "_base"));

                model.texture("overlay_emissive",
                        PhoenixTeslaNetwork.id("block/overlay/machine/" + overlayName + "_emissive"));

                return model;
            });

            builder.addReplaceableTextures("bottom", "top", "side");
        };
    }

    public static void casingTextures(BlockModelBuilder model, String casingTexturePath) {
        ResourceLocation casing = PhoenixTeslaNetwork.id("block/" + casingTexturePath);
        model.texture("bottom", casing);
        model.texture("top", casing);
        model.texture("side", casing);
    }
}
