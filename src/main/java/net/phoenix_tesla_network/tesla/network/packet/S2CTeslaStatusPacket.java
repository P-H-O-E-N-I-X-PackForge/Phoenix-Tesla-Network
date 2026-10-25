package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixModules;

/** Network energy snapshot for the suit HUD. Sent about once a second while the Tesla link module is worn. */
public record S2CTeslaStatusPacket(boolean online, String stored, String capacity) implements CustomPacketPayload {

    public static final Type<S2CTeslaStatusPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "s2c_tesla_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CTeslaStatusPacket> STREAM_CODEC = StreamCodec
            .composite(ByteBufCodecs.BOOL, S2CTeslaStatusPacket::online,
                    ByteBufCodecs.STRING_UTF8, S2CTeslaStatusPacket::stored,
                    ByteBufCodecs.STRING_UTF8, S2CTeslaStatusPacket::capacity,
                    S2CTeslaStatusPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CTeslaStatusPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PhoenixModules.TeslaLinkModule.Client.update(msg.online, msg.stored, msg.capacity));
    }
}
