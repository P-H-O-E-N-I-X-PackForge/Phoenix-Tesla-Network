package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** Helmet / leggings / boots. Behaviour lives in modules; only the built-in traits are here. */
public class PhoenixTechArmorItem extends ModularArmorItem {

    public PhoenixTechArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public boolean protectsAgainstFalls() {
        return getType() == Type.BOOTS;
    }

    @Override
    public boolean protectsAgainstFire() {
        return getType() == Type.CHESTPLATE;
    }

    @Override
    public float getHeatResistance() {
        return 0.5f;
    }

    // Armor layer textures. GTArmorMaterials.ARMOR (the old material) is gone, so the NANO_MUSCLE material's own
    // layer isn't found for our items and renders as the missing texture. Point at a texture that exists.
    // TODO: drop your own PNGs at assets/<ns>/textures/armor/<name>_1.png (helmet, chest, boots) and <name>_2.png
    // (leggings), then change these two constants.
    private static final String TEXTURE_NAMESPACE = "gtceu";
    private static final String TEXTURE_NAME = "nano_muscle_suite";
    // Helmet uses GTM's quarktech texture. If this renders as the missing-texture checkerboard, the asset name is
    // different: check assets/gtceu/textures/armor/ in the GTM jar and fix this one string.
    private static final String HELMET_TEXTURE_NAME = "quark_tech_suite";

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      ArmorMaterial.Layer layer, boolean innerModel) {
        ResourceLocation fromModule = super.getArmorTexture(stack, entity, slot, layer, innerModel);
        if (fromModule != null) return fromModule;
        String name = slot == EquipmentSlot.HEAD ? HELMET_TEXTURE_NAME : TEXTURE_NAME;
        return ResourceLocation.fromNamespaceAndPath(TEXTURE_NAMESPACE, String.format("textures/armor/%s_%d.png",
                name, slot == EquipmentSlot.LEGS ? 2 : 1));
    }
}
