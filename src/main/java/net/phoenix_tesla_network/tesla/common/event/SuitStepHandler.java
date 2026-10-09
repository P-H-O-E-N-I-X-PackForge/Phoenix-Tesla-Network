package net.phoenix_tesla_network.tesla.common.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTechSuite;

@Mod.EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SuitStepHandler {

    private SuitStepHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;

        if (player.getPersistentData().getBoolean("phoenix_tesla_step_applied") &&
                !(player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof PhoenixArmorItem)) {
            PhoenixTechSuite.resetStepHeight(player);
        }

        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PhoenixArmorItem)) {
            var persistent = player.getPersistentData();
            boolean changed = false;
            if (persistent.getBoolean(PhoenixTechSuite.FLIGHT_SPEED_ZEROED)) {
                player.getAbilities().setFlyingSpeed(PhoenixTechSuite.VANILLA_FLYING_SPEED);
                persistent.remove(PhoenixTechSuite.FLIGHT_SPEED_ZEROED);
                changed = true;
            }
            if (persistent.getBoolean(PhoenixTechSuite.MAYFLY_GRANTED)) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    changed = true;
                }
                persistent.remove(PhoenixTechSuite.MAYFLY_GRANTED);
            }
            if (changed) player.onUpdateAbilities();
        }
    }
}
