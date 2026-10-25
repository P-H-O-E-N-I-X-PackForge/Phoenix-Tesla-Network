package net.phoenix_tesla_network.tesla.common.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixWingFlight;

/**
 * Safety net only: PhoenixWingModule.onUnequip already restores flight state. This catches logouts, deaths and
 * chest swaps that skip the unequip hook. The step-height half is gone: StepModule's attribute modifier is
 * managed by GTM's attribute-module system.
 */
@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID)
public final class SuitStepHandler {

    private SuitStepHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PhoenixArmorItem) return;

        var persistent = player.getPersistentData();
        boolean changed = false;
        if (persistent.getBoolean(PhoenixWingFlight.FLIGHT_SPEED_ZEROED)) {
            player.getAbilities().setFlyingSpeed(PhoenixWingFlight.VANILLA_FLYING_SPEED);
            persistent.remove(PhoenixWingFlight.FLIGHT_SPEED_ZEROED);
            changed = true;
        }
        if (persistent.getBoolean(PhoenixWingFlight.MAYFLY_GRANTED)) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                changed = true;
            }
            persistent.remove(PhoenixWingFlight.MAYFLY_GRANTED);
        }
        if (changed) player.onUpdateAbilities();
    }
}
