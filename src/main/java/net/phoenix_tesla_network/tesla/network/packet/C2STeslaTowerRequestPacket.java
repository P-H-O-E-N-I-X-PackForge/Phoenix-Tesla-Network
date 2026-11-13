package net.phoenix_tesla_network.tesla.network.packet;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import java.util.UUID;

/**
 * From the open Tesla Tower screen: {@link #REFRESH} asks for fresh numbers (about once a second) and
 * {@link #TOGGLE_POWER} switches the tower on or off. A toggle is only honoured for the team that owns the tower, or an
 * operator.
 */
public record C2STeslaTowerRequestPacket(BlockPos pos, int action) implements CustomPacketPayload {

    public static final int REFRESH = 0;
    public static final int TOGGLE_POWER = 1;

    public static final Type<C2STeslaTowerRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "c2s_tesla_tower_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2STeslaTowerRequestPacket> STREAM_CODEC = StreamCodec
            .of((buf, msg) -> {
                buf.writeBlockPos(msg.pos);
                buf.writeVarInt(msg.action);
            }, buf -> new C2STeslaTowerRequestPacket(buf.readBlockPos(), buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2STeslaTowerRequestPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) > 128 * 128) {
                return;
            }
            if (!(MetaMachine.getMachine(player.level(), msg.pos) instanceof TeslaTowerMachine tower) ||
                    !tower.isFormed()) {
                return;
            }
            if (msg.action == TOGGLE_POWER) {
                UUID owner = tower.getOwnerTeamUUID();
                UUID mine = TeamUtils.getTeamIdOrPlayerFallback(player.getUUID());
                if (player.hasPermissions(2) || owner == null || owner.equals(mine)) {
                    tower.setWorkingEnabled(!tower.isWorkingEnabled());
                }
            }
            S2CTeslaTowerPacket.send(player, S2CTeslaTowerPacket.of(tower, false));
        });
    }
}
