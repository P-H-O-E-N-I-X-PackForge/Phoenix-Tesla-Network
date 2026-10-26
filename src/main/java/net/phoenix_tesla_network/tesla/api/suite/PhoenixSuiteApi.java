package net.phoenix_tesla_network.tesla.api.suite;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTechArmorItem;

import org.jetbrains.annotations.Nullable;

/**
 * Public read-only API for other Phoenix mods (Phoenix Core, Chronicles, Solaris, Guilds, ...). Everything here
 * works on both logical sides because it only reads equipped items, which the client already has.
 *
 * Reacting to the suite: subscribe to {@link net.phoenix_tesla_network.tesla.api.suite.event.PhoenixLoreScanEvent}
 * and {@link net.phoenix_tesla_network.tesla.api.suite.event.PhoenixSetBonusEvent} on NeoForge.EVENT_BUS.
 */
public final class PhoenixSuiteApi {

    private static final EquipmentSlot[] ARMOR = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET };

    private PhoenixSuiteApi() {}

    /** Number of Phoenix Tech pieces worn (0-4). */
    public static int piecesWorn(LivingEntity entity) {
        int n = 0;
        for (EquipmentSlot slot : ARMOR) {
            if (entity.getItemBySlot(slot).getItem() instanceof PhoenixTechArmorItem) n++;
        }
        return n;
    }

    /** True when all four pieces are worn: the set bonus condition. */
    public static boolean isFullSet(LivingEntity entity) {
        return piecesWorn(entity) == 4;
    }

    /** First module of the given class on any worn piece, or null. Includes disabled modules. */
    @Nullable
    public static ModuleContext findModule(LivingEntity entity, Class<? extends ItemModule> type) {
        for (EquipmentSlot slot : ARMOR) {
            ItemStack stack = entity.getItemBySlot(slot);
            var modular = GTCapabilityHelper.getModularItem(stack);
            if (modular == null) continue;
            for (ModuleContext ctx : modular.getAllModuleInstances()) {
                if (type.isInstance(ctx.getModule())) return ctx;
            }
        }
        return null;
    }

    /** True if a module of the given class is installed AND enabled on any worn piece. */
    public static boolean hasActiveModule(LivingEntity entity, Class<? extends ItemModule> type) {
        ModuleContext ctx = findModule(entity, type);
        return ctx != null && ctx.getModule().isEnabled(ctx);
    }
}
