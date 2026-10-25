package net.phoenix_tesla_network.tesla.api.pattern;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.MultiPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.PlaceholderError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;
import com.gregtechceu.gtceu.common.block.LampBlock;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.phoenix_tesla_network.tesla.PhoenixTeslaAPI;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;
import net.phoenix_tesla_network.tesla.common.block.TeslaBatteryBlock;

import com.tterrag.registrate.util.entry.BlockEntry;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.common.data.GTBlocks.BORDERLESS_LAMPS;
import static com.gregtechceu.gtceu.common.data.GTBlocks.LAMPS;

public class PhoenixPredicates {

    public static MultiPredicate teslaBatteries() {
        return teslaBatteries(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static MultiPredicate teslaBatteries(int minTier, int maxTier) {
        List<BlockInfo> candidates = PhoenixTeslaAPI.TESLA_BATTERIES.entrySet().stream()
                .filter(e -> e.getKey().getTier() >= minTier && e.getKey().getTier() <= maxTier)
                .sorted(Comparator.comparingInt(e -> e.getKey().getTier()))
                .map(e -> new BlockInfo(e.getValue().get().defaultBlockState()))
                .collect(Collectors.toList());

        return Predicates.builder("tesla_battery")
                .candidates(candidates)
                .predicate(ctx -> {
                    BlockState state = ctx.state();
                    for (Map.Entry<ITeslaBattery, Supplier<TeslaBatteryBlock>> entry : PhoenixTeslaAPI.TESLA_BATTERIES
                            .entrySet()) {
                        if (state.is(entry.getValue().get())) {
                            int tier = entry.getKey().getTier();
                            return tier == -1 || (tier >= minTier && tier <= maxTier);
                        }
                    }
                    return false;
                })
                .errorFunction(ctx -> PlaceholderError.instance())
                .toMultiPredicate()
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.batteries"));
    }

    public static MultiPredicate tieredAbilities(int minTier, int maxTier, PartAbility... abilities) {
        int lo = Math.min(minTier, maxTier);
        int hi = Math.max(minTier, maxTier);

        int[] tiers = new int[hi - lo + 1];
        for (int i = 0; i < tiers.length; i++) tiers[i] = lo + i;

        MultiPredicate combined = null;
        for (PartAbility ability : abilities) {
            MultiPredicate next = Predicates.ability(ability, tiers);
            combined = combined == null ? next : combined.or(next);
        }
        return combined == null ? Predicates.any() : combined;
    }

    @SafeVarargs
    public static MultiPredicate lamps(BlockEntry<LampBlock>... lampEntries) {
        List<BlockInfo> candidates = Arrays.stream(lampEntries)
                .map(e -> new BlockInfo(e.get().defaultBlockState()))
                .collect(Collectors.toList());

        return Predicates.builder("lamp")
                .candidates(candidates)
                .predicate(ctx -> {
                    BlockState state = ctx.state();
                    for (BlockEntry<LampBlock> entry : lampEntries) {
                        if (state.is(entry.get())) return true;
                    }
                    return false;
                })
                .errorFunction(ctx -> PlaceholderError.instance())
                .toMultiPredicate();
    }

    public static MultiPredicate anyLamp() {
        List<BlockEntry<LampBlock>> all = new ArrayList<>();
        all.addAll(LAMPS.values());
        all.addAll(BORDERLESS_LAMPS.values());
        return lamps(all.toArray(BlockEntry[]::new));
    }

    private static final Map<DyeColor, MultiPredicate> LAMPS_BY_COLOR = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            LAMPS_BY_COLOR.put(color, lamps(LAMPS.get(color), BORDERLESS_LAMPS.get(color)));
        }
    }

    public static MultiPredicate lampsByColor(DyeColor color) {
        return LAMPS_BY_COLOR.getOrDefault(color, anyLamp());
    }
}
