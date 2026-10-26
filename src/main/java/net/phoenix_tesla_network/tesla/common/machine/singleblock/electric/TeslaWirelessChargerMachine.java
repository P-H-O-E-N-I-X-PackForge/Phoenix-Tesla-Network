package net.phoenix_tesla_network.tesla.common.machine.singleblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine.State;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.util.StackNbt;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeslaWirelessChargerMachine extends TieredEnergyMachine
                                         implements IDataStickInteractable, IMuiMachine {

    @SaveField
    @SyncToClient
    private UUID boundTeam;

    @SaveField
    @SyncToClient
    private long lastTransferred = 0L;

    @SaveField
    @SyncToClient
    private State state = State.IDLE;

    private static final long FE_PER_EU = 4;

    private final List<UUID> playersInRange = new ArrayList<>();
    private TickableSubscription tickSubs;

    public TeslaWirelessChargerMachine(BlockEntityCreationInfo holder, int tier) {
        super(holder, tier, NotifiableEnergyContainer.receiverContainer(
                GTValues.V[tier] * 64L, GTValues.V[tier], 1L));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            tickSubs = subscribeServerTick(tickSubs, this::tickCharge);
            registerToNetwork();
        }
    }

    private void changeState(State newState) {
        if (this.state != newState) {
            this.state = newState;
            setRenderState(getRenderState().setValue(GTMachineModelProperties.CHARGER_STATE, newState));
        }
    }

    private void tickCharge() {
        autoBindIfConfigured();

        if (!(getLevel() instanceof ServerLevel level) || boundTeam == null) {
            changeState(State.IDLE);
            return;
        }

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);
        TeslaTeamEnergyData.TeamEnergy network = data.getOrCreate(boundTeam);

        if (getOffsetTimer() % 10 == 0) {
            if (!TeslaRange.isInRange(network, level.dimension(), getBlockPos())) {
                this.lastTransferred = 0L;
                network.machineDisplayFlow.put(getBlockPos(), 0L);
                changeState(State.IDLE);
                return;
            }

            if (network.stored.signum() <= 0) {
                this.lastTransferred = 0L;
                network.machineDisplayFlow.put(getBlockPos(), 0L);
                changeState(State.IDLE);
                return;
            }

            List<Player> playersToCharge = new ArrayList<>();
            for (Player player : level.getServer().getPlayerList().getPlayers()) {
                if (TeamUtils.isPlayerOnTeam(player, boundTeam) &&
                        TeslaRange.isInRange(network, player.level().dimension(), player.blockPosition())) {
                    playersToCharge.add(player);
                }
            }

            handleRangeNotifications(playersToCharge);

            long movedInThisCycle = 0L;
            long voltage = GTValues.V[getTier()];
            long amps = 4;
            long ticksInBatch = 10;

            long totalBatchBudget = voltage * amps * ticksInBatch;

            playerLoop:
            for (Player player : playersToCharge) {
                List<IItemHandler> inventories = new ArrayList<>();
                inventories.add(new PlayerMainInvWrapper(player.getInventory()));
                CuriosApi.getCuriosInventory(player).ifPresent(h -> inventories.add(h.getEquippedCurios()));

                double efficiency = TeslaLoss.efficiency(network, player.level().dimension(),
                        player.blockPosition());

                for (IItemHandler handler : inventories) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (totalBatchBudget <= 0) break playerLoop;

                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.isEmpty()) continue;

                        long accepted = chargeStack(stack, network, efficiency, totalBatchBudget);

                        if (accepted > 0) {

                            if (handler instanceof IItemHandlerModifiable modifiable) {
                                modifiable.setStackInSlot(i, stack);
                            }
                            network.drain(TeslaLoss.gross(BigInteger.valueOf(accepted), efficiency));
                            movedInThisCycle += accepted;
                            totalBatchBudget -= accepted;
                        }
                    }
                }
            }

            this.lastTransferred = movedInThisCycle / ticksInBatch;
            network.machineDisplayFlow.put(getBlockPos(), this.lastTransferred);

            if (movedInThisCycle > 0) {
                network.machineCurrentFlow.merge(getBlockPos(), movedInThisCycle, Long::sum);
                network.markHatchActive(getBlockPos(), level.getGameTime());
                data.setDirty();
                changeState(State.RUNNING);
            } else {
                changeState(playersToCharge.isEmpty() ? State.IDLE : State.FINISHED);
            }
        }
    }

    private long chargeStack(ItemStack stack, TeslaTeamEnergyData.TeamEnergy network, double efficiency,
                             long budget) {
        long networkAvailable = TeslaLoss.netLong(network.stored
                .min(BigInteger.valueOf(TeslaLoss.grossLong(budget, efficiency)))
                .longValue(), efficiency);
        long maxOffer = Math.min(budget, networkAvailable);
        if (maxOffer <= 0) return 0;

        IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
        if (electric != null && electric.chargeable()) {
            long itemNeeded = electric.getMaxCharge() - electric.getCharge();
            if (itemNeeded <= 0) return 0;
            long offer = Math.min(itemNeeded, maxOffer);
            return electric.charge(offer, getTier(), true, false);
        }

        IEnergyStorage fe = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (fe != null && fe.canReceive()) {
            long feNeeded = (long) fe.getMaxEnergyStored() - fe.getEnergyStored();
            if (feNeeded <= 0) return 0;
            long feOffer = Math.min(feNeeded, maxOffer * FE_PER_EU);
            long acceptedFe = fe.receiveEnergy((int) Math.min(feOffer, Integer.MAX_VALUE), false);
            if (acceptedFe <= 0) return 0;
            
            return (acceptedFe + FE_PER_EU - 1) / FE_PER_EU;
        }
        return 0;
    }

    private void handleRangeNotifications(List<Player> nearby) {
        List<UUID> nearbyUUIDs = nearby.stream().map(Player::getUUID).toList();
        for (Player p : nearby) {
            if (!playersInRange.contains(p.getUUID())) {
                p.displayClientMessage(Component.literal("Tesla Field Connected").withStyle(ChatFormatting.AQUA), true);
                playersInRange.add(p.getUUID());
            }
        }
        playersInRange.removeIf(uuid -> {
            if (!nearbyUUIDs.contains(uuid)) {
                Player p = getLevel().getPlayerByUUID(uuid);
                if (p != null)
                    p.displayClientMessage(Component.literal("Tesla Field Disconnected").withStyle(ChatFormatting.GRAY),
                            true);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (boundTeam != null && getLevel() instanceof ServerLevel level) {
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).machineDisplayFlow.remove(getBlockPos());
        }
    }

    private void autoBindIfConfigured() {
        if (isRemote() || getOffsetTimer() % 20 != 0) return;
        if (PhoenixTeslaConfigs.INSTANCE.features.teslaConnectionMode !=
                PhoenixTeslaConfigs.FeatureConfigs.TeslaConnectionMode.TEAM_AUTO) {
            return;
        }

        UUID owner = getOwnerUUID();
        if (TeamUtils.isNil(owner)) return;

        UUID team = TeamUtils.getTeamIdOrPlayerFallback(owner);
        if (team == null) return;

        if (team.equals(boundTeam)) return;

        unregisterFromNetwork();
        this.boundTeam = team;
        registerToNetwork();
        this.markAsChanged();
    }

    private void registerToNetwork() {
        if (!isRemote() && boundTeam != null && getLevel() instanceof ServerLevel level) {
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).addCharger(getBlockPos());
        }
    }

    private void unregisterFromNetwork() {
        if (!isRemote() && boundTeam != null && getLevel() instanceof ServerLevel level) {
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).removeCharger(getBlockPos());
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack stick) {
        if (!stick.is(PhoenixTeslaItems.TESLA_BINDER.get())) return InteractionResult.PASS;
        if (isRemote()) return InteractionResult.SUCCESS;

        var tag = StackNbt.getTag(stick);
        if (tag == null || !tag.hasUUID("TargetTeam")) return InteractionResult.PASS;
        UUID stickTeam = tag.getUUID("TargetTeam");

        if (this.boundTeam != null && this.boundTeam.equals(stickTeam)) {
            unregisterFromNetwork();
            this.boundTeam = null;
            player.sendSystemMessage(Component.literal("Charger Unbound").withStyle(ChatFormatting.YELLOW));
        } else {
            unregisterFromNetwork();
            this.boundTeam = stickTeam;
            registerToNetwork();
            player.sendSystemMessage(Component.literal("Charger Synchronized").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        this.markAsChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        ModularPanel<?> panel = new ModularPanel<>("phoenix_tesla_network:tesla_wireless_charger_panel")
                .size(170, 95);

        Flow layout = Flow.column()
                .size(170, 95)
                .padding(10)
                .crossAxisAlignment(Alignment.CrossAxis.START);

        layout.child(new TextWidget<>(Component.literal("Tesla Field Generator")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).marginBottom(8));

        layout.child(new TextWidget<>(() -> {
            if (boundTeam == null) {
                return Component.literal("STATUS: ").append(Component.literal("UNBOUND").withStyle(ChatFormatting.RED));
            }
            return Component.literal("NETWORK: ")
                    .append(Component.literal(TeamUtils.getTeamName(boundTeam)).withStyle(ChatFormatting.AQUA));
        }).marginBottom(4));

        layout.child(new TextWidget<>(() -> {
            if (boundTeam == null) return Component.empty();
            return Component.literal("REACH: ")
                    .append(Component.literal("Determined by Tesla Network").withStyle(ChatFormatting.LIGHT_PURPLE));
        }).marginBottom(4));

        layout.child(new TextWidget<>(() -> {
            if (boundTeam == null) return Component.empty();
            String rate = FormattingUtil.formatNumbers(lastTransferred);
            return Component.literal("OUTPUT: ")
                    .append(Component.literal(rate + " EU/t").withStyle(ChatFormatting.GREEN));
        }));

        panel.child(layout);
        return panel;
    }
}
