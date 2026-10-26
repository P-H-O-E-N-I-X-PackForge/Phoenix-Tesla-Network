package net.phoenix_tesla_network.tesla.common.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.suite.PhoenixSuiteApi;
import net.phoenix_tesla_network.tesla.api.suite.event.PhoenixSetBonusEvent;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixModules;

/**
 * Full-set bonus (all four pieces worn), independent of which modules are installed:
 * - Resilience: 10% less incoming damage.
 * - Regrowth: slow regeneration while connected to your Tesla network.
 * - Fires PhoenixSetBonusEvent on gain/loss so other mods can hook it.
 */
@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID)
public final class PhoenixSetBonusHandler {

    private static final String ACTIVE = "phoenix_set_active";
    private static final float DAMAGE_MULTIPLIER = 0.9f;
    private static final int REGEN_INTERVAL = 100;

    private PhoenixSetBonusHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;

        boolean full = PhoenixSuiteApi.isFullSet(player);
        var pd = player.getPersistentData();
        if (full != pd.getBoolean(ACTIVE)) {
            if (full) pd.putBoolean(ACTIVE, true);
            else pd.remove(ACTIVE);
            NeoForge.EVENT_BUS.post(new PhoenixSetBonusEvent(player, full));
            player.displayClientMessage(Component.literal(full ? "§6✦ Phoenix Tech Suite: set bonus active" :
                    "§7Phoenix Tech Suite: set bonus lost"), true);
        }

        if (full && player.tickCount % REGEN_INTERVAL == 0 && player.getHealth() < player.getMaxHealth() &&
                PhoenixModules.Tesla.network(player) != null) {
            player.heal(1.0f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 4,
                    0.3, 0.5, 0.3, 0.0);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && PhoenixSuiteApi.isFullSet(player)) {
            event.setAmount(event.getAmount() * DAMAGE_MULTIPLIER);
        }
    }
}
