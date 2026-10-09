package net.phoenix_tesla_network.tesla.common.machine;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.pattern.PhoenixPredicates;
import net.phoenix_tesla_network.tesla.client.renderer.machine.multiblock.PhoenixDynamicRenderHelpers;
import net.phoenix_tesla_network.tesla.common.block.PhoenixTeslaBlocks;
import net.phoenix_tesla_network.tesla.common.data.PhoenixTeslaRecipeTypes;
import net.phoenix_tesla_network.tesla.common.data.materials.PhoenixProgressionMaterials;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaRelayMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerType;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.part.TeslaEnergyHatchPartMachine;
import net.phoenix_tesla_network.tesla.common.machine.singleblock.electric.TeslaWirelessChargerMachine;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.datagen.models.PhoenixMachineModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.GTValues.VCF;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixTeslaMachines {

    public static final int[] MULTI_AMP_TESLA_HATCH = GTValues.tiersBetween(EV, GTCEuAPI.isHighTier() ? MAX : UHV);

    public static final MultiblockMachineDefinition TESLA_TOWER_BASIC = registerTower(TeslaTowerType.BASIC);
    public static final MultiblockMachineDefinition TESLA_TOWER_ADVANCED = registerTower(TeslaTowerType.ADVANCED);
    public static final MultiblockMachineDefinition TESLA_TOWER = registerTower(TeslaTowerType.ULTIMATE);

    public static final MultiblockMachineDefinition TESLA_RELAY = registerRelay();

    public static ItemStack getCreativeTabIcon() {
        for (MultiblockMachineDefinition definition : new MultiblockMachineDefinition[] { TESLA_TOWER,
                TESLA_TOWER_ADVANCED, TESLA_TOWER_BASIC, TESLA_RELAY }) {
            if (definition != null) return definition.asStack();
        }
        return ItemStack.EMPTY;
    }

    private static MultiblockMachineDefinition registerTower(TeslaTowerType type) {
        if (!type.profile().enabled) return null;

        return REGISTRATE
                .multiblock(type.registryName(), TeslaTowerMachine::new)
                .langValue(type.displayName())
                .rotationState(RotationState.ALL)
                .recipeType(PhoenixTeslaRecipeTypes.TESLA_TOWER)
                .appearanceBlock(type == TeslaTowerType.ADVANCED ? PhoenixTeslaBlocks.ADVANCED_TESLA_CASING :
                        PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING)
                .pattern(definition -> buildTowerPattern(definition, type))
                .model(
                        createWorkableCasingMachineModel(
                                PhoenixTeslaNetwork.id(type == TeslaTowerType.ADVANCED || type == TeslaTowerType.ULTIMATE ?
                                        "block/casings/multiblock/advanced_tesla_casing" :
                                        "block/casings/multiblock/tesla_casing"),
                                PhoenixTeslaNetwork.id("block/multiblock/tesla_tower"))
                                .andThen(d -> d
                                        .addDynamicRenderer(
                                                PhoenixDynamicRenderHelpers::getTeslaTowerRenderer)))
                .hasBER(true)
                .tooltipBuilder((stack, list) -> {
                    list.add(Component.literal("The pulsating heart of your Tesla Network.")
                            .withStyle(TeslaTowerMachine.NEBULA_HSL));
                    addTowerRangeLines(list, type.profile());
                    list.add(Component.literal("Tesla Network is shared with your Team's other Tesla Towers"));
                    list.add(Component.literal(
                            "Tesla Network size is §7determined§f by the §7Tesla Battery§f Tier"));
                })
                .register();
    }

    private static void addTowerRangeLines(List<Component> list, PhoenixTeslaConfigs.TowerProfile profile) {
        MutableComponent range = Component.literal("Range: ").withStyle(ChatFormatting.GRAY);
        if (profile.infiniteRange) {
            range.append(Component.literal("Infinite").withStyle(ChatFormatting.AQUA));
        } else {
            range.append(Component.literal(profile.rangeBlocks + " Blocks").withStyle(ChatFormatting.AQUA));
            range.append(Component.literal(" (extendable with Tesla Range Extenders)")
                    .withStyle(ChatFormatting.GRAY));
        }
        list.add(range);

        list.add(Component.literal("Interdimensional Transfer: ").withStyle(ChatFormatting.GRAY)
                .append(profile.crossDimension ?
                        Component.literal("Allowed").withStyle(ChatFormatting.GREEN) :
                        Component.literal("Not allowed").withStyle(ChatFormatting.RED)));
    }

    private static TraceabilityPredicate hatchPredicate(MultiblockMachineDefinition definition,
                                                        PhoenixTeslaConfigs.TowerProfile profile) {
        return hatchPredicate(definition, profile, PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING.get());
    }

    static TraceabilityPredicate hatchPredicate(MultiblockMachineDefinition definition,
                                                PhoenixTeslaConfigs.TowerProfile profile,
                                                Block casing) {
        int min = profile.minHatchTier;
        int max = profile.maxHatchTier;
        TraceabilityPredicate predicate = blocks(casing);

        if (profile.allowItemFluidHatches) {
            predicate = predicate.or(Predicates.autoAbilities(definition.getRecipeTypes(), false, false, true, true,
                    true, true));
        }
        if (profile.allowMaintenanceHatch) {
            predicate = predicate.or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1));
        }
        if (profile.allowEnergyInput) {
            predicate = predicate
                    .or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.INPUT_ENERGY)
                            .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.INPUT_ENERGY)
                            .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        if (profile.allowEnergyOutput) {
            predicate = predicate.or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.OUTPUT_ENERGY)
                    .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        if (profile.allowSubstationInput) {
            predicate = predicate.or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.SUBSTATION_INPUT_ENERGY)
                    .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        if (profile.allowSubstationOutput) {
            predicate = predicate.or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.SUBSTATION_OUTPUT_ENERGY)
                    .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        if (profile.allowLaserInput) {
            predicate = predicate.or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.INPUT_LASER)
                    .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        if (profile.allowLaserOutput) {
            predicate = predicate.or(PhoenixPredicates.tieredAbilities(min, max, PartAbility.OUTPUT_LASER)
                    .setMaxGlobalLimited(10).setPreviewCount(1));
        }
        return predicate;
    }

    private static BlockPattern buildTowerPattern(MultiblockMachineDefinition definition, TeslaTowerType type) {
        var profile = type.profile();

        if (type == TeslaTowerType.BASIC) return basicTowerPattern(definition, profile);
        if (type == TeslaTowerType.ADVANCED) return advancedTowerPattern(definition, profile);
        return ultimateTowerPattern(definition, profile);
    }

    private static final String MOD = "phoenix_tesla_network:";

    private static Block byId(String id) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(id));
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing block '" + id + "' while building a Tesla Tower pattern");
        }
        return block;
    }

    private static TraceabilityPredicate lamp(DyeColor color) {
        return Predicates.lampsByColor(color);
    }

    private static TraceabilityPredicate glass(PhoenixTeslaConfigs.TowerProfile profile) {
        List<Block> allowed = new ArrayList<>(3);
        if (profile.allowTemperedGlass) allowed.add(GTBlocks.CASING_TEMPERED_GLASS.get());
        if (profile.allowLaminatedGlass) allowed.add(GTBlocks.CASING_LAMINATED_GLASS.get());
        if (profile.allowFusionGlass) allowed.add(GTBlocks.FUSION_GLASS.get());
        if (allowed.isEmpty()) {
            allowed.add(GTBlocks.CASING_TEMPERED_GLASS.get());
            allowed.add(GTBlocks.CASING_LAMINATED_GLASS.get());
            allowed.add(GTBlocks.FUSION_GLASS.get());
        }
        return blocks(allowed.toArray(new Block[0]));
    }

    private static BlockPattern basicTowerPattern(MultiblockMachineDefinition definition,
                                                  PhoenixTeslaConfigs.TowerProfile profile) {
        return FactoryBlockPattern.start()
                .aisle("AABBBBBAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("ABDDDDDBA", "AAAEDEAAA", "AAAEDEAAA", "AADDDDDAA",
                        "AAFFFFFAA", "AAFFFFFAA", "AAFFFFFAA", "AADDDDDAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("BDDGGGDDB", "AAAAAAAAA", "AAAAAAAAA", "ADDDDDDDA",
                        "AFAAAAAFA", "AFAAAAAFA", "AFAAAAAFA", "ADDDDDDDA",
                        "AAEAAAEAA", "AAEAAAEAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("BDGGGGGDB", "AAAAEAAEA", "AAAAAAAEA", "ADDGGGDDA",
                        "AFAGEGAFA", "AFAGEGAFA", "AFAGEGAFA", "ADDDDDDDA",
                        "AAADEDAAA", "AAADEDAAA", "AAADDDAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("BDGGGGGDB", "ADAEHEADA", "ADAAEAADA", "ADDGEGDDA",
                        "AFAEIEAFA", "AFAEIEAFA", "AFAEIEAFA", "ADDDIDDDA",
                        "AAAEIEAAA", "AAAEIEAAA", "AAADIDAAA", "AAAAIAAAA",
                        "AAAAIAAAA")
                .aisle("BDGGGGGDB", "AEAAEAAEA", "AEAAAAAEA", "ADDGGGDDA",
                        "AFAGEGAFA", "AFAGEGAFA", "AFAGEGAFA", "ADDDDDDDA",
                        "AAADEDAAA", "AAADEDAAA", "AAADDDAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("BDDGGGDDB", "AAAAAAAAA", "AAAAAAAAA", "ADDDDDDDA",
                        "AFAAAAAFA", "AFAAAAAFA", "AFAAAAAFA", "ADDDDDDDA",
                        "AAEAAAEAA", "AAEAAAEAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("ABDDDDDBA", "AAAEJEAAA", "AAAEDEAAA", "AADDDDDAA",
                        "AAFFFFFAA", "AAFFFFFAA", "AAFFFFFAA", "AADDDDDAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .aisle("AABBBBBAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA", "AAAAAAAAA",
                        "AAAAAAAAA")
                .where('A', Predicates.any())
                .where('B', blocks(PhoenixTeslaBlocks.SOURCE_FIBER_MACHINE_CASING.get()))
                .where('D', hatchPredicate(definition, profile,
                        PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING.get()))
                .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.Steel)))
                .where('F', glass(profile))
                .where('G', lamp(DyeColor.PURPLE))
                .where('H', PhoenixPredicates.teslaBatteries(profile.minBatteryTier, profile.maxBatteryTier))
                .where('I', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                .where('J', controller(blocks(definition.get())))
                .build();
    }

    private static BlockPattern advancedTowerPattern(MultiblockMachineDefinition definition,
                                                     PhoenixTeslaConfigs.TowerProfile profile) {
        return FactoryBlockPattern.start()
                .aisle("AABBBBBBBAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AACCCCCCCAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("ABBCCCCCBBA", "AAAAECEAAAA", "AAAAECEAAAA", "AAACCCCCAAA",
                        "AAAFFCFFAAA", "AAAFFCFFAAA", "AAAFFCFFAAA", "ACCGGCGGCCA",
                        "AAAEAAAEAAA", "AAAEAAAEAAA", "AAAGGGGGAAA", "AAAFFFFFAAA",
                        "AAAFFFFFAAA", "AAAGGGGGAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BBCCGGGCCBB", "AAAAAAAAAAA", "AAAAAAAAAAA", "AACCGGGCCAA",
                        "AAGFAAAFGAA", "AAGFAAAFGAA", "AAGFAAAFGAA", "CCGGHCHGGCC",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAGGBBBGGAA", "AAFFAAAFFAA",
                        "AAFFAAAFFAA", "AAGGBBBGGAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAABBBBBAAA", "AAACFFFCAAA", "AAACFFFCAAA", "AAACFFFCAAA",
                        "AAACCCCCAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BCCGGHGGCCB", "AAAAAAAAAAA", "AAAAAAAAAAA", "ACCGGHGGCCA",
                        "AFFAAAAAFFA", "AFFAAAAAFFA", "AFFAAAAAFFA", "CGGHHCHHGGC",
                        "AEAAAAAAAEA", "AEAAAAAAAEA", "AGGBBHBBGGA", "AFFAAAAAFFA",
                        "AFFAAAAAFFA", "AGGBBBBBGGA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AABBHHHBBAA", "AACHAAAHCAA", "AACHAAAHCAA", "AACHAAAHCAA",
                        "AACCBBBCCAA", "AAAACCCAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BCGGHHHGGCB", "AEAAAEAAAEA", "AEAAAAAAAEA", "ACGGHHHGGCA",
                        "AFAAAAAAAFA", "AFAAAAAAAFA", "AFAAAAAAAFA", "CGHHHCHHHGC",
                        "AAAAIEIAAAA", "AAAAIEIAAAA", "AGBBHEHBBGA", "AFAAAEAAAFA",
                        "AFAAAEAAAFA", "AGBBBEBBBGA", "AAAAHEHAAAA", "AAAAHEHAAAA",
                        "AABHHEHHBAA", "AAFAAEAAFAA", "AAFAAEAAFAA", "AAFAAEAAFAA",
                        "AACBBEBBCAA", "AAACCECCAAA", "AAAACECAAAA", "AAAACECAAAA",
                        "AAAACECAAAA", "AAAACCCAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BCGHHHHHGCB", "ACAAEEEAACA", "ACAAAJAAACA", "ACGHHEHHGCA",
                        "ACAAAEAAACA", "ACAAAEAAACA", "ACAAAEAAACA", "CCCCCCCCCCC",
                        "AAAAEKEAAAA", "AAAAEKEAAAA", "AGBHEKEHBGA", "AFAAEKEAAFA",
                        "AFAAEKEAAFA", "AGBBEKEBBGA", "AAAAEKEAAAA", "AAAAEKEAAAA",
                        "AABHEKEHBAA", "AAFAEKEAFAA", "AAFAEKEAFAA", "AAFAEKEAFAA",
                        "AACBEKEBCAA", "AAACEKECAAA", "AAAAEKEAAAA", "AAAAEKEAAAA",
                        "AAAAEKEAAAA", "AAAACKCAAAA", "AAAAAKAAAAA", "AAAAAKAAAAA")
                .aisle("BCGGHHHGGCB", "AEAAAEAAAEA", "AEAAAAAAAEA", "ACGGHHHGGCA",
                        "AFAAAAAAAFA", "AFAAAAAAAFA", "AFAAAAAAAFA", "CGHHHCHHHGC",
                        "AAAAIEIAAAA", "AAAAIEIAAAA", "AGBBAEHBBGA", "AFAAAEAAAFA",
                        "AFAAAEAAAFA", "AGBBBEBBBGA", "AAAAHEHAAAA", "AAAAHEHAAAA",
                        "AABHHEHHBAA", "AAFAAEAAFAA", "AAFAAEAAFAA", "AAFAAEAAFAA",
                        "AACBBEBBCAA", "AAACCECCAAA", "AAAACECAAAA", "AAAACECAAAA",
                        "AAAACECAAAA", "AAAACCCAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BCCGGHGGCCB", "AAAAAAAAAAA", "AAAAAAAAAAA", "ACCGGHGGCCA",
                        "AFFAAAAAFFA", "AFFAAAAAFFA", "AFFAAAAAFFA", "CGGHHCHHGGC",
                        "AEAAAAAAAEA", "AEAAAAAAAEA", "AGGBBHBBGGA", "AFFAAAAAFFA",
                        "AFFAAAAAFFA", "AGGBBBBBGGA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AABBHHHBBAA", "AACHAAAHCAA", "AACHAAAHCAA", "AACHAAAHCAA",
                        "AACCBBBCCAA", "AAAACCCAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("BBCCGGGCCBB", "AAAAAAAAAAA", "AAAAAAAAAAA", "AACCGGGCCAA",
                        "AAGFAAAFGAA", "AAGFAAAFGAA", "AAGFAAAFGAA", "CCGGHCHGGCC",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAGGBBBGGAA", "AAFFAAAFFAA",
                        "AAFFAAAFFAA", "AAGGBBBGGAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAABBBBBAAA", "AAACFFFCAAA", "AAACFFFCAAA", "AAACFFFCAAA",
                        "AAACCCCCAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("ABBCCCCCBBA", "AAAAELEAAAA", "AAAAECEAAAA", "AAACCCCCAAA",
                        "AAAFFCFFAAA", "AAAFFCFFAAA", "AAAFFCFFAAA", "ACCGGCGGCCA",
                        "AAAEAAAEAAA", "AAAEAAAEAAA", "AAAGGGGGAAA", "AAAFFFFFAAA",
                        "AAAFFFFFAAA", "AAAGGGGGAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .aisle("AABBBBBBBAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AACCCCCCCAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA",
                        "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA", "AAAAAAAAAAA")
                .where('A', Predicates.any())
                .where('B', blocks(PhoenixTeslaBlocks.SOURCE_FIBER_MACHINE_CASING.get()))
                .where('C', hatchPredicate(definition, profile,
                        PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING.get()))
                .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, PhoenixProgressionMaterials.RESONANT_RHODIUM_ALLOY)))
                .where('F', glass(profile))
                .where('G', blocks(PhoenixTeslaBlocks.MACHINE_CASING_RHODIUM_PLATED_PALLADIUM.get()))
                .where('H', lamp(DyeColor.PURPLE))
                .where('I', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                .where('J', PhoenixPredicates.teslaBatteries(profile.minBatteryTier, profile.maxBatteryTier))
                .where('K', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                .where('L', controller(blocks(definition.get())))
                .build();
    }

    private static BlockPattern ultimateTowerPattern(MultiblockMachineDefinition definition,
                                                     PhoenixTeslaConfigs.TowerProfile profile) {
        return FactoryBlockPattern.start()
                .aisle("AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAABBBHFHBBBAAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA",
                        "AAAAAAAFAAAFAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAACCBCCAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAAIIIIIIIIIAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAABBEEEFEEEBBAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA",
                        "AAAABBHHJFJHHBBAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAAGCCAAACCGAAAAA", "AAAAABBBHFHBBBAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAIIJJJJJJJIIAAAA", "AAAAAAAAFJFAAAAAAAA", "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAFJFAAAAAAAA",
                        "AAABBEEGJJJGEEBBAAA", "AAAGCAAAAAAAAACGAAA", "AAAGCAAAAAAAAACGAAA", "AAAGCAAAAAAAAACGAAA",
                        "AAABBHHJIFIJHHBBAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAABBEEEFEEEBBAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA",
                        "AAAACCAAAAAAACCAAAA", "AAAABBHHJFJHHBBAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA",
                        "AAAAAAAFAAAFAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAACCBCCAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAIIJJBBBBBJJIIAAA", "AAAAAJAAAAAAAJAAAAA", "AAAAAJAAAAAAAJAAAAA", "AAAAAJAAAAAAAJAAAAA",
                        "AABBEJGGJBJGGJEBBAA", "AACCAAAAAAAAAAACCAA", "AACCAAAAAAAAAAACCAA", "AACCAAAAAAAAAAACCAA",
                        "AABBHHJIIFIIJHHBBAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAABBEEIIIIIEEBBAAA", "AAACCAAAAAAAAACCAAA", "AAACCAAAAAAAAACCAAA",
                        "AAACCAAAAAAAAACCAAA", "AAABBHHJJFJJHHBBAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAAGCCAAACCGAAAAA", "AAAAABBBJFJBBBAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIIJJBBBBBBBJJIIAA", "AAAAJAAAAAAAAAJAAAA", "AAAAJAAAAAAAAAJAAAA", "AAAAJAAAAAAAAAJAAAA",
                        "ABBEJGGJJBJJGGJEBBA", "AGCAAAAAAAAAAAAACGA", "AGCAAAAAAAAAAAAACGA", "AGCAAAAAAAAAAAAACGA",
                        "ABBHHJIIJJJIIJHHBBA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAABAAAAAAAAA", "AABBEEIIJBJIIEEBBAA", "AAGCAAAAAAAAAAACGAA", "AAGCAAAAAAAAAAACGAA",
                        "AAGCAAAAAAAAAAACGAA", "AABBHHJEEEEEJHHBBAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAABAAAAAAAAA", "AAAABBEEEFEEEBBAAAA", "AAAAGCAAAAAAACGAAAA", "AAAAGCAAAAAAACGAAAA",
                        "AAAAGCAAAAAAACGAAAA", "AAAABBJJIFIJJBBAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJJBBBGGGBBBJJIAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "ABEEGGJJBBBJJGGEEBA", "ACAAAAAAAAAAAAAAACA", "ACAAAAAAAAAAAAAAACA", "ACAAAAAAAAAAAAAAACA",
                        "ABHHJIIBBGBBIIJHHBA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA",
                        "AAAAAAAALBLAAAAAAAA", "AABEEIIJBBBJIIEEBAA", "AACAAAAAAAAAAAAACAA", "AACAAAAAAAAAAAAACAA",
                        "AACAAAAAAAAAAAAACAA", "AABHHJIIBBBIIJHHBAA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA",
                        "AAAAAAAALBLAAAAAAAA", "AAAABEEJJJJJEEBAAAA", "AAAACAAAAAAAAACAAAA", "AAAACAAAAAAAAACAAAA",
                        "AAAACAAAAAAAAACAAAA", "AAAABJJIBFBIJJBAAAA", "AAAAAAAAIAIAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJBBBGGEGGBBBJIAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "BBEGGJJJBEBJJJGGEBB", "CCAAAAAAAAAAAAAAACC", "CCAAAAAAAAAAAAAAACC", "CCAAAAAAAAAAAAAAACC",
                        "BBHJIIBEEGEEBIIJHBB", "AFAAAAACAAACAAAAAFA", "AFAAAAACAAACAAAAAFA", "AFAAAAACAAACAAAAAFA",
                        "AFAAAAAILBLIAAAAAFA", "ABBEIIJJBEBJJIIEBBA", "ACCAAAAAAAAAAAAACCA", "ACCAAAAAAAAAAAAACCA",
                        "ACCAAAAAAAAAAAAACCA", "ABBHJEIBJJJBIEJHBBA", "AAAFAAACAAACAAAFAAA", "AAAFAAACAAACAAAFAAA",
                        "AAAFAAAILBLIAAAFAAA", "AAABBEJJIIIJJEBBAAA", "AAACCAAAAAAAAACCAAA", "AAACCAAAAAAAAACCAAA",
                        "AAACCAAAAAAAAACCAAA", "AAABBJIBBBBBIJBBAAA", "AAAAAAAIIBIIAAAAAAA", "AAAAAAAJJAJJAAAAAAA",
                        "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJFJJAAAAAAA",
                        "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJBBGGEEEGGBBJIAA", "AAAFAAAAABAAAAAFAAA", "AAAFAAAAAAAAAAAFAAA", "AAAFAAAAABAAAAAFAAA",
                        "BEEJJJBBEGEBBJJJEEB", "CAAAAAAAAFAAAAAAAAC", "CAAAAAAAAFAAAAAAAAC", "CAAAAAAAAFAAAAAAAAC",
                        "BHJIIJBEFIFEBJIIJHB", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGBKAAAAAA",
                        "AAAAAALLLBLLLAAAAAA", "ABEEIJBBEGEBBJIEEBA", "ACAAAAAAAFAAAAAAACA", "ACAAAAAAAFAAAAAAACA",
                        "ACAAAAAAAFAAAAAAACA", "ABHJJEBJJGJJBEJJHBA", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGBKAAAAAA",
                        "AAAAAALLLBLLLAAAAAA", "AAABEEJIIGIIJEEBAAA", "AAACAAAAAFAAAAACAAA", "AAACAAAAAFAAAAACAAA",
                        "AAACAAAAAFAAAAACAAA", "AAABJIBBBIBBBIJBAAA", "AAAAAAIIBBBIIAAAAAA", "AAAAAAAJFFFJAAAAAAA",
                        "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA",
                        "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAFFFAAAAAAAA",
                        "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJBBGEEEEEGBBJIAA", "AAAJAAAABFBAAAAJAAA", "AAAJAAAAAMAAAAAJAAA", "AAAJAAAABNBAAAAJAAA",
                        "BFFJBBBEGNGEBBBJFFB", "BAAAAAAAFNFAAAAAAAB", "BAAAAAAAFNFAAAAAAAB", "BAAAAAAAFNFAAAAAAAB",
                        "BFFFFJGGINIGGJFFFFB", "AAAAAFAAINIAAFAAAAA", "AAAAAFAAINIAAFAAAAA", "AAAAAFAAINIAAFAAAAA",
                        "AAAAABBBBNBBBBAAAAA", "ABFFIBBEGNGEBBIFFBA", "ABAAAAAAFNFAAAAAABA", "ABAAAAAAFNFAAAAAABA",
                        "ABAAAAAAFNFAAAAAABA", "ABFFFEBJGNGJBEFFFBA", "AAAAAFAAINIAAFAAAAA", "AAAAAFAAINIAAFAAAAA",
                        "AAAAABBBBNBBBBAAAAA", "AAABFFJIGNGIJFFBAAA", "AAABAAAAFNFAAAABAAA", "AAABAAAAFNFAAAABAAA",
                        "AAABAAAAFNFAAAABAAA", "AAABFFFBIIIBFFFBAAA", "AAAAAAABBNBBAAAAAAA", "AAAAAAAAFNFAAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAFFNFFAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA",
                        "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAFNFAAAAAAAA", "AAAAAAAAANAAAAAAAAA",
                        "AAAAAAAAANAAAAAAAAA")
                .aisle("AAIJBBGGEEEGGBBJIAA", "AAAFAAAAABAAAAAFAAA", "AAAFAAAAAAAAAAAFAAA", "AAAFAAAAABAAAAAFAAA",
                        "BEEJJJBBEGEBBJJJEEB", "CAAAAAAAAFAAAAAAAAC", "CAAAAAAAAFAAAAAAAAC", "CAAAAAAAAFAAAAAAAAC",
                        "BHJIIJBEFIFEBJIIJHB", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGAKAAAAAA",
                        "AAAAAALLLBLLLAAAAAA", "ABEEIJBBEGEBBJIEEBA", "ACAAAAAAAFAAAAAAACA", "ACAAAAAAAFAAAAAAACA",
                        "ACAAAAAAAFAAAAAAACA", "ABHJJEBJJGJJBEJJHBA", "AAAAAAKAGIGAKAAAAAA", "AAAAAAKAGIGAKAAAAAA",
                        "AAAAAALLLBLLLAAAAAA", "AAABEEJIIGIIJEEBAAA", "AAACAAAAAFAAAAACAAA", "AAACAAAAAFAAAAACAAA",
                        "AAACAAAAAFAAAAACAAA", "AAABJIBBBIBBBIJBAAA", "AAAAAAIIBBBIIAAAAAA", "AAAAAAAJFFFJAAAAAAA",
                        "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA",
                        "AAAAAAAJFFFJAAAAAAA", "AAAAAAAJFFFJAAAAAAA", "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAFFFAAAAAAAA",
                        "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAFFFAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJBBBGGEGGBBBJIAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "BBEGGJJJBEBJJJGGEBB", "CCAAAAAAAAAAAAAAACC", "CCAAAAAAAAAAAAAAACC", "CCAAAAAAAAAAAAAAACC",
                        "BBHJIIBEEGEEBIIJHBB", "AFAAAAACAAACAAAAAFA", "AFAAAAACAAACAAAAAFA", "AFAAAAACAAACAAAAAFA",
                        "AFAAAAAILBLIAAAAAFA", "ABBEIIJJBEBJJIIEBBA", "ACCAAAAAAAAAAAAACCA", "ACCAAAAAAAAAAAAACCA",
                        "ACCAAAAAAAAAAAAACCA", "ABBHJEIBJJJBIEJHBBA", "AAAFAAACAAACAAAFAAA", "AAAFAAACAAACAAAFAAA",
                        "AAAFAAAILBLIAAAFAAA", "AAABBEJJIIIJJEBBAAA", "AAACCAAAAAAAAACCAAA", "AAACCAAAAAAAAACCAAA",
                        "AAACCAAAAAAAAACCAAA", "AAABBJIBBBBBIJBBAAA", "AAAAAAAIIBIIAAAAAAA", "AAAAAAAJJAJJAAAAAAA",
                        "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJFJJAAAAAAA",
                        "AAAAAAAJJAJJAAAAAAA", "AAAAAAAJJAJJAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIJJBBBGGGBBBJJIAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "ABEEGGJJBBBJJGGEEBA", "ACAAAAAAAAAAAAAAACA", "ACAAAAAAAAAAAAAAACA", "ACAAAAAAAAAAAAAAACA",
                        "ABHHJIIBBGBBIIJHHBA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA",
                        "AAAAAAAALBLAAAAAAAA", "AABEEIIJBBBJIIEEBAA", "AACAAAAAAAAAAAAACAA", "AACAAAAAAAAAAAAACAA",
                        "AACAAAAAAAAAAAAACAA", "AABHHJIIBBBIIJHHBAA", "AAAAAAAAKAKAAAAAAAA", "AAAAAAAAKAKAAAAAAAA",
                        "AAAAAAAALBLAAAAAAAA", "AAAABEEJJJJJEEBAAAA", "AAAACAAAAAAAAACAAAA", "AAAACAAAAAAAAACAAAA",
                        "AAAACAAAAAAAAACAAAA", "AAAABJJIBFBIJJBAAAA", "AAAAAAAAIAIAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAIIJJBBBBBBBJJIIAA", "AAAAJAAAAAAAAAJAAAA", "AAAAJAAAAAAAAAJAAAA", "AAAAJAAAAAAAAAJAAAA",
                        "ABBEJGGJJBJJGGJEBBA", "AGCAAAAAAAAAAAAACGA", "AGCAAAAAAAAAAAAACGA", "AGCAAAAAAAAAAAAACGA",
                        "ABBHHJIIJJJIIJHHBBA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAABAAAAAAAAA", "AABBEEIIJBJIIEEBBAA", "AAGCAAAAAAAAAAACGAA", "AAGCAAAAAAAAAAACGAA",
                        "AAGCAAAAAAAAAAACGAA", "AABBHHJEEEEEJHHBBAA", "AAAAAAAAAFAAAAAAAAA", "AAAAAAAAAFAAAAAAAAA",
                        "AAAAAAAAABAAAAAAAAA", "AAAABBEEEFEEEBBAAAA", "AAAAGCAAAAAAACGAAAA", "AAAAGCAAAAAAACGAAAA",
                        "AAAAGCAAAAAAACGAAAA", "AAAABBJJIFIJJBBAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAIIJJBBBBBJJIIAAA", "AAAAAJAAAAAAAJAAAAA", "AAAAAJAAAAAAAJAAAAA", "AAAAAJAAAAAAAJAAAAA",
                        "AABBEJGGJBJGGJEBBAA", "AACCAAAAAAAAAAACCAA", "AACCAAAAAAAAAAACCAA", "AACCAAAAAAAAAAACCAA",
                        "AABBHHJIIFIIJHHBBAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAABBEEIIIIIEEBBAAA", "AAACCAAAAAAAAACCAAA", "AAACCAAAAAAAAACCAAA",
                        "AAACCAAAAAAAAACCAAA", "AAABBHHJJFJJHHBBAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAAGCCAAACCGAAAAA", "AAAAABBBJFJBBBAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAIIJJJJJJJIIAAAA", "AAAAAAAAFJFAAAAAAAA", "AAAAAAAAFOFAAAAAAAA", "AAAAAAAAFJFAAAAAAAA",
                        "AAABBEEGJJJGEEBBAAA", "AAAGCAAAAAAAAACGAAA", "AAAGCAAAAAAAAACGAAA", "AAAGCAAAAAAAAACGAAA",
                        "AAABBHHJIFIJHHBBAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAABBEEEFEEEBBAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA",
                        "AAAACCAAAAAAACCAAAA", "AAAABBHHJFJHHBBAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA",
                        "AAAAAAAFAAAFAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAACCBCCAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAAIIIIIIIIIAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAABBEEEFEEEBBAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA", "AAAACCAAAAAAACCAAAA",
                        "AAAABBHHJFJHHBBAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAAGCCAAACCGAAAAA", "AAAAABBBHFHBBBAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAABBBEFEBBBAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA", "AAAAAGCCAAACCGAAAAA",
                        "AAAAABBBHFHBBBAAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA", "AAAAAAAFAAAFAAAAAAA",
                        "AAAAAAAFAAAFAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAACCBCCAAAAAAA", "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .aisle("AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAABBBBBAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA", "AAAAAAACCBCCAAAAAAA",
                        "AAAAAAABBBBBAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA", "AAAAAAAAAAAAAAAAAAA",
                        "AAAAAAAAAAAAAAAAAAA")
                .where('A', Predicates.any())
                .where('B', blocks(PhoenixTeslaBlocks.ADVANCED_INVARIANT_NAQ_ALLOY_CASING.get()))
                .where('C', glass(profile))
                .where('E', lamp(DyeColor.PURPLE))
                .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                        PhoenixProgressionMaterials.ADVANCED_QUIN_NAQUADIAN_ALLOY)))
                .where('G', blocks(PhoenixTeslaBlocks.MACHINE_CASING_RHODIUM_PLATED_PALLADIUM.get()))
                .where('H', lamp(DyeColor.BLACK))
                .where('I', blocks(PhoenixTeslaBlocks.ADVANCED_SOURCE_FIBER_CASING.get()))
                .where('J', hatchPredicate(definition, profile, PhoenixTeslaBlocks.ADVANCED_TESLA_CASING.get()))
                .where('K', blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                .where('L', blocks(PhoenixTeslaBlocks.RELIABLE_NAQUADAH_ALLOY_MACHINE_CASING.get()))
                .where('M', PhoenixPredicates.teslaBatteries(profile.minBatteryTier, profile.maxBatteryTier))
                .where('N', blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                .where('O', controller(blocks(definition.get())))
                .build();
    }

    private static MultiblockMachineDefinition registerRelay() {
        if (!PhoenixTeslaConfigs.get().towers.relay.enabled) return null;

        return REGISTRATE
                .multiblock("tesla_relay", TeslaRelayMachine::new)
                .langValue("Tesla Range Extender")
                .rotationState(RotationState.ALL)
                .recipeType(PhoenixTeslaRecipeTypes.TESLA_RELAY)
                .appearanceBlock(PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("TTTTT", "CGGGC", "CGGGC", "CGGGC", "CCCCC", "     ", "     ")
                        .aisle("TTTTT", "C   C", "C   C", "C   C", "CCCCC", "     ", "     ")
                        .aisle("TTTTT", "C F C", "C N C", "C F C", "CCFCC", "  F  ", "  E  ")
                        .aisle("TTTTT", "C   C", "C   C", "C   C", "CCCCC", "     ", "     ")
                        .aisle("TTSTT", "CGGGC", "CGGGC", "CGGGC", "CCCCC", "     ", "     ")
                        .where(" ", Predicates.any())
                        .where("T", blocks(PhoenixTeslaBlocks.INSANELY_SUPERCHARGED_TESLA_CASING.get()))
                        .where("C", blocks(PhoenixTeslaBlocks.MACHINE_CASING_NAQUADAH_ALLOY.get()))
                        .where("G", blocks(GTBlocks.CASING_TEMPERED_GLASS.get())
                                .or(blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                                .or(blocks(GTBlocks.FUSION_GLASS.get())))
                        .where("F",
                                blocks(ChemicalHelper.getBlock(TagPrefix.frameGt,
                                        PhoenixProgressionMaterials.ADVANCED_QUIN_NAQUADIAN_ALLOY)))
                        .where("N", blocks(GTBlocks.COIL_CUPRONICKEL.get()))
                        .where("E", Predicates.lampsByColor(DyeColor.PURPLE))
                        .where("S", controller(blocks(definition.get())))
                        .build())
                .model(
                        createWorkableCasingMachineModel(
                                PhoenixTeslaNetwork.id("block/casings/multiblock/tesla_casing"),
                                PhoenixTeslaNetwork.id("block/multiblock/tesla_tower"))
                                .andThen(d -> d.addDynamicRenderer(PhoenixDynamicRenderHelpers::getTeslaLinkRenderer)))
                .hasBER(true)
                .tooltipBuilder((stack, list) -> {
                    list.add(Component.literal("Extends the reach of your Tesla Network.")
                            .withStyle(TeslaTowerMachine.NEBULA_HSL));
                    list.add(Component.literal("Adds a " + PhoenixTeslaConfigs.get().towers.relay.rangeBlocks +
                            " block radius around itself.").withStyle(ChatFormatting.GRAY));
                    list.add(Component.literal("Only works while inside a Tesla Tower's range (or another"));
                    list.add(Component.literal("working Range Extender's), so they can be chained outward."));
                })
                .register();
    }

    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    private static String getTeslaOverlay(String iomode, int amperage) {
        if (amperage == 16) {
            return "tesla_hatches/tesla_" + iomode;
        }

        return "tesla_hatches/tesla_" + iomode + "_" + amperage + "a";
    }

    private static MachineDefinition[] registerTeslaHatch(String name, IO io, int amperage, PartAbility ability,
                                                          int... tiers) {
        String iomode = io == IO.OUT ? "input" : "output";

        return registerTieredMachines(
                name + "_" + amperage + "a",
                (holder, tier) -> new TeslaEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + " Tesla Energy " + (io == IO.OUT ? "Uplink" : "Downlink") +
                                " Hatch " + amperage + "A")
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                                Component.translatable(
                                        "gtceu.universal.tooltip.amperage_" + (io == IO.OUT ? "out" : "in"), amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil.formatNumbers(
                                                GTValues.V[tier] * (io == IO.OUT ? 16L : 64L) * amperage)),
                                Component.translatable("tooltip.PhoenixTeslaNetwork.tesla_hatch." + iomode))
                        .overlayTieredHullModel(getTeslaOverlay(iomode, amperage))
                        .register(),
                tiers);
    }

    public static final MachineDefinition[] TESLA_LASER_INPUT_256A = registerTeslaLaserHatch(
            "tesla_laser_input_hatch_256a", IO.OUT, 256,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_256A = registerTeslaLaserHatch(
            "tesla_laser_output_hatch_256a", IO.IN, 256,
            PartAbility.INPUT_LASER);

    public static final MachineDefinition[] TESLA_LASER_INPUT_1024A = registerTeslaLaserHatch(
            "tesla_laser_input_hatch_1024a", IO.OUT, 1024,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_1024A = registerTeslaLaserHatch(
            "tesla_laser_output_hatch_1024a", IO.IN, 1024,
            PartAbility.INPUT_LASER);

    public static final MachineDefinition[] TESLA_LASER_INPUT_4096A = registerTeslaLaserHatch(
            "tesla_laser_input_hatch_4096a", IO.OUT, 4096,
            PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_4096A = registerTeslaLaserHatch(
            "tesla_laser_output_hatch_4096a", IO.IN, 4096,
            PartAbility.INPUT_LASER);

    public static final MachineDefinition[] TESLA_LASER_INPUT_16384A = registerGatedLaserHatch("input", IO.OUT,
            16_384, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_16384A = registerGatedLaserHatch("output", IO.IN,
            16_384, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_INPUT_65536A = registerGatedLaserHatch("input", IO.OUT,
            65_536, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_65536A = registerGatedLaserHatch("output", IO.IN,
            65_536, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_INPUT_262144A = registerGatedLaserHatch("input", IO.OUT,
            262_144, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_262144A = registerGatedLaserHatch("output", IO.IN,
            262_144, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_INPUT_1048576A = registerGatedLaserHatch("input", IO.OUT,
            1_048_576, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_1048576A = registerGatedLaserHatch("output", IO.IN,
            1_048_576, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_INPUT_4194304A = registerGatedLaserHatch("input", IO.OUT,
            4_194_304, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_4194304A = registerGatedLaserHatch("output", IO.IN,
            4_194_304, PartAbility.INPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_INPUT_16777216A = registerGatedLaserHatch("input", IO.OUT,
            16_777_216, PartAbility.OUTPUT_LASER);
    public static final MachineDefinition[] TESLA_LASER_OUTPUT_16777216A = registerGatedLaserHatch("output", IO.IN,
            16_777_216, PartAbility.INPUT_LASER);

    private static MachineDefinition[] registerGatedLaserHatch(String direction, IO io, int amperage,
                                                               PartAbility ability) {
        int required = amperage > 1_048_576 ? 2 : 1;
        boolean enabled = PhoenixTeslaConfigs.get().features.laserHatchSizes.ordinal() >= required;
        if (!enabled && !net.minecraftforge.data.loading.DatagenModLoader.isRunningDataGen()) return null;

        return registerTeslaLaserHatch("tesla_laser_" + direction + "_hatch_" + amperage + "a", io, amperage, ability);
    }

    private static String getLaserName(IO io, int amperage) {
        if (io == IO.OUT) {
            if (amperage >= 4_194_304) return "Tesla Beam Apex Matrix";
            if (amperage >= 262_144) return "Tesla Beam Singularity Matrix";
            if (amperage >= 16_384) return "Tesla Beam Resonance Matrix";
            if (amperage >= 4096) return "Phased Tesla Beam Matrix";
            if (amperage >= 256) return "Tesla Beam Collimator Array";
            return "Tesla Beam Collimator";
        } else {
            if (amperage >= 4_194_304) return "Tesla Flux Apex Matrix";
            if (amperage >= 262_144) return "Tesla Flux Singularity Matrix";
            if (amperage >= 16_384) return "Tesla Flux Resonance Matrix";
            if (amperage >= 4096) return "Tesla Flux Coalescence Matrix";
            if (amperage >= 256) return "Tesla Flux Coalescence Array";
            return "Tesla Flux Coalescer";
        }
    }

    private static MachineDefinition[] registerTeslaLaserHatch(String name, IO io, int amperage, PartAbility ability) {
        String iomode = io == IO.OUT ? "input" : "output";
        String laserDisplayName = getLaserName(io, amperage);

        return registerTieredMachines(
                name + "_" + amperage + "a",
                (holder, tier) -> new TeslaEnergyHatchPartMachine(holder, tier, io, amperage),
                (tier, builder) -> builder
                        .langValue(GTValues.VNF[tier] + " " + laserDisplayName + " " + amperage + "A")
                        .rotationState(RotationState.ALL)
                        .abilities(ability)
                        .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]), GTValues.VNF[tier]),
                                Component.translatable(
                                        "gtceu.universal.tooltip.amperage_" + (io == IO.OUT ? "in" : "out"), amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil.formatNumbers(
                                                GTValues.V[tier] * (io == IO.OUT ? 16L : 64L) * amperage)),
                                Component.translatable("tooltip.PhoenixTeslaNetwork.tesla_hatch." + iomode))
                        .overlayTieredHullModel(getTeslaLaserOverlay(iomode, amperage))
                        .register(),
                GTValues.ALL_TIERS);
    }

    private static String getTeslaLaserOverlay(String iomode, int amperage) {
        return "tesla_hatches/tesla_" + iomode + "_" + "laser" + "_" + Math.min(amperage, 4096) + "a";
    }

    public static final MachineDefinition[] TESLA_INPUT_2A = registerTeslaHatch("tesla_energy_input_hatch", IO.OUT, 2,
            PartAbility.OUTPUT_ENERGY, GTValues.ALL_TIERS);

    public static final MachineDefinition[] TESLA_INPUT_4A = registerTeslaHatch("tesla_energy_input_hatch", IO.OUT, 4,
            PartAbility.OUTPUT_ENERGY, MULTI_AMP_TESLA_HATCH);

    public static final MachineDefinition[] TESLA_INPUT_16A = registerTeslaHatch("tesla_energy_input_hatch", IO.OUT, 16,
            PartAbility.OUTPUT_ENERGY, MULTI_AMP_TESLA_HATCH);

    public static final MachineDefinition[] TESLA_INPUT_64A = registerTeslaHatch("tesla_energy_input_hatch", IO.OUT, 64,
            PartAbility.SUBSTATION_OUTPUT_ENERGY,
            MULTI_AMP_TESLA_HATCH);

    public static final MachineDefinition[] TESLA_OUTPUT_2A = registerTeslaHatch("tesla_energy_output_hatch", IO.IN, 2,
            PartAbility.INPUT_ENERGY, GTValues.ALL_TIERS);

    public static final MachineDefinition[] TESLA_OUTPUT_4A = registerTeslaHatch("tesla_energy_output_hatch", IO.IN, 4,
            PartAbility.INPUT_ENERGY, MULTI_AMP_TESLA_HATCH);

    public static final MachineDefinition[] TESLA_OUTPUT_16A = registerTeslaHatch("tesla_energy_output_hatch", IO.IN,
            16, PartAbility.INPUT_ENERGY,
            MULTI_AMP_TESLA_HATCH);

    public static final MachineDefinition[] TESLA_OUTPUT_64A = registerTeslaHatch("tesla_energy_output_hatch", IO.IN,
            64, PartAbility.SUBSTATION_INPUT_ENERGY,
            MULTI_AMP_TESLA_HATCH);

    public static MachineDefinition[] registerWirelessCharger(
                                                              GTRegistrate registrate,
                                                              String name,
                                                              BiFunction<IMachineBlockEntity, Integer, TeslaWirelessChargerMachine> factory) {
        return registerChargerTieredMachines(
                registrate,
                name,
                factory::apply,
                (tier, builder) -> builder
                        .rotationState(RotationState.ALL)
                        .modelProperty(GTMachineModelProperties.CHARGER_STATE, ChargerMachine.State.IDLE)
                        .model(PhoenixMachineModels.createWirelessChargerModel())
                        .langValue("%s Tesla Wireless Charger".formatted(
                                VCF[tier] + GTValues.VOLTAGE_NAMES[tier] + ChatFormatting.RESET))
                        .tooltips(
                                Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in", 4),
                                Component.literal("Wireless Range: ").withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal("Wherever the Tesla Network reaches")
                                                .withStyle(ChatFormatting.AQUA)),
                                Component.literal("Charges Armor and Tools from the Tesla Network")
                                        .withStyle(ChatFormatting.GREEN))
                        .register(),
                GTValues.ALL_TIERS);
    }

    public static MachineDefinition[] registerChargerTieredMachines(
                                                                    GTRegistrate registrate,
                                                                    String name,
                                                                    BiFunction<IMachineBlockEntity, Integer, MetaMachine> machineFactory,
                                                                    BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> definitionBuilder,
                                                                    int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];

        for (int tier : tiers) {
            var builder = registrate
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> machineFactory.apply(holder, tier))
                    .tier(tier);

            definitions[tier] = definitionBuilder.apply(tier, builder);
        }

        return definitions;
    }

    public static final MachineDefinition[] TESLA_WIRELESS_CHARGER = registerWirelessCharger(
            REGISTRATE,
            "tesla_wireless_charger",
            TeslaWirelessChargerMachine::new);

    public static void init() {}
}
