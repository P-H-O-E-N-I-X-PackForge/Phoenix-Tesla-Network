package net.phoenix_tesla_network.tesla.common.machine.singleblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine.State;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.configs.PhoenixTeslaConfigs;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import top.theillusivec4.curios.api.CuriosApi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeslaWirelessChargerMachine extends TieredEnergyMachine
                                         implements IDataStickInteractable, IFancyUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaWirelessChargerMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    private UUID boundTeam;

    @DescSynced
    private long lastTransferred = 0L;

    @DescSynced
    @RequireRerender
    private State state = State.IDLE;

    // Conversion for plain FE items. Swap this for GTCEu's configured EU->FE ratio if you want it to follow the config.
    private static final long FE_PER_EU = 4;

    private final List<UUID> playersInRange = new ArrayList<>();
    private TickableSubscription tickSubs;

    public TeslaWirelessChargerMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
            if (!TeslaRange.isInRange(network, level.dimension(), getPos())) {
                this.lastTransferred = 0L;
                network.machineDisplayFlow.put(getPos(), 0L);
                changeState(State.IDLE);
                return;
            }

            if (network.stored.signum() <= 0) {
                this.lastTransferred = 0L;
                network.machineDisplayFlow.put(getPos(), 0L);
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
                // Covers main inventory, armor slots and offhand
                inventories.add(new PlayerInvWrapper(player.getInventory()));
                CuriosApi.getCuriosInventory(player).ifPresent(h -> inventories.add(h.getEquippedCurios()));

                // Efficiency depends only on the player's position, so compute it once per player
                double efficiency = TeslaLoss.efficiency(network, player.level().dimension(),
                        player.blockPosition());

                for (IItemHandler handler : inventories) {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        if (totalBatchBudget <= 0) break playerLoop;

                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.isEmpty()) continue;

                        long accepted = chargeStack(stack, network, efficiency, totalBatchBudget);

                        if (accepted > 0) {
                            // Charging mutates the stack in place. Writing it back makes Curios'
                            // handler fire onContentsChanged so the change is saved and synced.
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
            network.machineDisplayFlow.put(getPos(), this.lastTransferred);

            if (movedInThisCycle > 0) {
                network.machineCurrentFlow.merge(getPos(), movedInThisCycle, Long::sum);
                network.markHatchActive(getPos(), level.getGameTime());
                data.setDirty();
                changeState(State.RUNNING);
            } else {
                changeState(playersToCharge.isEmpty() ? State.IDLE : State.FINISHED);
            }
        }
    }

    /**
     * Charges a single stack, supporting both GT electric items and plain Forge Energy (FE) containers.
     *
     * @return EU accepted by the item (already bounded by the budget and network supply)
     */
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

        // Fallback: plain FE container (e.g. AE2 wireless terminals via ae2wtlib)
        IEnergyStorage fe = stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (fe != null && fe.canReceive()) {
            long feNeeded = (long) fe.getMaxEnergyStored() - fe.getEnergyStored();
            if (feNeeded <= 0) return 0;
            long feOffer = Math.min(feNeeded, maxOffer * FE_PER_EU);
            long acceptedFe = fe.receiveEnergy((int) Math.min(feOffer, Integer.MAX_VALUE), false);
            if (acceptedFe <= 0) return 0;
            // Round up so partial EU are never given away for free
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
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).machineDisplayFlow.remove(getPos());
        }
    }

    private void autoBindIfConfigured() {
        if (isRemote() || getOffsetTimer() % 20 != 0) return;
        if (PhoenixTeslaConfigs.INSTANCE.features.teslaConnectionMode !=
                PhoenixTeslaConfigs.FeatureConfigs.TeslaConnectionMode.TEAM_AUTO) {
            return;
        }

        UUID owner = getOwnerUUID();
        if (owner == null) return;

        UUID team = TeamUtils.getTeamIdOrPlayerFallback(owner);
        if (team == null) return;

        if (team.equals(boundTeam)) return;

        unregisterFromNetwork();
        this.boundTeam = team;
        registerToNetwork();
        this.markDirty();
    }

    private void registerToNetwork() {
        if (!isRemote() && boundTeam != null && getLevel() instanceof ServerLevel level) {
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).addCharger(getPos());
        }
    }

    private void unregisterFromNetwork() {
        if (!isRemote() && boundTeam != null && getLevel() instanceof ServerLevel level) {
            TeslaTeamEnergyData.get(level).getOrCreate(boundTeam).removeCharger(getPos());
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack stick) {
        if (!stick.is(PhoenixTeslaItems.TESLA_BINDER.get())) return InteractionResult.PASS;
        if (isRemote()) return InteractionResult.SUCCESS;

        UUID stickTeam = stick.getOrCreateTag().getUUID("TargetTeam");

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

        this.markDirty();
        return InteractionResult.SUCCESS;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup root = new WidgetGroup(0, 0, 170, 80);
        root.setBackground(GuiTextures.BACKGROUND_INVERSE);
        root.addWidget(new ComponentPanelWidget(10, 10, this::addDisplayText));
        return root;
    }

    private void addDisplayText(List<Component> text) {
        text.add(Component.literal(GTValues.VNF[getTier()] + " Wireless Charger").withStyle(ChatFormatting.GOLD,
                ChatFormatting.BOLD));
        text.add(Component.empty());

        if (boundTeam == null) {
            text.add(Component.literal("STATUS: ").append(Component.literal("UNBOUND").withStyle(ChatFormatting.RED)));
        } else {
            text.add(Component.literal("NETWORK: ")
                    .append(Component.literal(TeamUtils.getTeamName(boundTeam)).withStyle(ChatFormatting.AQUA)));
            text.add(Component.literal("REACH: ")
                    .append(Component.literal("Determined by Tesla Network")
                            .withStyle(ChatFormatting.LIGHT_PURPLE)));

            String rate = com.gregtechceu.gtceu.utils.FormattingUtil.formatNumbers(lastTransferred);
            text.add(Component.literal("OUTPUT: ")
                    .append(Component.literal(rate + " EU/t").withStyle(ChatFormatting.GREEN)));
        }
    }

    @Override
    public IGuiTexture getTabIcon() {
        return GuiTextures.CHARGER_OVERLAY;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Tesla Field Generator");
    }
}
