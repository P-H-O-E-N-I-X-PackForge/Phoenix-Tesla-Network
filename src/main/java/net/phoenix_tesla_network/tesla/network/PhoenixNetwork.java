package net.phoenix_tesla_network.tesla.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.phoenix_tesla_network.tesla.network.packet.C2SToggleTeslaModePacket;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;
import net.phoenix_tesla_network.tesla.network.packet.UpdateWingSettingsPacket;

public class PhoenixNetwork {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(UpdateWingSettingsPacket.TYPE, UpdateWingSettingsPacket.STREAM_CODEC,
                (msg, ctx) -> msg.handle(ctx));
        registrar.playToServer(C2SToggleTeslaModePacket.TYPE, C2SToggleTeslaModePacket.STREAM_CODEC,
                C2SToggleTeslaModePacket::handle);
        registrar.playToClient(S2CCoverageSyncPacket.TYPE, S2CCoverageSyncPacket.STREAM_CODEC,
                (msg, ctx) -> msg.handle(ctx));
    }
}
