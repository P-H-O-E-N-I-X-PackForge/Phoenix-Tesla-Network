package net.phoenix_tesla_network.tesla.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("item.gtceu.tool.ev_screwdriver", "%s Electric Screwdriver (EV)");
        provider.add("item.gtceu.tool.mv_screwdriver", "%s Electric Screwdriver (MV)");
        provider.add("item.gtceu.tool.luv_screwdriver", "%s Electric Screwdriver (LuV)");
        provider.add("item.gtceu.tool.zpm_screwdriver", "%s Electric Screwdriver (ZPM)");
        provider.add("item.gtceu.tool.zpm_drill", "%s Drill (ZPM)");
        provider.add("item.gtceu.tool.luv_drill", "%s Drill (LuV)");
        provider.add("item.gtceu.tool.mv_wrench", "%s Wrench (MV)");
        provider.add("item.gtceu.tool.ev_wrench", "%s Wrench (EV)");
        provider.add("item.gtceu.tool.luv_wrench", "%s Wrench (LuV)");
        provider.add("item.gtceu.tool.zpm_wrench", "%s Wrench (ZPM)");
        provider.add("item.gtceu.tool.mv_buzzsaw", "%s Buzzsaw (MV)");
        provider.add("item.gtceu.tool.ev_buzzsaw", "%s Buzzsaw  (EV)");
        provider.add("item.gtceu.tool.luv_buzzsaw", "%s Buzzsaw  (LuV)");
        provider.add("item.gtceu.tool.zpm_buzzsaw", "%s Buzzsaw  (ZPM)");
        provider.add("item.gtceu.tool.mv_chainsaw", "%s Buzzsaw  (MV)");
        provider.add("item.gtceu.tool.ev_chainsaw", "%s Buzzsaw  (EV)");
        provider.add("item.gtceu.tool.luv_chainsaw", "%s Buzzsaw  (LuV)");
        provider.add("item.gtceu.tool.zpm_chainsaw", "%s Buzzsaw  (ZPM)");
        provider.add("item.gtceu.tool.mv_wirecutter", "%s Wire Cutters  (MV)");
        provider.add("item.gtceu.tool.ev_wirecutter", "%s Wire Cutters  (EV)");
        provider.add("item.gtceu.tool.luv_wirecutter", "%s Wire Cutters (LuV)");
        provider.add("item.gtceu.tool.zpm_wirecutter", "%s Wire Cutters  (ZPM)");

        provider.add("shield.phoenix_tesla_network.type.normal", "Normal");
        provider.add("shield.phoenix_tesla_network.type.inactive", "Inactive");
        provider.add("shield.phoenix_tesla_network.type.decayed", "Decayed");
        provider.add("shield.phoenix_tesla_network.current_shield", "Shield Status: %s");

        provider.add("tooltip.phoenix_tesla_network.crystal_rose.generic", "A crystalline flower of immense power.");
        provider.add("tooltip.phoenix_tesla_network.crystal_rose.made_from", "Forged from %s.");
        provider.add("tooltip.phoenix_tesla_network.nanites.generic", "Microscopic machines swarming with potential.");
        provider.add("tooltip.phoenix_tesla_network.nanites.made_from", "Constructed from %s.");

        provider.add("gtceu.top.recipe_output", "Predicted Output:");
        provider.add("item.phoenix_tesla_network.jade.thread_header", "Alchemical Thread #%s: %s%%");
        provider.add("item.phoenix_tesla_network.jade.threads_active", "Active Alchemical Threads:");
        provider.add("gtceu.gui.content.range", "%s - %s");
        provider.add("gtceu.gui.content.times_item", "x %s");

        provider.add("jade.phoenix_tesla_network.source_container", "Source Energy");

        provider.add("jade.phoenix_tesla_network.source_tank_header", "Source Tank Content");
        provider.add("jade.phoenix_tesla_network.source_tank_format", "Capacity %s / %s - %d%%");
        provider.add("config.jade.plugin_phoenix_tesla_network.source_tank_info", "Source Tank Info");

        provider.add("key.categories.phoenix_tesla_network", "phoenix_tesla_network");
        provider.add("key.phoenix_tesla_network.tesla_discharge", "Activate Tesla Discharge");
        provider.add("key.phoenix_tesla_network.lore_scan", "Lore Scan");
        provider.add("key.phoenix_tesla_network.suite_menu", "Phoenix Suite Modules");
        provider.add("tooltip.phoenix_tesla_network.suite.menu", "Press %s to configure suite modules");
        provider.add("gui.phoenix_tesla_network.suite.title", "Phoenix Tech Suite - Modules");
        provider.add("gui.phoenix_tesla_network.suite.select", "Select a piece of the suite");
        provider.add("gui.phoenix_tesla_network.suite.no_modules", "No modules installed");
        provider.add("key.phoenix_tesla_network.manipulator_menu", "Matter Manipulator Menu");

        provider.add("creativetab.phoenix_creative_tab", "Phoenix Core");
        provider.add("gui.phoenix_tesla_network.color_select.title", "Select Color");
        provider.add("behaviour.paintspray.chameleon.status.color", "§7Mode: §f%s");
        provider.add("behaviour.paintspray.chameleon.status.solvent", "§7Mode: §dSolvent");
        provider.add("behaviour.paintspray.chameleon.tooltip.current_color", "Current Color: %s");
        provider.add("behaviour.paintspray.chameleon.tooltip.solvent", "Current: Solvent");
        provider.add("behaviour.paintspray.chameleon.tooltip.info", "Scroll or use the Keybind to change color.");
        provider.add("behaviour.paintspray.solvent.short", "Solvent");

        provider.add("item.phoenix_tesla_network.tesla_binder.linked", "§aLinked to: §f%s");
        provider.add("item.phoenix_tesla_network.tesla_binder.unlinked", "§cNot Linked");
        provider.add("item.phoenix_tesla_network.tesla_binder.frequency", "§7Frequency: §b%s");

        multiLang(provider, "gtceu.placeholder_info.shieldStability",
                "Returns the stability of the shield.",
                "Note that not having a shield projected may result in nonsense values of integrity.",
                "Usage:",
                "  {shieldStability} -> shield integrity: (integrity, in percent)");
    }

    public static void multiLang(RegistrateLangProvider provider, String key, String... values) {
        for (var i = 0; i < values.length; i++) {
            provider.add(getSubKey(key, i), values[i]);
        }
    }

    protected static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        var lines = multiline.split("\n");
        multiLang(provider, key, lines);
    }

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }
}
