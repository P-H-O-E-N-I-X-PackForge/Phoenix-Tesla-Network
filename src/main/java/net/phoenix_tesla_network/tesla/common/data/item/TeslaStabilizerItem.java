package net.phoenix_tesla_network.tesla.common.data.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class TeslaStabilizerItem extends Item {

    public TeslaStabilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        Level level = context.level();
        // the text has a line break in it, which a tooltip shows as a box: one line each
        for (String line : Component.translatable(this.getDescriptionId() + ".tooltip").getString().split("\\R")) {
            tooltip.add(Component.literal(line.strip()).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
