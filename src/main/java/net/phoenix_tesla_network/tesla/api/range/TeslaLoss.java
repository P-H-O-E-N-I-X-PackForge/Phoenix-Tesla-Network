package net.phoenix_tesla_network.tesla.api.range;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData.TeamEnergy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public final class TeslaLoss {

    private TeslaLoss() {}

    public static double efficiency(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        var cfg = PhoenixTeslaConfigs.get().towers.loss;
        if (!cfg.enabled) return 1.0;

        TeslaRange.Reach reach = TeslaRange.reach(team, dimension, pos);
        if (reach == null) return 1.0;

        return 1.0 - lossFraction(cfg, reach);
    }

    public static double suitEfficiency(TeamEnergy team, ResourceKey<Level> dimension, BlockPos pos) {
        if (!PhoenixTeslaConfigs.get().towers.loss.applyToSuit) return 1.0;
        return efficiency(team, dimension, pos);
    }

    public static double lossFraction(PhoenixTeslaConfigs.LossConfigs cfg, TeslaRange.Reach reach) {
        double loss = cfg.baseLossPercent;
        if (!reach.crossDimension()) {
            loss += cfg.lossPercentPerUnit * Math.pow(reach.distance() / cfg.distanceUnitBlocks, cfg.exponent);
        } else {
            loss += cfg.crossDimensionLossPercent;
        }
        return Math.min(cfg.maxLossPercent, Math.max(0.0, loss)) / 100.0;
    }

    public static BigInteger gross(BigInteger delivered, double efficiency) {
        if (efficiency >= 1.0 || delivered.signum() <= 0) return delivered;
        return new BigDecimal(delivered).divide(BigDecimal.valueOf(efficiency), 0, RoundingMode.CEILING)
                .toBigInteger();
    }

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
