package net.phoenix_tesla_network.tesla.common.ui;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.*;
import com.gregtechceu.gtceu.api.mui.GTGuiScreen;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The Phoenix Tech module screen: GTM's module-configuration screen restyled in the old purple Phoenix palette.
 * Structure and widget calls follow GTM's ModularArmorManagerUI; what's new is the look (backgrounds, borders,
 * colored text). Module settings widgets (sliders etc.) still come from each module's getSettings(...).
 * NOT COMPILED.
 */
public class PhoenixArmorManagerUI implements IUIHolder<PhoenixArmorUIFactory.ArmorGuiData> {

    // Palette from the old WingFlightScreen.
    public static final int PANEL_BG = 0xEE0A0512;
    public static final int PANEL_BEVEL = 0xFF3A2050;
    public static final int DISPLAY_BG = 0xFF120A1E;
    public static final int BUTTON_BG = 0xFF241436;
    public static final int BUTTON_BORDER = 0xFF7A3FBF;
    public static final int TITLE = 0xB000FF;
    public static final int LABEL = 0xAAAAAA;
    public static final int HIGHLIGHT = 0xC480E6;

    /** Fill + 1px border. */
    private static IDrawable box(int fill, int border) {
        return new DrawableStack(new Rectangle().color(fill), new Rectangle().hollow(1).color(border));
    }

    private static Component colored(Component c, int rgb) {
        return c.copy().withStyle(Style.EMPTY.withColor(rgb));
    }

    @Override
    public ModularPanel<?> buildUI(PhoenixArmorUIFactory.ArmorGuiData data, PanelSyncManager syncManager,
                                   UISettings settings) {
        ModularPanel<?> panel = new ModularPanel<>("phoenixSuite")
                .size(284, 224)
                .background(box(PANEL_BG, PANEL_BEVEL));

        PagedWidget<?> pages = new PagedWidget<>().pos(52, 24).size(224, 192);
        pages.addPage(new TextWidget<>(Text.dynamic(() -> colored(Component.translatable(
                "gui.phoenix_tesla_network.suite.select"), LABEL)))
                .width(216).horizontalCenter().verticalCenter());

        List<IPanelHandler> popouts = new ArrayList<>();
        Flow armorList = Flow.col().pos(8, 24).coverChildren().childPadding(6);
        String[] labels = { "helmet", "chestplate", "leggings", "boots" };

        for (int armorIndex = 0; armorIndex < data.armor.length; armorIndex++) {
            int pageIndex = armorIndex + 1;
            ItemStack stack = data.armor[armorIndex];
            IModularItem modular = GTCapabilityHelper.getModularItem(stack);

            ToggleButton button = new ToggleButton().size(36)
                    .background(false, box(BUTTON_BG, BUTTON_BORDER))
                    .background(true, box(0xFF3A2050, 0xFFC480E6))
                    .overlay(stack.isEmpty() ? Text.str("-") : new ItemDrawable(stack).asIcon().size(20))
                    .tooltipDynamic(tooltip -> {
                        tooltip.add(Component.translatable("gui.gtceu.armor_config." + labels[pageIndex - 1]));
                        if (!stack.isEmpty()) tooltip.add(stack.getHoverName());
                    })
                    .value(new BoolValue.Dynamic(() -> pages.getCurrentPageIndex() == pageIndex, selected -> {
                        if (!selected || modular == null) return;
                        popouts.forEach(IPanelHandler::closePanel);
                        pages.setPage(pageIndex);
                    }));
            armorList.child(button);

            Flow moduleList = Flow.col().size(224, 192).childPadding(2);
            moduleList.child(new TextWidget<>(Text.dynamic(() -> colored(stack.getHoverName(), HIGHLIGHT)))
                    .width(224).height(22).scale(0.75f));

            if (modular != null) {
                for (int slotIndex = 0; slotIndex < modular.getSlots().size(); slotIndex++) {
                    ModuleContext applied = modular.getModuleContextForSlot(slotIndex);
                    if (applied == null) continue;
                    int index = slotIndex;
                    String name = "suite" + armorIndex + "_module" + index;
                    IPanelHandler handler = syncManager.syncedPanel(name, true,
                            (psm, h) -> createPanelForModule(psm, h, modular, index, pageIndex * 100 + index, name));
                    popouts.add(handler);
                    moduleList.child(new ButtonWidget<>().size(224, 16)
                            .background(box(BUTTON_BG, BUTTON_BORDER))
                            .overlay(Text.dynamic(() -> applied.getModule().getDisplayName(applied)).scale(0.75f))
                            .tooltipDynamic(tooltip -> GTMuiWidgets.addWrappedModuleDescription(tooltip,
                                    applied.getModule().getInfo()))
                            .onMousePressed((ctx, mouseButton) -> {
                                if (mouseButton != 0) return false;
                                for (IPanelHandler other : popouts) {
                                    if (other != handler) other.closePanel();
                                }
                                // Open-only: closing is done by the popout's x button or by clicking outside it.
                                if (!handler.isPanelOpen()) handler.openPanel();
                                return true;
                            }));
                }
                if (modular.getAllModuleInstances().isEmpty()) {
                    moduleList.child(new TextWidget<>(Text.dynamic(() -> colored(Component.translatable(
                            "gui.phoenix_tesla_network.suite.no_modules"), LABEL))));
                }
            }
            pages.addPage(moduleList);
        }

        TextWidget<?> title = new TextWidget<>(Text.dynamic(() -> colored(Component.translatable(
                "gui.phoenix_tesla_network.suite.title"), TITLE)))
                .pos(8, 8).width(268);
        return panel.child(title).child(armorList).child(pages);
    }

    private ModularPanel<?> createPanelForModule(PanelSyncManager psm, IPanelHandler self,
                                                 IModularItem modularItem, int index, int settingsId,
                                                 String name) {
        ModuleContext initial = Objects.requireNonNull(modularItem.getModuleContextForSlot(index));
        ModuleContext appliedModule = new ModuleContext(initial.getAppliedTo(), modularItem, initial.getData()) {

            @Override
            public ModuleData getData() {
                return Objects.requireNonNull(modularItem.getData().getModuleDataForSlot(index));
            }

            @Override
            public <T extends ModuleData> T getData(Class<T> dataClass) {
                return dataClass.cast(getData());
            }
        };
        ItemModule module = appliedModule.getModule();
        ItemStack moduleItem = appliedModule.getData().getModuleItem();

        // Closes this popout. (closeOnOutOfBoundsClick below also closes it when you click elsewhere.)
        ButtonWidget<?> closeButton = new ButtonWidget<>().size(12, 12)
                .background(box(BUTTON_BG, BUTTON_BORDER))
                .hoverBackground(box(0xFF3A2050, 0xFFC480E6))
                .overlay(Text.str("x"))
                .onMousePressed((ctx, mouseButton) -> {
                    if (mouseButton != 0) return false;
                    self.closePanel();
                    return true;
                });

        return new ModularPanel<>(name)
                .background(box(PANEL_BG, BUTTON_BORDER))
                .closeOnOutOfBoundsClick(true)
                .coverChildren()
                .child(Flow.col()
                        .margin(4)
                        .coverChildren()
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(closeButton)
                        .childIf(moduleItem == null || moduleItem.isEmpty(),
                                () -> new TextWidget<>(Text.dynamic(
                                        () -> colored(module.getDisplayName(appliedModule), HIGHLIGHT)))
                                        .scale(0.75f)
                                        .horizontalCenter()
                                        .paddingBottom(2))
                        .childIf(moduleItem != null && !moduleItem.isEmpty(), () -> {
                            assert moduleItem != null;
                            return Flow.row()
                                    .coverChildren()
                                    .padding(4)
                                    .paddingLeft(0)
                                    .childPadding(4)
                                    .child(new ItemDisplayWidget().item(moduleItem))
                                    .child(Flow.col()
                                            .coverChildrenWidth()
                                            .childPadding(2)
                                            .heightRel(1)
                                            .child(new TextWidget<>(Text.dynamic(
                                                    () -> colored(module.getDisplayName(appliedModule), HIGHLIGHT)))
                                                    .scale(0.75f)
                                                    .left(0))
                                            .child(new TextWidget<>(Text.dynamic(
                                                    () -> colored(moduleItem.getHoverName(), LABEL)))
                                                    .scale(0.6f)
                                                    .left(0)));
                        })
                        .children(module.getSettings(appliedModule, psm, settingsId)));
    }

    @Override
    public ModularScreen createScreen(PhoenixArmorUIFactory.ArmorGuiData data, ModularPanel<?> mainPanel) {
        return new GTGuiScreen(mainPanel);
    }
}
