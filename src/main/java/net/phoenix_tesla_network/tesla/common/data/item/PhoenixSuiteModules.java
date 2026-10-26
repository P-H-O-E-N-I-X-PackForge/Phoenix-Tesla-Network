package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.neoforged.neoforge.common.NeoForge;
import net.phoenix_tesla_network.tesla.api.suite.event.PhoenixLoreScanEvent;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Exploration / lore / teamwork modules. NOT COMPILED.
 *  - LoreScannerModule (helmet): scan what you're looking at; fires PhoenixLoreScanEvent for Phoenix Core to use.
 *  - MenderModule (chest): heals teammates (same Tesla team) in range from the network.
 *  - MagnetModule (leggings): pulls nearby dropped items to you. Sneak to pause.
 */
public final class PhoenixSuiteModules {

    private PhoenixSuiteModules() {}

    private static boolean slot(ItemStack stack, EquipmentSlot slot) {
        return stack.getItem() instanceof ModularArmorItem a && a.getEquipmentSlot() == slot;
    }

    // ---- lore scanner -------------------------------------------------------------------------------------------

    public static class LoreScannerModule extends PhoenixModule {

        private static final String READY_AT = "phoenix_scan_ready_at";
        private static final long COOLDOWN_TICKS = 20;
        private static final BigInteger NETWORK_COST = BigInteger.valueOf(1000);
        private static final long ARMOR_COST = 1000L;
        private static final double RANGE = 8.0;

        public LoreScannerModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.HEAD) && super.canApplyTo(stack);
        }

        public static boolean trigger(ServerPlayer player) {
            var modular = GTCapabilityHelper.getModularItem(player.getItemBySlot(EquipmentSlot.HEAD));
            if (modular == null) return false;
            for (var ctx : modular.getAllModuleInstances()) {
                if (ctx.getModule() instanceof LoreScannerModule m && m.isEnabled(ctx)) return m.scan(ctx, player);
            }
            return false;
        }

        private boolean scan(ModuleContext ctx, ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            long now = level.getGameTime();
            if (now < player.getPersistentData().getLong(READY_AT)) return false;

            // Pay: network first, suit battery as fallback (same as Discharge).
            IElectricItem armor = GTCapabilityHelper.getElectricItem(ctx.getAppliedTo());
            var network = PhoenixModules.Tesla.network(player);
            if (network != null && network.stored.compareTo(NETWORK_COST) >= 0) {
                network.drain(NETWORK_COST);
            } else if (armor != null && armor.canUse(ARMOR_COST)) {
                armor.discharge(ARMOR_COST, armor.getTier(), true, false, false);
            } else {
                player.displayClientMessage(Component.literal("§c✦ Scan failed: no energy"), true);
                return false;
            }
            player.getPersistentData().putLong(READY_AT, now + COOLDOWN_TICKS);

            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getViewVector(1.0f);
            Vec3 end = eye.add(look.scale(RANGE));
            HitResult blockHit = player.pick(RANGE, 1.0f, false);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(level, player, eye, end,
                    player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0),
                    e -> !e.isSpectator() && e.isPickable());

            PhoenixLoreScanEvent event;
            Component fallbackName;
            boolean entityCloser = entityHit != null && (blockHit.getType() == HitResult.Type.MISS ||
                    eye.distanceToSqr(entityHit.getLocation()) < eye.distanceToSqr(blockHit.getLocation()));
            if (entityCloser) {
                Entity target = entityHit.getEntity();
                event = new PhoenixLoreScanEvent(player, target.blockPosition(), null, target);
                fallbackName = target.getDisplayName();
            } else if (blockHit instanceof BlockHitResult bhr && blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = bhr.getBlockPos();
                var state = level.getBlockState(pos);
                event = new PhoenixLoreScanEvent(player, pos, state, null);
                fallbackName = state.getBlock().getName();
            } else {
                player.displayClientMessage(Component.literal("§7✦ Nothing in range"), true);
                return true;
            }

            NeoForge.EVENT_BUS.post(event);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS,
                    0.8f, 1.4f);
            Vec3 at = entityCloser ? entityHit.getLocation() : blockHit.getLocation();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 8, 0.2, 0.2, 0.2, 0.05);
            player.displayClientMessage(event.isHandled() && event.getFeedback() != null ? event.getFeedback() :
                    Component.literal("§b✦ Scanned: ").append(fallbackName), true);
            return true;
        }
    }

    // ---- mender aura --------------------------------------------------------------------------------------------

    /** Slider = radius in blocks. Heals every teammate in range (and you) from the network. */
    public static class MenderModule extends PhoenixMovementModules.LevelModule {

        private static final BigInteger COST_PER_HEAL = BigInteger.valueOf(25_000);
        private static final float HEAL = 2.0f;

        public MenderModule(ResourceLocation id) {
            super(id);
        }

        @Override protected int defaultLevel() { return 8; }
        @Override protected int minLevel() { return 2; }
        @Override protected String settingKey() { return "gui.phoenix_tesla_network.mender.radius"; }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.CHEST) && super.canApplyTo(stack);
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (!isEnabled(ctx) || !(entity instanceof Player player) ||
                    !(player.level() instanceof ServerLevel level)) {
                return;
            }
            if ((level.getGameTime() + player.getId()) % 20 != 0) return;

            var network = PhoenixModules.Tesla.network(player);
            if (network == null) return;

            double radius = level(ctx);
            UUID team = PhoenixModules.Tesla.teamId(player);
            AABB box = player.getBoundingBox().inflate(radius);
            for (Player other : level.getEntitiesOfClass(Player.class, box)) {
                if (other.distanceToSqr(player) > radius * radius) continue;
                if (other.getHealth() >= other.getMaxHealth() - 0.01f) continue;
                if (!PhoenixModules.Tesla.teamId(other).equals(team)) continue;
                if (network.stored.compareTo(COST_PER_HEAL) < 0) return;

                network.drain(COST_PER_HEAL);
                other.heal(HEAL);
                level.sendParticles(ParticleTypes.HEART, other.getX(), other.getY() + 1.5, other.getZ(), 3, 0.3, 0.3,
                        0.3, 0.0);
            }
        }
    }

    // ---- item magnet --------------------------------------------------------------------------------------------

    /** Slider = radius in blocks. Sneak to pause. */
    public static class MagnetModule extends PhoenixMovementModules.LevelModule {

        private static final double EU_PER_TICK = 2.0;
        private static final double PULL = 0.35;

        public MagnetModule(ResourceLocation id) {
            super(id);
        }

        @Override protected int defaultLevel() { return 6; }
        @Override protected int minLevel() { return 1; }
        @Override protected String settingKey() { return "gui.phoenix_tesla_network.magnet.radius"; }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.LEGS) && super.canApplyTo(stack);
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (!isEnabled(ctx) || !(entity instanceof Player player) || player.isShiftKeyDown() ||
                    player.level().isClientSide) {
                return;
            }
            double radius = level(ctx);
            var items = player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius),
                    i -> !i.hasPickUpDelay() && i.isAlive());
            if (items.isEmpty()) return;
            if (!ArmorUtils.useModuleEnergy(ctx.getAppliedTo(), ArmorUtils.toModuleEnergyCredits(EU_PER_TICK),
                    false)) {
                return;
            }
            Vec3 target = player.position().add(0, 0.5, 0);
            for (ItemEntity item : items) {
                Vec3 delta = target.subtract(item.position());
                if (delta.lengthSqr() < 0.04) continue;
                item.setDeltaMovement(delta.normalize().scale(PULL));
                item.hasImpulse = true;
            }
        }
    }

    // ---- naphtha (joke module) ----------------------------------------------------------------------------------

    /**
     * Chestplate. While sprinting, burns naphtha from any fluid container in your inventory (cells, buckets, tanks:
     * anything with an item fluid handler) for speed, haste and jump boosts, a flame trail, and it sets mobs you
     * run into on fire. The slider is burn intensity: effect level = (slider - 1) / 5, so 1-5 is level I and
     * 16-20 is level IV. Costs no electricity, only naphtha.
     */
    public static class NaphthaModule extends PhoenixMovementModules.LevelModule {

        private static final int INTERVAL_TICKS = 10;
        private static final int MB_PER_INTERVAL = 5; // 10 mB per second of sprinting
        private static final int EFFECT_TICKS = 25;
        private static final String DRY = "phoenix_naphtha_dry";

        public NaphthaModule(ResourceLocation id) {
            super(id);
        }

        @Override protected int defaultLevel() { return 6; }
        @Override protected int minLevel() { return 1; }
        @Override protected String settingKey() { return "gui.phoenix_tesla_network.naphtha.power"; }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return slot(stack, EquipmentSlot.CHEST) && super.canApplyTo(stack);
        }

        private static boolean isNaphtha(net.neoforged.neoforge.fluids.FluidStack fluid) {
            return net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid.getFluid()).getPath()
                    .equals("naphtha");
        }

        /** Drains one interval's worth of naphtha from the first container that has some. */
        private static boolean burn(Player player) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty() || stack.getCount() != 1) continue;
                var handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
                if (handler == null) continue;
                for (int tank = 0; tank < handler.getTanks(); tank++) {
                    var contained = handler.getFluidInTank(tank);
                    if (contained.isEmpty() || !isNaphtha(contained)) continue;
                    var drained = handler.drain(contained.copyWithAmount(MB_PER_INTERVAL),
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    if (drained.getAmount() > 0) {
                        ItemStack container = handler.getContainer();
                        if (container != stack) inv.setItem(i, container); // e.g. bucket -> empty bucket
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (!isEnabled(ctx) || !(entity instanceof ServerPlayer player) || !player.isSprinting() ||
                    player.tickCount % INTERVAL_TICKS != 0) {
                return;
            }
            var pd = player.getPersistentData();
            if (!burn(player)) {
                if (!pd.getBoolean(DRY)) {
                    pd.putBoolean(DRY, true);
                    player.displayClientMessage(Component.literal("§7The naphtha module sputters. Out of fuel."), true);
                }
                return;
            }
            if (pd.getBoolean(DRY)) {
                pd.remove(DRY);
                player.displayClientMessage(Component.literal("§6Smells like naphtha. Floor it."), true);
            }

            int amplifier = (level(ctx) - 1) / 5;
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, EFFECT_TICKS, amplifier, true, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DIG_SPEED, EFFECT_TICKS, amplifier / 2, true, false));
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, EFFECT_TICKS, amplifier / 2, true, false));

            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 8, 0.25, 0.05,
                    0.25, 0.02);
            level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.3, player.getZ(), 4, 0.2, 0.1,
                    0.2, 0.01);
            if (level.random.nextInt(8) == 0) {
                level.playSound(null, player.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6f,
                        0.8f + level.random.nextFloat() * 0.4f);
            }
            // Flame trail: anything non-player you run into catches fire. You're fire-immune, they are not.
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(1.5), e -> e != player && !(e instanceof Player))) {
                victim.igniteForSeconds(3);
            }
        }
    }
}
