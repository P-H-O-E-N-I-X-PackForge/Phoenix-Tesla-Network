package net.phoenix_tesla_network.tesla.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;

public class PhoenixProgressionMaterials {

    public static Material RESONANT_RHODIUM_ALLOY;
    public static Material ADVANCED_QUIN_NAQUADIAN_ALLOY;

    public static void register() {
        RESONANT_RHODIUM_ALLOY = new Material.Builder(PhoenixTeslaNetwork.id("resonant_rhodium_alloy"))
                .ingot().fluid()
                .color(0xE245F8).secondaryColor(0xA345B0).iconSet(MaterialIconSet.METALLIC)
                .components(GTMaterials.Rhodium, 3, GTMaterials.Palladium, 4, PhoenixOres.POLARITY_FLIPPED_BISMUTHITE,
                        1, GTMaterials.Cerium, 4)
                .cableProperties(GTValues.LuV, 1, 2, false)
                .blastTemp(3600, HIGH, 480, 400).fluidPipeProperties(2800, 200, true, true, false, false)
                .flags(GENERATE_PLATE, GENERATE_RING, PHOSPHORESCENT, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_DENSE, GENERATE_ROTOR,
                        GENERATE_FOIL)
                .buildAndRegister();

        ADVANCED_QUIN_NAQUADIAN_ALLOY = new Material.Builder(PhoenixTeslaNetwork.id("advanced_quin_naquadian_alloy"))
                .ingot()
                .liquid(7400)
                .color(0x000000)
                .secondaryColor(0x8B0000)
                .cableProperties(GTValues.ZPM, 1, 2, false)
                .iconSet(MaterialIconSet.RADIOACTIVE)
                .fluidPipeProperties(8000, 800, true, true, true, true)
                .components(GTMaterials.Naquadah, 5, GTMaterials.Trinium, 1, GTMaterials.Technetium, 3,
                        GTMaterials.Strontium, 4, GTMaterials.Iodine, 1)
                .blastTemp(7200, HIGH, GTValues.VA[GTValues.ZPM], 1950)
                .flags(GENERATE_PLATE,
                        GENERATE_RING,
                        PHOSPHORESCENT,
                        GENERATE_ROD,
                        GENERATE_BOLT_SCREW,
                        GENERATE_FRAME,
                        GENERATE_DENSE,
                        GENERATE_SMALL_GEAR,
                        GENERATE_ROTOR,
                        GENERATE_FOIL,
                        GENERATE_LONG_ROD)
                .buildAndRegister();
    }
}
