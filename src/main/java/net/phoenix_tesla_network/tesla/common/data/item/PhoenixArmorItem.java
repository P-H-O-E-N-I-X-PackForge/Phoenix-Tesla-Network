package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.phoenix_tesla_network.tesla.client.renderer.PhoenixArmorRenderer;

import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class PhoenixArmorItem extends PhoenixTechArmorItem implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PhoenixArmorItem(net.minecraft.core.Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** Elytra glide is available while the wing module is installed, enabled and not overridden by a GTM jetpack. */
    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player)) return false;
        var modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return false;
        for (var ctx : modular.getAllModuleInstances()) {
            if (ctx.getModule() instanceof PhoenixWingModule wing && wing.isEnabled(ctx) &&
                    !PhoenixWingModule.conflicted(ctx)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        if (!entity.level().isClientSide) {

            entity.gameEvent(GameEvent.ELYTRA_GLIDE);
        }
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "WingController", 5, event -> {
            Entity entity = event.getData(DataTickets.ENTITY);

            if (entity instanceof Player player) {

                if (player.isFallFlying()) {
                    if (player.getDeltaMovement().length() > 1.1) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation.phoenix.sonic"));
                    }
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation.phoenix.fly"));
                }

                if (player.getAbilities().flying) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.phoenix.creative_fly"));
                }

                if (!player.onGround() && player.getDeltaMovement().y > 0.1) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation.phoenix.flap"));
                }

                return event.setAndContinue(RawAnimation.begin()
                        .thenPlay("animation.phoenix.fold"));
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.phoenix.idle"));
        }));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private PhoenixArmorRenderer renderer;

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                                   EquipmentSlot equipmentSlot,
                                                                   HumanoidModel<?> original) {
                if (itemStack.getItem() instanceof PhoenixArmorItem phoenixItem) {

                    if (this.renderer == null) {
                        this.renderer = new PhoenixArmorRenderer(phoenixItem);
                    }

                    this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
                    return this.renderer;
                }
                return original;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
