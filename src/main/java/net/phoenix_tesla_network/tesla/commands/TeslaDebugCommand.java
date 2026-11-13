package net.phoenix_tesla_network.tesla.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;
import net.phoenix_tesla_network.tesla.utils.TeslaLang;

import com.mojang.brigadier.CommandDispatcher;

import java.util.UUID;

@Mod.EventBusSubscriber
public class TeslaDebugCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("tesla_debug")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> run(ctx.getSource())));
    }

    private static int run(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

        source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.tesla_network_system_debug")
                .withStyle(ChatFormatting.GOLD,
                        ChatFormatting.BOLD),
                false);

        var networks = data.getNetworksView();

        source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.global_machine_registry")
                .withStyle(ChatFormatting.YELLOW,
                        ChatFormatting.UNDERLINE),
                false);

        int globalCount = 0;
        for (var entry : networks.entrySet()) {
            UUID teamId = entry.getKey();
            TeslaTeamEnergyData.TeamEnergy teamData = entry.getValue();

            for (BlockPos pos : teamData.soulLinkedMachines) {
                UUID lookupTeam = data.getOwnerTeam(pos);
                boolean verified = teamId.equals(lookupTeam);

                source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.linked")
                        .append(Component.literal(pos.toShortString()).withStyle(ChatFormatting.WHITE))
                        .append(" -> Team: ")
                        .append(Component.literal(TeamUtils.getTeamName(teamId)).withStyle(ChatFormatting.AQUA))
                        .append(verified ?
                                TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.verified")
                                        .withStyle(ChatFormatting.GREEN) :
                                TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.map_missing")
                                        .withStyle(ChatFormatting.RED)),
                        false);
                globalCount++;
            }
        }

        if (globalCount == 0) {
            source.sendSuccess(
                    () -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.no_machines_currently_registered_in")
                            .withStyle(ChatFormatting.DARK_GRAY),
                    false);
        }

        source.sendSuccess(() -> Component.literal("--------------------------------"), false);

        if (networks.isEmpty()) {
            source.sendSuccess(
                    () -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.no_active_team_networks_found")
                            .withStyle(ChatFormatting.RED),
                    false);
            return 1;
        }

        for (var entry : networks.entrySet()) {
            UUID team = entry.getKey();
            TeslaTeamEnergyData.TeamEnergy teamData = entry.getValue();
            boolean online = data.isOnline(team);

            MutableComponent teamHeader = TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.network")
                    .append(Component.literal(TeamUtils.getTeamName(team)).withStyle(ChatFormatting.AQUA))
                    .append(online ?
                            TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.online")
                                    .withStyle(ChatFormatting.GREEN) :
                            TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.offline")
                                    .withStyle(ChatFormatting.RED));
            source.sendSuccess(() -> teamHeader, false);

            source.sendSuccess(() -> Component.literal("  EU: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(teamData.stored.toString()).withStyle(ChatFormatting.YELLOW))
                    .append(" / ")
                    .append(Component.literal(teamData.capacity.toString()).withStyle(ChatFormatting.GOLD)), false);

            long now = level.getGameTime();
            int liveCount = 0;
            for (long lastSeen : teamData.lastSeen.values()) {
                if (now - lastSeen < 40) liveCount++;
            }
            final int finalLiveCount = liveCount;
            source.sendSuccess(
                    () -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.live_heartbeats")
                            .withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(String.valueOf(finalLiveCount))
                                    .withStyle(ChatFormatting.LIGHT_PURPLE)),
                    false);

            if (!teamData.rangeNodes.isEmpty()) {
                source.sendSuccess(() -> TeslaLang
                        .tr("phoenix_tesla_network.tesla_debug_command.towers_range_extenders").withStyle(
                                ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        false);
                for (var node : teamData.rangeNodes.values()) {
                    source.sendSuccess(() -> Component.literal("    - ")
                            .append(Component.literal(node.type.name()).withStyle(ChatFormatting.AQUA))
                            .append(" " + node.pos.toShortString())
                            .append(Component.literal(" (" + node.dimension.location().getPath() + ")")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                            .append(node.working ?
                                    TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.working")
                                            .withStyle(ChatFormatting.GREEN) :
                                    TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.off")
                                            .withStyle(ChatFormatting.RED)),
                            false);
                }
            }

            var hatches = data.getHatches(team);
            if (!hatches.isEmpty()) {
                source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.connected_hardware")
                        .withStyle(ChatFormatting.GRAY,
                                ChatFormatting.ITALIC),
                        false);
                for (var info : hatches) {
                    String typePrefix = info.isSoulLinked ? "[S]" : (info.isPhysicalOutput ? "[I]" : "[O]");
                    ChatFormatting typeColor = info.isSoulLinked ? ChatFormatting.LIGHT_PURPLE :
                            (info.isPhysicalOutput ? ChatFormatting.GREEN : ChatFormatting.RED);

                    source.sendSuccess(() -> Component.literal("    - ")
                            .append(Component.literal(typePrefix).withStyle(typeColor))
                            .append(" " + info.pos.toShortString())
                            .append(Component.literal(" (" + info.dimension.location().getPath() + ")")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                            .append(" Flow: " + info.displayFlow + " EU/t"), false);
                }
            }

            if (!teamData.activeChargers.isEmpty()) {
                source.sendSuccess(() -> TeslaLang.tr("phoenix_tesla_network.tesla_debug_command.active_chargers")
                        .withStyle(ChatFormatting.BLUE), false);
                for (BlockPos cPos : teamData.activeChargers) {
                    source.sendSuccess(() -> Component.literal("    - " + cPos.toShortString())
                            .append(" Flow: " + teamData.machineDisplayFlow.getOrDefault(cPos, 0L) + " EU/t"), false);
                }
            }

            source.sendSuccess(() -> Component.literal(" "), false);
        }

        return 1;
    }
}
