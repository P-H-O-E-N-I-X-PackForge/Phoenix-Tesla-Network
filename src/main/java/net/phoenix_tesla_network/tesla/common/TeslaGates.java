package net.phoenix_tesla_network.tesla.common;

import net.minecraft.server.level.ServerPlayer;
import net.phoenixvine.wiki.api.gate.SuiteGates;

/**
 * What a player may do with the Tesla suite, as features of the shared gate API ({@link SuiteGates}): another mod binds
 * them to research or anything else, e.g. {@code SuiteGates.bind(TeslaGates.DISCHARGE, "*", "conflux:node/foo")}. A
 * feature that nothing is bound to is allowed. These gate the suite's key-triggered abilities.
 */
public final class TeslaGates {

    /** The discharge key. */
    public static final String DISCHARGE = "tesla:discharge";
    /** The lore scanner key. */
    public static final String LORE_SCAN = "tesla:lore_scan";
    /** Opening the suite menu. */
    public static final String SUITE_MENU = "tesla:suite_menu";
    /** Switching Tesla mode on the chest piece. */
    public static final String TESLA_MODE = "tesla:tesla_mode";

    private TeslaGates() {}

    public static boolean allowed(ServerPlayer player, String feature) {
        try {
            return SuiteGates.allowed(player, feature, SuiteGates.ANY_SCOPE);
        } catch (NoClassDefFoundError e) {
            // Tesla does not require the wiki: without it nothing can be bound, so everything is allowed
            return true;
        }
    }

    /** Like {@link #allowed}, and tells the player when the answer is no. */
    public static boolean check(ServerPlayer player, String feature) {
        if (allowed(player, feature)) return true;
        player.displayClientMessage(SuiteGates.denial(feature, SuiteGates.ANY_SCOPE), false);
        return false;
    }
}
