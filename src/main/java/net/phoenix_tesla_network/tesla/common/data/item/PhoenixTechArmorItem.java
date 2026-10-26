package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    // The suit's own armor layers (restored from the old PhoenixTechSuite): phoenix_tech_suite_1.png covers the
    // helmet, chestplate and boots, phoenix_tech_suite_2.png the leggings.
    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      ArmorMaterial.Layer layer, boolean innerModel) {
        ResourceLocation fromModule = super.getArmorTexture(stack, entity, slot, layer, innerModel);
        if (fromModule != null) return fromModule;
        String name = slot == EquipmentSlot.LEGS ? "phoenix_tech_suite_2" : "phoenix_tech_suite_1";
        return ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "textures/armor/" + name + ".png");
    }

    private static final String GTM_MENU_KEY = "gtceu.key.modular_item_gui";

    /**
     * GTM's modular-armor tooltip tells players to press its own menu key (V). We open our own Phoenix menu on a
     * different key, so drop any tooltip line that shows GTM's key and add one showing ours. Uses Component.keybind,
     * so it displays whatever the player actually bound.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.removeIf(line -> mentionsKeybind(line, GTM_MENU_KEY));
        tooltip.add(Component.translatable("tooltip.phoenix_tesla_network.suite.menu",
                Component.keybind("key.phoenix_tesla_network.suite_menu")).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static boolean mentionsKeybind(Component component, String keyName) {
        if (component.getContents() instanceof KeybindContents kb && kb.getName().equals(keyName)) return true;
        if (component.getContents() instanceof TranslatableContents tr) {
            for (Object arg : tr.getArgs()) {
                if (arg instanceof Component c && mentionsKeybind(c, keyName)) return true;
            }
        }
        for (Component sibling : component.getSiblings()) {
            if (mentionsKeybind(sibling, keyName)) return true;
        }
        return false;
    }
}
