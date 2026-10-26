package net.phoenix_tesla_network.tesla.common.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Purple Phoenix versions of GTM's module setting rows. Same sync wiring as ItemModuleSettingsBuilder (the widget's
 * value is the sync handler, with allowC2S), only the drawables and label colors differ. Add the rows returned here to
 * an ItemModuleSettingsBuilder with add(...) (that class is final, so it can't be subclassed).
 */
public final class PhoenixWidgets {

    public static final int PANEL_BG = 0xEE0A0512;
    public static final int PANEL_BEVEL = 0xFF3A2050;
    public static final int DISPLAY_BG = 0xFF120A1E;
    public static final int BUTTON_BG = 0xFF241436;
    public static final int BUTTON_BG_HOVER = 0xFF3A2050;
    public static final int BUTTON_BORDER = 0xFF7A3FBF;
    public static final int FILLED = 0xFF8800CC;
    public static final int HIGHLIGHT = 0xFFC480E6;
    public static final int LABEL = 0xAAAAAA;

    private PhoenixWidgets() {}

    public static IDrawable box(int fill, int border) {
        return new DrawableStack(new Rectangle().color(fill), new Rectangle().hollow(1).color(border));
    }

    public static Component colored(Component c, int rgb) {
        return c.copy().withStyle(Style.EMPTY.withColor(rgb));
    }

    private static Flow row(String labelKey, IWidget control) {
        return Flow.row()
                .coverChildren()
                .childPadding(5)
                .child(new TextWidget<>(Text.dynamic(() -> colored(Component.translatable(labelKey), LABEL))))
                .child(control);
    }

    public static IWidget toggleRow(String labelKey, BooleanSupplier getter, Consumer<Boolean> setter) {
        return row(labelKey, new ToggleButton().size(14)
                .value(new BooleanSyncValue(getter, setter::accept).allowC2S())
                .background(false, box(BUTTON_BG, BUTTON_BORDER))
                .background(true, box(FILLED, HIGHLIGHT))
                .hoverBackground(false, box(BUTTON_BG_HOVER, BUTTON_BORDER))
                .hoverBackground(true, box(FILLED, HIGHLIGHT))
                .overlay(true, GuiTextures.CHECKMARK));
    }

    public static IWidget intSliderRow(String labelKey, IntSupplier getter, IntConsumer setter, int min, int max) {
        SliderWidget slider = new SliderWidget()
                .value(SyncHandlers.intNumber(getter, setter).allowC2S())
                .bounds(min, max)
                .width(60)
                .stopper(1)
                .sliderTexture(box(FILLED, HIGHLIGHT))
                .stopperTexture(new Rectangle().color(0x66C480E6))
                .background(box(DISPLAY_BG, BUTTON_BORDER));
        return row(labelKey, slider)
                .child(new TextWidget<>(Text.dynamic(() -> colored(
                        Component.literal(String.valueOf(getter.getAsInt())), HIGHLIGHT & 0xFFFFFF)))
                        .width(18));
    }

    /**
     * A click-to-cycle button over a fixed list of states: left click = next, right click = previous. The value is
     * a synced int (allowC2S), registered on the panel sync manager the same way ItemModuleSettingsBuilder registers
     * its custom widgets, so clicks on the client reach the server.
     *
     * @param valueKeys one lang key per state; the button shows the current state's name
     */
    public static IWidget cycleRow(PanelSyncManager psm, int id, String labelKey, String[] valueKeys,
                                   IntSupplier getter, IntConsumer setter) {
        var value = SyncHandlers.intNumber(getter, setter).allowC2S();
        psm.syncValue("phoenix_cycle_" + labelKey, id, value);
        int count = valueKeys.length;
        ButtonWidget<?> button = new ButtonWidget<>().size(96, 14)
                .background(box(BUTTON_BG, BUTTON_BORDER))
                .hoverBackground(box(BUTTON_BG_HOVER, HIGHLIGHT))
                .overlay(Text.dynamic(() -> colored(
                        Component.translatable(valueKeys[Math.floorMod(getter.getAsInt(), count)]),
                        HIGHLIGHT & 0xFFFFFF)))
                .tooltipDynamic(tooltip -> tooltip.add(Component.translatable("gui.phoenix_tesla_network.cycle_hint")))
                .onMousePressed((ctx, mouseButton) -> {
                    if (mouseButton != 0 && mouseButton != 1) return false;
                    int next = Math.floorMod(value.getIntValue() + (mouseButton == 0 ? 1 : -1), count);
                    value.setIntValue(next);
                    return true;
                });
        return row(labelKey, button);
    }
}
