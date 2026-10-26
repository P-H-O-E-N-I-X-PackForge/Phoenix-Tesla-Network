package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.ModularItemComponent;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;

import static net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork.PHOENIX_CREATIVE_TAB;
import static net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration.REGISTRATE;

public class PhoenixTeslaItems {

    static {
        REGISTRATE.creativeModeTab(() -> PHOENIX_CREATIVE_TAB);
    }

    public static ItemEntry<TeslaBinderItem> TESLA_BINDER = REGISTRATE
            .item("tesla_binder", TeslaBinderItem::new)
            .lang("Tesla Binder")
            .properties(p -> p.stacksTo(1))
            .onRegister(c -> c.attachComponents(c))
            .model((ctx, prov) -> prov.handheld(ctx, prov.modLoc("item/tools/tesla_binder")))
            .register();

    /**
     * Same battery as before (500M EU, tier 8) plus module slots. Slot counts: chest 6 (link, rebirth, wing + GTM
     * extras), others 5. CHECK: that ElectricStats and ModularItemComponent can be attached together on a
     * ModularArmorItem, and whether you'd rather make the battery a BatteryItemModule (see the notes).
     */
    /** GTArmorMaterials.ARMOR is gone in 8.0. Placeholder so it builds; swap for your own material (see notes). */
    private static final net.minecraft.core.Holder<net.minecraft.world.item.ArmorMaterial> PHOENIX_MATERIAL = GTArmorMaterials.NANO_MUSCLE;

    // More than the quarktech suite: 8 per piece, 10 on the chest.
    private static final int SLOTS_SMALL = 8;
    private static final int SLOTS_CHEST = 10;

    private static void suite(PhoenixTechArmorItem item, int moduleSlots) {
        // ElectricStats' constructor is protected: anonymous subclass, same as the old ArmorLogicSuite did.
        item.attachComponents(new ElectricStats(500_000_000L, GTValues.UV, true, false) {},
                new ModularItemComponent(moduleSlots, GTValues.UV));
    }

    public static ItemEntry<PhoenixTechArmorItem> PHOENIX_HELMET = REGISTRATE
            .item("phoenix_helmet", p -> new PhoenixTechArmorItem(PHOENIX_MATERIAL, ArmorItem.Type.HELMET, p))
            .lang("Phoenix Tech Suite Helmet")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(ItemTags.HEAD_ARMOR)
            .tag(CustomTags.PPE_ARMOR)
            .onRegister(i -> suite(i, SLOTS_SMALL))
            .register();

    public static ItemEntry<PhoenixArmorItem> PHOENIX_CHESTPLATE = REGISTRATE
            .item("phoenix_chestplate",
                    p -> new PhoenixArmorItem(PHOENIX_MATERIAL, ArmorItem.Type.CHESTPLATE, p))
            .lang("Phoenix Tech Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(ItemTags.CHEST_ARMOR)
            .tag(ItemTags.FREEZE_IMMUNE_WEARABLES)
            .tag(CustomTags.PPE_ARMOR)
            .onRegister(i -> suite(i, SLOTS_CHEST))
            .register();

    public static ItemEntry<PhoenixTechArmorItem> PHOENIX_LEGGINGS = REGISTRATE
            .item("phoenix_leggings",
                    p -> new PhoenixTechArmorItem(PHOENIX_MATERIAL, ArmorItem.Type.LEGGINGS, p))
            .lang("Phoenix Tech Suite Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(ItemTags.LEG_ARMOR)
            .tag(CustomTags.PPE_ARMOR)
            .onRegister(i -> suite(i, SLOTS_SMALL))
            .register();

    public static ItemEntry<PhoenixTechArmorItem> PHOENIX_BOOTS = REGISTRATE
            .item("phoenix_boots", p -> new PhoenixTechArmorItem(PHOENIX_MATERIAL, ArmorItem.Type.BOOTS, p))
            .lang("Phoenix Tech Suite Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(ItemTags.FOOT_ARMOR)
            .tag(CustomTags.PPE_ARMOR)
            .onRegister(i -> suite(i, SLOTS_SMALL))
            .register();

    // ---- Module items ------------------------------------------------------------------------------------
    // Module items are plain ComponentItems. What lets a module install is the Equipment Foundry recipe in
    // PhoenixTeslaGTAddon.addRecipes (item + module + which armor), not the item's name.
    // Textures are vanilla placeholders: add item/modules/<name>.png and switch to prov.modLoc(...) when ready.

    /**
     * @param slot  where it installs, shown in the tooltip ("Chestplate", "Boots", ...)
     * @param extra optional extra tooltip line (compatibility notes etc.), or null
     */
    private static ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> moduleItem(String name, String lang,
                                                                                      String description,
                                                                                      String placeholder,
                                                                                      String slot, String extra) {
        REGISTRATE.addRawLang("module.phoenix_tesla_network." + name, lang);
        REGISTRATE.addRawLang("module.phoenix_tesla_network." + name + ".description", description);
        if (extra != null) REGISTRATE.addRawLang("tooltip.phoenix_tesla_network.module." + name + ".extra", extra);
        return REGISTRATE.item(name, com.gregtechceu.gtceu.api.item.ComponentItem::new)
                .lang(lang)
                .properties(p -> p.stacksTo(1).rarity(Rarity.EPIC))
                .model((ctx, prov) -> prov.generated(ctx,
                        net.minecraft.resources.ResourceLocation.withDefaultNamespace(
                                placeholder.contains("/") ? placeholder : "item/" + placeholder)))
                .onRegister(i -> i.attachComponents(
                        new com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior(lines -> {
                            lines.add(Component.translatable("module.phoenix_tesla_network." + name + ".description")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY));
                            lines.add(Component.translatable("tooltip.phoenix_tesla_network.module.slot", slot)
                                    .withStyle(net.minecraft.ChatFormatting.AQUA));
                            if (extra != null) {
                                lines.add(Component.translatable(
                                        "tooltip.phoenix_tesla_network.module." + name + ".extra")
                                        .withStyle(net.minecraft.ChatFormatting.YELLOW));
                            }
                        })))
                .register();
    }

    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> TESLA_LINK_MODULE = moduleItem(
            "tesla_link_module", "Tesla Link Module",
            "Fire immunity, retaliation lightning and wireless top-up of the suit battery from your Tesla network.",
            "nether_star", "Chestplate", null);
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_REBIRTH_MODULE = moduleItem(
            "phoenix_rebirth_module", "Phoenix Rebirth Module",
            "Negates fall and wall-crash damage and revives you from lethal damage using network energy.",
            "totem_of_undying", "Chestplate", "Draws on your Tesla network; has a short cooldown.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_PURIFIER_MODULE = moduleItem(
            "phoenix_purifier_module", "Phoenix Purifier Module",
            "Removes harmful potion effects at a battery cost.", "milk_bucket", "Helmet", null);
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_TESLA_DISCHARGE_MODULE = moduleItem(
            "phoenix_tesla_discharge_module", "Tesla Discharge Module",
            "Press the discharge key to strike nearby mobs with lightning.", "block/lightning_rod", "Boots",
            "Bind the discharge key in Controls. 5 second cooldown.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_WING_MODULE = moduleItem(
            "phoenix_wing_module", "Phoenix Wing Module",
            "Wing flight: elytra glide and creative-style flight. Not compatible with GTM jetpacks.", "elytra",
            "Chestplate", "Cannot be combined with GTM jetpack or creative flight modules.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_SPRINT_MODULE = moduleItem(
            "phoenix_sprint_module", "Phoenix Sprint Module",
            "Powered sprint boost with an adjustable strength.", "sugar", "Leggings",
            "Cannot be combined with GTM speed modules.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_JUMP_MODULE = moduleItem(
            "phoenix_jump_module", "Phoenix Jump Module",
            "Powered super jump with an adjustable height.", "rabbit_foot", "Boots",
            "Cannot be combined with GTM jump boost modules.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_STEP_MODULE = moduleItem(
            "phoenix_step_module", "Phoenix Step Module",
            "Raises your step height so you can walk up tall blocks.", "iron_boots", "Boots",
            "Cannot be combined with GTM step height modules.");

    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_LORE_SCANNER_MODULE = moduleItem(
            "phoenix_lore_scanner_module", "Lore Scanner Module",
            "Study what you are looking at. Other Phoenix mods can react to scans to unlock lore.", "spyglass",
            "Helmet", "Aim and press the Lore Scan key (set in Controls).");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_MENDER_MODULE = moduleItem(
            "phoenix_mender_module", "Mender Aura Module",
            "Heals you and your Tesla teammates in range using network energy.", "golden_apple",
            "Chestplate", "Adjustable radius. Needs a Tesla network connection.");
    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_MAGNET_MODULE = moduleItem(
            "phoenix_magnet_module", "Magnet Module",
            "Pulls nearby dropped items to you.", "ender_eye", "Leggings", "Sneak to pause the magnet.");

    public static final ItemEntry<com.gregtechceu.gtceu.api.item.ComponentItem> PHOENIX_NAPHTHA_MODULE = moduleItem(
            "phoenix_naphtha_module", "Naphtha Module",
            "Burns naphtha from any fluid container in your inventory for speed, haste and jump. Highly flammable.",
            "blaze_powder", "Chestplate",
            "Only burns while sprinting (about 10 mB/s). Sets mobs you run into on fire.");

    static {
        // UI strings used by the module settings screens.
        String ns = "gui.phoenix_tesla_network.";
        // Shared tooltip line: registered once here, not per module item (duplicate keys crash).
        REGISTRATE.addRawLang("tooltip.phoenix_tesla_network.module.slot", "Installs on: %s");
        REGISTRATE.addRawLang(ns + "wing.mode", "Mode");
        REGISTRATE.addRawLang(ns + "wing.mode.0", "Basic");
        REGISTRATE.addRawLang(ns + "wing.mode.1", "Powered");
        REGISTRATE.addRawLang(ns + "wing.mode.2", "Creative");
        REGISTRATE.addRawLang(ns + "wing.mode.3", "Creative + Wings");
        REGISTRATE.addRawLang(ns + "cycle_hint", "Left click: next. Right click: previous.");
        REGISTRATE.addRawLang(ns + "wing.speed", "Flight speed");
        REGISTRATE.addRawLang(ns + "wing.drift", "Drift");
        REGISTRATE.addRawLang(ns + "wing.vertical", "Vertical speed");
        REGISTRATE.addRawLang(ns + "wing.keep_flying", "Keep flying on land");
        REGISTRATE.addRawLang(ns + "sprint.speed", "Sprint boost");
        REGISTRATE.addRawLang(ns + "jump.height", "Jump height");
        REGISTRATE.addRawLang(ns + "mender.radius", "Radius");
        REGISTRATE.addRawLang(ns + "magnet.radius", "Radius");
        REGISTRATE.addRawLang(ns + "naphtha.power", "Burn intensity");
    }

    public static ItemEntry<TeslaStabilizerItem> ULV_TESLA_STABILIZER = REGISTRATE
            .item("ulv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("ULV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "ULV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-low-voltage wireless power.\nBarely a spark, but it counts.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/ulv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> LV_TESLA_STABILIZER = REGISTRATE
            .item("lv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("LV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "LV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for low-voltage wireless power.\nIs this even useful?");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> MV_TESLA_STABILIZER = REGISTRATE
            .item("mv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("MV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "MV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for medium-voltage wireless power.\nYou probably won't use this.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/mv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> HV_TESLA_STABILIZER = REGISTRATE
            .item("hv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("HV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "HV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for high-voltage wireless power.\nOne could say, this is quite shocking!");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/hv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> EV_TESLA_STABILIZER = REGISTRATE
            .item("ev_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("EV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "EV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for extreme-voltage wireless power. \nRadical!");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/ev_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> IV_TESLA_STABILIZER = REGISTRATE
            .item("iv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("IV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "IV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for insane-voltage wireless power.\nCrazy? I was crazy once.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/iv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> LuV_TESLA_STABILIZER = REGISTRATE
            .item("luv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("LuV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "LuV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ludicrous-voltage wireless power.\nYour progress is quite impressive! Keep going!");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/luv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> ZPM_TESLA_STABILIZER = REGISTRATE
            .item("zpm_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("ZPM Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "ZPM Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for zero-point-module wireless power.\nWe getting sci-fi up in here.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/zpm_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UV_TESLA_STABILIZER = REGISTRATE
            .item("uv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultimate-voltage wireless power.\nIs this the ultimate? Not quite!");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/uv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UHV_TESLA_STABILIZER = REGISTRATE
            .item("uhv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UHV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UHV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-high-voltage wireless power.\nIs this the peak of power? Or merely the beginning?");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/uhv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UEV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("uev_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UEV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UEV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-excessive-voltage wireless power.\nThis is well past excessive.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/uev_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UIV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("uiv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UIV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UIV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-immense-voltage wireless power.\nThe cables are starting to glow.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/uiv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UXV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("uxv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UXV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UXV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-extreme-voltage wireless power.\nAt this point, the stabilizer is the only thing holding reality together.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/uxv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> OpV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("opv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("OpV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "OpV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for overpowered-voltage wireless power.\nThe name says it all.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/opv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> MAX_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("max_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("MAX Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "MAX Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for maximum-voltage wireless power.\nThere is nothing beyond this.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/max_tesla_stabilizer")))
            .register();

    public static void init() {}
}
