package net.phoenix_tesla_network.tesla.common.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.phoenix_tesla_network.tesla.common.data.materials.CoreMaterials;
import net.phoenix_tesla_network.tesla.common.data.materials.PhoenixProgressionMaterials;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static net.phoenix_tesla_network.tesla.common.data.recipe.TeslaHatchRecipes.*;

public class CustomComponetRecipes {

    private static final int[] VA = GTValues.VA;

    public static void init(@NotNull RecipeOutput provider) {
        run(provider);
    }

    private static void run(@NotNull RecipeOutput provider) {
        for (int tier = LV; tier <= GTValues.OpV; tier++) {
            if (tier == GTValues.LuV) {
                processScannerTeslaStabilizerRecipe(provider, tier);
            } else if (tier >= GTValues.ZPM) {
                processStationTeslaStabilizerRecipe(provider, tier);
            } else {
                processTeslaStabilizerRecipe(provider, tier);
            }
        }
    }

    private static void processTeslaStabilizerRecipe(@NotNull RecipeOutput provider, int tier) {
        var rod = TagPrefix.rod;

        VanillaRecipeHelper.addShapedRecipe(provider, true,
                "tesla_stabilizer_" + GTValues.VN[tier].toLowerCase(),
                getTeslaStabilizerForTier(tier),
                " R ", "RCR", "XRX",
                'C', (getCoilForTier(tier)),
                'R', (getRodForTier(tier)),
                'X', getCircuitForTier(tier));
        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder("tesla_stabilizer_assembler_" + VN[tier].toLowerCase())
                .inputItems(getCoilForTier(tier))
                .inputItems(getRodForTier(tier), 4)
                .inputItems(getCircuitForTier(tier), 2)
                .outputItems(getTeslaStabilizerForTier(tier))
                .EUt(GTValues.VA[tier - 1])
                .duration(100)
                .save(provider);
    }

    private static void processScannerTeslaStabilizerRecipe(@NotNull RecipeOutput provider, int tier) {
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("tesla_stabilizer_scanner_" + VN[tier].toLowerCase())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144 * tier))
                .inputItems(getFrameForTier(tier))
                .inputItems(GTCraftingComponents.MOTOR.get(tier))
                .inputItems(getLongRodForTier(tier), 4)
                .inputItems(getPlateForTier(tier))
                .inputItems(getCircuitForTier(tier), 2)
                .inputItems(getCoilForTier(tier), 2)
                .inputItems(getFoilForTier(tier), 64)
                .inputItems(getQuadCableForTier(tier), 8)
                .outputItems(getTeslaStabilizerForTier(tier))
                .duration(800)
                .EUt(GTValues.VA[tier - 1])
                .scannerResearch(b -> b
                        .researchStack(getTeslaStabilizerForTier(tier - 1))
                        .duration(2400)
                        .EUt(VA[IV]))
                .save(provider);
    }

    private static void processStationTeslaStabilizerRecipe(@NotNull RecipeOutput provider, int tier) {
        if (getFrameForTier(tier).isEmpty() || getLongRodForTier(tier).isEmpty() || getPlateForTier(tier).isEmpty() ||
                getFoilForTier(tier).isEmpty() || getQuadCableForTier(tier).isEmpty()) {
            net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork.LOGGER.warn(
                    "Skipping tesla stabilizer station recipe for tier {}: an ingredient is empty",
                    tier);
            return;
        }
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder("tesla_stabilizer_station_" + VN[tier].toLowerCase())
                .inputItems(getFrameForTier(tier))
                .inputItems(GTCraftingComponents.MOTOR.get(tier))
                .inputItems(getLongRodForTier(tier), 4)
                .inputItems(getPlateForTier(tier), 4)
                .inputItems(getCircuitForTier(tier), 2)
                .inputItems(getCoilForTier(tier), 2)
                .inputItems(getFoilForTier(tier), 64)
                .inputItems(getQuadCableForTier(tier), 8)
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144 * tier))
                .outputItems(getTeslaStabilizerForTier(tier))
                .duration(800)
                .EUt(GTValues.VA[tier - 1])
                .stationResearch(b -> b
                        .researchStack(getTeslaStabilizerForTier(tier - 1))
                        .CWUt(8))
                .save(provider);
    }

    private static final Map<Integer, Material> MATERIAL_MAP = Map.of(
            LV, CoreMaterials.aurumSteel(),
            MV, CoreMaterials.aluminfrost(),
            HV, CoreMaterials.frostReinforcedStainedSteel(),
            EV, CoreMaterials.sourceImbuedTitanium(),
            IV, CoreMaterials.voidTouchedTungstenSteel(),
            LuV, PhoenixProgressionMaterials.RESONANT_RHODIUM_ALLOY,
            ZPM, PhoenixProgressionMaterials.ADVANCED_QUIN_NAQUADIAN_ALLOY);

    private static ItemStack get(TagPrefix prefix, int tier) {
        Material mat = MATERIAL_MAP.getOrDefault(tier, CoreMaterials.aurumSteel());
        return ChemicalHelper.get(prefix, mat);
    }

    private static final Map<Integer, TagKey<Item>> CIRCUIT_MAP = Map.of(
            LV, CustomTags.LV_CIRCUITS, MV, CustomTags.MV_CIRCUITS, HV, CustomTags.HV_CIRCUITS,
            EV, CustomTags.EV_CIRCUITS, IV, CustomTags.IV_CIRCUITS, LuV, CustomTags.LuV_CIRCUITS,
            ZPM, CustomTags.ZPM_CIRCUITS, UV, CustomTags.UV_CIRCUITS, UHV, CustomTags.UHV_CIRCUITS);

    public static ItemStack getFoilForTier(int tier) {
        return get(TagPrefix.foil, tier);
    }

    public static ItemStack getPlateForTier(int tier) {
        return get(TagPrefix.plate, tier);
    }

    public static ItemStack getDensePlateForTier(int tier) {
        return get(TagPrefix.plateDense, tier);
    }

    public static ItemStack getSmallGearForTier(int tier) {
        return get(TagPrefix.gearSmall, tier);
    }

    public static ItemStack getGearForTier(int tier) {
        return get(TagPrefix.gear, tier);
    }

    public static ItemStack getRodForTier(int tier) {
        return get(TagPrefix.rod, tier);
    }

    public static ItemStack getLongRodForTier(int tier) {
        return get(TagPrefix.rodLong, tier);
    }

    public static ItemStack getFrameForTier(int tier) {
        return get(TagPrefix.frameGt, tier);
    }

    public static ItemStack getQuadWireForTier(int tier) {
        return get(TagPrefix.wireGtQuadruple, tier);
    }

    public static ItemStack getQuadCableForTier(int tier) {
        return get(TagPrefix.cableGtQuadruple, tier);
    }

    public static ItemStack getOctalWireForTier(int tier) {
        return get(TagPrefix.wireGtOctal, tier);
    }

    public static ItemStack getOctalCableForTier(int tier) {
        return get(TagPrefix.cableGtOctal, tier);
    }

    public static ItemStack getHexWireForTier(int tier) {
        return get(TagPrefix.wireGtHex, tier);
    }

    public static TagKey<Item> getCircuitForTier(int tier) {
        return CIRCUIT_MAP.getOrDefault(tier, CustomTags.LV_CIRCUITS);
    }
}
