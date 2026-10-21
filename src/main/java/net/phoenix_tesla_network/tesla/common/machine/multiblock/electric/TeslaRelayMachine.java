package net.phoenix_tesla_network.tesla.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.phoenix_tesla_network.tesla.api.range.ITeslaLinkNode;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.util.StackNbt;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class TeslaRelayMachine extends WorkableElectricMultiblockMachine
                               implements IDataStickInteractable, ITeslaLinkNode {

    @SaveField
    private UUID ownerTeamUUID;

    @SaveField
    @SyncToClient
    private boolean linked;
    @SaveField
    @SyncToClient
    private long linkedPos;

    @SaveField
    @SyncToClient
    private float loadLevel;

    public float getLoadLevel() {
        return loadLevel;
    }

    public TeslaRelayMachine(BlockEntityCreationInfo holder) {
        super(holder);
        subscribeServerTick(this::relayTick);
    }

    public UUID getOwnerTeamUUID() {
        return ownerTeamUUID;
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        if (getLevel().isClientSide) return;

        ensureOwnerTeamUUID();
        syncNode();
    }

    @Override
    public void invalidateStructure(@NotNull String substructureName) {
        removeNode(ownerTeamUUID);
        super.invalidateStructure(substructureName);
    }

    private void relayTick() {
        if (getLevel() == null || getLevel().isClientSide) return;
        if (getLevel().getGameTime() % 20 != 0) return;

        if (!isFormed()) {
            if (recipeLogic.isActive()) recipeLogic.setStatus(RecipeLogic.Status.IDLE);
            return;
        }

        syncNode();

        if (ownerTeamUUID != null && getLevel() instanceof ServerLevel level) {
            var team = TeslaTeamEnergyData.get(level).getOrCreate(ownerTeamUUID);
            boolean active = isWorkingEnabled() &&
                    TeslaRange.isRelayActive(team, level.dimension(), getBlockPos());
            recipeLogic.setStatus(active ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);

            var parent = active ? TeslaRange.getRelayParent(team, level.dimension(), getBlockPos()) : null;
            linked = parent != null;
            linkedPos = parent != null ? parent.pos.asLong() : 0L;

            double flow = team.calculateTotalNetworkFlow();
            loadLevel = Math.round(Math.min(1.0, Math.log10(1.0 + flow) / 12.0) * 16.0) / 16f;
        }
    }

    public BlockPos getLinkedPos() {
        return linked && isFormed() && recipeLogic.isWorking() ? BlockPos.of(linkedPos) : null;
    }

    @Override
    public void clientTick() {
        super.clientTick();
        var level = getLevel();
        if (level == null || getLinkedPos() == null) return;

        var random = level.random;
        Vec3 at = getLinkAnchor();
        if (random.nextInt(6) == 0) {
            level.playLocalSound(at.x, at.y, at.z, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.35f,
                    1.7f + random.nextFloat() * 0.4f, false);
        }
        if (random.nextInt(90) == 0) {
            level.playLocalSound(at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.12f,
                    1.9f + random.nextFloat() * 0.2f, false);
        }
    }

    @Override
    public Vec3 getLinkAnchor() {
        Direction back = getFrontFacing().getOpposite();
        return Vec3.atCenterOf(getBlockPos()).add(back.getStepX() * 2.0, 6.0, back.getStepZ() * 2.0);
    }

    private void syncNode() {
        if (!(getLevel() instanceof ServerLevel level) || ownerTeamUUID == null) return;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
        var team = data.getOrCreate(ownerTeamUUID);
        if (team.upsertNode(TeslaTeamEnergyData.RangeNodeType.RELAY, level.dimension(), getBlockPos(),
                BigInteger.ZERO, isFormed() && isWorkingEnabled())) {
            data.setDirty();
        }
    }

    private void removeNode(UUID team) {
        if (team == null || !(getLevel() instanceof ServerLevel level)) return;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
        if (data.getOrCreate(team).removeNode(level.dimension(), getBlockPos())) {
            data.setDirty();
        }
    }

    private void ensureOwnerTeamUUID() {
        if (!(getLevel() instanceof ServerLevel) || ownerTeamUUID != null) return;

        UUID ownerUUID = getOwnerUUID();
        if (!TeamUtils.isNil(ownerUUID)) {
            ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(ownerUUID);
            markAsChanged();
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixTeslaItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        var tag = StackNbt.getTag(binder);
        if (!getLevel().isClientSide && tag != null && tag.hasUUID("TargetTeam")) {
            UUID newTeam = tag.getUUID("TargetTeam");
            if (!newTeam.equals(ownerTeamUUID)) {
                removeNode(ownerTeamUUID);
                ownerTeamUUID = newTeam;
                markAsChanged();
                if (isFormed()) syncNode();
            }

            player.sendSystemMessage(
                    Component.literal("Range extender frequency set to: " + TeamUtils.getTeamName(ownerTeamUUID))
                            .withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    private boolean isExtending() {
        if (ownerTeamUUID != null && getLevel() instanceof ServerLevel level) {
            var team = TeslaTeamEnergyData.get(level).getOrCreate(ownerTeamUUID);
            return isWorkingEnabled() && TeslaRange.isRelayActive(team, level.dimension(), getBlockPos());
        }
        return linked;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = super.getWidgetsForDisplay(syncManager);

        if (!isFormed()) {
            widgets.add(new TextWidget<>(Text.of(
                    Component.literal("Tesla Range Extender: Inactive").withStyle(ChatFormatting.RED))));
            return widgets;
        }

        widgets.add(new TextWidget<>(Text.dynamic(() -> {
            boolean active = isExtending();
            return Component.literal("Tesla Range Extender: ")
                    .append(Component.literal(active ? "EXTENDING" : "NOT CONNECTED")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
        })));
        widgets.add(new TextWidget<>(Text.dynamic(() -> Component.literal("Team: ")
                .append(Component.literal(ownerTeamUUID == null ? "None" : TeamUtils.getTeamName(ownerTeamUUID))
                        .withStyle(ChatFormatting.AQUA)))));
        widgets.add(new TextWidget<>(Text.dynamic(() -> Component.literal("Adds: ")
                .append(Component.literal(PhoenixTeslaConfigs.get().towers.relay.rangeBlocks + " blocks")
                        .withStyle(ChatFormatting.GOLD)))));
        widgets.add(new TextWidget<>(Text.dynamic(() -> isExtending() ? Component.empty() :
                Component.literal("Place it inside the range of a Tesla Tower or another working extender.")
                        .withStyle(ChatFormatting.GRAY))));
        return widgets;
    }
}
