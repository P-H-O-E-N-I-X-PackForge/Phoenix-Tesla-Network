package net.phoenix_tesla_network.tesla.common.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.util.StackNbt;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID)
public final class TeslaCoverageSyncHandler {

    private TeslaCoverageSyncHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;

        ItemStack binder = heldBinder(player);
        if (binder.isEmpty()) return;

        CompoundTag tag = StackNbt.getTag(binder);
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
            entries.add(new S2CCoverageSyncPacket.Entry(kind, node.pos, radius));
        }

        var lossCfg = PhoenixTeslaConfigs.get().towers.loss;
        var loss = lossCfg.enabled ?
                new S2CCoverageSyncPacket.LossParams(true, (float) lossCfg.baseLossPercent,
                        (float) lossCfg.lossPercentPerUnit, (float) lossCfg.distanceUnitBlocks,
                        (float) lossCfg.exponent, (float) lossCfg.maxLossPercent) :
                S2CCoverageSyncPacket.LossParams.OFF;

        PacketDistributor.sendToPlayer(player, new S2CCoverageSyncPacket(entries, loss));
    }

    private static ItemStack heldBinder(ServerPlayer player) {
        if (player.getMainHandItem().is(PhoenixTeslaItems.TESLA_BINDER.get())) return player.getMainHandItem();
        if (player.getOffhandItem().is(PhoenixTeslaItems.TESLA_BINDER.get())) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }
}
