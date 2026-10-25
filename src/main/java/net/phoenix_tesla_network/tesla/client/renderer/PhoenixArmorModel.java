package net.phoenix_tesla_network.tesla.client.renderer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;

import software.bernie.geckolib.model.DefaultedGeoModel;

public class PhoenixArmorModel extends DefaultedGeoModel<PhoenixArmorItem> {

    public PhoenixArmorModel(PhoenixArmorItem item) {
        super(BuiltInRegistries.ITEM.getKey(item));
    }

    @Override
    protected String subtype() {
        return "armor";
    }

    @Override
    public ResourceLocation getAnimationResource(PhoenixArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network",
                "animations/phoenix_armor.animation.json");
    }
}
