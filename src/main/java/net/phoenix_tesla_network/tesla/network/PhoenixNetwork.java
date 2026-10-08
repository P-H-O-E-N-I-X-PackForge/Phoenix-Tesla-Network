package net.phoenix_tesla_network.tesla.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.phoenix_tesla_network.tesla.network.packet.C2SToggleTeslaModePacket;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;
import net.phoenix_tesla_network.tesla.network.packet.UpdateWingSettingsPacket;

import java.util.Optional;

@SuppressWarnings("removal")
public class PhoenixNetwork {

    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("phoenix_tesla_network", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private static int id = 0;

    public static void init() {
        CHANNEL.registerMessage(id++,
                UpdateWingSettingsPacket.class,
                UpdateWingSettingsPacket::encode,
                UpdateWingSettingsPacket::new,
                UpdateWingSettingsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                C2SToggleTeslaModePacket.class,
                C2SToggleTeslaModePacket::encode,
                C2SToggleTeslaModePacket::new,
                C2SToggleTeslaModePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                S2CCoverageSyncPacket.class,
                S2CCoverageSyncPacket::encode,
                S2CCoverageSyncPacket::new,
                S2CCoverageSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
