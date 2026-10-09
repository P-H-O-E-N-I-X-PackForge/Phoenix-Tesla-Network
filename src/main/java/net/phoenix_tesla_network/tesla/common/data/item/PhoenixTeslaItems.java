package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.Tags;

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

    public static ItemEntry<ArmorComponentItem> PHOENIX_HELMET = REGISTRATE
            .item("phoenix_helmet", (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.HELMET, p)
                    .setArmorLogic(new PhoenixTechSuite(ArmorItem.Type.HELMET,
                            16384,
                            500_000_000L,
                            8)))
            .lang("Phoenix Tech Suite Helmet")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(Tags.Items.ARMORS_HELMETS)
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<PhoenixArmorItem> PHOENIX_CHESTPLATE = REGISTRATE
            .item("phoenix_chestplate",
                    (p) -> new PhoenixArmorItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, p,
                            new PhoenixTechSuite(ArmorItem.Type.CHESTPLATE, 16384, 500_000_000L, 6)))
            .lang("Phoenix Tech Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(Tags.Items.ARMORS_CHESTPLATES)
            .tag(ItemTags.FREEZE_IMMUNE_WEARABLES)
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<ArmorComponentItem> PHOENIX_LEGGINGS = REGISTRATE
            .item("phoenix_leggings", (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.LEGGINGS, p)
                    .setArmorLogic(new PhoenixTechSuite(ArmorItem.Type.LEGGINGS,
                            16384,
                            500_000_000L,
                            6)))
            .lang("Phoenix Tech Suite Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(Tags.Items.ARMORS_LEGGINGS)
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<ArmorComponentItem> PHOENIX_BOOTS = REGISTRATE
            .item("phoenix_boots", (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.BOOTS, p)
                    .setArmorLogic(new PhoenixTechSuite(ArmorItem.Type.BOOTS,
                            16384,
                            500_000_000L,
                            6)))
            .lang("Phoenix Tech Suite Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(Tags.Items.ARMORS_BOOTS)
            .tag(CustomTags.PPE_ARMOR)
            .tag(CustomTags.STEP_BOOTS)
            .register();

    public static ItemEntry<TeslaStabilizerItem> ULV_TESLA_STABILIZER = REGISTRATE
            .item("ulv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("ULV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "ULV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-low-voltage wireless power.\nBarely a spark, but it counts.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
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
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UIV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("uiv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UIV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UIV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-immense-voltage wireless power.\nThe cables are starting to glow.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> UXV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("uxv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("UXV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "UXV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for ultra-extreme-voltage wireless power.\nAt this point, the stabilizer is the only thing holding reality together.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> OpV_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("opv_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("OpV Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "OpV Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for overpowered-voltage wireless power.\nThe name says it all.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static ItemEntry<TeslaStabilizerItem> MAX_TESLA_STABILIZER = !GTCEuAPI.isHighTier() ? null : REGISTRATE
            .item("max_tesla_stabilizer", TeslaStabilizerItem::new)
            .lang("MAX Tesla Stabilizer")
            .setData(ProviderType.LANG, (ctx, prov) -> {
                prov.add(ctx.get(), "MAX Tesla Stabilizer");
                prov.add(ctx.get().getDescriptionId() + ".tooltip",
                        "A stabilizing unit for maximum-voltage wireless power.\nThere is nothing beyond this.");
            })
            .model((ctx, prov) -> prov.generated(ctx, prov.modLoc("item/tesla_stabilizer/lv_tesla_stabilizer")))
            .register();

    public static void init() {}
}
