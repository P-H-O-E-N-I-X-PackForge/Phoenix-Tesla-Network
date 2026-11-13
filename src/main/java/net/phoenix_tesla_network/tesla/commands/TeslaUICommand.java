package net.phoenix_tesla_network.tesla.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeslaLang;

import com.mojang.brigadier.CommandDispatcher;

import java.util.UUID;

@Mod.EventBusSubscriber
public class TeslaUICommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("tesla_energy_debug")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> run(ctx.getSource())));
    }

    private static int run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

        source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.tesla_team_energy_data"), false);

        var networks = data.getNetworksView();

        if (networks.isEmpty()) {
            source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.no_networks_found"), false);
            return 1;
        }

        for (var entry : networks.entrySet()) {
            UUID team = entry.getKey();
            TeslaTeamEnergyData.TeamEnergy e = entry.getValue();

            boolean online = data.isOnline(team);
            var status = online ? TeslaLang.tr("phoenix_tesla_network.ui_command.status_online") :
                    TeslaLang.tr("phoenix_tesla_network.ui_command.status_offline");

            source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.team", team, status), false);
            source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.stored", e.stored), false);
            source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.capacity", e.capacity),
                    false);

            int hatchCount = e.energyBuffered.size();
            int soulLinkCount = e.soulLinkedMachines.size();

            source.sendSuccess(
                    () -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.physical_hatches", hatchCount), false);
            source.sendSuccess(
                    () -> TeslaLang.tr("phoenix_tesla_network.tesla_u_i_command.soul_linked_machines", soulLinkCount),
                    false);
        }

        return 1;
    }
}
