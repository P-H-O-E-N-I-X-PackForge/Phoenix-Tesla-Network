package net.phoenix_tesla_network.tesla.client.ui;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix_tesla_network.tesla.network.packet.S2CTeslaTowerPacket;

/** Opens the Tesla Tower screen, or feeds fresh numbers to it if it is already open. */
@OnlyIn(Dist.CLIENT)
public final class TeslaTowerClientHandler {

    private TeslaTowerClientHandler() {}

    public static void accept(S2CTeslaTowerPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof TeslaTowerScreen open && open.pos().equals(msg.pos())) {
            open.update(msg);
        } else if (msg.open()) {
            mc.setScreen(new TeslaTowerScreen(msg));
        }
    }
}
