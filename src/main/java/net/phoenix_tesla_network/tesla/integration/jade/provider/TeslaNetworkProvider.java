package net.phoenix_tesla_network.tesla.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaRelayMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.part.TeslaEnergyHatchPartMachine;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.UUID;

public class TeslaNetworkProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = PhoenixTeslaNetwork.id("tesla_network_info");

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE) {
            UUID team = null;
            BlockPos pos = accessor.getPosition();
            long transferRate = 0;
            int mode = -1;

            if (accessor.getLevel() instanceof ServerLevel sl) {
                MinecraftServer server = sl.getServer();
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld == null) return;

                TeslaTeamEnergyData data = TeslaTeamEnergyData.get(overworld);
                MetaMachine machine = metaBE.getMetaMachine();

                if (machine instanceof TeslaEnergyHatchPartMachine hatch) {
                    team = hatch.getOwnerTeamUUID();
                    if (team != null) {
                        mode = hatch.isUplink() ? 0 : 1;
                        transferRate = data.getOrCreate(team).machineDisplayFlow.getOrDefault(pos, 0L);
                    }
                } else if (machine instanceof TeslaTowerMachine tower) {
                    team = tower.getOwnerUUID();
                } else if (machine instanceof TeslaRelayMachine relay) {
                    team = relay.getOwnerTeamUUID();
                    if (team != null) {
                        tag.putBoolean("RelayActive",
                                TeslaRange.isRelayActive(data.getOrCreate(team), sl.dimension(), pos));
                    }
                }

                else {
                    for (var entry : data.getNetworksView().entrySet()) {
                        TeslaTeamEnergyData.TeamEnergy teamData = entry.getValue();

                        if (teamData.soulLinkedMachines.contains(pos)) {
                            team = entry.getKey();
                            transferRate = teamData.machineDisplayFlow.getOrDefault(pos, 0L);

                            if (transferRate < 0) {
                                mode = 3;
                            } else {
                                mode = 1;
                            }
                            break;
                        } else if (teamData.activeChargers.contains(pos)) {
                            team = entry.getKey();
                            mode = 2;
                            transferRate = teamData.machineDisplayFlow.getOrDefault(pos, 0L);
                            break;
                        }
                    }
                }

                if (team != null) {
                    TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(team);
                    tag.putUUID("TeslaTeam", team);
                    tag.putString("TeamName", TeamUtils.getTeamName(team));
                    tag.putString("Stored", FormattingUtil.formatNumbers(teamData.stored));
                    tag.putString("Capacity", FormattingUtil.formatNumbers(teamData.capacity));
                    tag.putLong("LocalTransfer", transferRate);
                    tag.putInt("TransferMode", mode);
                    tag.putBoolean("InRange", TeslaRange.isInRange(teamData, sl.dimension(), pos));
                    if (PhoenixTeslaConfigs.get().towers.loss.enabled) {
                        var reach = TeslaRange.reach(teamData, sl.dimension(), pos);
                        if (reach != null) {
                            tag.putDouble("LossPercent", TeslaLoss.lossFraction(
                                    PhoenixTeslaConfigs.get().towers.loss, reach) * 100.0);
                            tag.putBoolean("LossCrossDim", reach.crossDimension());
                            tag.putDouble("LossDistance", reach.distance());
                        }
                    }

                    int physicalHatches = 0;
                    for (TeslaTeamEnergyData.HatchInfo hatch : data.getHatches(team)) {
                        if (hatch.isSoulLinked) continue;
                        if (Boolean.TRUE.equals(TeslaTeamEnergyData.isLivePhysicalHatch(server, hatch))) {
                            physicalHatches++;
                        }
                    }
                    int wiredMachines = teamData.soulLinkedMachines.size();
                    int wirelessChargers = teamData.activeChargers.size();

                    tag.putInt("TotalConnections", physicalHatches + wiredMachines + wirelessChargers);
                }
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(UID)) return;

        CompoundTag data = accessor.getServerData();
        if (!data.contains("TeslaTeam")) return;

        tooltip.add(Component.literal("Network: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getString("TeamName")).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.literal("Tesla Network: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(data.getString("Stored")).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(" / " + data.getString("Capacity") + " EU")
                        .withStyle(ChatFormatting.YELLOW)));

        if (data.contains("RelayActive")) {
            boolean active = data.getBoolean("RelayActive");
            tooltip.add(Component.literal("Range Extender: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(active ? "Extending" : "Not connected")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED)));
        } else if (data.contains("InRange") && !data.getBoolean("InRange")) {
            tooltip.add(Component.literal("Out of network range").withStyle(ChatFormatting.RED));
        }

        if (data.contains("LossPercent")) {
            double loss = data.getDouble("LossPercent");
            ChatFormatting lossColor = loss < 1.0 ? ChatFormatting.GREEN :
                    loss < 15.0 ? ChatFormatting.YELLOW : ChatFormatting.RED;
            String where = data.getBoolean("LossCrossDim") ? "cross-dimension" :
                    Math.round(data.getDouble("LossDistance")) + " blocks";
            tooltip.add(Component.literal("Link loss: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.format("%.1f%%", loss)).withStyle(lossColor))
                    .append(Component.literal(" (" + where + ")").withStyle(ChatFormatting.DARK_GRAY)));
        }

        int connections = data.getInt("TotalConnections");
        tooltip.add(Component.literal("Connections: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(connections)).withStyle(ChatFormatting.WHITE)));

        if (data.contains("TransferMode") && data.getInt("TransferMode") != -1) {
            long rate = data.getLong("LocalTransfer");
            int mode = data.getInt("TransferMode");

            MutableComponent label;
            ChatFormatting color;
            String icon = "";

            switch (mode) {
                case 0 -> {
                    label = Component.literal("Providing: ");
                    color = ChatFormatting.GREEN;
                }
                case 2 -> {
                    label = Component.literal("Transmitting: ");
                    color = ChatFormatting.AQUA;
                }
                case 3 -> {
                    label = Component.literal("Generating: ");
                    color = ChatFormatting.GOLD;
                    icon = "§6⚡ ";
                }
                default -> {
                    label = Component.literal("Taking: ");
                    color = ChatFormatting.RED;
                }
            }

            long displayRate = Math.abs(rate);

            if (displayRate > 0) {
                tooltip.add(label.withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(icon + FormattingUtil.formatNumbers(displayRate) + " EU/t")
                                .withStyle(color)));
            } else {
                tooltip.add(label.withStyle(ChatFormatting.GRAY)
                        .append(Component.literal("IDLE").withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
