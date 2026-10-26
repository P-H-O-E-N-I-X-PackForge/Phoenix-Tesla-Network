package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.AttributeItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ModuleData;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.common.module.JumpBoostItemModule;
import com.gregtechceu.gtceu.common.module.MovementSpeedItemModule;
import com.gregtechceu.gtceu.common.module.SpeedItemModule;
import com.gregtechceu.gtceu.common.module.StepHeightModule;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Phoenix versions of GTM's sprint / jump / step modules. They keep your old numbers (config-driven, 0..20 sliders,
 * the moveRelative sprint boost and the charge-decay jump), and refuse to share an armor piece with GTM's
 * equivalents. NOT COMPILED.
 */
public final class PhoenixMovementModules {

    private PhoenixMovementModules() {}

    /** Old cost: energyPerUse (16384) / 100 per action; the sprint cost was charged every 10 ticks. */
    private static final double JUMP_EU = 16384 / 100.0;
    private static final double SPRINT_EU_PER_TICK = 16384 / 100.0 / 10.0;

    /**
     * ItemModuleSettingsBuilder.num has int and double overloads, so untyped lambdas are ambiguous. Going through
     * this helper pins the int overload.
     */
    public static ItemModuleSettingsBuilder intSetting(ItemModuleSettingsBuilder builder, Text label,
                                                       IntSupplier getter, IntConsumer setter, int min, int max) {
        return builder.num(label, getter, setter, min, max);
    }

    private static boolean slot(ItemStack stack, EquipmentSlot slot) {
        return stack.getItem() instanceof ModularArmorItem a && a.getEquipmentSlot() == slot;
    }

    private static boolean stackHas(ItemStack stack, Class<?>... types) {
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return false;
        for (var ctx : modular.getAllModuleInstances()) {
            for (Class<?> t : types) if (t.isInstance(ctx.getModule())) return true;
        }
        return false;
    }

    // ---- shared "one slider" data -------------------------------------------------------------------------------

    public static final class LevelData extends ModuleData {

        public static final MapCodec<LevelData> CODEC = RecordCodecBuilder.mapCodec(i -> ModuleData.baseCodec(i)
                .and(Codec.intRange(0, 20).optionalFieldOf("level", 5).forGetter(LevelData::level))
                .apply(i, LevelData::new));

        private final int level;

        public LevelData(int slot, ItemModule module, ItemStack item, boolean enabled, int level) {
            super(slot, module, item, enabled);
            this.level = level;
        }

        public int level() { return level; }
        public LevelData withLevel(int v) { return new LevelData(slot, module, moduleItem, enabled, v); }
        @Override public ModuleData withModuleItem(ItemStack i) { return new LevelData(slot, module, i, enabled, level); }
        @Override public ModuleData withEnabled(boolean e) { return new LevelData(slot, module, moduleItem, e, level); }
        @Override public ModuleData copy() { return new LevelData(slot, module, moduleItem.copy(), enabled, level); }

        @Override
        public boolean equals(Object o) {
            return o instanceof LevelData l && super.equals(o) && level == l.level;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), level);
        }
    }

    public abstract static class LevelModule extends PhoenixModule {

        protected LevelModule(ResourceLocation id) {
            super(id);
        }

        protected abstract int defaultLevel();
        protected abstract int minLevel();
        protected abstract String settingKey();

        public int level(ModuleContext ctx) {
            return Math.max(0, Math.min(20, ctx.getData(LevelData.class).level()));
        }

        @Override public Class<? extends ModuleData> moduleDataClass() { return LevelData.class; }
        @Override public MapCodec<? extends ModuleData> moduleDataCodec() { return LevelData.CODEC; }

        @Override
        public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack stack) {
            return new LevelData(slot, module, stack, true, defaultLevel());
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public ItemModuleSettingsBuilder getSettings(ModuleContext ctx, PanelSyncManager psm, int id) {
            ItemModuleSettingsBuilder settings = super.getSettings(ctx, psm, id);
            settings.add(net.phoenix_tesla_network.tesla.common.ui.PhoenixWidgets.intSliderRow(settingKey(),
                    () -> level(ctx), v -> ctx.setData(ctx.getData(LevelData.class).withLevel(v)), minLevel(), 20));
            return settings;
        }
    }

    // ---- sprint (leggings) --------------------------------------------------------------------------------------

    public static class SprintModule extends LevelModule {

        private static final String LATCH = "phoenix_tesla_sprint_latch";

        public SprintModule(ResourceLocation id) {
            super(id);
        }

        @Override protected int defaultLevel() { return 5; }
        @Override protected int minLevel() { return 0; }
        @Override protected String settingKey() { return "gui.phoenix_tesla_network.sprint.speed"; }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.LEGS) && !stackHas(stack, SpeedItemModule.class,
                    MovementSpeedItemModule.class) && super.canApplyTo(stack);
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (entity instanceof Player p) boost(ctx, p, false);
        }

        @Override
        public void clientSideArmorTick(ModuleContext ctx, Player player) {
            boost(ctx, player, true);
        }

        private void boost(ModuleContext ctx, Player player, boolean client) {
            if (!isEnabled(ctx) || player.getAbilities().flying || player.isFallFlying()) return;

            float fwd = (SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) ? 1f : 0f) -
                    (SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player) ? 1f : 0f);
            float strafe = (SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player) ? 1f : 0f) -
                    (SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player) ? 1f : 0f);
            boolean hasInput = fwd != 0f || strafe != 0f;

            var pd = player.getPersistentData();
            boolean latched = pd.getBoolean(LATCH);
            if (player.isSprinting() && fwd > 0f) latched = true;
            else if (!hasInput || fwd > 0f || player.isShiftKeyDown() || player.isInWater()) latched = false;
            if (latched) pd.putBoolean(LATCH, true);
            else pd.remove(LATCH);

            boolean sprinting = hasInput && (player.isSprinting() || latched);
            if (!sprinting || !player.onGround()) return;

            long credits = ArmorUtils.toModuleEnergyCredits(SPRINT_EU_PER_TICK);
            if (!ArmorUtils.useModuleEnergy(ctx.getAppliedTo(), credits, client)) return;

            float modifier;
            if (player.isInWater()) {
                modifier = 0.02F;
            } else {
                float percent = (float) Math.pow(level(ctx) / 20.0f, 1.6);
                var cfg = PhoenixTeslaConfigs.INSTANCE.wingFlight;
                modifier = (float) (cfg.sprintAccelMin + percent * (cfg.sprintAccelMax - cfg.sprintAccelMin));
            }
            player.moveRelative(modifier, new Vec3(strafe, 0, fwd));
        }
    }

    // ---- jump (boots) -------------------------------------------------------------------------------------------

    public static class JumpModule extends LevelModule {

        private static final String CHARGE = "phoenix_tesla_jump_charge"; // client-local
        private static final String PREV_GROUND = "phoenix_tesla_prev_ground";

        public JumpModule(ResourceLocation id) {
            super(id);
        }

        @Override protected int defaultLevel() { return 5; }
        @Override protected int minLevel() { return 1; }
        @Override protected String settingKey() { return "gui.phoenix_tesla_network.jump.height"; }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.FEET) && !stackHas(stack, JumpBoostItemModule.class) &&
                    super.canApplyTo(stack);
        }

        /** Server: pay for each jump. (Replaces the old prevOnGround bookkeeping in handleBootsLogic.) */
        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (!isEnabled(ctx) || !(entity instanceof Player player)) return;
            var pd = player.getPersistentData();
            boolean prev = pd.getBoolean(PREV_GROUND);
            boolean jumping = player.getDeltaMovement().y > 0 && !player.onGround();
            if (prev && !player.onGround() && jumping && !player.isShiftKeyDown()) {
                ArmorUtils.useModuleEnergy(ctx.getAppliedTo(), ArmorUtils.toModuleEnergyCredits(JUMP_EU), false);
            }
            pd.putBoolean(PREV_GROUND, player.onGround());
        }

        /** Client: the old charge-decay impulse. The charge was a field on the shared logic object; now it's per player. */
        @Override
        public void clientSideArmorTick(ModuleContext ctx, Player player) {
            if (!isEnabled(ctx) || level(ctx) <= 0) return;
            var pd = player.getPersistentData();
            float charge = pd.getFloat(CHARGE);

            if (player.onGround() &&
                    ArmorUtils.useModuleEnergy(ctx.getAppliedTo(), ArmorUtils.toModuleEnergyCredits(JUMP_EU), true)) {
                charge = 1.0F;
            }
            Vec3 delta = player.getDeltaMovement();
            if (delta.y >= 0.0D && charge > 0.0F && !player.isInWater()) {
                if (delta.y > 0.05) {
                    if (charge == 1.0F) player.setDeltaMovement(delta.x * 3.6D, delta.y, delta.z * 3.6D);
                    float jumpPercent = (Math.max(1, Math.min(20, level(ctx))) - 1) / 19.0f;
                    var cfg = PhoenixTeslaConfigs.INSTANCE.wingFlight;
                    double impulse = cfg.jumpHeightMin + jumpPercent * (cfg.jumpHeightMax - cfg.jumpHeightMin);
                    player.addDeltaMovement(new Vec3(0.0, charge * impulse, 0.0));
                    charge *= 0.7F;
                } else if (charge < 1.0F) {
                    charge = 0.0F;
                }
            }
            pd.putFloat(CHARGE, charge);
        }
    }

    // ---- step height (boots) ------------------------------------------------------------------------------------

    /**
     * Rides GTM's attribute-module machinery, so the modifier is applied while worn and removed on unequip: this
     * replaces applyStepHeight/resetStepHeight, the STEP_APPLIED/STEP_STAMP bookkeeping, StepAssistCleanup and the
     * step half of SuitStepHandler.
     */
    public static class StepModule extends AttributeItemModule {

        public static final float VANILLA = 0.6F;
        public static final float MIN_BOOSTED = 1.0025F;

        public StepModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.FEET) && !stackHas(stack, StepHeightModule.class) &&
                    super.canApplyTo(stack);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public Holder<Attribute> getAttribute(ModuleContext ctx) {
            return Attributes.STEP_HEIGHT;
        }

        @Override
        public double getMaxAttributeAmount() {
            return Math.max(MIN_BOOSTED, PhoenixTeslaConfigs.INSTANCE.wingFlight.stepHeightMax) - VANILLA;
        }

        @Override
        public double getMinModifier(ModuleContext ctx) {
            return MIN_BOOSTED - VANILLA;
        }

        @Override
        public AttributeModifier.Operation getModifierOperation() {
            return AttributeModifier.Operation.ADD_VALUE;
        }

        @Override
        protected String getSliderString(ModuleContext ctx, double value) {
            return "%.1f blocks".formatted(value + VANILLA);
        }
    }
}
