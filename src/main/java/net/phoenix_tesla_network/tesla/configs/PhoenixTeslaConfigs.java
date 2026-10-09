package net.phoenix_tesla_network.tesla.configs;

import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;

@Config(id = PhoenixTeslaNetwork.MOD_ID)
public class PhoenixTeslaConfigs {

    public static PhoenixTeslaConfigs INSTANCE;
    public static ConfigHolder<PhoenixTeslaConfigs> CONFIG_HOLDER;

    public static void init() {
        if (INSTANCE != null) return;
        CONFIG_HOLDER = Configuration.registerConfig(PhoenixTeslaConfigs.class, ConfigFormats.yaml());
        INSTANCE = CONFIG_HOLDER.getConfigInstance();
    }

    public static PhoenixTeslaConfigs get() {
        if (INSTANCE == null) init();
        return INSTANCE;
    }

    @Configurable
    public FeatureConfigs features = new FeatureConfigs();

    @Configurable
    public WingFlightConfigs wingFlight = new WingFlightConfigs();

    @Configurable
    public TowerConfigs towers = new TowerConfigs();

    @Configurable
    @Configurable.Comment({
            "How much energy (EU) one Tesla Battery of each tier stores. A tower's buffer is the sum of its",
            "batteries. Changing a value changes how much every existing battery of that tier holds."
    })
    public BatteryConfigs teslaBatteries = new BatteryConfigs();

    public static class BatteryConfigs {

        @Configurable
        @Configurable.Comment({ "ULV Tesla Battery capacity, in EU. Default 960,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double ulvCapacity = 160_000.0;

        @Configurable
        @Configurable.Comment({ "LV Tesla Battery capacity, in EU. Default 960,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double lvCapacity = 960_000.0;

        @Configurable
        @Configurable.Comment({ "MV Tesla Battery capacity, in EU. Default 6,720,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double mvCapacity = 6_720_000.0;

        @Configurable
        @Configurable.Comment({ "HV Tesla Battery capacity, in EU. Default 57,600,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double hvCapacity = 57_600_000.0;

        @Configurable
        @Configurable.Comment({ "EV Tesla Battery capacity, in EU. Default 37,500,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double evCapacity = 37_500_000.0;

        @Configurable
        @Configurable.Comment({ "IV Tesla Battery capacity, in EU. Default 750,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double ivCapacity = 750_000_000.0;

        @Configurable
        @Configurable.Comment({ "LuV Tesla Battery capacity, in EU. Default 6,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double luvCapacity = 6_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "ZPM Tesla Battery capacity, in EU. Default 48,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double zpmCapacity = 48_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "UV Tesla Battery capacity, in EU. Default 384,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double uvCapacity = 384_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "UHV Tesla Battery capacity, in EU. Default 1,000,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double uhvCapacity = 1_000_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "UEV Tesla Battery capacity, in EU. Default 5,000,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double uevCapacity = 5_000_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "UIV Tesla Battery capacity, in EU. Default 25,000,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double uivCapacity = 25_000_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "UXV Tesla Battery capacity, in EU. Default 100,000,000,000,000." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double uxvCapacity = 100_000_000_000_000.0;

        @Configurable
        @Configurable.Comment({ "OPV Tesla Battery capacity, in EU. Default 3.689349e+21." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double opvCapacity = 3.689349e+21;

        @Configurable
        @Configurable.Comment({ "MAX Tesla Battery capacity, in EU. Default 8.301035e+27." })
        @Configurable.DecimalRange(min = 1.0, max = 1.0E30)
        public double maxCapacity = 8.301035e+27;

        public double capacityFor(int tier) {
            return switch (tier) {
                case 0 -> ulvCapacity;
                case 1 -> lvCapacity;
                case 2 -> mvCapacity;
                case 3 -> hvCapacity;
                case 4 -> evCapacity;
                case 5 -> ivCapacity;
                case 6 -> luvCapacity;
                case 7 -> zpmCapacity;
                case 8 -> uvCapacity;
                case 9 -> uhvCapacity;
                case 10 -> uevCapacity;
                case 11 -> uivCapacity;
                case 12 -> uxvCapacity;
                case 13 -> opvCapacity;
                case 14 -> maxCapacity;
                default -> 1.0;
            };
        }
    }

    public static class LossConfigs {

        @Configurable
        @Configurable.Comment({
                "Master switch. False means lossless transfer everywhere (no other loss setting matters).",
                "",
                "loss % = min(maxLossPercent, baseLossPercent",
                "              + lossPercentPerUnit * (distance / distanceUnitBlocks) ^ exponent)",
                "  distance = blocks from the receiving end to the nearest covering tower or extender,",
                "  so extenders cut loss. If only a cross-dimensional tower covers it, crossDimensionLossPercent",
                "  is used instead of the distance term.",
                "A machine receives (100 - loss)% of what the network spends, and the network receives",
                "(100 - loss)% of what a generator sends."
        })
        public boolean enabled = true;

        @Configurable
        @Configurable.Comment({ "Flat loss on every transfer, even right next to a tower. Percent." })
        @Configurable.DecimalRange(min = 0.0, max = 95.0)
        public double baseLossPercent = 0.0;

        @Configurable
        @Configurable.Comment({ "Extra loss per distanceUnitBlocks of distance (before the exponent). Percent." })
        @Configurable.DecimalRange(min = 0.0, max = 95.0)
        public double lossPercentPerUnit = 5.0;

        @Configurable
        @Configurable.Comment({ "Distance, in blocks, that lossPercentPerUnit is measured per." })
        @Configurable.DecimalRange(min = 1.0, max = 1_000_000.0)
        public double distanceUnitBlocks = 128.0;

        @Configurable
        @Configurable.Comment({
                "1 = loss grows linearly with distance, above 1 = nearly lossless close in and steep far out,",
                "below 1 = most of the loss comes early and flattens out."
        })
        @Configurable.DecimalRange(min = 0.1, max = 4.0)
        public double exponent = 1.0;

        @Configurable
        @Configurable.Comment({ "Loss used instead of the distance term when only a cross-dimensional tower covers",
                "the receiving end (it is in a different dimension from the tower). Percent." })
        @Configurable.DecimalRange(min = 0.0, max = 95.0)
        public double crossDimensionLossPercent = 10.0;

        @Configurable
        @Configurable.Comment({ "Loss never goes above this. Percent." })
        @Configurable.DecimalRange(min = 0.0, max = 95.0)
        public double maxLossPercent = 50.0;

        @Configurable
        @Configurable.Comment({
                "Whether the Phoenix Tech Suite's tool charging from the network is subject to loss.",
                "The suit's own running costs (flight, rebirth, ...) are never reduced by loss."
        })
        public boolean applyToSuit = true;
    }

    public static class TowerConfigs {

        @Configurable
        @Configurable.Comment({
                "Basic Tesla Tower (tier 1). Finite range by default - extend it with Tesla Range Extenders.",
                "GT tier numbers used by every tier setting below: 0=ULV 1=LV 2=MV 3=HV 4=EV 5=IV 6=LuV 7=ZPM",
                "8=UV 9=UHV 10=UEV 11=UIV 12=UXV 13=OpV 14=MAX",
                "Glass defaults: tempered + laminated + fusion."
        })
        public TowerProfile tier1Basic = new TowerProfile(true, 1, 5, 1, 5, 128, false, false)
                .glass(true, true, true);

        @Configurable
        @Configurable.Comment({
                "Advanced Tesla Tower (tier 2). Infinite range inside its own dimension by default.",
                "Glass defaults: laminated + fusion."
        })
        public TowerProfile tier2Advanced = new TowerProfile(true, 1, 9, 1, 9, 128, true, false)
                .glass(false, true, true);

        @Configurable
        @Configurable.Comment({
                "Tesla Tower (tier 3, the original tower - existing towers keep this behavior).",
                "Infinite range and cross-dimensional by default.",
                "Glass defaults: fusion only."
        })
        public TowerProfile tier3Ultimate = new TowerProfile(true, 0, 14, 1, 14, 128, true, true)
                .glass(false, false, true);

        @Configurable
        @Configurable.Comment({
                "Tesla Range Extender: a multiblock that adds its own coverage radius to the network.",
                "It only works while it is itself inside the coverage of a tower or another working extender,",
                "so extenders can be chained outward from a tower."
        })
        public RelayConfig relay = new RelayConfig();

        @Configurable
        @Configurable.Comment({
                "Transmission loss. Energy moving between the team network and a hatch, linked machine, wireless",
                "charger or the suit's tool charging is scaled by the efficiency below, based on how far the",
                "receiving end is from the nearest tower or range extender covering it."
        })
        public LossConfigs loss = new LossConfigs();

        @Configurable
        @Configurable.Comment({
                "Whether the Phoenix Tech Suite (flight, wireless tool charging, rebirth, ...) must be inside",
                "the network's range to use the network. Has no effect on infinite-range towers."
        })
        public boolean suitRespectsRange = true;
    }

    public static class TowerProfile {

        public TowerProfile() {}

        public TowerProfile(boolean enabled, int minHatchTier, int maxHatchTier, int minBatteryTier,
                            int maxBatteryTier, int rangeBlocks, boolean infiniteRange, boolean crossDimension) {
            this.enabled = enabled;
            this.minHatchTier = minHatchTier;
            this.maxHatchTier = maxHatchTier;
            this.minBatteryTier = minBatteryTier;
            this.maxBatteryTier = maxBatteryTier;
            this.rangeBlocks = rangeBlocks;
            this.infiniteRange = infiniteRange;
            this.crossDimension = crossDimension;
        }

        public TowerProfile glass(boolean tempered, boolean laminated, boolean fusion) {
            this.allowTemperedGlass = tempered;
            this.allowLaminatedGlass = laminated;
            this.allowFusionGlass = fusion;
            return this;
        }

        @Configurable
        @Configurable.Comment({
                "Whether this tower is registered at all. Disabling removes the block from the game",
                "(existing placed towers of a disabled type will be lost). Needs a restart."
        })
        public boolean enabled = true;

        @Configurable
        @Configurable.Comment({ "Lowest energy hatch tier this tower accepts (GT tier number).", "Needs a restart." })
        @Configurable.Range(min = 0, max = 14)
        public int minHatchTier = 1;

        @Configurable
        @Configurable.Comment({
                "Highest energy hatch tier this tower accepts (GT tier number). Applies to energy, substation",
                "and laser hatches. Needs a restart."
        })
        @Configurable.Range(min = 0, max = 14)
        public int maxHatchTier = 14;

        @Configurable
        @Configurable.Comment({
                "Whether energy input hatches can be placed on this tower. If false the tower has no mandatory hatch. Needs a restart." })
        public boolean allowEnergyInput = true;

        @Configurable
        @Configurable.Comment({ "Whether energy output hatches can be placed on this tower. Needs a restart." })
        public boolean allowEnergyOutput = true;

        @Configurable
        @Configurable.Comment({ "Whether substation input hatches can be placed on this tower. Needs a restart." })
        public boolean allowSubstationInput = true;

        @Configurable
        @Configurable.Comment({ "Whether substation output hatches can be placed on this tower. Needs a restart." })
        public boolean allowSubstationOutput = true;

        @Configurable
        @Configurable.Comment({ "Whether laser input hatches can be placed on this tower. Needs a restart." })
        public boolean allowLaserInput = true;

        @Configurable
        @Configurable.Comment({ "Whether laser output hatches can be placed on this tower. Needs a restart." })
        public boolean allowLaserOutput = true;

        @Configurable
        @Configurable.Comment({ "Whether item and fluid hatches can be placed on this tower. Needs a restart." })
        public boolean allowItemFluidHatches = true;

        @Configurable
        @Configurable.Comment({ "Whether a maintenance hatch can be placed on this tower. Needs a restart." })
        public boolean allowMaintenanceHatch = true;

        @Configurable
        @Configurable.Comment({
                "Whether GT Tempered Glass can be used in this tower's glass slots. Needs a restart.",
                "Any combination of the three glass options works. If all three are false the tower falls back",
                "to allowing all of them (otherwise it could never be built)."
        })
        public boolean allowTemperedGlass = true;

        @Configurable
        @Configurable.Comment({
                "Whether GT Laminated Glass can be used in this tower's glass slots. Needs a restart." })
        public boolean allowLaminatedGlass = true;

        @Configurable
        @Configurable.Comment({ "Whether Fusion Glass can be used in this tower's glass slots. Needs a restart." })
        public boolean allowFusionGlass = true;

        @Configurable
        @Configurable.Comment({
                "Lowest Tesla Battery tier this tower accepts. Tesla Batteries exist for LV (1) to MAX (14).",
                "Needs a restart."
        })
        @Configurable.Range(min = 0, max = 14)
        public int minBatteryTier = 9;

        @Configurable
        @Configurable.Comment({ "Highest Tesla Battery tier this tower accepts. Needs a restart." })
        @Configurable.Range(min = 0, max = 14)
        public int maxBatteryTier = 14;

        @Configurable
        @Configurable.Comment({
                "Radius, in blocks, of the area this tower covers while infiniteRange is false.",
                "Hatches, linked machines, chargers and suits outside it (and outside every extender) lose access."
        })
        @Configurable.Range(min = 1, max = 30_000_000)
        public int rangeBlocks = 128;

        @Configurable
        @Configurable.Comment({ "If true the tower covers its whole dimension and rangeBlocks is ignored." })
        public boolean infiniteRange = false;

        @Configurable
        @Configurable.Comment({
                "If true the network also reaches other dimensions: range is measured ignoring which dimension",
                "something is in, so an infinite-range cross-dimensional tower covers everything everywhere."
        })
        public boolean crossDimension = false;
    }

    public static class RelayConfig {

        @Configurable
        @Configurable.Comment({
                "Whether the Tesla Range Extender is registered at all. Needs a restart."
        })
        public boolean enabled = true;

        @Configurable
        @Configurable.Comment({ "Radius, in blocks, each working extender adds to the network around itself." })
        @Configurable.Range(min = 1, max = 30_000_000)
        public int rangeBlocks = 128;
    }

    public static class FeatureConfigs {

        @Configurable
        @Configurable.Comment({ "Whether the Tech Suite's on-screen HUD (flight mode, tuning sliders, network",
                "status, rebirth cooldown, energy bar) is drawn at all. Set to false to hide it entirely." })
        public boolean techSuiteHUDEnabled = true;

        @Configurable
        @Configurable.Comment({
                "The connection mode for Tesla Towers.",
                "TEAM_AUTO: All towers under a team/player share the same cloud automatically.",
                "DATA_STICK: Towers must be manually linked to hatches using a Data Stick."
        })
        public TeslaConnectionMode teslaConnectionMode = TeslaConnectionMode.DATA_STICK;

        @Configurable
        @Configurable.Comment({
                "Which Tesla laser hatch sizes exist. Needs a restart (and a world that used a larger set will",
                "lose the hatches of any size you take away).",
                "STANDARD: 256A, 1024A, 4096A.",
                "EXTENDED:  + 16384A, 65536A, 262144A, 1048576A.",
                "MAXIMUM:   + 4194304A, 16777216A (more than any pack should ever need)."
        })
        public LaserHatchSizes laserHatchSizes = LaserHatchSizes.STANDARD;

        public enum LaserHatchSizes {
            STANDARD,
            EXTENDED,
            MAXIMUM
        }

        public enum TeslaConnectionMode {
            TEAM_AUTO,
            DATA_STICK
        }
    }

    public static class WingFlightConfigs {

        @Configurable
        @Configurable.Comment({
                "EU/t drained from the Tesla network during powered elytra/sonic flight.",
                "Speed and boost scale proportionally with this value.",
                "Default: 5000"
        })
        public long poweredFlightEUt = 5_000L;

        @Configurable
        @Configurable.Comment({
                "EU/t drained from the Tesla network during creative flight mode.",
                "Set to 0 for truly free creative flight.",
                "Fly speed scales proportionally with this value.",
                "Default: 1000"
        })
        public long creativeFlightEUt = 1_000L;

        @Configurable
        @Configurable.Comment({
                "Base boost scale for powered elytra flight at minimum speed setting.",
                "The actual boost = boostMin + (speedSlider * (boostMax - boostMin))",
                "Default: 0.01"
        })
        public double poweredBoostMin = 0.01;

        @Configurable
        @Configurable.Comment({
                "Max boost scale for powered elytra flight at maximum speed setting.",
                "Scales further with poweredFlightEUt so higher drain = faster top speed.",
                "Default: 0.16"
        })
        public double poweredBoostMax = 0.16;

        @Configurable
        @Configurable.Comment({
                "Min creative fly speed (at speed slider = 0).",
                "Default: 0.05"
        })
        public double creativeSpeedMin = 0.05;

        @Configurable
        @Configurable.Comment({
                "Max creative fly speed (at speed slider = 10).",
                "Scales further with creativeFlightEUt so higher drain = faster top speed.",
                "Default: 0.35"
        })
        public double creativeSpeedMax = 0.35;

        @Configurable
        @Configurable.Comment({
                "Horizontal speed, in blocks/tick, for plain \"Creative\" mode's free-strafing flight",
                "at speed slider = 5 (the slider's \"normal\" midpoint, range 0-20, matching vanilla",
                "creative-fly speed) - not creativeSpeedMin/Max's vanilla Abilities.flyingSpeed units,",
                "since vanilla's own flight accumulates well beyond that raw value tick over tick via",
                "friction, but free-strafing sets velocity directly with no such buildup, so it needs",
                "its own, much larger-looking value to reach an equivalent actual speed. Scales",
                "proportionally with the slider on both sides, same as verticalSpeed: 0 = stopped,",
                "10 = 3.5x this, 20 = 7x this.",
                "Default: 0.55 (matches vanilla creative-fly speed at slider = 5)"
        })
        public double creativeFreeSpeedBase = 0.55;

        @Configurable
        @Configurable.Comment({
                "Min speed clamp for powered flight, at speed slider = 0 (the SPEED slider drives this",
                "cap - previously only Drift affected it, so maxing Speed never raised the ceiling).",
                "Default: 0.6"
        })
        public double poweredDriftMin = 0.6;

        @Configurable
        @Configurable.Comment({
                "Max speed clamp for powered flight, at speed slider = 20. Drift loosens it further on",
                "top (up to +50% at drift slider = 10).",
                "Default: 4.0"
        })
        public double poweredDriftMax = 4.0;

        @Configurable
        @Configurable.Comment({
                "Coasting half-life, in seconds, at drift slider = 0: velocity snaps to zero the instant",
                "you stop thrusting as long as this is 0 - not meant to be changed.",
                "Default: 0.0"
        })
        public double coastHalfLifeMin = 0.0;

        @Configurable
        @Configurable.Comment({
                "Coasting half-life, in seconds, at drift slider = 9 (time to bleed half your speed while",
                "airborne and not thrusting). Drift slider = 10 stays a hard zero-decay special case.",
                "Scaling a half-life linearly instead of raw per-tick retention keeps the whole slider",
                "meaningful - retention compounds, so even 0.7/tick is <0.1% of your speed after 1 second.",
                "Default: 8.0"
        })
        public double coastHalfLifeMax = 8.0;

        @Configurable
        @Configurable.Comment({
                "Climb-speed multiplier for powered/sonic flight at vertical-speed slider = 5",
                "(the slider's \"normal\" midpoint, range 0-20) - matches the flat 8x climb boost",
                "this used to be hardcoded to, before the slider existed. Scales proportionally",
                "with the slider on both sides: 0 = none, 10 = double this, 20 = quadruple this.",
                "Default: 8.0"
        })
        public double poweredVerticalBase = 8.0;

        @Configurable
        @Configurable.Comment({
                "Min forward-accel boost for the leggings' sprint boost (at sprint-speed slider = 0).",
                "Actual boost = sprintAccelMin + ((sprintSpeed/20) * (sprintAccelMax - sprintAccelMin))",
                "Default: 0.03"
        })
        public double sprintAccelMin = 0.03;

        @Configurable
        @Configurable.Comment({
                "Max forward-accel boost for the leggings' sprint boost (at sprint-speed slider = 20).",
                "The slider is eased (slider^1.6), so the low and middle settings stay gentle.",
                "Default: 0.6"
        })
        public double sprintAccelMax = 0.6;

        @Configurable
        @Configurable.Comment({
                "Min upward-impulse strength for the boots' boosted jump (at jump-height slider = 1).",
                "The slider is on the Wing Flight Control screen; 0 turns the boosted jump off.",
                "Actual impulse = jumpHeightMin + (((jumpHeight - 1)/19) * (jumpHeightMax - jumpHeightMin))",
                "Default: 0.21"
        })
        public double jumpHeightMin = 0.21;

        @Configurable
        @Configurable.Comment({
                "Max upward-impulse strength for the boots' boosted jump (at jump-height slider = 20).",
                "Default: 0.65"
        })
        public double jumpHeightMax = 0.65;

        @Configurable
        @Configurable.Comment({
                "How high (in blocks) the boots let you step up without jumping, at the step-height slider's",
                "maximum (20). The slider runs: 0 = vanilla, 1 = just over one block (1.0023), up to this at 20.",
                "Default: 6.0"
        })
        @Configurable.DecimalRange(min = 0.6, max = 10.0)
        public double stepHeightMax = 6.0;
    }
}
