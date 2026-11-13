package net.phoenix_tesla_network.tesla.network.packet;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Everything the Tesla Tower full-screen UI shows: the tower, the network of its team, and how much is flowing. Sent
 * when the screen is opened and again whenever the open screen asks for a refresh.
 */
public record S2CTeslaTowerPacket(BlockPos pos, boolean open, boolean online, int kind, String towerName,
                                  String rangeText, String teamName, String stored, String capacity, float fill,
                                  long inPerSec, long outPerSec, int batteryTier, int hatches, int soulLinked,
                                  int towers)
        implements CustomPacketPayload {

    public static final Type<S2CTeslaTowerPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "s2c_tesla_tower"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CTeslaTowerPacket> STREAM_CODEC = StreamCodec
            .of((buf, msg) -> encode(msg, buf), S2CTeslaTowerPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static S2CTeslaTowerPacket of(TeslaTowerMachine tower, boolean open) {
        var bank = tower.getEnergyBank();
        BigInteger stored = bank == null ? BigInteger.ZERO : bank.getStored();
        BigInteger capacity = bank == null ? BigInteger.ZERO : bank.getCapacity();
        float fill = capacity.signum() <= 0 ? 0f :
                new BigDecimal(stored).divide(new BigDecimal(capacity), 6, RoundingMode.HALF_UP).floatValue();

        UUID team = tower.getOwnerTeamUUID();
        int hatches = 0;
        int soul = 0;
        int towers = 1;
        if (team != null && tower.getLevel() instanceof ServerLevel level) {
            for (var hatch : TeslaTeamEnergyData.get(level).getHatches(team)) {
                hatches++;
                if (hatch.isSoulLinked) soul++;
            }
            towers = Math.max(1, TeslaTowerMachine.getTowersByTeam(team).size());
        }

        var profile = tower.getTowerType().profile();
        String range;
        if (profile.infiniteRange) {
            range = profile.crossDimension ? "Infinite, all dimensions" : "Infinite, this dimension";
        } else {
            range = profile.rangeBlocks + " blocks" + (profile.crossDimension ? ", all dimensions" : "");
        }
        int tier = bank == null ? 0 : Math.max(0, Math.min(GTValues.VN.length - 1, bank.getHighestTier()));
        return new S2CTeslaTowerPacket(tower.getBlockPos(), open, tower.isFormed() && tower.isWorkingEnabled(),
                tower.getTowerType().ordinal(), tower.getTowerType().displayName(), range,
                team == null ? "None" : TeamUtils.getTeamName(team), stored.toString(), capacity.toString(),
                Math.max(0f, Math.min(1f, fill)), tower.getInputPerSec(), tower.getOutputPerSec(), tier, hatches,
                soul, towers);
    }

    public static void send(ServerPlayer player, S2CTeslaTowerPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    private static void encode(S2CTeslaTowerPacket m, FriendlyByteBuf buf) {
        buf.writeBlockPos(m.pos);
        buf.writeBoolean(m.open);
        buf.writeBoolean(m.online);
        buf.writeVarInt(m.kind);
        buf.writeUtf(m.towerName);
        buf.writeUtf(m.rangeText);
        buf.writeUtf(m.teamName);
        buf.writeUtf(m.stored, 512);
        buf.writeUtf(m.capacity, 512);
        buf.writeFloat(m.fill);
        buf.writeLong(m.inPerSec);
        buf.writeLong(m.outPerSec);
        buf.writeVarInt(m.batteryTier);
        buf.writeVarInt(m.hatches);
        buf.writeVarInt(m.soulLinked);
        buf.writeVarInt(m.towers);
    }

    private static S2CTeslaTowerPacket decode(FriendlyByteBuf buf) {
        return new S2CTeslaTowerPacket(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt(),
                buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(512), buf.readUtf(512), buf.readFloat(),
                buf.readLong(), buf.readLong(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt());
    }

    public static void handle(S2CTeslaTowerPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> net.phoenix_tesla_network.tesla.client.ui.TeslaTowerClientHandler.accept(msg));
    }
}
