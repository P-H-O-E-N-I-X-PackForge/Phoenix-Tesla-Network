package net.phoenix_tesla_network.tesla.common.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.network.PhoenixNetwork;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TeslaCoverageSyncHandler {

    private TeslaCoverageSyncHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        ItemStack binder = heldBinder(player);
        if (binder.isEmpty()) return;

        CompoundTag tag = binder.getTag();
        UUID team = tag != null && tag.hasUUID("TargetTeam") ? tag.getUUID("TargetTeam") :
                TeamUtils.getTeamIdOrPlayerFallback(player.getUUID());
        if (team == null) return;

        ServerLevel level = player.serverLevel();
        TeslaTeamEnergyData.TeamEnergy energy = TeslaTeamEnergyData.get(level).getIfPresent(team);
        if (energy == null) return;

        List<S2CCoverageSyncPacket.Entry> entries = new ArrayList<>();
        for (TeslaTeamEnergyData.RangeNode node : energy.rangeNodes.values()) {
            if (!node.dimension.equals(level.dimension())) continue;

            float radius = (float) TeslaRange.radiusBlocks(node);
            byte kind;
            if (node.type.isTower()) {
                kind = node.working ? S2CCoverageSyncPacket.TOWER : S2CCoverageSyncPacket.TOWER_OFF;
            } else {
                kind = TeslaRange.isRelayActive(energy, node.dimension, node.pos) ?
                        S2CCoverageSyncPacket.RELAY_ACTIVE : S2CCoverageSyncPacket.RELAY_IDLE;
            }
            var lossNodeType = TeslaRange.lossTypeOf(energy, node);
            byte lossType = (byte) (lossNodeType == null ? TeslaTeamEnergyData.RangeNodeType.TOWER_ULTIMATE.ordinal() :
                    lossNodeType.ordinal());
            entries.add(new S2CCoverageSyncPacket.Entry(kind, node.pos, radius, lossType));
        }

        var loss = new S2CCoverageSyncPacket.LossParams[S2CCoverageSyncPacket.LOSS_TYPES];
        for (int i = 0; i < loss.length; i++) {
            var cfg = TeslaRange.lossConfig(TeslaTeamEnergyData.RangeNodeType.values()[i]);
            loss[i] = cfg.enabled ?
                    new S2CCoverageSyncPacket.LossParams(true, (float) cfg.baseLossPercent,
                            (float) cfg.lossPercentPerUnit, (float) cfg.distanceUnitBlocks,
                            (float) cfg.exponent, (float) cfg.maxLossPercent) :
                    S2CCoverageSyncPacket.LossParams.OFF;
        }

        PhoenixNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CCoverageSyncPacket(entries, loss));
    }

    private static ItemStack heldBinder(ServerPlayer player) {
        if (player.getMainHandItem().is(PhoenixTeslaItems.TESLA_BINDER.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(PhoenixTeslaItems.TESLA_BINDER.get())) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }
}
