package net.phoenix_tesla_network.tesla;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.MaterialRegistryEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.phoenix_tesla_network.tesla.client.PhoenixTeslaClient;
import net.phoenix_tesla_network.tesla.client.particle.PhoenixParticles;
import net.phoenix_tesla_network.tesla.common.data.PhoenixTeslaRecipeTypes;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.common.data.materials.PhoenixOres;
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
@SuppressWarnings("removal")
public class PhoenixTeslaNetwork {

    public static final String MOD_ID = "phoenix_tesla_network";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate PHOENIX_TESLA_REGISTRATE = GTRegistrate.create(PhoenixTeslaNetwork.MOD_ID);

    public static RegistryEntry<CreativeModeTab> PHOENIX_CREATIVE_TAB = REGISTRATE
            .defaultCreativeTab(PhoenixTeslaNetwork.MOD_ID,
                    builder -> builder
                            .displayItems(
                                    new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(PhoenixTeslaNetwork.MOD_ID,
                                            REGISTRATE))
                            .title(REGISTRATE.addLang("itemGroup", PhoenixTeslaNetwork.id("creative_tab"),
                                    "PhoenixTeslaNetwork (CoreMod)"))
                            .icon(PhoenixTeslaMachines::getCreativeTabIcon)
                            .build())
            .register();

    public PhoenixTeslaNetwork() {
        PhoenixTeslaNetwork.init();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        PhoenixParticles.init(modEventBus);
        if (Platform.isClient()) {
            modEventBus.addListener(this::clientSetup);

            PhoenixTeslaClient.init(modEventBus);
        }

        modEventBus.addListener(this::addMaterialRegistries);
        modEventBus.addListener(this::addMaterials);
        modEventBus.addListener(this::modifyMaterials);

        modEventBus.addGenericListener(GTRecipeType.class, this::registerRecipeTypes);
        modEventBus.addGenericListener(MachineDefinition.class, this::registerMachines);
        modEventBus.addGenericListener(SoundEntry.class, this::registerSounds);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void init() {
        PhoenixTeslaConfigs.init();
        REGISTRATE.registerRegistrate();
        PhoenixTeslaItems.init();
        PhoenixTeslaDatagen.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("Hello from common setup! This is *after* registries are done, so we can do this:");
            LOGGER.info("Look, I found a {}!", Items.DIAMOND);
            PhoenixNetwork.init();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Hey, we're on Minecraft version {}!", Minecraft.getInstance().getLaunchedVersion());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void addMaterialRegistries(MaterialRegistryEvent event) {
        GTCEuAPI.materialManager.createRegistry(PhoenixTeslaNetwork.MOD_ID);
    }

    private void addMaterials(MaterialEvent event) {
        PhoenixOres.register();
        PhoenixProgressionMaterials.register();
    }

    private void modifyMaterials(PostMaterialEvent event) {}

    private void registerRecipeTypes(GTCEuAPI.RegisterEvent<ResourceLocation, GTRecipeType> event) {
        PhoenixTeslaRecipeTypes.init();
    }

    private void registerMachines(GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        PhoenixTeslaMachines.init();
    }

    public void registerSounds(GTCEuAPI.RegisterEvent<ResourceLocation, SoundEntry> event) {}
}
