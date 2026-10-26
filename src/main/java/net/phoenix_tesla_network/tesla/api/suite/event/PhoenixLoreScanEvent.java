package net.phoenix_tesla_network.tesla.api.suite.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import org.jetbrains.annotations.Nullable;

/**
 * Fired on the server (NeoForge.EVENT_BUS) when a player uses the Lore Scanner module on something.
 * Exactly one of {state/pos} or {entity} is set. Phoenix Core and friends call {@link #handle(Component)} to claim
 * the scan (unlock a lore entry, complete a Chronicles task, drop a Solaris waypoint, ...). Unclaimed scans fall back
 * to a plain "Scanned: <name>" message.
 */
public class PhoenixLoreScanEvent extends PlayerEvent {

    @Nullable
    private final BlockPos pos;
    @Nullable
    private final BlockState state;
    @Nullable
    private final Entity entity;
    @Nullable
    private Component feedback;
    private boolean handled;

    public PhoenixLoreScanEvent(Player player, @Nullable BlockPos pos, @Nullable BlockState state,
                                @Nullable Entity entity) {
        super(player);
        this.pos = pos;
        this.state = state;
        this.entity = entity;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public BlockState getState() {
        return state;
    }

    @Nullable
    public Entity getTargetEntity() {
        return entity;
    }

    public boolean isBlock() {
        return state != null;
    }

    public boolean isHandled() {
        return handled;
    }

    @Nullable
    public Component getFeedback() {
        return feedback;
    }

    /** Claim the scan. The message is shown to the player on the action bar. */
    public void handle(Component message) {
        this.handled = true;
        this.feedback = message;
    }
}
