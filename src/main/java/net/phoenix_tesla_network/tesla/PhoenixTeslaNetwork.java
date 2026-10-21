package net.phoenix_tesla_network.tesla;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.phoenix_tesla_network.tesla.client.PhoenixTeslaClient;
import net.phoenix_tesla_network.tesla.client.particle.PhoenixParticles;
import net.phoenix_tesla_network.tesla.common.data.PhoenixTeslaRecipeTypes;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.common.data.materials.PhoenixProgressionMaterials;
import net.phoenix_tesla_network.tesla.common.machine.PhoenixTeslaMachines;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.datagen.PhoenixTeslaDatagen;
import net.phoenix_tesla_network.tesla.network.PhoenixNetwork;

import com.tterrag.registrate.util.entry.RegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

@Mod(PhoenixTeslaNetwork.MOD_ID)
public class PhoenixTeslaNetwork {

    public static final String MOD_ID = "phoenix_tesla_network";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final GTRegistrate PHOENIX_TESLA_REGISTRATE = REGISTRATE;

    public static RegistryEntry<CreativeModeTab, CreativeModeTab> PHOENIX_CREATIVE_TAB = REGISTRATE
            .defaultCreativeTab(MOD_ID,
                    builder -> builder
                            .title(REGISTRATE.addLang("itemGroup", PhoenixTeslaNetwork.id("creative_tab"),
                                    "Phoenix Tesla Network"))
                            .icon(PhoenixTeslaMachines::getCreativeTabIcon)
                            .build())
            .register();

    public PhoenixTeslaNetwork(IEventBus modEventBus, ModContainer container) {
        PhoenixTeslaConfigs.init();
        REGISTRATE.registerEventListeners(modEventBus);

        PhoenixParticles.init(modEventBus);
        PhoenixProgressionMaterials.register();
        PhoenixTeslaRecipeTypes.init();
        PhoenixTeslaItems.init();
        PhoenixTeslaMachines.init();
        PhoenixTeslaDatagen.init();

        modEventBus.addListener(this::registerPayloads);
        if (FMLEnvironment.dist.isClient()) {
            PhoenixTeslaClient.init(modEventBus);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PhoenixNetwork.register(event.registrar("1"));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
