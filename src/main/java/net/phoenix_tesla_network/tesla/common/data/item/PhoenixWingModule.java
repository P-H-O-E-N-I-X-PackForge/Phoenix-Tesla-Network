package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.common.module.AdvancedJetpackModule;
import com.gregtechceu.gtceu.common.module.CreativeFlightModule;
import com.gregtechceu.gtceu.common.module.JetpackModule;
import com.gregtechceu.gtceu.common.module.LiquidFuelJetpackModule;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.UUID;

/**
 * Chest module: wing flight. Mutually exclusive with every GTM flight module (jetpacks, creative flight).
 * NOT COMPILED.
 */
public class PhoenixWingModule extends ItemModule {

    public static final String[] MODES = { "basic", "powered", "creative", "creative+wings" };
    private static final String STATE_KEY = "phoenix_wing_state";

    public PhoenixWingModule(ResourceLocation id) {
        super(id);
    }

    // ---- exclusivity --------------------------------------------------------------------------------------------

    private static boolean isFlightModule(ItemModule m) {
        return m instanceof JetpackModule || m instanceof AdvancedJetpackModule ||
                m instanceof LiquidFuelJetpackModule || m instanceof CreativeFlightModule;
    }

    /** Install-time check: refuse if any GTM flight module is already on the armor. */
    @Override
    public boolean canApplyTo(ItemStack stack) {
        if (!(stack.getItem() instanceof ModularArmorItem armor) || armor.getEquipmentSlot() != EquipmentSlot.CHEST) {
            return false;
        }
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return false;
        for (var ctx : modular.getAllModuleInstances()) {
            if (isFlightModule(ctx.getModule())) return false;
        }
        return super.canApplyTo(stack);
    }

    /**
     * Run-time check. GTM's modules don't know about ours, so they can still be installed afterwards.
     * While any of them is enabled we stand down, so two systems never fight over abilities.mayfly.
     */
    public static boolean conflicted(ModuleContext ctx) {
        for (var other : ctx.getModularItemStack().getAllModuleInstances()) {
            if (other.getModule() != ctx.getModule() && isFlightModule(other.getModule()) &&
                    other.getModule().isEnabled(other)) {
                return true;
            }
        }
        return false;
    }

    // ---- data ---------------------------------------------------------------------------------------------------

    public static final class WingData extends ModuleData {

        /**
         * DFU's P4.and(...) only takes up to four more fields, and WingData has nine in total, so the three
         * sliders are encoded as one nested "tuning" object.
         */
        private record Tuning(int speed, int drift, int vertical) {

            static final Codec<Tuning> CODEC = RecordCodecBuilder.create(t -> t.group(
                    Codec.intRange(0, 20).optionalFieldOf("speed", 5).forGetter(Tuning::speed),
                    Codec.intRange(0, 10).optionalFieldOf("drift", 5).forGetter(Tuning::drift),
                    Codec.intRange(0, 20).optionalFieldOf("vertical", 5).forGetter(Tuning::vertical))
                    .apply(t, Tuning::new));
        }

        public static final MapCodec<WingData> CODEC = RecordCodecBuilder.mapCodec(i -> ModuleData.baseCodec(i)
                .and(i.group(
                        Codec.INT.optionalFieldOf("mode", 0).forGetter(WingData::mode),
                        Tuning.CODEC.optionalFieldOf("tuning", new Tuning(5, 5, 5))
                                .forGetter(w -> new Tuning(w.flightSpeed, w.flightDrift, w.flightVertical)),
                        Codec.BOOL.optionalFieldOf("keepFlyingOnLand", false)
                                .forGetter(WingData::keepFlyingOnLand)))
                .apply(i, (slot, module, item, enabled, mode, t, keep) -> new WingData(slot, module, item, enabled,
                        mode, t.speed(), t.drift(), t.vertical(), keep)));

        private final int mode;
        private final int flightSpeed, flightDrift, flightVertical;
        private final boolean keepFlyingOnLand;

        public WingData(int slot, ItemModule module, ItemStack item, boolean enabled, int mode, int speed,
                        int drift, int vertical, boolean keep) {
            super(slot, module, item, enabled);
            this.mode = Math.max(0, Math.min(MODES.length - 1, mode));
            this.flightSpeed = speed;
            this.flightDrift = drift;
            this.flightVertical = vertical;
            this.keepFlyingOnLand = keep;
        }

        public int mode() {
            return mode;
        }

        public String flightMode() {
            return MODES[mode];
        }

        public int flightSpeed() {
            return flightSpeed;
        }

        public int flightDrift() {
            return flightDrift;
        }

        public int flightVertical() {
            return flightVertical;
        }

        public boolean keepFlyingOnLand() {
            return keepFlyingOnLand;
        }

        public boolean isCreative() {
            return mode >= 2;
        }

        private WingData w(ItemStack item, boolean en, int m, int s, int d, int v, boolean k) {
            return new WingData(slot, module, item, en, m, s, d, v, k);
        }

        @Override
        public ModuleData withModuleItem(ItemStack i) {
            return w(i, enabled, mode, flightSpeed, flightDrift, flightVertical, keepFlyingOnLand);
        }

        @Override
        public ModuleData withEnabled(boolean e) {
            return w(moduleItem, e, mode, flightSpeed, flightDrift, flightVertical, keepFlyingOnLand);
        }

        @Override
        public ModuleData copy() {
            return w(moduleItem.copy(), enabled, mode, flightSpeed, flightDrift, flightVertical, keepFlyingOnLand);
        }

        public WingData withMode(int v) {
            return w(moduleItem, enabled, v, flightSpeed, flightDrift, flightVertical, keepFlyingOnLand);
        }

        public WingData withSpeed(int v) {
            return w(moduleItem, enabled, mode, v, flightDrift, flightVertical, keepFlyingOnLand);
        }

        public WingData withDrift(int v) {
            return w(moduleItem, enabled, mode, flightSpeed, v, flightVertical, keepFlyingOnLand);
        }

        public WingData withVertical(int v) {
            return w(moduleItem, enabled, mode, flightSpeed, flightDrift, v, keepFlyingOnLand);
        }

        public WingData withKeep(boolean v) {
            return w(moduleItem, enabled, mode, flightSpeed, flightDrift, flightVertical, v);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof WingData x && super.equals(o) && mode == x.mode && flightSpeed == x.flightSpeed &&
                    flightDrift == x.flightDrift && flightVertical == x.flightVertical &&
                    keepFlyingOnLand == x.keepFlyingOnLand;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mode, flightSpeed, flightDrift, flightVertical, keepFlyingOnLand);
        }
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return WingData.class;
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
        return WingData.CODEC;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new WingData(slot, module, moduleStack, true, 0, 5, 5, 5, false);
    }

    // ---- behaviour ----------------------------------------------------------------------------------------------

    @Override
    public Component getInfo() {
        return Component.translatable(getDescriptionLanguageKey());
    }

    /** Transient per-player state (landing slide, ground streak, ...). Entity data, not stack NBT: no resyncs. */
    public static CompoundTag state(Player player) {
        CompoundTag pd = player.getPersistentData();
        CompoundTag s = pd.contains(STATE_KEY) ? pd.getCompound(STATE_KEY) : new CompoundTag();
        pd.put(STATE_KEY, s);
        return s;
    }

    private void run(ModuleContext ctx, Player player) {
        CompoundTag state = state(player);
        if (conflicted(ctx)) {
            PhoenixWingFlight.disableFlight(player, state);
            return;
        }
        UUID team = PhoenixModules.Tesla.teamId(player);
        TeslaTeamEnergyData teslaData = null;
        boolean online = false;
        if (player.level() instanceof ServerLevel sl) {
            teslaData = TeslaTeamEnergyData.get(sl);
            online = PhoenixModules.Tesla.network(player) != null;
        }
        PhoenixWingFlight.tick(player, ctx.getData(WingData.class), isEnabled(ctx), state, player.level(), online,
                teslaData, team);
    }

    @Override
    public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
        super.onArmorTick(ctx, entity);
        if (entity instanceof Player p && !p.level().isClientSide) run(ctx, p);
    }

    @Override
    public void clientSideArmorTick(ModuleContext ctx, Player player) {
        run(ctx, player);
    }

    @Override
    public void onUnequip(ModuleContext ctx, LivingEntity entity) {
        if (entity instanceof Player p) PhoenixWingFlight.disableFlight(p, state(p));
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext ctx, PanelSyncManager psm, int id) {
        // ItemModuleSettingsBuilder has no enum/cycle widget: mode is a 0..3 slider (see lang for the legend).
        // intSetting pins the int overload of num(...); untyped lambdas are ambiguous with the double one.
        ItemModuleSettingsBuilder b = super.getSettings(ctx, psm, id);
        PhoenixMovementModules.intSetting(b, Text.lang("gui.phoenix_tesla_network.wing.mode"),
                () -> ctx.getData(WingData.class).mode(),
                v -> ctx.setData(ctx.getData(WingData.class).withMode(v)), 0, MODES.length - 1);
        PhoenixMovementModules.intSetting(b, Text.lang("gui.phoenix_tesla_network.wing.speed"),
                () -> ctx.getData(WingData.class).flightSpeed(),
                v -> ctx.setData(ctx.getData(WingData.class).withSpeed(v)), 0, 20);
        PhoenixMovementModules.intSetting(b, Text.lang("gui.phoenix_tesla_network.wing.drift"),
                () -> ctx.getData(WingData.class).flightDrift(),
                v -> ctx.setData(ctx.getData(WingData.class).withDrift(v)), 0, 10);
        PhoenixMovementModules.intSetting(b, Text.lang("gui.phoenix_tesla_network.wing.vertical"),
                () -> ctx.getData(WingData.class).flightVertical(),
                v -> ctx.setData(ctx.getData(WingData.class).withVertical(v)), 0, 20);
        return b.bool(Text.lang("gui.phoenix_tesla_network.wing.keep_flying"),
                () -> ctx.getData(WingData.class).keepFlyingOnLand(),
                v -> ctx.setData(ctx.getData(WingData.class).withKeep(v)));
    }
}
