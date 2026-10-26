package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.ui.PhoenixArmorUIFactory;

public class C2SOpenSuiteMenuPacket implements CustomPacketPayload {

    public static final Type<C2SOpenSuiteMenuPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "c2s_open_suite_menu"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenSuiteMenuPacket> STREAM_CODEC = StreamCodec
            .of((buf, msg) -> {}, buf -> new C2SOpenSuiteMenuPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SOpenSuiteMenuPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player && player.containerMenu == player.inventoryMenu) {
                PhoenixArmorUIFactory.INSTANCE.open(player);
            }
        });
    }
}
