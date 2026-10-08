package net.phoenix_tesla_network.tesla.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixMachineLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("phoenix_tesla_network.soul_lens.tooltip.flavor", "The Veil is thinner than you realize.");
        provider.add("phoenix_tesla_network.soul_lens.tooltip.1", "Your way of checking on the Soul of the World.");
        provider.add("gtceu.bio_engine", "Bio Aetheric Engine");

        provider.add("emi_info.phoenix_tesla_network.required_shield", "Required Shield: %s");
        provider.add("emi_info.phoenix_tesla_network.shield_heal", "Shield Health Restored: +%s");
        provider.add("emi_info.phoenix_tesla_network.shield_damage", "Shield Damage Applied: -%s");

        provider.add("tooltip.phoenix_tesla_network.tesla_hatch.laser_input",
                "§bOptical Collimator§r: Concentrates energy into a coherent Tesla-Laser beam.");
        provider.add("tooltip.phoenix_tesla_network.tesla_hatch.laser_output",
                "§bPhotonic Receptor§r: Decodes high-frequency laser flux back into EU.");
        provider.add("tooltip.phoenix_tesla_network.tesla_hatch.input",
                "§bWireless Transmitter§r: Siphons energy into the Tesla Cloud.");
        provider.add("tooltip.phoenix_tesla_network.tesla_hatch.output",
                "§bWireless Receiver§r: Broadcasts energy from the Tesla Cloud.");
        provider.add("tooltip.phoenix_tesla_network.tesla_hatch.lore", "§6Nevvonian Core Tech: Frequency Locked.");

        provider.add("tech.phoenix_tesla_network.laser.input.low", "Tesla Optical Collimator");
        provider.add("tech.phoenix_tesla_network.laser.input.mid", "Tesla Optical Collimation Grid");
        provider.add("tech.phoenix_tesla_network.laser.input.high", "Tesla Phased Beam Matrix");

        provider.add("tech.phoenix_tesla_network.laser.output.low", "Tesla Photonic Coalescer");
        provider.add("tech.phoenix_tesla_network.laser.output.mid", "Tesla Photonic Coalescence Array");
        provider.add("tech.phoenix_tesla_network.laser.output.high", "Tesla Photonic Coalescence Matrix");

        provider.add("gui.phoenix_tesla_network.heat_exchanger.heat_exchange_surface", "Exchange Columns: %d");
        provider.add("gui.phoenix_tesla_network.heat_exchanger.current_efficiency", "Thermal Conductivity: Tier %d");
        provider.add("gui.phoenix_tesla_network.missing_spring", "Missing Heat Exchange Spring!");

        provider.add("gui.phoenix_tesla_network.source_hatch.label.import", "Source Input Hatch");
        provider.add("gui.phoenix_tesla_network.source_hatch.label.export", "Source Output Hatch");
        provider.add("gui.phoenix_tesla_network.source_hatch.source", "Source Stored: %s");
        provider.add("phoenix.core.recipe.source_in", "Source Consumed: %s.");
        provider.add("phoenix.core.recipe.source_out", "Source Yield: %s.");
        provider.add("tooltip.phoenix_tesla_network.source_hatch.consumption", "§cMax Source Consumption:§d %s");
        provider.add("tooltip.phoenix_tesla_network.source_hatch.capacity", "§cMax Source Capacity:§d %s");
        provider.add("recipe.capability.source.name", "Source");

        provider.add("phoenix_tesla_network.not_formed", "Structure not formed!");
        provider.add("phoenix_tesla_network.status.safe_idle", "Status: §aIDLE");
        provider.add("phoenix_tesla_network.status.safe_working", "Status: §6ACTIVE");
        provider.add("phoenix_tesla_network.status.danger_timer", "§cCRITICAL: Meltdown in %s seconds!");
        provider.add("phoenix_tesla_network.status.no_coolant", "§eWARNING: Coolant Supply Exhausted");
        provider.add("phoenix_tesla_network.nuke_radius", "Blast area: %s");

        provider.add("phoenix_tesla_network.blanket.input", "Breeding Target");
        provider.add("phoenix_tesla_network.blanket.potential_outputs", "Potential Transmutations:");
        provider.add("phoenix_tesla_network.blanket.bias_hint",
                "§d§oHigher instability yields are favored by a Fast Neutron Spectrum (High Heat/Bias).");
        provider.add("phoenix_tesla_network.blanket_outputs", "§7Possible Products:");
        provider.add("phoenix_tesla_network.blanket_input", "§7Target Material: §f%s");
        provider.add("phoenix_tesla_network.blanket_output", "§7Breeding Product: §f%s");
        provider.add("phoenix_tesla_network.blanket_desc",
                "Irradiate target materials to produce specialized isotopes.");
        provider.add("phoenix_tesla_network.blanket_cycle", "Transmutes §f%s§7 units every §6%s§7 seconds");

        provider.add("phoenix_tesla_network.neutron_bias", "§7Neutron Bias: §f%s");
        provider.add("phoenix_tesla_network.spectrum_shift", "§7Spectrum Shift: §f%s");
        provider.add("phoenix_tesla_network.current_heat", "Core Temperature: %s HU");
        provider.add("phoenix_tesla_network.net_heat", "Net Heat Change: %s HU/t");
        provider.add("phoenix_tesla_network.heat_production", "Heat Production: %s");
        provider.add("phoenix_tesla_network.eu_generation", "Output: %s EU/t");
        provider.add("phoenix_tesla_network.parallels", "Parallel Processing: %sx");

        provider.add("phoenix_tesla_network.moderator", "Primary Moderator: %s");
        provider.add("phoenix_tesla_network.moderator_fuel_discount", "Fuel Efficiency: +%s%%");
        provider.add("phoenix_tesla_network.cooler", "Primary Cooling: %s");
        provider.add("phoenix_tesla_network.coolant", "Coolant: %s");
        provider.add("phoenix_tesla_network.coolant_rate", "Coolant Flow: %s mb/t");
        provider.add("phoenix_tesla_network.coolant_output", "Hot Coolant Produced: %s");
        provider.add("phoenix_tesla_network.coolant_status.ok", "§aCoolant Supply OK");
        provider.add("phoenix_tesla_network.coolant_status.empty", "§cCoolant Supply Depleted");
        provider.add("phoenix_tesla_network.summary", "Cooling: %s / %s HU/t");
        provider.add("phoenix_tesla_network.cooling_power", "§bCooling Capacity: §f%s HU/t");

        provider.add("phoenix_tesla_network.fuel_cycle", "Consumes §f%s§7 units every §6%s§7 seconds");
        provider.add("phoenix_tesla_network.depleted_fuel", "§7Depleted Fuel: §f%s");
        provider.add("phoenix_tesla_network.fuel_usage", "Fuel Consumption: §f%s");
        provider.add("phoenix_tesla_network.fuel_required", "§7Requires Fuel: §f%s");
        provider.add("phoenix_tesla_network.coolant_required", "§3Required Coolant: §f%s");

        provider.add("block.phoenix_tesla_network.fission_cooler.capacity", "§bCooling Capacity: §f%s HU/t");
        provider.add("block.phoenix_tesla_network.fission_cooler.required_coolant", "§3Required Coolant: §f%s");
        provider.add("block.phoenix_tesla_network.fission_moderator.multiplier", "§6Heat Multiplier: §f%sx");
        provider.add("block.phoenix_tesla_network.fission_moderator.parallel", "§aParallel Bonus: §f+%s");
        provider.add("block.phoenix_tesla_network.fission_moderator.shift", "Hold Shift for details");
        provider.add("block.phoenix_tesla_network.fission_moderator.info_header", "Fission Moderator");
        provider.add("block.phoenix_tesla_network.fission_moderator.boost", "EU Boost: %s");
        provider.add("block.phoenix_tesla_network.fission_moderator.fuel_discount", "Fuel Discount: %s");

        provider.add("phoenix_tesla_network.current_heat_display", "Core Temperature: %s / %s HU");
        provider.add("phoenix_tesla_network.status.scram", "§c§lSCRAM ACTIVE");

        provider.add("block.phoenix_tesla_network.fission_scram_hatch.desc",
                "Stops fuel consumption and heat generation when receiving a Redstone signal.");

        provider.add("phoenix_tesla_network.machine.fission_scram_hatch.tooltip",
                "§cEmergency Reactor Brake§r: Halts the reactor on §fany§r redstone signal.");
        provider.add("phoenix_tesla_network.machine.fission_scram_hatch.tooltip2",
                "§8No configuration. No mercy. Build your circuit carefully.");

        provider.add("phoenix_tesla_network.machine.fission_advanced_scram_hatch.tooltip",
                "§6Precision SCRAM Control§r: Triggers only above a configured signal strength,");
        provider.add("phoenix_tesla_network.machine.fission_advanced_scram_hatch.tooltip2",
                "§7and only after a sustained signal. Configurable via UI.");

        provider.add("phoenix_tesla_network.machine.fission_stability_sensor.tooltip",
                "§eThermal Monitor§r: Emits a §fproportional§r redstone signal based on core heat.");

        provider.add("gui.phoenix_tesla_network.stability_sensor.title", "Thermal Stability Configuration");
        provider.add("gui.phoenix_tesla_network.stability_sensor.min", "Min Heat Threshold %");
        provider.add("gui.phoenix_tesla_network.stability_sensor.max", "Max Heat Threshold %");
        provider.add("gui.phoenix_tesla_network.stability_sensor.invert", "Invert Signal");

        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.title",
                "Advanced Thermal Stability Configuration");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.min", "Min Heat Threshold %");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.max", "Max Heat Threshold %");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.strength", "Emit Strength (1–15)");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.invert", "Invert Signal");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.hint1",
                "Emits fixed strength on back face only.");
        provider.add("gui.phoenix_tesla_network.advanced_stability_sensor.hint2", "Pair with an Advanced SCRAM Hatch.");

        provider.add("phoenix_tesla_network.status.scram_basic", "§c§lSCRAM ACTIVE §8(Basic Hatch)");
        provider.add("phoenix_tesla_network.status.scram_advanced", "§6§lSCRAM ACTIVE §8(Advanced Hatch)");
        provider.add("phoenix_tesla_network.status.scram_arming", "§e§lSCRAM ARMING: §f%d / %d ticks");

        provider.add("phoenix.multiblock.pattern.info.multiple_fuel_rods",
                "Requires Fuel Rods. These generate base heat and determine recipe parallels.");
        provider.add("phoenix.multiblock.pattern.info.multiple_blankets",
                "Requires Blanket Rods. These act as targets for transmutation in Breeder cycles.");
        provider.add("phoenix.multiblock.pattern.info.multiple_moderators",
                "Moderators adjust heat generation and can provide EU or Parallel bonuses.");
        provider.add("phoenix.multiblock.pattern.info.multiple_coolers",
                "Coolers remove heat based on their tier and provided coolant fluid.");

        provider.add("gtceu.high_performance_breeder_reactor",
                "High-Performance Breeder Reactor");
        provider.add("gtceu.heat_exchanging",
                "Heat Exchanging");
        provider.add("gtceu.source_extraction",
                "Source Extraction");
        provider.add("gtceu.source_imbuement",
                "Source Imbuement");
        provider.add("gtceu.source_reactor",
                "Source Reactor");
        provider.add("gtceu.advanced_pressurized_fission_reactor",
                "Advanced Pressurized Fission Reactor");
        provider.add("gtceu.pressurized_fission_reactor", "Pressurized Fission Reactor");

        provider.add("gtceu.honey_chamber", "Honey Chamber");
        provider.add("gtceu.please", "Please Multiblock");
        provider.add("gtceu.simulated_colony", "Simulated Colony");
        provider.add("gtceu.comb_decanting", "Comb Decanter");
        provider.add("gtceu.swarm_nurturing", "Swarm Nurturing Chamber");
        provider.add("gtceu.apis_progenitor", "Apis Progenitor");

        provider.add("gtceu.tooltip.tier", "Tier: %s");

        provider.add("config.jade.plugin_phoenix_tesla_network.source_machine_info", "Source Machine Information");
        provider.add("config.jade.plugin_phoenix_tesla_network.plasma_furnace_info",
                "High-Pressure Plasma Arc Furnace Info");
        provider.add("config.jade.plugin_phoenix_tesla_network.tesla_network_info", "Tesla Network Information");
        provider.add("config.jade.plugin_phoenix_tesla_network.fission_machine_info", "Fission Machine Info");

        provider.add("jade.phoenix_tesla_network.shield_state", "Shield State: %s");
        provider.add("jade.phoenix_tesla_network.shield_health", "Shield Health: %d");
        provider.add("jade.phoenix_tesla_network.shield_cooldown", "Shield Recharging: %ds");
        provider.add("jade.phoenix_tesla_network.plasma_boost_duration", "Power Multiplier: %s");

        provider.add("jade.phoenix_tesla_network.plasma_boost_active", "Plasma Boost: %s Active");
        provider.add("jade.phoenix_tesla_network.no_plasma_boost", "No Plasma Catalyst");
        provider.add("jade.phoenix_tesla_network.tesla_stored", "Stored: ");
        provider.add("jade.phoenix_tesla_network.tesla_receiving", "Receiving: %s EU/t");
        provider.add("jade.phoenix_tesla_network.tesla_providing", "Providing: %s EU/t");
        provider.add("block.phoenix_tesla_network.tesla_battery.tooltip_empty",
                "§7A hollow casing. Provides no storage.");
        provider.add("block.phoenix_tesla_network.tesla_battery.tooltip_filled", "§aCapacity: §f%s EU");
        provider.add("config.jade.plugin_phoenix_tesla_network.imbuer_threads_info", "Alchemical Imbuer Threads Info");

        provider.add("jade.phoenix_tesla_network.blanket_input", "Blanket Fuel: %s");
        provider.add("jade.phoenix_tesla_network.blanket_output", "Breeding Product: %s");
        provider.add("jade.phoenix_tesla_network.blanket_amount", "Base per cycle: %s");
        provider.add("jade.phoenix_tesla_network.heat", "§cCore Heat: %s HU");
        provider.add("jade.phoenix_tesla_network.fission_meltdown_timer", "§6MELTDOWN: %s seconds!");
        provider.add("jade.phoenix_tesla_network.fission_safe", "§aCore Stable");
        provider.add("jade.phoenix_tesla_network.fission_no_coolant", "§cNO COOLANT DETECTED");
        provider.add("jade.phoenix_tesla_network.fission_heating", "§eCORE HEATING UP");

        provider.add("jade.phoenix_tesla_network.source_giving", "Producing Source");
        provider.add("jade.phoenix_tesla_network.source_taking", "Consuming Source");
        provider.add("jade.phoenix_tesla_network.source_consumption", "Source Consumption:");
        provider.add("jade.phoenix_tesla_network.source_production", "Source Production:");

        provider.add("multiblock.tooltip.machinetype", "Machine Type: %s");
        provider.add("multiblock.yellowline", "§e━━━━━━━━━━━━━━━━━━━━");
        provider.add("multiblock.underyellowline", "Hold §e§lSHIFT§r to display structure details!");
        provider.add("multiblock.structureadvtooltip", "Structure:");

        provider.add("multiblock.pchaccess1", "\u00A0\u00A0\u00A0§9Parallel Control Hatch: ✓");
        provider.add("multiblock.pchaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock can use PCHs to increase it's efficiency.");
        provider.add("multiblock.subtickaccess1", "\u00A0\u00A0\u00A0§3SubTick: ✓");
        provider.add("multiblock.subtickaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock performs subtick recipes!");
        provider.add("multiblock.perfocaccess1", "\u00A0\u00A0\u00A0§dPerfect OCs: ✓");
        provider.add("multiblock.perfocaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock supports perfect overclocks (4/4).");
        provider.add("multiblock.nooc1", "\u00A0\u00A0\u00A0§cOverclocks: X");
        provider.add("multiblock.nooc2", "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock can not overclock.");

        provider.add("multiblock.laseraccess1", "\u00A0\u00A0\u00A0§6Laser Target Access: ✓");
        provider.add("multiblock.laseraccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock can be powered with Laser Target Hatches.");
        provider.add("multiblock.needlaseraccess1", "\u00A0\u00A0\u00A0§6Laser Target Access: ✓");
        provider.add("multiblock.needlaseraccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock MUST be powered with Laser Target Hatches.");
        provider.add("multiblock.nopower1", "\u00A0\u00A0\u00A0§cEnergy Output: X");
        provider.add("multiblock.nopower2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock does NOT produce/use §3Energy§7.");
        provider.add("multiblock.energyoutputaccess1", "\u00A0\u00A0\u00A0§3Energy Output: ✓");
        provider.add("multiblock.energyoutputaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock provides §3Energy§7 output!");

        provider.add("multiblock.sourceoutputaccess1", "\u00A0\u00A0\u00A0§zSource Output: §3✓");
        provider.add("multiblock.sourceoutputaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock provides §zSource§7 output!");
        provider.add("multiblock.sourceinputaccess1", "\u00A0\u00A0\u00A0§zSource Input: §3✓");
        provider.add("multiblock.sourceinputaccess2",
                "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0§7This multiblock requires §zSource §7input!");

        provider.add("multiblock.tooltip.controller", "\u00A0\u00A0\u00A0Controller: %s");
        provider.add("multiblock.tooltip.iteminput", "\u00A0\u00A0\u00A0Input Bus: %s");
        provider.add("multiblock.tooltip.fluidinput", "\u00A0\u00A0\u00A0Input Hatch: %s");
        provider.add("multiblock.tooltip.itemoutput", "\u00A0\u00A0\u00A0Output Bus: %s");
        provider.add("multiblock.tooltip.fluidoutput", "\u00A0\u00A0\u00A0Output Hatch: %s");
        provider.add("multiblock.tooltip.energy", "\u00A0\u00A0\u00A0Energy Input: %s");
        provider.add("multiblock.tooltip.energyoutput", "\u00A0\u00A0\u00A0Energy Output: %s");
        provider.add("multiblock.tooltip.maintenance", "\u00A0\u00A0\u00A0Maintenance Hatch: %s");
        provider.add("multiblock.tooltip.muffler", "\u00A0\u00A0\u00A0Muffler Hatch: %s");
        provider.add("multiblock.tooltip.pch", "\u00A0\u00A0\u00A0Parallel Control Hatch: %s");

        provider.add("gtultimate.custom.tooltip_one_energy_hatch", "§fAccepts §lEXACTLY §61 energy hatch.");
        provider.add("gtultimate.custom.tooltip_dimensional_anchor",
                "§9§oOpens stable rifts to other dimensions, determined by its placement.\\n§7These gateways allow for accelerated resource extraction unique to each realm.\\n§7Requires distinct recipes for Overworld, Nether, and End configurations.");

        String anchorDesc = "§9§oOpens stable rifts to other dimensions, determined by its placement.\n" +
                "§7These gateways allow for accelerated resource extraction unique to each realm.\n" +
                "§7Requires distinct recipes for Overworld, Nether, and End configurations.";
        String fabricatorDesc = "§d§oUtilizes raw aetherial energy to create complex constructs.\n§5Transmutes pure magical essence into tangible matter.";
        String alchemicalImbuerDesc = "§7The hub of your §zSource network§7, acting as a link for natural §zsoul§7.\n" +
                "§7Handles the §rextraction§7 of §zsource§r from §rcarbon§7 based sources as well as imbuing §zsource§r into §runique materials.\n" +
                "§7Source §rproduction and consumption§7 is decided by the current base §zsoul, §rflora§7, and §rharmonization§7 in your area.";
        String sourceReactorDesc = "§7A §zSource§7 based reactor capable of converting §fmundane materials§7 for use in further §zSource§7 related processes.\n" +
                "Reactor ability is handled by the current §zsoul§7 of your area. \n" +
                "§zSource gem §7blocks can also be used near the reactor to provide further boost. \n" +
                "Reactor will §cNOT RUN§7 below a §zsoul§7 cap of 1.";
        String bioEngineDesc = "§7An §zEngine§7 capable of converting §zSource§7 into power.\n" +
                "§7It's power flows throughout it's chassis §zdenoting it's strength.\n" +
                "§fEU provided §7is dependent on the base §zsoul§7 and §aflora §7power in your area.\n" +
                "§7Caps out at a §r5x boost.";

        String largeSteamSifterDesc = "§bSifts through the chaff to get to the good stuff. \n" +
                "§7Good Vibrations.";

        ;

        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_large_steam_sifter",
                largeSteamSifterDesc);
        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_dimensional_anchor", anchorDesc);
        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_aetherial_fabricator", fabricatorDesc);
        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_alchemical_imbuer", alchemicalImbuerDesc);
        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_source_reactor", sourceReactorDesc);
        PhoenixLangHandler.multilineLang(provider, "gtultimate.custom.tooltip_bio_engine", bioEngineDesc);

        PhoenixLangHandler.multiLang(provider, "tooltip.phoenix_tesla_network.shield_stability_hatch",
                "Outputs shield stability",
                "as a redstone signal.");

        provider.add("phoenix_tesla_network.machine.multiblock.source_tank.tooltip",
                "Fill and drain through the controller or source hatches.");
        provider.add("phoenix_tesla_network.universal.tooltip.source_storage_capacity", "§zSource §9Capacity: §f%d mB");
    }
}
