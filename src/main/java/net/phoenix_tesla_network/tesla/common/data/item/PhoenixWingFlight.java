package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.util.StackNbt;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Wing flight physics, moved out of PhoenixTechSuite with the method bodies unchanged. Only the plumbing differs:
 * the methods are static, "data" is now a transient per-player state tag (see PhoenixWingModule.state) instead of
 * the chest's stack NBT, and the two settings these methods still read through that tag (FlightMode,
 * KeepFlyingOnLand) are copied into it from WingData each tick by tick(...).
 *
 * NOT COMPILED. Mechanical extraction: expect to fix a few missing imports.
 */
public final class PhoenixWingFlight {

    private PhoenixWingFlight() {}

    public static final String FLIGHT_SPEED_ZEROED = "phoenix_tesla_flight_speed_zeroed";
    public static final String MAYFLY_GRANTED = "phoenix_tesla_mayfly_granted";
    public static final float VANILLA_FLYING_SPEED = 0.05F;

    /** Replaces handleFlightSystem + the mode gating that used to live in handleChestplateLogic. */
    static void tick(Player player, PhoenixWingModule.WingData d, boolean enabled, CompoundTag data, Level world,
                     boolean networkOnline, TeslaTeamEnergyData teslaData, UUID teamID) {
        data.putString("FlightMode", d.flightMode());
        data.putBoolean("KeepFlyingOnLand", d.keepFlyingOnLand());

        if (enabled && (world.isClientSide || networkOnline)) {
            PhoenixTeslaConfigs.WingFlightConfigs cfg = PhoenixTeslaConfigs.INSTANCE.wingFlight;

            float speedPercent = Math.max(0, Math.min(20, d.flightSpeed())) / 20.0f;
            float driftPercent = Math.max(0, Math.min(10, d.flightDrift())) / 10.0f;
            float verticalScale = Math.max(0, Math.min(20, d.flightVertical())) / 5.0f;

            if (!d.isCreative() && player.getAbilities().getFlyingSpeed() == 0f) {
                player.getAbilities().setFlyingSpeed(VANILLA_FLYING_SPEED);
                player.onUpdateAbilities();
                player.getPersistentData().remove(FLIGHT_SPEED_ZEROED);
            }

            if (d.isCreative()) {
                handleCreativeFlight(player, data, world, cfg, speedPercent, driftPercent, verticalScale,
                        networkOnline, teslaData, teamID);
            } else {
                handleElytraFlight(player, data, world, cfg, speedPercent, driftPercent, verticalScale,
                        networkOnline, teslaData, teamID);
            }
            handleLandingSlide(player, data);
        } else {
            disableFlight(player, data);
            data.putInt("WingLandingSlideTicks", 0);
        }
    }

    static void applyWingThrust(Player player, Level world, CompoundTag data,
                                 PhoenixTeslaConfigs.WingFlightConfigs cfg,
                                 float speedMult, float driftMult, float verticalScale,
                                 TeslaTeamEnergyData teslaData, UUID teamID) {
        if (player.horizontalCollision) {

            Vec3 stuck = player.getDeltaMovement();
            player.setDeltaMovement(stuck.x * 0.4, Math.max(stuck.y, 0.45), stuck.z * 0.4);
            player.hurtMarked = true;
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 cur = player.getDeltaMovement();

        double euScale = cfg.poweredFlightEUt / 5_000.0;
        double baseThrust = cfg.poweredBoostMin + (speedMult * (cfg.poweredBoostMax - cfg.poweredBoostMin));
        double sigmoid = sigmoidAcceleration(player.tickCount, 5.0, baseThrust, baseThrust * 0.3);
        double thrust = sigmoid * euScale;

        double newX = cur.x + look.x * thrust;
        double newZ = cur.z + look.z * thrust;

        double climbMultiplier = cfg.poweredVerticalBase * verticalScale;

        double newY;
        if (look.y > 0.05) {
            newY = Math.max(cur.y, look.y * thrust * climbMultiplier);
        } else if (look.y < -0.015) {

            newY = Math.max(cur.y + look.y * thrust, -3.0);
        } else {

            newY = Math.max(cur.y, -0.05);
        }

        Vec3 preRealign = new Vec3(newX, newY, newZ);
        Vec3 realigned = realignTowardLook(player, preRealign, 0.22);
        newX = realigned.x;
        newY = realigned.y;
        newZ = realigned.z;

        double speedCap = cfg.poweredDriftMin + (speedMult * (cfg.poweredDriftMax - cfg.poweredDriftMin));
        double maxSpeed = speedCap * (1.0 + (driftMult * 0.5));
        double horizLen = Math.sqrt(newX * newX + newZ * newZ);
        if (horizLen > maxSpeed) {
            double scale = maxSpeed / horizLen;
            newX *= scale;
            newZ *= scale;
        }

        Vec3 newVel = new Vec3(newX, newY, newZ);

        player.setDeltaMovement(newVel);
        player.fallDistance = 0;

        if (!world.isClientSide) {
            data.putBoolean("IsSonicFlight", true);

            teslaData.getOrCreate(player.getUUID())
                    .drain(java.math.BigInteger.valueOf(cfg.poweredFlightEUt));
        }
    }

    static Vec3 realignTowardLook(Player player, Vec3 velocity, double strength) {
        double speed = velocity.length();
        if (speed < 0.05) return velocity;

        Vec3 lookDir = player.getLookAngle();
        Vec3 blendedDir = velocity.scale(1.0 / speed).scale(1 - strength).add(lookDir.scale(strength));
        double blendedLen = blendedDir.length();
        if (blendedLen < 1.0E-4) return velocity;

        return blendedDir.scale(speed / blendedLen);
    }

    static double getDriftRetention(PhoenixTeslaConfigs.WingFlightConfigs cfg, float driftMult) {
        double halfLifeSeconds = cfg.coastHalfLifeMin + (driftMult * (cfg.coastHalfLifeMax - cfg.coastHalfLifeMin));
        if (halfLifeSeconds <= 0.0) return 0.0;
        return Math.pow(0.5, 1.0 / (halfLifeSeconds * 20.0));
    }

    static void applyCoastDamping(Player player, PhoenixTeslaConfigs.WingFlightConfigs cfg, float driftMult) {
        if (player.horizontalCollision) {

            Vec3 stuck = player.getDeltaMovement();
            player.setDeltaMovement(stuck.x * 0.4, Math.max(stuck.y, 0.45), stuck.z * 0.4);
            player.hurtMarked = true;
            return;
        }

        if (driftMult >= 1.0f) return;

        double retention = getDriftRetention(cfg, driftMult);
        if (retention >= 1.0) return;

        Vec3 cur = player.getDeltaMovement();
        double newX = cur.x * retention;
        double newZ = cur.z * retention;

        Vec3 look = player.getLookAngle();
        double newY;
        if (look.y > 0.05) {

            newY = cur.y;
        } else if (look.y < -0.015) {

            newY = Math.max(cur.y + look.y * 0.24, -3.0);
        } else {
            newY = Math.max(cur.y, -0.05);
        }

        Vec3 realigned = realignTowardLook(player, new Vec3(newX, newY, newZ), 0.16);
        newX = realigned.x;
        newY = realigned.y;
        newZ = realigned.z;

        player.setDeltaMovement(newX, newY, newZ);
    }

    static double sigmoidAcceleration(double t, double peakTime,
                                              double peakAcceleration,
                                              double initialAcceleration) {
        return ((2 * peakAcceleration) / (1 + Math.exp(-t / peakTime)) - peakAcceleration) + initialAcceleration;
    }

    static boolean isReallyGrounded(Player player) {
        return player.onGround() || (player.verticalCollision && player.getDeltaMovement().y <= 0.02);
    }

    static void handleLandingSlide(Player player, CompoundTag data) {
        int cooldown = data.getInt("WingLandingCooldown");
        if (cooldown > 0) data.putInt("WingLandingCooldown", cooldown - 1);

        int slideTicks = data.getInt("WingLandingSlideTicks");
        if (slideTicks <= 0) return;

        Vec3 cur = player.getDeltaMovement();
        slideTicks--;

        double clampedY = Math.min(cur.y, 0);

        if (slideTicks <= 0 || (Math.abs(cur.x) < 0.02 && Math.abs(cur.z) < 0.02)) {

            player.setDeltaMovement(0, 0, 0);
            data.putInt("WingLandingSlideTicks", 0);
        } else {
            player.setDeltaMovement(cur.x * 0.4, clampedY, cur.z * 0.4);
            data.putInt("WingLandingSlideTicks", slideTicks);
        }
    }

    static void handleElytraFlight(Player player, CompoundTag data, Level world,
                                    PhoenixTeslaConfigs.WingFlightConfigs cfg,
                                    float speedMult, float driftMult, float verticalScale,
                                    boolean networkOnline, TeslaTeamEnergyData teslaData,
                                    UUID teamID) {
        if (player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (!player.isFallFlying()) {
            data.putBoolean("WasFallFlying", false);
        } else {
            if (!data.getBoolean("WasFallFlying")) {
                data.putInt("WingFlightStartTick", player.tickCount);
            }
            data.putBoolean("WasFallFlying", true);

            boolean isSneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);
            boolean isPowered = "powered".equals(data.getString("FlightMode"));

            boolean justLaunched = player.tickCount - data.getInt("WingFlightStartTick") < 5;

            boolean grounded = isReallyGrounded(player);
            int groundStreak = grounded ? data.getInt("WingGroundStreak") + 1 : 0;
            data.putInt("WingGroundStreak", groundStreak);
            boolean settledOnGround = groundStreak > 10;

            if (player.onGround() && !justLaunched && settledOnGround) {

                player.stopFallFlying();

                if (player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
                data.putInt("WingGroundStreak", 0);
                data.putBoolean("WasFallFlying", false);

                if (isPowered) {
                    data.putInt("WingLandingSlideTicks", 6);
                    data.putInt("WingLandingCooldown", 10);
                    player.setPos(player.getX(), player.getY() + 0.35, player.getZ());
                }

                if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
                return;
            }
            player.fallDistance = 0;

            if (isPowered && isSneaking) {
                long cost = (long) cfg.poweredFlightEUt;
                java.math.BigInteger requiredBI = java.math.BigInteger.valueOf(cost);
                boolean hasPower = false;

                if (networkOnline && teslaData != null) {
                    if (teslaData.getOrCreate(teamID).drain(requiredBI).compareTo(requiredBI) >= 0) {
                        hasPower = true;
                    }
                }

                if (!hasPower) {
                    IElectricItem item = GTCapabilityHelper.getElectricItem(player.getItemBySlot(EquipmentSlot.CHEST));
                    if (item != null && item.canUse(cost)) {
                        item.discharge(cost, item.getTier(), true, false, false);
                        hasPower = true;
                    }
                }

                if (hasPower) {

                    applyWingThrust(player, world, data, cfg, speedMult, driftMult, verticalScale, teslaData, teamID);

                    if (!world.isClientSide && world instanceof ServerLevel sl) {
                        Vec3 look = player.getLookAngle();
                        double tx = player.getX() - look.x * 0.8;
                        double ty = player.getY() + 0.5;
                        double tz = player.getZ() - look.z * 0.8;

                        sl.sendParticles(ParticleTypes.FLAME, tx, ty, tz, 3, 0.15, 0.15, 0.15, 0.02);
                    }
                } else {
                    applyCoastDamping(player, cfg, driftMult);
                    if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
                }
            } else if (isPowered) {

                applyCoastDamping(player, cfg, driftMult);
                if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
            } else {
                if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
            }
        }
    }

    static void handleCreativeFlight(Player player, CompoundTag data, Level world,
                                      PhoenixTeslaConfigs.WingFlightConfigs cfg,
                                      float speedMult, float driftMult, float verticalScale,
                                      boolean networkOnline, TeslaTeamEnergyData teslaData,
                                      UUID teamID) {
        String flightMode = data.getString("FlightMode");
        IElectricItem item = GTCapabilityHelper.getElectricItem(player.getItemBySlot(EquipmentSlot.CHEST));

        long cost = (long) (cfg.creativeFlightEUt * (1.0 + (speedMult * 0.5)));
        java.math.BigInteger requiredBI = java.math.BigInteger.valueOf(cost);

        boolean canAfford = player.isCreative() || player.isSpectator();

        if (!canAfford) {

            if (networkOnline && teslaData != null && teslaData.getOrCreate(teamID).stored.compareTo(requiredBI) >= 0) {
                canAfford = true;
            }

            else if (item != null && item.canUse(cost)) {
                canAfford = true;
            }
        }

        if (canAfford) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getPersistentData().putBoolean(MAYFLY_GRANTED, true);
                }
            }
        } else {

            disableFlight(player, data);
            return;
        }

        boolean keepFlyingOnLand = data.getBoolean("KeepFlyingOnLand");
        if (keepFlyingOnLand && !player.isFallFlying() && !player.getAbilities().flying) {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }

        if (flightMode.equals("creative+wings")) {

            if (!keepFlyingOnLand && player.getAbilities().flying && !player.isFallFlying() &&
                    isReallyGrounded(player)) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }

            if (!player.isFallFlying()) {
                data.putBoolean("WasFallFlying", false);
            } else {

                if (!data.getBoolean("WasFallFlying")) {
                    data.putInt("WingFlightStartTick", player.tickCount);
                }
                data.putBoolean("WasFallFlying", true);

                boolean isSprinting = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player);

                boolean justLaunched = player.tickCount - data.getInt("WingFlightStartTick") < 5;

                boolean grounded = isReallyGrounded(player);
                int groundStreak = grounded ? data.getInt("WingGroundStreak") + 1 : 0;
                data.putInt("WingGroundStreak", groundStreak);
                boolean settledOnGround = groundStreak > 10;

                if (player.onGround() && !justLaunched && settledOnGround) {

                    player.stopFallFlying();

                    if (!keepFlyingOnLand && player.getAbilities().flying) {
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                    data.putInt("WingGroundStreak", 0);
                    data.putBoolean("WasFallFlying", false);
                    data.putInt("WingLandingSlideTicks", 6);
                    data.putInt("WingLandingCooldown", 10);

                    player.setPos(player.getX(), player.getY() + 0.35, player.getZ());
                    if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
                    return;
                }

                player.fallDistance = 0;

                if (isSprinting && canAfford) {
                    applyWingThrust(player, world, data, cfg, speedMult, driftMult, verticalScale, teslaData, teamID);
                } else {

                    applyCoastDamping(player, cfg, driftMult);
                    if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
                }

                if (!world.isClientSide && isSprinting) {
                    consumeFlightEnergy(item, teslaData, teamID, networkOnline, requiredBI, cost);
                }
                return;
            }
        } else {
            if (player.isFallFlying()) player.stopFallFlying();
        }

        if (player.getAbilities().flying) {

            applyCreativeFreeFlight(player, world, cfg, speedMult, driftMult, verticalScale);
            if (!world.isClientSide) consumeFlightEnergy(item, teslaData, teamID, networkOnline, requiredBI, cost);
        }

        if (!world.isClientSide) data.putBoolean("IsSonicFlight", false);
    }

    static double creativeDriftRetention(double vanillaRetention, float driftMult,
                                                 PhoenixTeslaConfigs.WingFlightConfigs cfg) {
        double slider = Math.rint(driftMult * 10.0);
        if (slider >= 10.0) return 1.0;
        if (slider <= 0.0) return 0.0;

        double vanillaHalfLife = Math.log(0.5) / Math.log(vanillaRetention);
        double halfLife;
        if (slider <= 5.0) {
            halfLife = vanillaHalfLife * (slider / 5.0);
        } else {
            double longest = Math.max(cfg.coastHalfLifeMax * 20.0, vanillaHalfLife * 2.0);
            halfLife = vanillaHalfLife * Math.pow(longest / vanillaHalfLife, (slider - 5.0) / 5.0);
        }
        return Math.pow(0.5, 1.0 / halfLife);
    }

    static void applyCreativeFreeFlight(Player player, Level world, PhoenixTeslaConfigs.WingFlightConfigs cfg,
                                         float speedMult, float driftMult, float verticalScale) {
        if (world.isClientSide) {
            (player.getAbilities()).setFlyingSpeed(0f);
            player.onUpdateAbilities();
            player.getPersistentData().putBoolean(FLIGHT_SPEED_ZEROED, true);
        }

        boolean forward = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player);
        boolean back = SyncedKeyMappings.VANILLA_BACKWARD.isKeyDown(player);
        boolean left = SyncedKeyMappings.VANILLA_LEFT.isKeyDown(player);
        boolean right = SyncedKeyMappings.VANILLA_RIGHT.isKeyDown(player);
        boolean up = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
        boolean down = player.isShiftKeyDown();

        float forwardAxis = (forward ? 1f : 0f) - (back ? 1f : 0f);

        float strafeAxis = (left ? 1f : 0f) - (right ? 1f : 0f);

        float yawRad = player.getYRot() * ((float) Math.PI / 180F);
        float sinYaw = Mth.sin(yawRad);
        float cosYaw = Mth.cos(yawRad);

        double dirX = strafeAxis * cosYaw - forwardAxis * sinYaw;
        double dirZ = forwardAxis * cosYaw + strafeAxis * sinYaw;
        double dirLen = Math.sqrt(dirX * dirX + dirZ * dirZ);
        if (dirLen > 1.0) {
            dirX /= dirLen;
            dirZ /= dirLen;
        }

        double speedScale = speedMult * 7.0;
        double horizSpeed = cfg.creativeFreeSpeedBase * speedScale;

        double vertSpeed = horizSpeed * verticalScale;

        double horizDrag = 0.91;
        if (player.onGround()) {
            var below = net.minecraft.core.BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.500001,
                    player.getZ());
            horizDrag = player.level().getBlockState(below).getFriction(player.level(), below, player) * 0.91;
        }
        horizDrag = Math.max(horizDrag, 0.05);
        double vertDrag = 0.6;
        double horizRetention = creativeDriftRetention(horizDrag, driftMult, cfg) / horizDrag;
        double vertRetention = creativeDriftRetention(vertDrag, driftMult, cfg) / vertDrag;

        Vec3 cur = player.getDeltaMovement();
        boolean horizInput = dirLen > 1.0E-4;
        double newX = horizInput ? dirX * horizSpeed : cur.x * horizRetention;
        double newZ = horizInput ? dirZ * horizSpeed : cur.z * horizRetention;

        double vAxis = (up ? 1.0 : 0.0) - (down ? 1.0 : 0.0);
        double newY = vAxis != 0.0 ? vAxis * vertSpeed : cur.y * vertRetention;

        player.setDeltaMovement(newX, newY, newZ);
        player.fallDistance = 0;
    }

    static void consumeFlightEnergy(IElectricItem item, TeslaTeamEnergyData teslaData, UUID teamID,
                                     boolean networkOnline, java.math.BigInteger requiredBI, long cost) {
        boolean success = false;
        if (networkOnline && teslaData != null) {
            if (teslaData.getOrCreate(teamID).drain(requiredBI).compareTo(requiredBI) >= 0) {
                success = true;
            }
        }
        if (!success && item != null) {
            item.discharge(cost, item.getTier(), true, false, false);
        }
    }

    static void disableFlight(Player player, CompoundTag data) {
        if (player.getAbilities().getFlyingSpeed() == 0f) {
            player.getAbilities().setFlyingSpeed(VANILLA_FLYING_SPEED);
            player.onUpdateAbilities();
            player.getPersistentData().remove(FLIGHT_SPEED_ZEROED);
        }
        if (player.getAbilities().mayfly && !player.isCreative()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        data.putBoolean("IsSonicFlight", false);
    }
}
