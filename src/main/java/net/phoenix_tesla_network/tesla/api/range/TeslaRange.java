package net.phoenix_tesla_network.tesla.api.range;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaRelayMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.RangeNode;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.TeamEnergy;

import java.util.*;

public final class TeslaRange {

    private record Emitter(RangeNode source, ResourceKey<Level> dimension, BlockPos pos, double radiusSqr,
                           boolean anyDimension) {

        boolean covers(ResourceKey<Level> dim, BlockPos target) {
            if (!anyDimension && !dimension.equals(dim)) return false;
            return radiusSqr < 0 || pos.distSqr(target) <= radiusSqr;
        }
    }

    private record Coverage(List<Emitter> emitters, Set<String> activeRelayKeys, Map<String, RangeNode> relayParents) {

        boolean covers(ResourceKey<Level> dim, BlockPos target) {
            for (Emitter emitter : emitters) {
                if (emitter.covers(dim, target)) return true;
            }
            return false;
        }

        Reach reach(ResourceKey<Level> dim, BlockPos target) {
            double best = Double.MAX_VALUE;
            boolean crossOnly = false;
            for (Emitter emitter : emitters) {
                if (!emitter.covers(dim, target)) continue;
                if (emitter.dimension().equals(dim)) {
                    best = Math.min(best, emitter.pos().distSqr(target));
                } else {
                    crossOnly = true;
                }
            }
            if (best != Double.MAX_VALUE) return new Reach(Math.sqrt(best), false);
            return crossOnly ? new Reach(0, true) : null;
        }
    }

    public record Reach(double distance, boolean crossDimension) {}

    private record Cached(int version, Coverage coverage) {}

    private static final Map<TeamEnergy, Cached> CACHE = new WeakHashMap<>();

    private TeslaRange() {}

    public static boolean isInRange(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        if (!team.rangeManaged) return true;
        return coverage(team).covers(dimension, pos);
    }

    public static boolean isInRange(TeslaTeamEnergyData data, UUID team, ResourceKey<Level> dimension, BlockPos pos) {
        TeamEnergy energy = data.getIfPresent(team);
        return energy == null || isInRange(energy, dimension, pos);
    }

    public static Reach reach(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        if (!team.rangeManaged) return null;
        return coverage(team).reach(dimension, pos);
    }

    public static boolean isRelayActive(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        if (!team.rangeManaged) return false;
        return coverage(team).activeRelayKeys().contains(RangeNode.key(dimension, pos));
    }

    public static RangeNode getRelayParent(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        if (!team.rangeManaged) return null;
        return coverage(team).relayParents().get(RangeNode.key(dimension, pos));
    }

    public static boolean canPlayerUseNetwork(TeslaTeamEnergyData data, UUID team, Player player) {
        if (!data.isOnline(team)) return false;
        if (!PhoenixTeslaConfigs.get().towers.suitRespectsRange) return true;
        return isInRange(data, team, player.level().dimension(), player.blockPosition());
    }

    public static void pruneStaleNodes(MinecraftServer server, TeslaTeamEnergyData data, TeamEnergy team) {
        List<RangeNode> stale = null;
        for (RangeNode node : team.rangeNodes.values()) {
            ServerLevel level = server.getLevel(node.dimension);
            if (level == null || !level.isLoaded(node.pos)) continue;

            MetaMachine machine = MetaMachine.getMachine(level, node.pos);
            boolean valid = node.type.isTower() ? machine instanceof TeslaTowerMachine :
                    machine instanceof TeslaRelayMachine;
            if (!valid) {
                if (stale == null) stale = new ArrayList<>();
                stale.add(node);
            }
        }
        if (stale != null) {
            for (RangeNode node : stale) team.removeNode(node.dimension, node.pos);
            data.setDirty();
        }
    }

    private static Coverage coverage(TeamEnergy team) {
        Cached cached = CACHE.get(team);
        if (cached != null && cached.version() == team.rangeVersion) return cached.coverage();

        Coverage computed = compute(team);
        CACHE.put(team, new Cached(team.rangeVersion, computed));
        return computed;
    }

    private static Coverage compute(TeamEnergy team) {
        PhoenixTeslaConfigs.TowerConfigs cfg = PhoenixTeslaConfigs.get().towers;
        List<Emitter> emitters = new ArrayList<>();
        List<RangeNode> relays = new ArrayList<>();

        for (RangeNode node : team.rangeNodes.values()) {
            if (!node.working) continue;

            if (node.type.isTower()) {
                PhoenixTeslaConfigs.TowerProfile profile = profileFor(cfg, node.type);
                if (!profile.enabled) continue;
                double radiusSqr = profile.infiniteRange ? -1 : square(profile.rangeBlocks);
                emitters.add(new Emitter(node, node.dimension, node.pos, radiusSqr, profile.crossDimension));
            } else if (cfg.relay.enabled) {
                relays.add(node);
            }
        }

        Set<String> activeRelays = new HashSet<>();
        Map<String, RangeNode> relayParents = new HashMap<>();
        double relayRadiusSqr = square(cfg.relay.rangeBlocks);
        boolean progressed = true;
        while (progressed && !relays.isEmpty()) {
            progressed = false;
            for (Iterator<RangeNode> it = relays.iterator(); it.hasNext();) {
                RangeNode relay = it.next();
                Emitter parent = null;
                double parentDist = Double.MAX_VALUE;
                for (Emitter emitter : emitters) {
                    if (!emitter.covers(relay.dimension, relay.pos)) continue;

                    double dist = emitter.dimension().equals(relay.dimension) ? emitter.pos().distSqr(relay.pos) :
                            Double.MAX_VALUE / 2;
                    if (parent == null || dist < parentDist) {
                        parent = emitter;
                        parentDist = dist;
                    }
                }
                if (parent != null) {
                    emitters.add(new Emitter(relay, relay.dimension, relay.pos, relayRadiusSqr, false));
                    activeRelays.add(relay.key());
                    if (parent.dimension().equals(relay.dimension)) relayParents.put(relay.key(), parent.source());
                    it.remove();
                    progressed = true;
                }
            }
        }

        return new Coverage(List.copyOf(emitters), Set.copyOf(activeRelays), Map.copyOf(relayParents));
    }

    public static double radiusBlocks(RangeNode node) {
        PhoenixTeslaConfigs.TowerConfigs cfg = PhoenixTeslaConfigs.get().towers;
        if (!node.type.isTower()) return cfg.relay.rangeBlocks;

        PhoenixTeslaConfigs.TowerProfile profile = profileFor(cfg, node.type);
        return profile.infiniteRange ? -1 : profile.rangeBlocks;
    }

    private static PhoenixTeslaConfigs.TowerProfile profileFor(PhoenixTeslaConfigs.TowerConfigs cfg,
                                                               TeslaTeamEnergyData.RangeNodeType type) {
        return switch (type) {
            case TOWER_BASIC -> cfg.tier1Basic;
            case TOWER_ADVANCED -> cfg.tier2Advanced;
            default -> cfg.tier3Ultimate;
        };
    }

    private static double square(int blocks) {
        return (double) blocks * blocks;
    }
}
