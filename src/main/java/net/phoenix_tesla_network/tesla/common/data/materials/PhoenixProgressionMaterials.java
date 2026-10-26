package net.phoenix_tesla_network.tesla.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MaterialRegistryEntry;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*;
import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

/**
 * GTM 8.0 style: materials are declared through REGISTRATE.material(...), which registers them in GT's material
 * registry (the old "new MaterialBuilder(...).createMaterial()" path doesn't, and that is what left these materials
 * without frame/plate/etc. blocks). The registrate prefixes the id with this mod's namespace, so the ids stay
 * phoenix_tesla_network:resonant_rhodium_alloy and phoenix_tesla_network:advanced_quin_naquadian_alloy.
 */
public class PhoenixProgressionMaterials {

    public static MaterialRegistryEntry RESONANT_RHODIUM_ALLOY;
    public static MaterialRegistryEntry ADVANCED_QUIN_NAQUADIAN_ALLOY;

    public static void register() {
        RESONANT_RHODIUM_ALLOY = REGISTRATE.material("resonant_rhodium_alloy", builder -> builder
                .ingot().fluid()
                .color(0xE245F8).secondaryColor(0xA345B0).iconSet(METALLIC)
                .components(GTMaterials.Rhodium, 3, GTMaterials.Palladium, 4, GTMaterials.Cerium, 4)
                // The old call passed the tier index (GTValues.LuV = 6) as the voltage; the voltage is V[LuV].
                .cableProperties(V[LuV], 1, 2)
                .blast(b -> b.temp(3600, GasTier.HIGH)
                        .blastStats(480, 400))
                .fluidPipeProperties(2800, 200, true, true, false, false)
                .flags(GENERATE_PLATE, GENERATE_RING, PHOSPHORESCENT, GENERATE_ROD, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_SMALL_GEAR, GENERATE_BOLT_SCREW, GENERATE_FRAME, GENERATE_DENSE, GENERATE_ROTOR,
                        GENERATE_FOIL));

        ADVANCED_QUIN_NAQUADIAN_ALLOY = REGISTRATE.material("advanced_quin_naquadian_alloy", builder -> builder
                .ingot()
                .liquid(new FluidBuilder().temperature(7400))
                .color(0x000000).secondaryColor(0x8B0000).iconSet(RADIOACTIVE)
                .components(GTMaterials.Naquadah, 5, GTMaterials.Trinium, 1, GTMaterials.Technetium, 3,
                        GTMaterials.Strontium, 4, GTMaterials.Iodine, 1)
                // Same fix as above: V[ZPM], not the tier index.
                .cableProperties(V[ZPM], 1, 2)
                .blast(b -> b.temp(7200, GasTier.HIGH)
                        .blastStats(VA[ZPM], 1950))
                .fluidPipeProperties(8000, 800, true, true, true, true)
                .flags(GENERATE_PLATE, GENERATE_RING, PHOSPHORESCENT, GENERATE_ROD, GENERATE_BOLT_SCREW,
                        GENERATE_FRAME, GENERATE_DENSE, GENERATE_SMALL_GEAR, GENERATE_ROTOR, GENERATE_FOIL,
                        GENERATE_LONG_ROD));
    }
}
