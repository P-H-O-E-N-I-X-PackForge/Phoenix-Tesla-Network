package net.phoenix_tesla_network.tesla.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixArmorItem;

import java.util.function.Supplier;

public class C2SToggleTeslaModePacket {

    public C2SToggleTeslaModePacket() {}

    public C2SToggleTeslaModePacket(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (!(chest.getItem() instanceof PhoenixArmorItem)) return;

            CompoundTag tag = chest.getOrCreateTag();
            tag.putBoolean("teslaMode", !tag.getBoolean("teslaMode"));

            player.inventoryMenu.sendAllDataToRemote();
        });
        ctx.get().setPacketHandled(true);
    }
}
