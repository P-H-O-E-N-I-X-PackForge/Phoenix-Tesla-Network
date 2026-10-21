package net.phoenix_tesla_network.tesla.network.packet;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTechSuite;

import java.util.function.Supplier;

/** Fired by the Tesla Discharge key: the Tech Suite boots shock everything around the wearer. */
public class C2STeslaDischargePacket {

    private static final int COOLDOWN_TICKS = 100;

    public C2STeslaDischargePacket() {}

    public C2STeslaDischargePacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.isEmpty() || !(boots.getItem() instanceof ArmorComponentItem armor) ||
                    !(armor.getArmorLogic() instanceof PhoenixTechSuite suite)) {
                return;
            }

            CompoundTag data = boots.getOrCreateTag();
            if (data.getInt("dischargeCooldown") > 0) return;

            var electric = GTCapabilityHelper.getElectricItem(boots);
            if (electric == null) return;

            suite.doTeslaDischarge((ServerLevel) player.level(), player, electric);
            data.putInt("dischargeCooldown", COOLDOWN_TICKS);
        });
        ctx.get().setPacketHandled(true);
    }
}
