package net.phoenix_tesla_network.tesla.common.machine.multiblock.electric;

import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.part.TeslaEnergyHatchPartMachine;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class TeslaWirelessRegistry {

    private static final Map<UUID, Set<TeslaEnergyHatchPartMachine>> TEAM_HATCHES = new HashMap<>();

    private static final Map<UUID, Set<TeslaTowerMachine>> OWNER_TOWERS = new HashMap<>();

    public static void registerHatch(TeslaEnergyHatchPartMachine hatch) {
        if (hatch.getOwnerTeamUUID() == null) return;
        TEAM_HATCHES.computeIfAbsent(hatch.getOwnerTeamUUID(), k -> new HashSet<>()).add(hatch);
    }

    public static void unregisterHatch(TeslaEnergyHatchPartMachine hatch) {
        if (hatch.getOwnerTeamUUID() == null) return;
        Set<TeslaEnergyHatchPartMachine> hatches = TEAM_HATCHES.get(hatch.getOwnerTeamUUID());
        if (hatches != null) {
            hatches.remove(hatch);
            if (hatches.isEmpty()) TEAM_HATCHES.remove(hatch.getOwnerTeamUUID());
        }
    }

    @Nullable
    public static Set<TeslaEnergyHatchPartMachine> getHatches(UUID team) {
        return TEAM_HATCHES.get(team);
    }

    public static void registerTower(TeslaTowerMachine tower) {
        if (tower.getOwnerUUID() != null) {
            OWNER_TOWERS.computeIfAbsent(tower.getOwnerUUID(), k -> new LinkedHashSet<>()).add(tower);
        }
    }

    public static void unregisterTower(TeslaTowerMachine tower) {
        if (tower.getOwnerUUID() != null) {
            Set<TeslaTowerMachine> towers = OWNER_TOWERS.get(tower.getOwnerUUID());
            if (towers != null) {
                towers.remove(tower);
                if (towers.isEmpty()) OWNER_TOWERS.remove(tower.getOwnerUUID());
            }
        }
    }

    @Nullable
    public static TeslaTowerMachine getTowerByTeam(UUID team) {
        Set<TeslaTowerMachine> towers = OWNER_TOWERS.get(team);
        if (towers == null || towers.isEmpty()) return null;
        return towers.iterator().next();
    }

    @Nullable
    public static TeslaTowerMachine.TeslaEnergyBank getBank(UUID team) {
        TeslaTowerMachine tower = getTowerByTeam(team);
        if (tower == null) return null;
        return tower.getEnergyBank();
    }

    public static void tickTeamHatches(UUID team) {
        TeslaTowerMachine.TeslaEnergyBank bank = getBank(team);
        if (bank == null) return;

        Set<TeslaEnergyHatchPartMachine> hatches = getHatches(team);
        if (hatches == null) return;

        for (TeslaEnergyHatchPartMachine hatch : hatches) {

            if (!hatch.isWireless()) continue;

            hatch.tickWireless();
        }
    }
}
