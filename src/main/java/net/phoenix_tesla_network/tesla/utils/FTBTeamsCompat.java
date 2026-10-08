package net.phoenix_tesla_network.tesla.utils;

import net.minecraft.world.entity.player.Player;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;

import java.util.Optional;
import java.util.UUID;

final class FTBTeamsCompat {

    private FTBTeamsCompat() {}

    private static Optional<Team> findTeam(UUID teamId) {
        try {
            if (teamId == null || !FTBTeamsAPI.api().isManagerLoaded()) return Optional.empty();

            for (Team team : FTBTeamsAPI.api().getManager().getTeams()) {
                if (teamId.equals(team.getId())) return Optional.of(team);
            }
        } catch (RuntimeException ignored) {}
        return Optional.empty();
    }

    static UUID getTeamIdOrPlayerFallback(UUID playerUUID) {
        try {
            if (!FTBTeamsAPI.api().isManagerLoaded()) return playerUUID;

            return FTBTeamsAPI.api().getManager().getTeamForPlayerID(playerUUID)
                    .map(team -> {
                        if (team.isPartyTeam() || team.isServerTeam()) {
                            return team.getTeamId();
                        }
                        return playerUUID;
                    })
                    .orElse(playerUUID);
        } catch (RuntimeException e) {
            return playerUUID;
        }
    }

    static String getTeamName(UUID teamId) {
        return findTeam(teamId)
                .map(team -> team.isPlayerTeam() ? TeamUtils.resolvePlayerName(team.getOwner()) :
                        team.getShortName())
                .orElseGet(() -> TeamUtils.resolvePlayerName(teamId));
    }

    static boolean isPlayerOnTeam(Player player, UUID teamUUID) {
        return findTeam(teamUUID)
                .map(team -> team.getMembers().contains(player.getUUID()))
                .orElse(player.getUUID().equals(teamUUID));
    }
}
