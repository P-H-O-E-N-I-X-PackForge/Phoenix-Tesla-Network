package net.phoenix_tesla_network.tesla.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.client.coverage.ClientCoverageData;
import net.phoenix_tesla_network.tesla.client.gui.WingFlightScreen;
import net.phoenix_tesla_network.tesla.client.keybind.PhoenixKeybinds;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.network.PhoenixNetwork;
import net.phoenix_tesla_network.tesla.network.packet.C2SToggleTeslaModePacket;

@Mod.EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.player.tickCount % 10 == 0 &&
                (mc.player.getMainHandItem().is(PhoenixTeslaItems.TESLA_BINDER.get()) ||
                        mc.player.getOffhandItem().is(PhoenixTeslaItems.TESLA_BINDER.get()))) {
            var text = ClientCoverageData.describeAt(mc.player.position());
            if (text != null) mc.gui.setOverlayMessage(text, false);
        }

        while (PhoenixKeybinds.OPEN_WING_GUI.consumeClick()) {
            if (mc.screen == null &&
                    mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PhoenixArmorItem) {
                mc.setScreen(new WingFlightScreen());
            }
        }

        while (PhoenixKeybinds.TESLA_MODE.consumeClick()) {
            if (mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PhoenixArmorItem) {
                PhoenixNetwork.CHANNEL.sendToServer(new C2SToggleTeslaModePacket());
            }
        }
    }
}
