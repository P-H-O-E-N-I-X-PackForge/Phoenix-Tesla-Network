package net.phoenix_tesla_network.tesla.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.client.coverage.ClientCoverageData;
import net.phoenix_tesla_network.tesla.client.gui.WingFlightScreen;
import net.phoenix_tesla_network.tesla.client.keybind.PhoenixKeybinds;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.network.packet.C2SToggleTeslaModePacket;

@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
                PacketDistributor.sendToServer(new C2SToggleTeslaModePacket());
            }
        }
    }
}
