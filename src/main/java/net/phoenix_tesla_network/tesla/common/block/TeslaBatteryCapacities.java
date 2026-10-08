package net.phoenix_tesla_network.tesla.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.block.BatteryBlock;
import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.ItemStack;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;

import com.tterrag.registrate.util.entry.ItemEntry;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public final class TeslaBatteryCapacities {

    private static final long LV_FALLBACK = 120_000L;
    private static final long MV_FALLBACK = 420_000L;
    private static final long HV_FALLBACK = 1_800_000L;

    private static final Map<Integer, Long> ITEM_CACHE = new HashMap<>();

    private TeslaBatteryCapacities() {}

    public static BigInteger capacityFor(int tier) {
        var cfg = PhoenixTeslaConfigs.get().teslaBatteries;
        BigDecimal scaled = BigDecimal.valueOf(referenceCapacity(tier))
                .multiply(BigDecimal.valueOf(cfg.multiplierFor(tier)))
                .multiply(BigDecimal.valueOf(cfg.globalMultiplier));
        return scaled.setScale(0, RoundingMode.HALF_UP).toBigInteger().max(BigInteger.ONE);
    }

    public static long referenceCapacity(int tier) {
        return switch (tier) {
            case GTValues.LV -> lithiumBattery(tier, GTItems.BATTERY_LV_LITHIUM, LV_FALLBACK);
            case GTValues.MV -> lithiumBattery(tier, GTItems.BATTERY_MV_LITHIUM, MV_FALLBACK);
            case GTValues.HV -> lithiumBattery(tier, GTItems.BATTERY_HV_LITHIUM, HV_FALLBACK);
            case GTValues.EV -> BatteryBlock.BatteryPartType.EV_LAPOTRONIC.getCapacity();
            case GTValues.IV -> BatteryBlock.BatteryPartType.IV_LAPOTRONIC.getCapacity();
            case GTValues.LuV -> BatteryBlock.BatteryPartType.LuV_LAPOTRONIC.getCapacity();
            case GTValues.ZPM -> BatteryBlock.BatteryPartType.ZPM_LAPOTRONIC.getCapacity();
            case GTValues.UV -> BatteryBlock.BatteryPartType.UV_LAPOTRONIC.getCapacity();
            default -> throw new IllegalArgumentException("No reference capacity for tier " + tier);
        };
    }

    private static long lithiumBattery(int tier, ItemEntry<ComponentItem> battery, long fallback) {
        Long cached = ITEM_CACHE.get(tier);
        if (cached != null) return cached;

        IElectricItem electric = GTCapabilityHelper.getElectricItem(new ItemStack(battery.get()));
        if (electric == null || electric.getMaxCharge() <= 0) return fallback;

        ITEM_CACHE.put(tier, electric.getMaxCharge());
        return electric.getMaxCharge();
    }
}
