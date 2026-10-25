package net.phoenix_tesla_network.tesla.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.phoenix_tesla_network.tesla.network.packet.C2STeslaDischargePacket;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;
import net.phoenix_tesla_network.tesla.network.packet.S2CTeslaStatusPacket;

public class PhoenixNetwork {

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(C2STeslaDischargePacket.TYPE, C2STeslaDischargePacket.STREAM_CODEC,
                C2STeslaDischargePacket::handle);
        registrar.playToClient(S2CCoverageSyncPacket.TYPE, S2CCoverageSyncPacket.STREAM_CODEC,
                (msg, ctx) -> msg.handle(ctx));
        registrar.playToClient(S2CTeslaStatusPacket.TYPE, S2CTeslaStatusPacket.STREAM_CODEC,
                S2CTeslaStatusPacket::handle);
    }
}
