package net.phoenix_tesla_network.tesla.network.packet;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTechSuite;
import net.phoenix_tesla_network.tesla.util.StackNbt;

/** Fired by the Tesla Discharge key: the Tech Suite boots shock everything around the wearer. */
public class C2STeslaDischargePacket implements CustomPacketPayload {

    public static final Type<C2STeslaDischargePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("phoenix_tesla_network", "c2s_tesla_discharge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2STeslaDischargePacket> STREAM_CODEC = StreamCodec
            .unit(new C2STeslaDischargePacket());

    private static final int COOLDOWN_TICKS = 100;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2STeslaDischargePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
            if (boots.isEmpty() || !(boots.getItem() instanceof ArmorComponentItem armor) ||
                    !(armor.getArmorLogic() instanceof PhoenixTechSuite suite)) {
                return;
            }

            CompoundTag data = StackNbt.getOrCreateTag(boots);
            if (data.getInt("dischargeCooldown") > 0) return;

            var electric = GTCapabilityHelper.getElectricItem(boots);
            if (electric == null) return;

            suite.doTeslaDischarge((ServerLevel) player.level(), player, electric);
            data.putInt("dischargeCooldown", COOLDOWN_TICKS);
        });
    }
}
