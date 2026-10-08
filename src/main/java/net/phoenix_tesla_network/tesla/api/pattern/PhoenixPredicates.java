package net.phoenix_tesla_network.tesla.api.pattern;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.phoenix_tesla_network.tesla.PhoenixTeslaAPI;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;
import net.phoenix_tesla_network.tesla.common.block.TeslaBatteryBlock;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

import static net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine.TTB_BATTERY_HEADER;

public class PhoenixPredicates {

    public static TraceabilityPredicate teslaBatteries() {
        return teslaBatteries(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static TraceabilityPredicate teslaBatteries(int minTier, int maxTier) {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();

            for (Map.Entry<ITeslaBattery, Supplier<TeslaBatteryBlock>> entry : PhoenixTeslaAPI.TESLA_BATTERIES
                    .entrySet()) {
                if (state.is(entry.getValue().get())) {
                    ITeslaBattery battery = entry.getKey();

                    if (battery.getTier() != -1 && (battery.getTier() < minTier || battery.getTier() > maxTier)) {
                        return false;
                    }

                    if (battery.getTier() != -1 && battery.getCapacity().compareTo(BigInteger.ZERO) > 0) {
                        String key = TTB_BATTERY_HEADER + battery.getBatteryName();

                        TeslaTowerMachine.BatteryMatchWrapper wrapper = blockWorldState.getMatchContext()
                                .getOrCreate(key, () -> new TeslaTowerMachine.BatteryMatchWrapper(battery));

                        wrapper.increment();
                    }
                    return true;
                }
            }
            return false;
        }, () -> PhoenixTeslaAPI.TESLA_BATTERIES.entrySet().stream()
                .filter(entry -> entry.getKey().getTier() >= minTier && entry.getKey().getTier() <= maxTier)
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getTier()))
                .map(entry -> new BlockInfo(entry.getValue().get().defaultBlockState(), null))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static TraceabilityPredicate tieredAbilities(int minTier, int maxTier, PartAbility... abilities) {
        int lo = Math.min(minTier, maxTier);
        int hi = Math.max(minTier, maxTier);

        List<Block> blocks = new ArrayList<>();
        for (PartAbility ability : abilities) {
            blocks.addAll(ability.getBlockRange(lo, hi));
        }
        return Predicates.blocks(blocks.toArray(Block[]::new));
    }
}
