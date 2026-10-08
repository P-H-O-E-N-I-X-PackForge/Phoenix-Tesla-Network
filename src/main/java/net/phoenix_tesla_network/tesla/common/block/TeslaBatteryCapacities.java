package net.phoenix_tesla_network.tesla.common.block;

import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** What each Tesla Battery tier stores, straight from the config. */
public final class TeslaBatteryCapacities {

    private TeslaBatteryCapacities() {}

    public static BigInteger capacityFor(int tier) {
        double configured = PhoenixTeslaConfigs.get().teslaBatteries.capacityFor(tier);
        return BigDecimal.valueOf(configured).setScale(0, RoundingMode.HALF_UP).toBigInteger().max(BigInteger.ONE);
    }
}
