package net.phoenix_tesla_network.tesla.common.data.item;

import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.resources.ResourceLocation;
import net.phoenix_tesla_network.tesla.common.ui.PhoenixWidgets;

import brachy.modularui.value.sync.PanelSyncManager;

/**
 * Base for Phoenix modules: settings start with a Phoenix-styled "enabled" toggle. Subclasses call super and add rows.
 */
public abstract class PhoenixModule extends ItemModule {

    protected PhoenixModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext ctx, PanelSyncManager psm, int id) {
        ItemModuleSettingsBuilder settings = new ItemModuleSettingsBuilder(psm, id);
        settings.add(PhoenixWidgets.toggleRow("gui.gtceu.item_module.enabled", () -> isEnabled(ctx),
                v -> setEnabled(ctx, v)));
        return settings;
    }
}
