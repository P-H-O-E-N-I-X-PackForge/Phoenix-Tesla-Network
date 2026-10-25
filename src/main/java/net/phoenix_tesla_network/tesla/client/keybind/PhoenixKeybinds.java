package net.phoenix_tesla_network.tesla.client.keybind;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class PhoenixKeybinds {

    public static final KeyMapping TESLA_DISCHARGE = new KeyMapping(
            "key.phoenix_tesla_network.tesla_discharge",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_7,
            "key.categories.phoenix_tesla_network");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TESLA_DISCHARGE);
    }
}
