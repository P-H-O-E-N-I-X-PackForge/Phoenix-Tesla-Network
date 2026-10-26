package net.phoenix_tesla_network.tesla.api.suite.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Fired on the server when a player gains or loses the full-set bonus (all four Phoenix Tech pieces). */
public class PhoenixSetBonusEvent extends PlayerEvent {

    private final boolean active;

    public PhoenixSetBonusEvent(Player player, boolean active) {
        super(player);
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
