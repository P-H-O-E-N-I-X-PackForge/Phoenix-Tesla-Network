package net.phoenix_tesla_network.tesla.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.phoenix_tesla_network.tesla.PhoenixTeslaAPI;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import org.jetbrains.annotations.NotNull;

import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

@SuppressWarnings("unused")
public class PhoenixTeslaBlocks {

    public static void init() {}

    private static @NotNull BlockEntry<Block> registerSimpleBlock(String name, String id, String texture,
                                                                  NonNullBiFunction<Block, Item.Properties, ? extends BlockItem> func) {
        return REGISTRATE
                .block(id, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH)
                .properties(p -> p.isValidSpawn((state, level, pos, ent) -> false)
                        .strength(5.0f, 6.0f)
                        .requiresCorrectToolForDrops())
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                        prov.models().cubeAll(ctx.getName(), PhoenixTeslaNetwork.id("block/" + texture))))
                .lang(name)
                .item(func)
                .build()
                .register();
    }

    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_LV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.LV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_MV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.MV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_HV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.HV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_EV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.EV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_IV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.IV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_LUV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.LuV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_ZPM = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.ZPM);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_UV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.UV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_UHV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.UHV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_UEV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.UEV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_UIV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.UIV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_UXV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.UXV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_OPV = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.OPV);
    public static final BlockEntry<TeslaBatteryBlock> TESLA_BATTERY_MAX = createTeslaBattery(
            TeslaBatteryBlock.TeslaBatteryType.MAX);

    private static final String PLACEHOLDER_TEXTURE_DIR = "block/casings/battery/zpm_lapotronic/";

    private static BlockEntry<TeslaBatteryBlock> createTeslaBattery(ITeslaBattery batteryData) {
        String tierName = batteryData.getBatteryName();

        var battery = REGISTRATE
                .block("tesla_battery_%s".formatted(tierName), p -> new TeslaBatteryBlock(p, batteryData))
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .lang("Tesla Battery " + GTValues.VN[batteryData.getTier()])
                .blockstate((ctx, prov) -> {
                    boolean placeholder = batteryData instanceof TeslaBatteryBlock.TeslaBatteryType type &&
                            type.usesPlaceholderTexture();

                    ResourceLocation side, top, bottom;
                    if (placeholder) {
                        side = new ResourceLocation("gtceu", PLACEHOLDER_TEXTURE_DIR + "side");
                        top = new ResourceLocation("gtceu", PLACEHOLDER_TEXTURE_DIR + "top");
                        bottom = top;
                    } else {
                        String folderPath = "block/casings/batteries/tesla_" + tierName + "/";
                        side = PhoenixTeslaNetwork.id(folderPath + "side");
                        top = PhoenixTeslaNetwork.id(folderPath + "top");
                        bottom = PhoenixTeslaNetwork.id(folderPath + "bottom");
                    }

                    prov.simpleBlock(ctx.getEntry(),
                            prov.models().cubeBottomTop(ctx.getName(), side, bottom, top));
                })
                .item(BlockItem::new)
                .build()
                .register();

        PhoenixTeslaAPI.TESLA_BATTERIES.put(batteryData, battery);
        return battery;
    }

    public static BlockEntry<Block> RELIABLE_NAQUADAH_ALLOY_MACHINE_CASING = registerSimpleBlock(
            "§cReliable Naquadah Alloy Machine Casing", "reliable_naquadah_alloy_machine_casing",
            "reliable_naquadah_alloy_machine_casing", BlockItem::new);
    public static BlockEntry<Block> INSANELY_SUPERCHARGED_TESLA_CASING = registerSimpleBlock(
            "§4Insanely Supercharged Tesla Tower Casing", "insanely_supercharged_tesla_casing",
            "casings/multiblock/tesla_casing", BlockItem::new);
    public static BlockEntry<Block> SOURCE_FIBER_MACHINE_CASING = registerSimpleBlock(
            "§dSource Fiber Machine Casing", "source_fiber_machine_casing",
            "casings/multiblock/machine_casing_source_fiber_mesh", BlockItem::new);
    public static BlockEntry<Block> MACHINE_CASING_RHODIUM_PLATED_PALLADIUM = registerSimpleBlock(
            "Pristine Rhodium Plated Palladium Machine Casing", "machine_casing_pristine_rhodium_plated_palladium",
            "casings/multiblock/machine_casing_pristine_rhodium_plated_palladium", BlockItem::new);
    public static BlockEntry<Block> MACHINE_CASING_NAQUADAH_ALLOY = registerSimpleBlock(
            "Invariant Naquadah Alloy Machine Casing", "machine_casing_invariant_naquadah_alloy",
            "casings/multiblock/machine_casing_invariant_naquadah_alloy", BlockItem::new);

    public static BlockEntry<Block> ADVANCED_INVARIANT_NAQ_ALLOY_CASING = registerSimpleBlock(
            "§bAdvanced Invariant Naquadah Alloy Machine Casing", "advanced_invariant_naquadah_alloy_machine_casing",
            "casings/multiblock/advanced_machine_casing_invariant_naquadah_alloy", BlockItem::new);
    public static BlockEntry<Block> ADVANCED_SOURCE_FIBER_CASING = registerSimpleBlock(
            "§dAdvanced Source Fiber Machine Casing", "advanced_source_fiber_machine_casing",
            "casings/multiblock/advanced_machine_casing_source_fiber_mesh", BlockItem::new);
    public static BlockEntry<Block> ADVANCED_TESLA_CASING = registerSimpleBlock(
            "§6Advanced Tesla Tower Casing", "advanced_tesla_casing",
            "casings/multiblock/advanced_tesla_casing", BlockItem::new);
}
