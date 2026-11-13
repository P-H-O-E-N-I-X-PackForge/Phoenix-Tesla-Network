package net.phoenix_tesla_network.tesla.client.gui;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.phoenix_tesla_network.tesla.client.TeslaText;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;
import net.phoenix_tesla_network.tesla.utils.TeslaLang;

import static net.phoenix_tesla_network.tesla.api.gui.PhoenixGuiTextures.TESLA_BACKGROUND;

public class WingFlightGui {

    public static final String NBT_MODE = "FlightMode";
    public static final String NBT_SPEED = "FlightSpeed";
    public static final String NBT_DRIFT = "FlightDrift";

    public static final String MODE_POWERED = "powered";
    public static final String MODE_CREATIVE = "creative";

    private static final int STEPS = 10;

    public static ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ItemStack stack = holder.getHeld();

        if (!(stack.getItem() instanceof PhoenixArmorItem)) return null;

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_MODE)) tag.putString(NBT_MODE, MODE_POWERED);
        if (!tag.contains(NBT_SPEED)) tag.putInt(NBT_SPEED, 5);
        if (!tag.contains(NBT_DRIFT)) tag.putInt(NBT_DRIFT, 5);

        int W = 200;
        int H = 230;

        ModularUI ui = new ModularUI(W, H, holder, player)
                .background(GuiTextures.BACKGROUND);

        WidgetGroup root = new WidgetGroup(0, 0, W, H);

        root.addWidget(
                new LabelWidget(8, 6, TeslaText.str("phoenix_tesla_network.wing_flight_gui.wing_flight_control")));

        WidgetGroup header = new WidgetGroup(5, 18, W - 10, 46);
        header.setBackground(TESLA_BACKGROUND);
        header.addWidget(new ComponentPanelWidget(5, 4, text -> {
            String mode = stack.getOrCreateTag().getString(NBT_MODE);
            boolean isPowered = MODE_POWERED.equals(mode);

            text.add(TeslaLang.tr("phoenix_tesla_network.wing_flight_gui.mode").withStyle(ChatFormatting.GRAY)
                    .append(Component
                            .literal(isPowered ? TeslaText.str("phoenix_tesla_network.wing_flight_gui.powered") :
                                    TeslaText.str("phoenix_tesla_network.wing_flight_gui.creative"))
                            .withStyle(isPowered ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE)));

            text.add(Component
                    .literal(isPowered ? TeslaText.str("phoenix_tesla_network.wing_flight_gui.drain_5_000_eu_t") :
                            TeslaText.str("phoenix_tesla_network.wing_flight_gui.drain_none_creative")));
        }));
        root.addWidget(header);

        int btnY = 72;
        root.addWidget(new ButtonWidget(5, btnY, W - 10, 20, GuiTextures.BUTTON, c -> {
            if (player.level().isClientSide) return;
            CompoundTag t = stack.getOrCreateTag();
            String current = t.getString(NBT_MODE);
            t.putString(NBT_MODE, MODE_POWERED.equals(current) ? MODE_CREATIVE : MODE_POWERED);
            if (player instanceof ServerPlayer sp) HeldItemUIFactory.INSTANCE.openUI(holder, sp);
        }));
        String currentMode = tag.getString(NBT_MODE);
        root.addWidget(new LabelWidget(W / 2 - 55, btnY + 6,
                MODE_POWERED.equals(currentMode) ?
                        TeslaText.str("phoenix_tesla_network.wing_flight_gui.switch_to_creative_flight") :
                        TeslaText.str("phoenix_tesla_network.wing_flight_gui.switch_to_powered_flight")));

        root.addWidget(new LabelWidget(8, 102, TeslaText.str("phoenix_tesla_network.wing_flight_gui.flight_speed")));
        root.addWidget(buildStepControl(holder, player, stack, NBT_SPEED, 118, W));

        root.addWidget(new LabelWidget(8, 158, TeslaText.str("phoenix_tesla_network.wing_flight_gui.flight_drift")));
        root.addWidget(buildStepControl(holder, player, stack, NBT_DRIFT, 174, W));

        root.addWidget(new LabelWidget(8, H - 12,
                TeslaText.str("phoenix_tesla_network.wing_flight_gui.press_numpad9_or_esc_to")));

        ui.widget(root);
        return ui;
    }

    private static WidgetGroup buildStepControl(HeldItemUIFactory.HeldItemHolder holder,
                                                Player player, ItemStack stack,
                                                String nbtKey, int y, int W) {
        WidgetGroup group = new WidgetGroup(5, y, W - 10, 34);

        group.addWidget(new ButtonWidget(0, 0, 18, 18, GuiTextures.BUTTON, c -> {
            if (player.level().isClientSide) return;
            CompoundTag t = stack.getOrCreateTag();
            t.putInt(nbtKey, Math.max(0, t.getInt(nbtKey) - 1));
            if (player instanceof ServerPlayer sp) HeldItemUIFactory.INSTANCE.openUI(holder, sp);
        }));
        group.addWidget(new LabelWidget(6, 5, "§f-"));

        group.addWidget(new ButtonWidget(W - 28, 0, 18, 18, GuiTextures.BUTTON, c -> {
            if (player.level().isClientSide) return;
            CompoundTag t = stack.getOrCreateTag();
            t.putInt(nbtKey, Math.min(STEPS, t.getInt(nbtKey) + 1));
            if (player instanceof ServerPlayer sp) HeldItemUIFactory.INSTANCE.openUI(holder, sp);
        }));
        group.addWidget(new LabelWidget(W - 23, 5, "§f+"));

        int barX = 22;
        int barW = W - 10 - 22 - 22;
        int segW = barW / STEPS;
        int filled = stack.getOrCreateTag().getInt(nbtKey);

        for (int i = 0; i < STEPS; i++) {
            final int segment = i + 1;
            group.addWidget(new ButtonWidget(
                    barX + (i * segW), 0, segW - 1, 18,
                    segment <= filled ? GuiTextures.BUTTON : GuiTextures.SLOT,
                    c -> {
                        if (player.level().isClientSide) return;
                        stack.getOrCreateTag().putInt(nbtKey, segment);
                        if (player instanceof ServerPlayer sp) HeldItemUIFactory.INSTANCE.openUI(holder, sp);
                    }));
        }

        int current = stack.getOrCreateTag().getInt(nbtKey);
        group.addWidget(new LabelWidget(0, 22,
                String.format("§8%s: §f%d / %d",
                        nbtKey.equals(NBT_SPEED) ? "Speed" : "Drift", current, STEPS)));

        return group;
    }
}
