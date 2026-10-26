package net.phoenix_tesla_network.tesla.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.client.coverage.ClientCoverageData;
import net.phoenix_tesla_network.tesla.client.keybind.PhoenixKeybinds;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.network.packet.C2SLoreScanPacket;
import net.phoenix_tesla_network.tesla.network.packet.C2SOpenSuiteMenuPacket;
import net.phoenix_tesla_network.tesla.network.packet.C2STeslaDischargePacket;

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

        while (PhoenixKeybinds.TESLA_DISCHARGE.consumeClick()) {
            if (mc.screen == null && !mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).isEmpty()) {
                PacketDistributor.sendToServer(new C2STeslaDischargePacket());
            }
        }

        while (PhoenixKeybinds.LORE_SCAN.consumeClick()) {
            if (mc.screen == null && !mc.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()) {
                PacketDistributor.sendToServer(new C2SLoreScanPacket());
            }
        }

        while (PhoenixKeybinds.SUITE_MENU.consumeClick()) {
            if (mc.screen == null) PacketDistributor.sendToServer(new C2SOpenSuiteMenuPacket());
        }
    }
}
