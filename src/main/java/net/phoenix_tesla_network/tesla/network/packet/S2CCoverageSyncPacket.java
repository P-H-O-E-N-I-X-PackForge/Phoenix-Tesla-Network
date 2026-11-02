package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.phoenix_tesla_network.tesla.client.coverage.ClientCoverageData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2CCoverageSyncPacket {

    public static final byte TOWER = 0;
    public static final byte TOWER_OFF = 1;
    public static final byte RELAY_ACTIVE = 2;
    public static final byte RELAY_IDLE = 3;

    /** Index into the loss params: 0 = basic, 1 = advanced, 2 = ultimate tower. Extenders use their tower's. */
    public static final int LOSS_TYPES = 3;

    public record Entry(byte kind, BlockPos pos, float radius, byte lossType) {}

    public record LossParams(boolean enabled, float base, float perUnit, float unit, float exponent, float max) {

        public static final LossParams OFF = new LossParams(false, 0, 0, 1, 1, 0);

        public float lossAt(float distance) {
            if (!enabled) return 0f;
            double loss = base + perUnit * Math.pow(distance / unit, exponent);
            return (float) Math.min(max, Math.max(0.0, loss));
        }

        public float distanceFor(float percent) {
            if (!enabled || percent > max) return Float.POSITIVE_INFINITY;
            if (percent <= base) return 0f;
            if (perUnit <= 0f) return Float.POSITIVE_INFINITY;
            return (float) (unit * Math.pow((percent - base) / perUnit, 1.0 / exponent));
        }
    }

    private final List<Entry> entries;
    private final LossParams[] loss;

    public S2CCoverageSyncPacket(List<Entry> entries, LossParams[] loss) {
        this.entries = entries;
        this.loss = loss;
    }

    public S2CCoverageSyncPacket(FriendlyByteBuf buf) {
        this.loss = new LossParams[LOSS_TYPES];
        for (int i = 0; i < LOSS_TYPES; i++) {
            loss[i] = new LossParams(buf.readBoolean(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat());
        }
        int count = buf.readVarInt();
        this.entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readByte(), buf.readBlockPos(), buf.readFloat(), buf.readByte()));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        for (int i = 0; i < LOSS_TYPES; i++) {
            LossParams l = loss[i];
            buf.writeBoolean(l.enabled());
            buf.writeFloat(l.base());
            buf.writeFloat(l.perUnit());
            buf.writeFloat(l.unit());
            buf.writeFloat(l.exponent());
            buf.writeFloat(l.max());
        }
        buf.writeVarInt(entries.size());
        for (Entry entry : entries) {
            buf.writeByte(entry.kind());
            buf.writeBlockPos(entry.pos());
            buf.writeFloat(entry.radius());
            buf.writeByte(entry.lossType());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(
                () -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCoverageData.accept(entries, loss)));
        ctx.get().setPacketHandled(true);
    }
}
