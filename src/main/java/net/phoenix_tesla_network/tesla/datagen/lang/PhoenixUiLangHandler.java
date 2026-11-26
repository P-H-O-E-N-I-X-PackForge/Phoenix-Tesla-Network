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
        provider.add("phoenix_tesla_network.wing_flight_gui.mode", "Mode: ");
        provider.add("phoenix_tesla_network.wing_flight_screen.wing_flight_control", "Wing Flight Control");
        provider.add("phoenix_tesla_network.tesla_debug_command.active_chargers", "  Active Chargers:");
        provider.add("phoenix_tesla_network.tesla_debug_command.connected_hardware", "  Connected Hardware:");
        provider.add("phoenix_tesla_network.tesla_debug_command.off", " [OFF]");
        provider.add("phoenix_tesla_network.tesla_debug_command.working", " [WORKING]");
        provider.add("phoenix_tesla_network.tesla_debug_command.towers_range_extenders", "  Towers & Range Extenders:");
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
        provider.add("phoenix_tesla_network.phoenix_tech_suite.wireless_charging_active", "⚡ Wireless Charging Active");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.to_open_the_suite_settings",
                " to open the suite settings");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.press", "Press ");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.network_capacity", "Network Capacity: ");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.tesla_network_storage", "Tesla Network Storage: ");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.phoenix_rebirth_activated",
                "§6§l⚡ PHOENIX REBIRTH ACTIVATED ⚡");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.discharge_failed_no_energy_available",
                "§c⚡ DISCHARGE FAILED: §7No energy available!");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.syncing_network", "⚡ Syncing Network...");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.network_offline", "⚡ NETWORK: OFFLINE");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.rebirth_ready", "❤ REBIRTH: READY");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.rebirth_s", "☠ REBIRTH: %ss");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.wireless_charging", "ᗯ WIRELESS CHARGING");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.jump_20", "  » JUMP: %s/20");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.sprint_20", "  » SPRINT: %s/20");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.drift_10", "  » DRIFT: %s/10");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.vertical_20", "  » VERTICAL: %s/20");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.speed_20", "  » SPEED: %s/20");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.rebirth_failed_insufficient_network_ener",
                "§c⚡ REBIRTH FAILED: §7Insufficient network energy!");
        provider.add("phoenix_tesla_network.phoenix_tech_suite.rebirth_recharging_s", "§c⚡ REBIRTH RECHARGING: §e%ss");
        provider.add("phoenix_tesla_network.tesla_binder_item.net_output", "Net Output: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.net_input", "Net Input: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.capacity", "Capacity: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.highlight_in_world", "Highlight in-world");
        provider.add("phoenix_tesla_network.tesla_binder_item.status", "Status: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.internal_buffer", "Internal Buffer: ");
        provider.add("phoenix_tesla_network.tesla_binder_item.location", "Location: ");
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
        provider.add("phoenix_tesla_network.tesla_tower_machine.battery_tier", "Battery Tier: ");
        provider.add("phoenix_tesla_network.tesla_tower_machine.total_output", "Total Output: ");
        provider.add("phoenix_tesla_network.tesla_tower_machine.total_input", "Total Input: ");
        provider.add("phoenix_tesla_network.tesla_tower_machine.tower", "Tower: ");
        provider.add("phoenix_tesla_network.tesla_tower_machine.tesla_network", "Tesla Network: ");
        provider.add("phoenix_tesla_network.tesla_tower_machine.tesla_network_inactive", "Tesla Network: Inactive");
        provider.add("phoenix_tesla_network.tesla_tower_machine.tower_frequency_copied_to_binder",
                "Tower frequency copied to Binder.");
        provider.add("phoenix_tesla_network.tesla_tower_machine.tower_frequency_set_to", "Tower frequency set to: %s");
        provider.add("phoenix_tesla_network.tesla_tower_machine.the_signal_has_begun", "The Signal Has Begun.");
        provider.add("phoenix_tesla_network.tesla_tower_machine.we_see_you_we_know", "We See You, We Know You.");
        provider.add("phoenix_tesla_network.tesla_tower_type.tesla_batteries", "Tesla Batteries: %s - %s");
        provider.add("phoenix_tesla_network.tesla_tower_type.hatches", "Hatches: %s - %s");
        provider.add("phoenix_tesla_network.tesla_tower_type.block_range_extend_with_tesla",
                "%s block range%s (extend with Tesla Range Extenders)");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.tesla_hatch_already_synced_to",
                "Tesla Hatch: Already synced to this frequency.");
        provider.add("phoenix_tesla_network.tesla_energy_hatch_part_machine.tesla_hatch_connected_to_frequency",
                "Tesla Hatch: Connected to frequency %s...");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.tesla_field_generator",
                "Tesla Field Generator");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.output", "OUTPUT: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.determined_by_tesla_network",
                "Determined by Tesla Network");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.reach", "REACH: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.network", "NETWORK: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.unbound", "UNBOUND");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.status", "STATUS: ");
        provider.add("phoenix_tesla_network.tesla_wireless_charger_machine.wireless_charger", "%s Wireless Charger");
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
        provider.add("phoenix_tesla_network.wing_flight_gui.wing_flight_control", "§5Wing Flight Control");
        provider.add("phoenix_tesla_network.wing_flight_gui.powered", "⚡ Powered");
        provider.add("phoenix_tesla_network.wing_flight_gui.creative", "✦ Creative");
        provider.add("phoenix_tesla_network.wing_flight_gui.drain_5_000_eu_t", "§7Drain: §c5,000 EU/t");
        provider.add("phoenix_tesla_network.wing_flight_gui.drain_none_creative", "§7Drain: §aNone (Creative)");
        provider.add("phoenix_tesla_network.wing_flight_gui.switch_to_creative_flight",
                "§eSwitch to ✦ Creative Flight");
        provider.add("phoenix_tesla_network.wing_flight_gui.switch_to_powered_flight", "§6Switch to ⚡ Powered Flight");
        provider.add("phoenix_tesla_network.wing_flight_gui.flight_speed", "§7Flight Speed");
        provider.add("phoenix_tesla_network.wing_flight_gui.flight_drift", "§7Flight Drift");
        provider.add("phoenix_tesla_network.wing_flight_gui.press_numpad9_or_esc_to",
                "§8Press Numpad9 or Esc to close");
        provider.add("phoenix_tesla_network.wing_flight.current", "Current: %s");
        provider.add("phoenix_tesla_network.wing_flight.press_esc_to_save", "Press ESC to save");
        provider.add("phoenix_tesla_network.wing_flight.flight_speed", "Flight Speed");
        provider.add("phoenix_tesla_network.wing_flight.flight_vertical_speed", "Flight Vertical Speed");
        provider.add("phoenix_tesla_network.wing_flight.flight_drift", "Flight Drift");
        provider.add("phoenix_tesla_network.wing_flight.landing_behavior", "Landing Behavior");
        provider.add("phoenix_tesla_network.wing_flight.sprint_speed", "Sprint Speed");
        provider.add("phoenix_tesla_network.wing_flight.jump_boost_0_off", "Jump Boost  (0 = off)");
        provider.add("phoenix_tesla_network.wing_flight.step_height_0_vanilla", "Step Height  (0 = vanilla)");
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
