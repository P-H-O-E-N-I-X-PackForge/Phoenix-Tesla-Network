package net.phoenix_tesla_network.tesla.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

public class PhoenixElements {

    public static Element PHOENIX_ENRICHED_TRITANIUM;
    public static Element EMBER;
    public static Element PHOENIX_ENRICHED_NAQUADAH;
    public static Element AKASHIC_ZERONIUM;
    public static Element AETHERIUM_STEEL;
    public static Element VOID_TOUCHED_TUNGSTEN_STEEL;
    public static Element CELESTIAL_AURORIUM;
    public static Element PRIMORDIAL_FLUX_METAL;
    public static Element ETERNAL_STARFORGED_STEEL;
    public static Element DIMENSIONAL_REFLECTION_ALLOY;
    public static Element TIMEWOVEN_PLATINUM;
    public static Element SOULBOUND_ETHERSTEEL;
    public static Element TACHYON_INFUSED_CHROMIUM;
    public static Element ECHO_CRYSTAL_ALLOY;
    public static Element NEBULAR_RESONANCE_INGOT;
    public static Element PARADOXIUM;
    public static Element SUBSPACE_COBALT;
    public static Element SINGULARITY_FORGED_TITANIUM;
    public static Element EXOTIC_VANADIUM_COMPOSITE;
    public static Element DARK_MATTER_PLATED_IRIDIUM;
    public static Element ZIRCALLOY;
    public static Element SOURCE_IMBUED_TITANIUM;
    public static Element ICY_STEEL_MATRIX;

    public static void init() {
        PHOENIX_ENRICHED_TRITANIUM = create("phoenix_enriched_tritanium", 1, 32, "PET");
        EMBER = create("ember", 1, 2, "🔥");
        PHOENIX_ENRICHED_NAQUADAH = create("phoenix_enriched_naquadah", 25, 32, "PENaq");
        AKASHIC_ZERONIUM = create("akashic_zeronium", 24, 12, "ASHK");
        AETHERIUM_STEEL = create("aetherium_steel", 26, 30, "AES");
        VOID_TOUCHED_TUNGSTEN_STEEL = create("void_touched_tungsten_steel", 74, 110, "VTT");

        CELESTIAL_AURORIUM = create("celestial_aurorium", -1, -1, "CAu");
        PRIMORDIAL_FLUX_METAL = create("primordial_flux_metal", -1, -1, "PFM");
        ETERNAL_STARFORGED_STEEL = create("eternal_starforged_steel", -1, -1, "ESS");
        DIMENSIONAL_REFLECTION_ALLOY = create("dimensional_reflection_alloy", -1, -1, "DRA");
        TIMEWOVEN_PLATINUM = create("timewoven_platinum", -1, -1, "TWPt");
        SOULBOUND_ETHERSTEEL = create("soulbound_ethersteel", -1, -1, "SEth");
        TACHYON_INFUSED_CHROMIUM = create("tachyon_infused_chromium", -1, -1, "TiCr");
        ECHO_CRYSTAL_ALLOY = create("echo_crystal_alloy", -1, -1, "ECA");
        NEBULAR_RESONANCE_INGOT = create("nebular_resonance_ingot", -1, -1, "NRI");
        PARADOXIUM = create("paradoxium", -1, -1, "Px");

        SUBSPACE_COBALT = create("subspace_cobalt", 27, 33, "QIC");
        SINGULARITY_FORGED_TITANIUM = create("singularity_forged_titanium", 22, 26, "SFTi");
        EXOTIC_VANADIUM_COMPOSITE = create("exotic_vanadium_composite", 23, 28, "EVC");
        DARK_MATTER_PLATED_IRIDIUM = create("dark_matter_plated_iridium", 77, 116, "DMPIr");

        ZIRCALLOY = create("zircalloy", 77, 125, "Zr⁷BiHf³");
        SOURCE_IMBUED_TITANIUM = create("source_imbued_titanium", 10, 118, "✨C✨Ti");
        ICY_STEEL_MATRIX = create("icy_steel_matrix", 8, 118, "❆Is<>");
    }

    private static Element create(String name, long protons, long neutrons, String symbol) {
        return create(name, protons, neutrons, -1L, (String) null, name, symbol, false);
    }

    private static Element create(String id, long protons, long neutrons, long halfLife, String decayTo, String name,
                                  String symbol, boolean isIsotope) {
        Element element = new Element(protons, neutrons, halfLife, decayTo, name, symbol, isIsotope);
        GTRegistries.ELEMENTS.register(id, element);
        return element;
    }
}
