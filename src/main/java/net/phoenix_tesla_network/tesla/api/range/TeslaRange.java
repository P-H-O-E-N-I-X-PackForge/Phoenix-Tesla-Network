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
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.RangeNodeType;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.TeamEnergy;

import java.util.*;

public final class TeslaRange {

    private record Emitter(RangeNode source, ResourceKey<Level> dimension, BlockPos pos, double radiusSqr,
                           boolean anyDimension, RangeNodeType lossType) {

        boolean covers(ResourceKey<Level> dim, BlockPos target) {
            if (!anyDimension && !dimension.equals(dim)) return false;
            return radiusSqr < 0 || pos.distSqr(target) <= radiusSqr;
        }
    }

    private record Coverage(List<Emitter> emitters, Set<String> activeRelayKeys, Map<String, RangeNode> relayParents,
                            Map<String, RangeNodeType> relayLossTypes) {

        boolean covers(ResourceKey<Level> dim, BlockPos target) {
            for (Emitter emitter : emitters) {
                if (emitter.covers(dim, target)) return true;
            }
            return false;
        }

        /**
         * The covering source with the lowest loss at {@code target}, judged with each source's own tower loss
         * config. Same-dimension sources win over cross-dimension ones; ties go to the nearer source.
         */
        Reach reach(ResourceKey<Level> dim, BlockPos target) {
            Reach best = null;
            double bestLoss = Double.MAX_VALUE;
            for (Emitter emitter : emitters) {
                if (!emitter.covers(dim, target)) continue;
                boolean cross = !emitter.dimension().equals(dim);
                double distance = cross ? 0 : Math.sqrt(emitter.pos().distSqr(target));
                double loss = TeslaLoss.lossFraction(lossConfig(emitter.lossType()), distance, cross);
                if (best == null || better(cross, loss, distance, best, bestLoss)) {
                    best = new Reach(distance, cross, emitter.lossType());
                    bestLoss = loss;
                }
            }
            return best;
        }

        private static boolean better(boolean cross, double loss, double distance, Reach best, double bestLoss) {
            if (cross != best.crossDimension()) return !cross;
            if (loss != bestLoss) return loss < bestLoss;
            return distance < best.distance();
        }
    }

    /**
     * @param lossType which tower tier's loss config applies (always a tower type, never RELAY: an extender takes the
     *                 type of the tower that powers it)
     */
    public record Reach(double distance, boolean crossDimension, RangeNodeType lossType) {}

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

    /**
     * Which tower tier's loss config governs {@code node}: a tower's own tier, or for a working extender the tier of
     * the tower it hangs off. Null for an extender that is not connected.
     */
    public static RangeNodeType lossTypeOf(TeamEnergy team, RangeNode node) {
        if (node.type.isTower()) return node.type;
        if (!team.rangeManaged) return null;
        return coverage(team).relayLossTypes().get(node.key());
    }

    /** The loss settings of one tower tier. */
    public static PhoenixTeslaConfigs.LossConfigs lossConfig(RangeNodeType type) {
        return profileFor(PhoenixTeslaConfigs.get().towers, type).loss;
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
                emitters.add(new Emitter(node, node.dimension, node.pos, radiusSqr, profile.crossDimension,
                        node.type));
            } else if (cfg.relay.enabled) {
                relays.add(node);
            }
        }

        Set<String> activeRelays = new HashSet<>();
        Map<String, RangeNode> relayParents = new HashMap<>();
        Map<String, RangeNodeType> relayLossTypes = new HashMap<>();
        double relayRadiusSqr = square(cfg.relay.rangeBlocks);
        boolean progressed = true;
        while (progressed && !relays.isEmpty()) {
            progressed = false;
            for (Iterator<RangeNode> it = relays.iterator(); it.hasNext();) {
                RangeNode relay = it.next();
                Emitter parent = null;
                double parentDist = Double.MAX_VALUE;
                double parentLoss = Double.MAX_VALUE;
                for (Emitter emitter : emitters) {
                    if (!emitter.covers(relay.dimension, relay.pos)) continue;

                    boolean sameDim = emitter.dimension().equals(relay.dimension);
                    double dist = sameDim ? emitter.pos().distSqr(relay.pos) : Double.MAX_VALUE / 2;
                    double loss = TeslaLoss.lossFraction(lossConfig(emitter.lossType()),
                            sameDim ? Math.sqrt(dist) : 0, !sameDim);
                    if (parent == null || loss < parentLoss || (loss == parentLoss && dist < parentDist)) {
                        parent = emitter;
                        parentDist = dist;
                        parentLoss = loss;
                    }
                }
                if (parent != null) {
                    emitters.add(new Emitter(relay, relay.dimension, relay.pos, relayRadiusSqr, false,
                            parent.lossType()));
                    relayLossTypes.put(relay.key(), parent.lossType());
                    activeRelays.add(relay.key());
                    if (parent.dimension().equals(relay.dimension)) relayParents.put(relay.key(), parent.source());
                    it.remove();
                    progressed = true;
                }
            }
        }

        return new Coverage(List.copyOf(emitters), Set.copyOf(activeRelays), Map.copyOf(relayParents),
                Map.copyOf(relayLossTypes));
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
