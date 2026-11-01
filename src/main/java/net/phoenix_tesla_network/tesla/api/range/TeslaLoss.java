package net.phoenix_tesla_network.tesla.api.range;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.TeamEnergy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Transmission loss. Every tower tier has its own {@link PhoenixTeslaConfigs.LossConfigs}; the one that applies to a
 * given transfer is the config of the tower (or, for an extender, the tower powering it) that serves the receiving end,
 * as picked by {@link TeslaRange#reach}.
 */
public final class TeslaLoss {

    private TeslaLoss() {}

    public static double efficiency(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        TeslaRange.Reach reach = TeslaRange.reach(team, dimension, pos);
        if (reach == null) return 1.0;
        return 1.0 - lossFraction(reach);
    }

    public static double suitEfficiency(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        TeslaRange.Reach reach = TeslaRange.reach(team, dimension, pos);
        if (reach == null) return 1.0;
        if (!TeslaRange.lossConfig(reach.lossType()).applyToSuit) return 1.0;
        return 1.0 - lossFraction(reach);
    }

    /** Loss (0..1) of the tower config that {@code reach} was resolved against. */
    public static double lossFraction(TeslaRange.Reach reach) {
        return lossFraction(TeslaRange.lossConfig(reach.lossType()), reach);
    }

    public static double lossFraction(PhoenixTeslaConfigs.LossConfigs cfg, TeslaRange.Reach reach) {
        return lossFraction(cfg, reach.distance(), reach.crossDimension());
    }

    public static double lossFraction(PhoenixTeslaConfigs.LossConfigs cfg, double distance, boolean crossDimension) {
        if (!cfg.enabled) return 0.0;
        double loss = cfg.baseLossPercent;
        if (!crossDimension) {
            loss += cfg.lossPercentPerUnit * Math.pow(distance / cfg.distanceUnitBlocks, cfg.exponent);
        } else {
            loss += cfg.crossDimensionLossPercent;
        }
        return Math.min(cfg.maxLossPercent, Math.max(0.0, loss)) / 100.0;
    }

    /** What a sender must put in so that {@code delivered} arrives (rounds up). */
    public static BigInteger gross(BigInteger delivered, double efficiency) {
        if (efficiency >= 1.0 || delivered.signum() <= 0) return delivered;
        return new BigDecimal(delivered).divide(BigDecimal.valueOf(efficiency), 0, RoundingMode.CEILING)
                .toBigInteger();
    }

    /** What arrives when {@code gross} is sent (rounds down). */
    public static BigInteger net(BigInteger gross, double efficiency) {
        if (efficiency >= 1.0 || gross.signum() <= 0) return gross;
        return new BigDecimal(gross).multiply(BigDecimal.valueOf(efficiency)).setScale(0, RoundingMode.FLOOR)
                .toBigInteger();
    }

    public static long grossLong(long delivered, double efficiency) {
        return gross(BigInteger.valueOf(delivered), efficiency).min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    public static long netLong(long gross, double efficiency) {
        return net(BigInteger.valueOf(gross), efficiency).longValue();
    }
}
