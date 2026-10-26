package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixSuiteModules;

public class C2SLoreScanPacket implements CustomPacketPayload {

    public static final Type<C2SLoreScanPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "c2s_lore_scan"));
    // Empty payload: write nothing, build a fresh instance on read (StreamCodec.unit only accepts one instance).
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SLoreScanPacket> STREAM_CODEC = StreamCodec
            .of((buf, msg) -> {}, buf -> new C2SLoreScanPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SLoreScanPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) PhoenixSuiteModules.LoreScannerModule.trigger(player);
        });
    }
}
