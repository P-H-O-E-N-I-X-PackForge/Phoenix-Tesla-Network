package net.phoenix_tesla_network.tesla.client.coverage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;

import java.util.List;

public final class ClientCoverageData {

    private static final long MAX_AGE_TICKS = 50;

    private static List<S2CCoverageSyncPacket.Entry> entries = List.of();
    private static S2CCoverageSyncPacket.LossParams loss = S2CCoverageSyncPacket.LossParams.OFF;
    private static long receivedAt = Long.MIN_VALUE;

    private ClientCoverageData() {}

    public static void accept(List<S2CCoverageSyncPacket.Entry> newEntries,
                              S2CCoverageSyncPacket.LossParams newLoss) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        entries = newEntries;
        loss = newLoss;
        receivedAt = level.getGameTime();
    }

    public static S2CCoverageSyncPacket.LossParams loss() {
        return loss;
    }

    public static Component describeAt(Vec3 pos) {
        var current = fresh();
        if (current.isEmpty()) return null;

        double best = Double.MAX_VALUE;
        for (S2CCoverageSyncPacket.Entry entry : current) {
            if (entry.kind() != S2CCoverageSyncPacket.TOWER && entry.kind() != S2CCoverageSyncPacket.RELAY_ACTIVE) {
                continue;
            }
            double dist = pos.distanceTo(Vec3.atCenterOf(entry.pos()));
            if (entry.radius() >= 0 && dist > entry.radius()) continue;
            best = Math.min(best, dist);
        }

        if (best == Double.MAX_VALUE) {
            return Component.literal("Outside network coverage").withStyle(ChatFormatting.RED);
        }
        if (!loss.enabled()) {
            return Component.literal("Inside network coverage - lossless").withStyle(ChatFormatting.GREEN);
        }

        float percent = loss.lossAt((float) best);
        ChatFormatting color = percent < 1f ? ChatFormatting.GREEN :
                percent < 15f ? ChatFormatting.YELLOW : ChatFormatting.RED;
        return Component.literal("Link loss here: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.format("%.1f%%", percent)).withStyle(color))
                .append(Component.literal(" (" + Math.round(best) + " blocks from the nearest source)")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static List<S2CCoverageSyncPacket.Entry> fresh() {
        var level = Minecraft.getInstance().level;
        if (level == null || level.getGameTime() - receivedAt > MAX_AGE_TICKS) return List.of();
        return entries;
    }
}
