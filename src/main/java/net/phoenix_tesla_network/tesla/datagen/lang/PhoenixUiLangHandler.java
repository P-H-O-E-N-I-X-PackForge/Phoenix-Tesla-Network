package net.phoenix_tesla_network.tesla.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixUiLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("phoenix_tesla_network.client_coverage_data.blocks_from_the_nearest_source",
                " (%s blocks from the nearest source)");
        provider.add("phoenix_tesla_network.client_coverage_data.link_loss_here", "Link loss here: ");
        provider.add("phoenix_tesla_network.client_coverage_data.inside_network_coverage_lossless",
                "Inside network coverage - lossless");
        provider.add("phoenix_tesla_network.client_coverage_data.outside_network_coverage", "Outside network coverage");
        provider.add("phoenix_tesla_network.tesla_debug_command.active_chargers", "  Active Chargers:");
        provider.add("phoenix_tesla_network.tesla_debug_command.connected_hardware", "  Connected Hardware:");
        provider.add("phoenix_tesla_network.tesla_debug_command.live_heartbeats", "  Live Heartbeats: ");
        provider.add("phoenix_tesla_network.tesla_debug_command.offline", " [OFFLINE]");
        provider.add("phoenix_tesla_network.tesla_debug_command.online", " [ONLINE]");
        provider.add("phoenix_tesla_network.tesla_debug_command.network", "Network: ");
        provider.add("phoenix_tesla_network.tesla_debug_command.no_active_team_networks_found",
                "No active team networks found.");
        provider.add("phoenix_tesla_network.tesla_debug_command.no_machines_currently_registered_in",
                "  No machines currently registered in Global Map.");
        provider.add("phoenix_tesla_network.tesla_debug_command.map_missing", " [MAP MISSING]");
        provider.add("phoenix_tesla_network.tesla_debug_command.verified", " [VERIFIED]");
        provider.add("phoenix_tesla_network.tesla_debug_command.linked", "  [LINKED] ");
        provider.add("phoenix_tesla_network.tesla_debug_command.global_machine_registry", "Global Machine Registry:");
        provider.add("phoenix_tesla_network.tesla_debug_command.tesla_network_system_debug",
                "=== TESLA NETWORK SYSTEM DEBUG ===");
        provider.add("phoenix_tesla_network.tesla_u_i_command.soul_linked_machines", "  §dSoul-Linked Machines: §f%s");
        provider.add("phoenix_tesla_network.tesla_u_i_command.physical_hatches", "  §7Physical Hatches: §f%s");
        provider.add("phoenix_tesla_network.tesla_u_i_command.capacity", "  §7Capacity: §b%s");
        provider.add("phoenix_tesla_network.tesla_u_i_command.stored", "  §7Stored: §b%s");
        provider.add("phoenix_tesla_network.tesla_u_i_command.team", "§eTeam: §f%s (%s§f)");
        provider.add("phoenix_tesla_network.tesla_u_i_command.no_networks_found", "§cNo networks found.");
        provider.add("phoenix_tesla_network.tesla_u_i_command.tesla_team_energy_data",
                "§6=== Tesla Team Energy Data ===");
        provider.add("phoenix_tesla_network.phoenix_modules.discharge_failed_no_energy_available",
                "§c⚡ DISCHARGE FAILED: §7No energy available!");
        provider.add("phoenix_tesla_network.phoenix_modules.phoenix_rebirth_activated",
                "§6§l⚡ PHOENIX REBIRTH ACTIVATED ⚡");
        provider.add("phoenix_tesla_network.phoenix_modules.set_bonus_active", "Set bonus: active");
        provider.add("phoenix_tesla_network.phoenix_modules.spd_drift", "  spd %s drift %s");
        provider.add("phoenix_tesla_network.phoenix_modules.flight", "Flight: ");
        provider.add("phoenix_tesla_network.phoenix_modules.speed", "Speed: ");
        provider.add("phoenix_tesla_network.phoenix_modules.suit", "Suit: ");
        provider.add("phoenix_tesla_network.phoenix_modules.tesla", "Tesla: ");
        provider.add("phoenix_tesla_network.phoenix_modules.tesla_offline", "Tesla: offline");
        provider.add("phoenix_tesla_network.phoenix_suite_modules.smells_like_naphtha_floor_it",
                "§6Smells like naphtha. Floor it.");
        provider.add("phoenix_tesla_network.phoenix_suite_modules.the_naphtha_module_sputters_out",
                "§7The naphtha module sputters. Out of fuel.");
        provider.add("phoenix_tesla_network.phoenix_suite_modules.scanned", "§b✦ Scanned: ");
        provider.add("phoenix_tesla_network.phoenix_suite_modules.nothing_in_range", "§7✦ Nothing in range");
        provider.add("phoenix_tesla_network.phoenix_suite_modules.scan_failed_no_energy", "§c✦ Scan failed: no energy");
        provider.add("phoenix_tesla_network.tesla_binder_item.status", "\nStatus: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.internal_buffer", "\nInternal Buffer: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.location", "\nLocation: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.details", "%s Details");
        provider.add("phoenix_tesla_network.tesla_binder_item.highlight_device", "Highlight Device");
        provider.add("phoenix_tesla_network.tesla_binder_item.back", "< Back");
        provider.add("phoenix_tesla_network.tesla_binder_item.net_output", "\nNet Output: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.net_input", "\nNet Input: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.capacity", "\nCapacity: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.stored", "\nStored: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.tesla_network_management", "Tesla Network Management");
        provider.add("phoenix_tesla_network.tesla_binder_item.shift_right_click_to_bind",
                "Shift + Right-Click to bind to your frequency.");
        provider.add("phoenix_tesla_network.tesla_binder_item.currently_not_bound_to_a",
                "Currently not bound to a soul.");
        provider.add("phoenix_tesla_network.tesla_binder_item.bound_to_team", "Bound to Team: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.bound_to_player", "Bound to Player: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.right_click_to_open_the",
                "Right-Click to open the Tesla Network UI.");
        provider.add("phoenix_tesla_network.tesla_binder_item.tesla_binder_bound", "Tesla Binder (Bound)");
        provider.add("phoenix_tesla_network.tesla_binder_item.tesla_frequency_linked_to",
                "Tesla frequency linked to: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.invalid_target_machine_has_no",
                "Invalid Target: Machine has no internal soul-buffer.");
        provider.add("phoenix_tesla_network.tesla_binder_item.binder_is_not_initialized_shift",
                "Binder is not initialized. Shift-Right Click the air first.");
        provider.add("phoenix_tesla_network.tesla_binder_item.connection_severed_machine_removed_from",
                "Connection Severed: Machine removed from soul network.");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.charges_armor_and_tools_from",
                "Charges Armor and Tools from the Tesla Network");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.wherever_the_tesla_network_reaches",
                "Wherever the Tesla Network reaches");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.wireless_range", "Wireless Range: ");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.only_works_while_in_range",
                "Only works while in range from Tesla Tower's or other Range Extender's (chainable)");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.adds_a_radius_of_blocks",
                "Adds a radius of %s Blocks around itself");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.extends_the_range_of_your",
                "Extends the Range of your Tesla Network");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.allowed_tesla_batteries",
                "Allowed Tesla Batteries: ");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.not_allowed", "Not allowed");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.allowed", "Allowed");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.interdimensional_transfer",
                "Interdimensional Transfer: ");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.extendable_with_tesla_range_extenders",
                " (extendable with Tesla Range Extenders)");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.blocks", "%s Blocks");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.infinite", "Infinite");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.range", "Range: ");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.tesla_network_size_is_determined",
                "Tesla Network size is §7determined§f by the §7Tesla Battery§f Tier");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.tesla_network_is_shared_with",
                "Tesla Network is shared with your Team's other Tesla Towers");
        provider.add("phoenix_tesla_network.phoenix_tesla_machines.the_pulsating_heart_of_your",
                "The pulsating heart of your Tesla Network.");
        provider.add("phoenix_tesla_network.tesla_relay_machine.place_it_inside_the_range",
                "Place it inside the range of a Tesla Tower or another working extender.");
        provider.add("phoenix_tesla_network.tesla_relay_machine.blocks", "%s blocks");
        provider.add("phoenix_tesla_network.tesla_relay_machine.adds", "Adds: ");
        provider.add("phoenix_tesla_network.tesla_relay_machine.team", "Team: ");
        provider.add("phoenix_tesla_network.tesla_relay_machine.tesla_range_extender", "Tesla Range Extender: ");
        provider.add("phoenix_tesla_network.tesla_relay_machine.tesla_range_extender_inactive",
                "Tesla Range Extender: Inactive");
        provider.add("phoenix_tesla_network.tesla_relay_machine.range_extender_frequency_set_to",
                "Range extender frequency set to: %s");
        provider.add("phoenix_tesla_network.tesla_tower_type.tesla_batteries", "Tesla Batteries: %s - %s");
        provider.add("phoenix_tesla_network.tesla_tower_type.hatches", "Hatches: %s - %s");
        provider.add("phoenix_tesla_network.tesla_tower_type.block_range_extend_with_tesla",
                "%s block range%s (extend with Tesla Range Extenders)");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.link", "Link: ");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.unlinked", "Unlinked");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.buffered", "Buffered: ");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.direction", "Direction: ");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.tesla_hatch", "TESLA HATCH");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.tesla_hatch_already_synced_to",
                "Tesla Hatch: Already synced to this frequency.");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.tesla_hatch_connected_to_frequency",
                "Tesla Hatch: Connected to frequency %s...");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.output", "OUTPUT: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.determined_by_tesla_network",
                "Determined by Tesla Network");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.reach", "REACH: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.network", "NETWORK: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.unbound", "UNBOUND");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.status", "STATUS: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.tesla_field_generator",
                "Tesla Field Generator");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.charger_synchronized",
                "Charger Synchronized");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.charger_unbound", "Charger Unbound");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.tesla_field_disconnected",
                "Tesla Field Disconnected");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.tesla_field_connected",
                "Tesla Field Connected");
        provider.add("phoenix_tesla_network.tesla_network_provider.idle", "IDLE");
        provider.add("phoenix_tesla_network.tesla_network_provider.taking", "Taking: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.generating", "Generating: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.transmitting", "Transmitting: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.providing", "Providing: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.connections", "Connections: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.link_loss", "Link loss: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.out_of_network_range", "Out of network range");
        provider.add("phoenix_tesla_network.tesla_network_provider.range_extender", "Range Extender: ");
        provider.add("phoenix_tesla_network.tesla_network_provider.tesla_network", "Tesla Network: ");
        provider.add("phoenix_tesla_network.tesla_tower.vertices", "vertices");
        provider.add("phoenix_tesla_network.tesla_tower.draws", "draws");
        provider.add("phoenix_tesla_network.tesla_tower.click_to_skip", "click to skip");
        provider.add("phoenix_tesla_network.tesla_tower.tesla_network", "TESLA NETWORK");
        provider.add("phoenix_tesla_network.tesla_tower.online", "ONLINE");
        provider.add("phoenix_tesla_network.tesla_tower.offline", "OFFLINE");
        provider.add("phoenix_tesla_network.tesla_tower.stored", "STORED");
        provider.add("phoenix_tesla_network.tesla_tower.input", "Input");
        provider.add("phoenix_tesla_network.tesla_tower.output", "Output");
        provider.add("phoenix_tesla_network.tesla_tower.net", "Net");
        provider.add("phoenix_tesla_network.tesla_tower.battery_tier", "Battery tier  %s");
        provider.add("phoenix_tesla_network.tesla_tower.links_soul_linked", "Links  %s  (%s soul-linked)");
        provider.add("phoenix_tesla_network.tesla_tower.last_minute", "last minute");
        provider.add("phoenix_tesla_network.modules.discharge_hit_one", "§b⚡ DISCHARGE: §eHit %s target!");
        provider.add("phoenix_tesla_network.modules.discharge_hit_many", "§b⚡ DISCHARGE: §eHit %s targets!");
        provider.add("phoenix_tesla_network.modules.discharge_none", "§b⚡ DISCHARGE: §7No targets in range.");
        provider.add("phoenix_tesla_network.binder.synced_buffer",
                "Core Synchronized: Battery Buffer linked to soul frequency (charge only).");
        provider.add("phoenix_tesla_network.binder.synced_machine",
                "Core Synchronized: Machine linked to soul frequency.");
        provider.add("phoenix_tesla_network.ui_command.status_online", "§aONLINE");
        provider.add("phoenix_tesla_network.ui_command.status_offline", "§7OFFLINE");
    }
}
