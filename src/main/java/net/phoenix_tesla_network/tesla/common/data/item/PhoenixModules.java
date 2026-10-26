package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.module.IHUDProviderItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.registry.PhoenixRegistration;
import net.phoenix_tesla_network.tesla.network.packet.S2CTeslaStatusPacket;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import com.tterrag.registrate.util.entry.RegistryEntry;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Signatures match ItemModule.java as of GTM 8.0 (ModuleContext first, entity second).
 *
 * Modules are registry singletons shared by all players: no per-player state in fields.
 * ModuleData is immutable; changing it goes through ctx.setData(...). Not used here yet because
 * ModuleData.java hasn't been seen. Entity persistent data holds the little state we need.
 *
 * Register with your Registrate, same as CosmicModules:
 * REGISTRATE.simple("tesla_link_module", GTRegistries.Keys.ITEM_MODULE, () -> new
 * TeslaLinkModule(id("tesla_link_module")));
 */
public final class PhoenixModules {

    private PhoenixModules() {}

    // Handles used by the Equipment Foundry recipes (PhoenixTeslaGTAddon.addRecipes).
    public static RegistryEntry<ItemModule, TeslaLinkModule> TESLA_LINK;
    public static RegistryEntry<ItemModule, RebirthModule> REBIRTH;
    public static RegistryEntry<ItemModule, PurifierModule> PURIFIER;
    public static RegistryEntry<ItemModule, TeslaDischargeModule> DISCHARGE;
    public static RegistryEntry<ItemModule, PhoenixWingModule> WING;
    public static RegistryEntry<ItemModule, PhoenixMovementModules.SprintModule> SPRINT;
    public static RegistryEntry<ItemModule, PhoenixMovementModules.JumpModule> JUMP;
    public static RegistryEntry<ItemModule, PhoenixMovementModules.StepModule> STEP;
    public static RegistryEntry<ItemModule, PhoenixSuiteModules.LoreScannerModule> LORE_SCANNER;
    public static RegistryEntry<ItemModule, PhoenixSuiteModules.MenderModule> MENDER;
    public static RegistryEntry<ItemModule, PhoenixSuiteModules.MagnetModule> MAGNET;
    public static RegistryEntry<ItemModule, PhoenixSuiteModules.NaphthaModule> NAPHTHA;

    public static void init() {
        net.phoenix_tesla_network.tesla.common.ui.PhoenixArmorUIFactory.register();
        TESLA_LINK = reg("tesla_link_module", TeslaLinkModule::new);
        REBIRTH = reg("phoenix_rebirth_module", RebirthModule::new);
        PURIFIER = reg("phoenix_purifier_module", PurifierModule::new);
        DISCHARGE = reg("phoenix_tesla_discharge_module", TeslaDischargeModule::new);
        WING = reg("phoenix_wing_module", PhoenixWingModule::new);
        SPRINT = reg("phoenix_sprint_module", PhoenixMovementModules.SprintModule::new);
        JUMP = reg("phoenix_jump_module", PhoenixMovementModules.JumpModule::new);
        STEP = reg("phoenix_step_module", PhoenixMovementModules.StepModule::new);
        LORE_SCANNER = reg("phoenix_lore_scanner_module", PhoenixSuiteModules.LoreScannerModule::new);
        MENDER = reg("phoenix_mender_module", PhoenixSuiteModules.MenderModule::new);
        MAGNET = reg("phoenix_magnet_module", PhoenixSuiteModules.MagnetModule::new);
        NAPHTHA = reg("phoenix_naphtha_module", PhoenixSuiteModules.NaphthaModule::new);
    }

    private static <T extends ItemModule> RegistryEntry<ItemModule, T> reg(String name,
                                                                           java.util.function.Function<ResourceLocation, T> factory) {
        return PhoenixRegistration.REGISTRATE.simple(name, GTRegistries.Keys.ITEM_MODULE,
                () -> factory.apply(PhoenixTeslaNetwork.id(name)));
    }

    public static final class Tesla {

        private Tesla() {}

        public static UUID teamId(Player player) {
            UUID id = TeamUtils.getTeamIdOrPlayerFallback(player.getUUID());
            return id != null ? id : player.getUUID();
        }

        /** Server-side only; null when out of range or unusable. */
        public static TeslaTeamEnergyData.TeamEnergy network(Player player) {
            if (!(player.level() instanceof ServerLevel level)) return null;
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
            UUID team = teamId(player);
            if (data == null || !TeslaRange.canPlayerUseNetwork(data, team, player)) return null;
            return data.getOrCreate(team);
        }
    }

    /** Chest module: fire immunity + wireless top-up of the suit's battery from the team network. */
    public static class TeslaLinkModule extends PhoenixModule implements IHUDProviderItemModule {

        public TeslaLinkModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public boolean isPPE(ModuleContext ctx) {
            return true;
        }

        @Override
        public void onUnequip(ModuleContext ctx, LivingEntity entity) {
            if (entity instanceof Player p) ((IFireImmuneEntity) p).gtceu$setFireImmune(false);
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity); // base: server-only, enabled-only, drains energyUsagePerTick
            if (!isEnabled(ctx) || entity.level().isClientSide || !(entity instanceof Player player)) return;

            ((IFireImmuneEntity) player).gtceu$setFireImmune(true);
            if (player.isOnFire()) player.extinguishFire();

            var online = Tesla.network(player);
            if (online != null && player.level() instanceof ServerLevel sl) {
                // old handleGlobalTeslaEffects: lightning retaliation + ambient sparks
                if (player.hurtTime == 10 && player.getLastHurtByMob() != null) {
                    LivingEntity attacker = player.getLastHurtByMob();
                    attacker.hurt(sl.damageSources().lightningBolt(), 5.0F);
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, attacker.getX(), attacker.getY() + 1,
                            attacker.getZ(), 10, 0.1, 0.1, 0.1, 0.1);
                }
                if (sl.getGameTime() % 20 == 0) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(),
                            2, 0.2, 0.5, 0.2, 0.02);
                }
            }

            // HUD sync: replaces stack-NBT netStored/netCapacity + inventoryMenu.sendAllDataToRemote().
            if (player instanceof ServerPlayer sp && player.level().getGameTime() % 20 == 0) {
                var snap = Tesla.network(player);
                PacketDistributor.sendToPlayer(sp, snap == null ? new S2CTeslaStatusPacket(false, "", "") :
                        new S2CTeslaStatusPacket(true, snap.stored.toString(), snap.capacity.toString()));
            }

            // Don't charge while a container is open (matches old behavior).
            if (player.containerMenu != player.inventoryMenu) return;

            IElectricItem item = GTCapabilityHelper.getElectricItem(ctx.getAppliedTo());
            if (item == null) return;
            long room = item.getMaxCharge() - item.getCharge();
            if (room <= 0) return;

            var network = Tesla.network(player);
            if (network == null) return;

            long request = Math.min(room, item.getTransferLimit());
            double eff = TeslaLoss.suitEfficiency(network, player.level().dimension(), player.blockPosition());
            BigInteger drained = network.drain(TeslaLoss.gross(BigInteger.valueOf(request), eff));
            if (drained.signum() > 0) {
                item.charge(TeslaLoss.net(drained, eff).longValue(), item.getTier(), true, false);
                if (player.level().getGameTime() % 5 == 0 && player.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1, player.getZ(),
                            3, 0.2, 0.4, 0.2, 0.05);
                }
            }
        }

        // ---- HUD ----------------------------------------------------------------------------------------------
        // Client cache. The old code smuggled these through the chest's stack NBT and forced
        // inventoryMenu.sendAllDataToRemote(); send a tiny S2C payload every ~20 ticks while online instead and
        // call TeslaLinkModule.Client.update(...) from its handler. (Payload registration not written here.)
        public static final class Client {

            public static volatile String stored = "", capacity = "";
            public static volatile boolean online = false;

            public static void update(boolean online, String stored, String capacity) {
                Client.online = online;
                Client.stored = stored;
                Client.capacity = capacity;
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawHUD(ModuleContext ctx, ArmorUtils.ModularHUD hud, GuiGraphics graphics) {
            if (!Client.online) {
                hud.newLine(Component.literal("Tesla: offline").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }
            hud.newLine(Component.literal("Tesla: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(Client.stored + " / " + Client.capacity)
                            .withStyle(ChatFormatting.WHITE)));
        }
    }

    /** Helmet module: strips harmful effects for a battery cost. Replaces removeNegativeEffects(). */
    public static class PurifierModule extends PhoenixModule {

        public static final Reference2IntMap<Holder<MobEffect>> COST = new Reference2IntOpenHashMap<>();

        static {
            COST.put(MobEffects.POISON, 10000);
            COST.put(MobEffects.WITHER, 25000);
            COST.put(MobEffects.CONFUSION, 8000);
            COST.put(MobEffects.DIG_SLOWDOWN, 12500);
            COST.put(MobEffects.MOVEMENT_SLOWDOWN, 9000);
            COST.put(MobEffects.UNLUCK, 5000);
        }

        public PurifierModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public void onArmorTick(ModuleContext ctx, LivingEntity entity) {
            super.onArmorTick(ctx, entity);
            if (!isEnabled(ctx) || entity.level().isClientSide) return;
            IElectricItem item = GTCapabilityHelper.getElectricItem(ctx.getAppliedTo());
            if (item == null) return;
            for (MobEffectInstance e : new java.util.ArrayList<>(entity.getActiveEffects())) {
                int base = COST.getOrDefault(e.getEffect(), -1);
                if (base < 0) continue;
                long cost = (long) base * (e.getAmplifier() + 1);
                if (item.canUse(cost)) {
                    item.discharge(cost, item.getTier(), true, false, false);
                    entity.removeEffect(e.getEffect());
                }
            }
        }
    }

    /** Chest module: Phoenix Rebirth. Replaces the old damageArmor() override. */
    public static class RebirthModule extends PhoenixModule {

        private static final BigInteger COST = BigInteger.valueOf(10_000_000L);
        private static final String READY_AT = "phoenix_rebirth_ready_at";
        private static final long COOLDOWN_TICKS = 600;

        public RebirthModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public float changeDamage(ModuleContext ctx, LivingEntity entity, float damage, DamageSource source) {
            if (!isEnabled(ctx) || !(entity instanceof Player player) ||
                    !(player.level() instanceof ServerLevel level)) {
                return damage;
            }
            if (source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.FALL)) return 0;
            if (player.getHealth() > damage) return damage;

            long now = level.getGameTime();
            if (now < player.getPersistentData().getLong(READY_AT)) return damage;

            var network = Tesla.network(player);
            if (network == null || network.stored.compareTo(COST) < 0) return damage;

            network.drain(COST);
            player.getPersistentData().putLong(READY_AT, now + COOLDOWN_TICKS);

            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
            level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            player.displayClientMessage(Component.literal("§6§l⚡ PHOENIX REBIRTH ACTIVATED ⚡"), true);
            return 0;
        }
    }

    /** Boots module: Tesla Discharge (keybind -> C2STeslaDischargePacket -> trigger). Ported from doTeslaDischarge. */
    public static class TeslaDischargeModule extends PhoenixModule {

        private static final String READY_AT = "phoenix_discharge_ready_at";
        private static final long COOLDOWN_TICKS = 100;
        private static final BigInteger NETWORK_COST = BigInteger.valueOf(5000);
        private static final long ARMOR_COST = 5000L;

        public TeslaDischargeModule(ResourceLocation id) {
            super(id);
        }

        @Override
        public Component getInfo() {
            return Component.translatable(getDescriptionLanguageKey());
        }

        @Override
        public boolean canApplyTo(ItemStack stack) {
            return stack.getItem() instanceof ModularArmorItem a && a.getEquipmentSlot() == EquipmentSlot.FEET &&
                    super.canApplyTo(stack);
        }

        /** Called by the packet handler. Finds the module on the player's boots and fires it. */
        public static boolean trigger(ServerPlayer player) {
            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            var modular = GTCapabilityHelper.getModularItem(boots);
            if (modular == null) return false;
            for (var ctx : modular.getAllModuleInstances()) {
                if (ctx.getModule() instanceof TeslaDischargeModule m && m.isEnabled(ctx)) {
                    return m.discharge(ctx, player);
                }
            }
            return false;
        }

        private boolean discharge(ModuleContext ctx, ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            long now = level.getGameTime();
            // Cooldown is an absolute game time on the player (the old one was a per-tick counter in boots NBT).
            if (now < player.getPersistentData().getLong(READY_AT)) return false;

            IElectricItem armor = GTCapabilityHelper.getElectricItem(ctx.getAppliedTo());
            var network = Tesla.network(player);
            boolean hasEnergy = false;
            if (network != null && network.stored.compareTo(NETWORK_COST) >= 0) {
                network.drain(NETWORK_COST);
                hasEnergy = true;
            } else if (armor != null && armor.canUse(ARMOR_COST)) {
                armor.discharge(ARMOR_COST, armor.getTier(), true, false, false);
                hasEnergy = true;
            }
            if (!hasEnergy) {
                player.displayClientMessage(Component.literal("§c⚡ DISCHARGE FAILED: §7No energy available!"), true);
                return false;
            }
            player.getPersistentData().putLong(READY_AT, now + COOLDOWN_TICKS);

            int hitCount = 0;
            for (Entity target : level.getEntities(player, player.getBoundingBox().inflate(5.0))) {
                if (target instanceof LivingEntity living && !(target instanceof Player)) {
                    living.hurt(level.damageSources().lightningBolt(), 20.0f);
                    living.igniteForSeconds(3);
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, living.getX(), living.getY() + 1, living.getZ(),
                            15, 0.3, 0.3, 0.3, 0.1);
                    hitCount++;
                }
            }
            level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1, player.getZ(), 1, 0, 0, 0, 0);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0, 0,
                    0, 0);
            level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS,
                    0.8f, 1.5f);
            player.displayClientMessage(Component.literal(hitCount > 0 ?
                    "§b⚡ DISCHARGE: §eHit " + hitCount + " target" + (hitCount > 1 ? "s" : "") + "!" :
                    "§b⚡ DISCHARGE: §7No targets in range."), true);
            return true;
        }
    }
}
