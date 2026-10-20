package net.phoenix_tesla_network.tesla.utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;

public final class TeamUtils {

    private static final boolean PHOENIX_GUILDS_LOADED = ModList.get().isLoaded("phoenix_guilds");
    private static final boolean FTB_TEAMS_LOADED = ModList.get().isLoaded("ftbteams");

    private TeamUtils() {}

    public static UUID getTeamIdOrPlayerFallback(UUID playerUUID) {
        if (isNil(playerUUID)) return null;
        if (PHOENIX_GUILDS_LOADED) return PhoenixGuildsCompat.getTeamIdOrPlayerFallback(playerUUID);
        if (FTB_TEAMS_LOADED) return FTBTeamsCompat.getTeamIdOrPlayerFallback(playerUUID);

        return playerUUID;
    }

    public static String getTeamName(UUID teamId) {
        if (isNil(teamId)) return "Unknown";
        if (PHOENIX_GUILDS_LOADED) return stripIdSuffix(PhoenixGuildsCompat.getTeamName(teamId));
        if (FTB_TEAMS_LOADED) return stripIdSuffix(FTBTeamsCompat.getTeamName(teamId));

        return resolvePlayerName(teamId);
    }

    private static String stripIdSuffix(String name) {
        return name == null ? "Unknown" : name.replaceFirst("#[0-9A-Fa-f]{2,8}$", "");
    }

    /** The all-zero UUID is "no owner", not a real player or team. */
    public static boolean isNil(UUID id) {
        return id == null || (id.getMostSignificantBits() == 0L && id.getLeastSignificantBits() == 0L);
    }

    /** Returns the player's name if the server can resolve it (online or cached), otherwise null. */
    public static String lookupPlayerName(UUID playerUUID) {
        if (playerUUID == null) return null;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerPlayer online = server.getPlayerList().getPlayer(playerUUID);
            if (online != null) return online.getGameProfile().getName();

            if (server.getProfileCache() != null) {
                Optional<com.mojang.authlib.GameProfile> cached = server.getProfileCache().get(playerUUID);
                if (cached.isPresent()) return cached.get().getName();
            }
        }
        return null;
    }

    public static String resolvePlayerName(UUID playerUUID) {
        String name = lookupPlayerName(playerUUID);
        if (name != null) return name;
        return "Player: " + playerUUID.toString().substring(0, 8);
    }

    public static boolean isPlayerOnTeam(Player player, UUID teamUUID) {
        if (!(player instanceof ServerPlayer)) return false;
        if (PHOENIX_GUILDS_LOADED) return PhoenixGuildsCompat.isPlayerOnTeam(player, teamUUID);
        if (FTB_TEAMS_LOADED) return FTBTeamsCompat.isPlayerOnTeam(player, teamUUID);

        return player.getUUID().equals(teamUUID);
    }
}
