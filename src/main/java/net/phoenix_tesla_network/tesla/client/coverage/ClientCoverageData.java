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
    private static S2CCoverageSyncPacket.LossParams[] loss = offAll();
    private static long receivedAt = Long.MIN_VALUE;

    private ClientCoverageData() {}

    private static S2CCoverageSyncPacket.LossParams[] offAll() {
        var off = new S2CCoverageSyncPacket.LossParams[S2CCoverageSyncPacket.LOSS_TYPES];
        java.util.Arrays.fill(off, S2CCoverageSyncPacket.LossParams.OFF);
        return off;
    }

    public static void accept(List<S2CCoverageSyncPacket.Entry> newEntries,
                              S2CCoverageSyncPacket.LossParams[] newLoss) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        entries = newEntries;
        loss = newLoss;
        receivedAt = level.getGameTime();
    }

    /** Loss settings of the tower tier an entry belongs to (extenders carry their tower's tier). */
    public static S2CCoverageSyncPacket.LossParams loss(S2CCoverageSyncPacket.Entry entry) {
        int type = entry.lossType();
        return type >= 0 && type < loss.length ? loss[type] : S2CCoverageSyncPacket.LossParams.OFF;
    }

    public static Component describeAt(Vec3 pos) {
        var current = fresh();
        if (current.isEmpty()) return null;

        // Each source has its own tower's loss curve, so pick the source with the lowest loss here.
        double best = Double.MAX_VALUE;
        float percent = Float.MAX_VALUE;
        boolean lossy = false;
        for (S2CCoverageSyncPacket.Entry entry : current) {
            if (entry.kind() != S2CCoverageSyncPacket.TOWER && entry.kind() != S2CCoverageSyncPacket.RELAY_ACTIVE) {
                continue;
            }
            double dist = pos.distanceTo(Vec3.atCenterOf(entry.pos()));
            if (entry.radius() >= 0 && dist > entry.radius()) continue;

            var params = loss(entry);
            float entryPercent = params.lossAt((float) dist);
            if (entryPercent < percent || (entryPercent == percent && dist < best)) {
                percent = entryPercent;
                best = dist;
                lossy = params.enabled();
            }
        }

        if (best == Double.MAX_VALUE) {
            return Component.literal("Outside network coverage").withStyle(ChatFormatting.RED);
        }
        if (!lossy) {
            return Component.literal("Inside network coverage - lossless").withStyle(ChatFormatting.GREEN);
        }

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
