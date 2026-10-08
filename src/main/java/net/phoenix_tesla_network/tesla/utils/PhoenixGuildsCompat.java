package net.phoenix_tesla_network.tesla.utils;

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.UUID;

final class PhoenixGuildsCompat {

    private static final Method GET_ID_OR_FALLBACK;
    private static final Method GET_DISPLAY_NAME;
    private static final Method IS_IN_GUILD_OR_IS;

    static {
        Method idOrFallback = null, displayName = null, inGuildOrIs = null;
        try {
            Class<?> api = Class.forName("net.phoenixvine.guilds.GuildAPI");
            idOrFallback = api.getMethod("getGuildIdOrPlayerFallback", UUID.class);
            displayName = api.getMethod("getDisplayName", UUID.class);
            inGuildOrIs = api.getMethod("isPlayerInGuildOrIs", UUID.class, UUID.class);
        } catch (ReflectiveOperationException ignored) {}
        GET_ID_OR_FALLBACK = idOrFallback;
        GET_DISPLAY_NAME = displayName;
        IS_IN_GUILD_OR_IS = inGuildOrIs;
    }

    private PhoenixGuildsCompat() {}

    static UUID getTeamIdOrPlayerFallback(UUID playerUUID) {
        if (GET_ID_OR_FALLBACK == null) return playerUUID;
        try {
            return (UUID) GET_ID_OR_FALLBACK.invoke(null, playerUUID);
        } catch (ReflectiveOperationException e) {
            return playerUUID;
        }
    }

    static String getTeamName(UUID teamId) {
        if (GET_DISPLAY_NAME == null) return TeamUtils.resolvePlayerName(teamId);
        try {
            return (String) GET_DISPLAY_NAME.invoke(null, teamId);
        } catch (ReflectiveOperationException e) {
            return TeamUtils.resolvePlayerName(teamId);
        }
    }

    static boolean isPlayerOnTeam(Player player, UUID teamUUID) {
        if (IS_IN_GUILD_OR_IS == null) return player.getUUID().equals(teamUUID);
        try {
            return (boolean) IS_IN_GUILD_OR_IS.invoke(null, player.getUUID(), teamUUID);
        } catch (ReflectiveOperationException e) {
            return player.getUUID().equals(teamUUID);
        }
    }
}
