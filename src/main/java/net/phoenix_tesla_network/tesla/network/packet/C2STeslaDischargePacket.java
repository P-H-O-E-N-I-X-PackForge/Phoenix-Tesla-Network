package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixModules;

public class C2STeslaDischargePacket implements CustomPacketPayload {

    public static final Type<C2STeslaDischargePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "c2s_tesla_discharge"));
    // StreamCodec.unit(new ...) only encodes that one exact instance and throws "Can't encode X, expected X" for any
    // other. This payload has no data, so write nothing and build a fresh instance on read.
    public static final StreamCodec<RegistryFriendlyByteBuf, C2STeslaDischargePacket> STREAM_CODEC = StreamCodec
            .of((buf, msg) -> {}, buf -> new C2STeslaDischargePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** Cooldown, energy and feedback all live in TeslaDischargeModule now. */
    public static void handle(C2STeslaDischargePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) PhoenixModules.TeslaDischargeModule.trigger(player);
        });
    }
}
