package net.phoenix_tesla_network.tesla.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GradientUtil;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.api.gui.PhoenixGuiTextures;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;
import net.phoenix_tesla_network.tesla.api.range.ITeslaLinkNode;
import net.phoenix_tesla_network.tesla.api.range.TeslaLoss;
import net.phoenix_tesla_network.tesla.api.range.TeslaRange;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.part.TeslaEnergyHatchPartMachine;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.unique.UniqueWorkableElectricMultiblockMachine;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;
import net.phoenix_tesla_network.tesla.utils.TeamUtils;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import static net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.part.TeslaEnergyHatchPartMachine.TESLA_DEBUG;

public class TeslaTowerMachine extends UniqueWorkableElectricMultiblockMachine
                               implements IEnergyInfoProvider, IFancyUIMachine, IDataStickInteractable, ITeslaLinkNode {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            TeslaTowerMachine.class, UniqueWorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public TeslaTowerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyBank = new TeslaEnergyBank(this, List.of());
        subscribeServerTick(this::transferEnergyTick);
    }

    public static final String TTB_BATTERY_HEADER = "TTBatteries_";

    private TeslaTowerType towerType;

    public TeslaTowerType getTowerType() {
        if (towerType == null) {
            towerType = TeslaTowerType.fromId(getDefinition().getId());
            if (towerType == null) towerType = TeslaTowerType.ULTIMATE;
        }
        return towerType;
    }

    private TeslaTeamEnergyData.RangeNodeType getRangeNodeType() {
        return switch (getTowerType()) {
            case BASIC -> TeslaTeamEnergyData.RangeNodeType.TOWER_BASIC;
            case ADVANCED -> TeslaTeamEnergyData.RangeNodeType.TOWER_ADVANCED;
            case ULTIMATE -> TeslaTeamEnergyData.RangeNodeType.TOWER_ULTIMATE;
        };
    }

    private static final BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    @Override
    public boolean isActive() {
        return isFormed() && isWorkingEnabled() && recipeLogic.isActive();
    }

    @Getter
    private TeslaEnergyBank energyBank;
    private EnergyContainerList inputHatches;
    private EnergyContainerList outputHatches;

    private long netInLastSec;
    @Getter
    private long inputPerSec;
    private long netOutLastSec;
    @Getter
    private long outputPerSec;

    private boolean introSequencePlayed = false;

    protected ConditionalSubscriptionHandler tickSubscription;

    public static TextColor nebulaColor(float speed) {
        float baseHue = 260.5f;
        float hueRange = 25f;

        float time = (GTValues.CLIENT_TIME & ((1 << 20) - 1)) * speed;

        float hue = baseHue + (float) (Math.sin(time * Math.PI / 180.0) * (hueRange / 2f));

        return TextColor.fromRgb(GradientUtil.toRGB(hue, 95f, 65f));
    }

    public static final UnaryOperator<Style> NEBULA_HSL = style -> style.withColor(nebulaColor(8.0f));

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        if (!getLevel().isClientSide) {
            ensureOwnerTeamUUID();
        }

        UUID ownerId = getOwnerUUID();

        if (!getLevel().isClientSide && ownerId != null && !introSequencePlayed) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                Player player = serverLevel.getPlayerByUUID(ownerId);

                if (player != null) {
                    introSequencePlayed = true;

                    player.displayClientMessage(
                            Component.literal("We See You, We Know You.")
                                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
                            true);

                    player.sendSystemMessage(Component.literal("The Signal Has Begun.")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

                    int messageDelay = 100;
                }
            }
        }

        if (ownerTeamUUID != null) {
            registerTower(this);
        }
        TeslaWirelessRegistry.registerTower(this);

        IMaintenanceMachine maintenance = null;
        List<IEnergyContainer> inputs = new ArrayList<>();
        List<IEnergyContainer> outputs = new ArrayList<>();

        for (IMultiPart part : getParts()) {
            if (part instanceof IMaintenanceMachine maintenanceMachine) {
                maintenance = maintenanceMachine;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                IO io = handlerList.getHandlerIO();
                if (io == IO.NONE) continue;
                var containers = handlerList.getCapability(EURecipeCapability.CAP).stream()
                        .filter(IEnergyContainer.class::isInstance)
                        .map(IEnergyContainer.class::cast)
                        .toList();
                if (io.support(IO.IN)) inputs.addAll(containers);
                if (io.support(IO.OUT)) outputs.addAll(containers);
            }

            if (part instanceof TeslaEnergyHatchPartMachine hatch && !hatch.isWireless()) {
                if (hatch.getIO() == IO.IN) inputs.add(hatch.getEnergyContainer());
                else if (hatch.getIO() == IO.OUT) outputs.add(hatch.getEnergyContainer());
            }
        }

        this.inputHatches = new EnergyContainerList(inputs);
        this.outputHatches = new EnergyContainerList(outputs);

        List<ITeslaBattery> batteries = new ArrayList<>();
        for (Map.Entry<String, Object> entry : getMultiblockState().getMatchContext().entrySet()) {
            if (entry.getKey().startsWith(TTB_BATTERY_HEADER) &&
                    entry.getValue() instanceof BatteryMatchWrapper wrapper) {
                for (int i = 0; i < wrapper.amount; i++) batteries.add(wrapper.partType);
            }
        }

        if (batteries.isEmpty()) {
            onStructureInvalid();
            return;
        }

        if (this.energyBank == null) {
            this.energyBank = new TeslaEnergyBank(this, batteries);
        } else {
            this.energyBank = energyBank.rebuild(batteries);
        }

        updateBatteryTier();

        if (!getLevel().isClientSide && ownerTeamUUID != null) {
            syncToTeslaSavedData();
        }
    }

    private void pushToSoulLinkedMachines(ServerLevel level, TeslaTeamEnergyData.TeamEnergy team) {
        if (team.soulLinkedMachines.isEmpty()) return;

        for (BlockPos targetPos : team.soulLinkedMachines) {

            ResourceKey<Level> dimKey = team.getMachineDimension(targetPos);
            ServerLevel targetLevel = level.getServer().getLevel(dimKey);

            if (targetLevel == null || !targetLevel.isLoaded(targetPos)) continue;
            if (!TeslaRange.isInRange(team, dimKey, targetPos)) continue;

            team.markHatchActive(targetPos, targetLevel.getGameTime());

            if (team.stored.signum() == 0) continue;
            double efficiency = TeslaLoss.efficiency(team, dimKey, targetPos);

            MetaMachine machine = MetaMachine.getMachine(targetLevel, targetPos);
            if (machine == null) continue;

            long injectedThisTick = 0;

            if (machine instanceof ChargerMachine charger) {
                var energy = charger.energyContainer;
                if (energy != null) {
                    long voltage = energy.getInputVoltage();
                    long available = TeslaLoss.netLong(
                            team.stored.min(BigInteger.valueOf(Long.MAX_VALUE)).longValue(), efficiency);
                    long maxTransfer = voltage * energy.getInputAmperage();
                    long toPush = Math.min(available, maxTransfer);

                    long accepted = energy.acceptEnergyFromNetwork(null, voltage,
                            (long) Math.ceil((double) toPush / voltage));

                    if (accepted > 0) {
                        injectedThisTick = accepted * voltage;
                        team.drain(TeslaLoss.gross(BigInteger.valueOf(injectedThisTick), efficiency));
                    }
                }
            } else if (machine instanceof TieredEnergyMachine tieredMachine) {
                var energy = tieredMachine.energyContainer;
                if (energy != null && energy.getInputVoltage() > 0) {
                    long demand = energy.getEnergyCanBeInserted();
                    if (demand > 0) {
                        long voltage = energy.getInputVoltage();
                        long transferLimit = voltage * energy.getInputAmperage();
                        long toInject = Math.min(demand, transferLimit);
                        long available = team.stored.min(BigInteger.valueOf(
                                TeslaLoss.grossLong(toInject, efficiency))).longValue();

                        if (available > 0) {
                            BigInteger drained = team.drain(BigInteger.valueOf(available));
                            injectedThisTick = TeslaLoss.net(drained, efficiency).longValue();
                            if (injectedThisTick > 0) {
                                energy.addEnergy(injectedThisTick);

                                if (targetLevel.getGameTime() % 10 == 0) {
                                    targetLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                            targetPos.getX() + 0.5, targetPos.getY() + 1.1, targetPos.getZ() + 0.5,
                                            5, 0.2, 0.2, 0.2, 0.05);
                                }
                            }
                        }
                    }
                }
            }

            if (injectedThisTick > 0) {
                team.machineCurrentFlow.merge(targetPos, injectedThisTick, Long::sum);
            }
        }
    }

    private void pullFromSoulLinkedGenerators(ServerLevel level, TeslaTeamEnergyData.TeamEnergy team) {
        if (team.soulLinkedMachines.isEmpty()) return;

        for (BlockPos targetPos : team.soulLinkedMachines) {

            ResourceKey<Level> dimKey = team.getMachineDimension(targetPos);
            ServerLevel targetLevel = level.getServer().getLevel(dimKey);

            if (targetLevel == null || !targetLevel.isLoaded(targetPos)) continue;
            if (!TeslaRange.isInRange(team, dimKey, targetPos)) continue;

            team.markHatchActive(targetPos, targetLevel.getGameTime());

            MetaMachine machine = MetaMachine.getMachine(targetLevel, targetPos);
            if (machine == null) continue;

            long pulledThisTick = 0;

            if (machine instanceof TieredEnergyMachine generator) {
                var energy = generator.energyContainer;
                if (energy != null && energy.getEnergyStored() > 0) {
                    long voltage = energy.getOutputVoltage();
                    long maxAmperage = energy.getOutputAmperage();
                    long available = energy.getEnergyStored();
                    long maxTransfer = voltage * maxAmperage;
                    long toPull = Math.min(available, maxTransfer);

                    if (toPull > 0) {
                        double efficiency = TeslaLoss.efficiency(team, dimKey, targetPos);
                        BigInteger accepted = team.fill(BigInteger.valueOf(TeslaLoss.netLong(toPull, efficiency)));
                        long acceptedLong = accepted.longValue();
                        if (acceptedLong > 0) {
                            long taken = Math.min(toPull, TeslaLoss.grossLong(acceptedLong, efficiency));
                            energy.removeEnergy(taken);
                            pulledThisTick = taken;
                        }
                    }
                }
            }

            if (pulledThisTick > 0) {
                team.machineCurrentFlow.merge(targetPos, -pulledThisTick, Long::sum);

                if (targetLevel.getGameTime() % 12 == 0) {
                    targetLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            targetPos.getX() + 0.5, targetPos.getY() + 1.2, targetPos.getZ() + 0.5,
                            3, 0.1, 0.1, 0.1, 0.02);
                }
            }
        }
    }

    protected void transferEnergyTick() {
        if (getLevel().isClientSide) return;
        if (!isWorkingEnabled() || !isFormed()) {
            if (recipeLogic.isActive()) recipeLogic.setStatus(RecipeLogic.Status.IDLE);

            if (isFormed() && getLevel().getGameTime() % 20 == 0) syncToTeslaSavedData();
            return;
        }

        ServerLevel sl = (ServerLevel) getLevel();
        boolean isDoingWork = false;

        if (sl.getGameTime() % 20 == 0) {
            syncToTeslaSavedData();

            if (ownerTeamUUID != null) {
                TeslaTeamEnergyData data = TeslaTeamEnergyData.get(sl);
                TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(ownerTeamUUID);

                TeslaRange.pruneStaleNodes(sl.getServer(), data, team);

                team.physicalNetIn += netInLastSec;
                team.physicalNetOut += netOutLastSec;

                if (team.lastStatsTick != sl.getGameTime()) {
                    team.lastStatsTick = sl.getGameTime();

                    long totalWirelessInput = 0;
                    long totalWirelessOutput = 0;

                    for (var entry : team.energyOutput.entrySet()) {
                        totalWirelessInput += entry.getValue().longValue();
                        team.machineDisplayFlow.put(entry.getKey(), entry.getValue().longValue() / 20);
                    }
                    for (var entry : team.energyInput.entrySet()) {
                        totalWirelessOutput += entry.getValue().longValue();
                        team.machineDisplayFlow.put(entry.getKey(), -entry.getValue().longValue() / 20);
                    }

                    for (BlockPos mPos : new HashSet<>(team.soulLinkedMachines)) {
                        long accumulated = team.machineCurrentFlow.getOrDefault(mPos, 0L);

                        team.machineDisplayFlow.put(mPos, accumulated / 20);

                        if (accumulated < 0) {
                            totalWirelessInput += Math.abs(accumulated);
                        } else {
                            totalWirelessOutput += accumulated;
                        }

                        team.machineCurrentFlow.put(mPos, 0L);
                    }

                    team.lastNetInput = (team.physicalNetIn + totalWirelessInput) / 20;
                    team.lastNetOutput = (team.physicalNetOut + totalWirelessOutput) / 20;
                    team.physicalNetIn = 0;
                    team.physicalNetOut = 0;

                    team.energyInput.clear();
                    team.energyOutput.clear();
                }
                data.setDirty();
            }

            inputPerSec = netInLastSec;
            outputPerSec = netOutLastSec;
            netInLastSec = 0;
            netOutLastSec = 0;
        }

        if (ownerTeamUUID != null) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(sl);
            TeslaTeamEnergyData.TeamEnergy team = data.getOrCreate(ownerTeamUUID);

            if (inputHatches != null) {
                long incoming = inputHatches.getEnergyStored();
                if (incoming > 0) {
                    BigInteger accepted = team.fill(BigInteger.valueOf(incoming));
                    if (accepted.signum() > 0) {
                        inputHatches.changeEnergy(-accepted.longValue());
                        netInLastSec += accepted.longValue();
                        isDoingWork = true;
                    }
                }
            }

            if (team.lastWirelessPassTick != sl.getGameTime()) {
                team.lastWirelessPassTick = sl.getGameTime();

                pullFromSoulLinkedGenerators(sl, team);

                if (!team.soulLinkedMachines.isEmpty() && team.stored.signum() > 0) {
                    pushToSoulLinkedMachines(sl, team);

                    isDoingWork = true;
                }
            }

            data.setDirty();
        }

        recipeLogic.setStatus(isDoingWork ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE);
    }

    private static final Map<UUID, Set<TeslaTowerMachine>> TEAM_TOWER_MAP = new HashMap<>();

    public static void registerTower(TeslaTowerMachine tower) {
        if (tower.ownerTeamUUID != null) {
            TEAM_TOWER_MAP.computeIfAbsent(tower.ownerTeamUUID, k -> new LinkedHashSet<>()).add(tower);
        }
    }

    public static void unregisterTower(TeslaTowerMachine tower) {
        if (tower.ownerTeamUUID != null) {
            Set<TeslaTowerMachine> towers = TEAM_TOWER_MAP.get(tower.ownerTeamUUID);
            if (towers != null) {
                towers.remove(tower);
                if (towers.isEmpty()) TEAM_TOWER_MAP.remove(tower.ownerTeamUUID);
            }
        }
    }

    public static Set<TeslaTowerMachine> getTowersByTeam(UUID team) {
        return TEAM_TOWER_MAP.getOrDefault(team, Set.of());
    }

    public static TeslaTowerMachine getTowerByTeam(UUID team) {
        return getTowersByTeam(team).stream().findFirst().orElse(null);
    }

    @Override
    public void onStructureInvalid() {
        inputHatches = null;
        outputHatches = null;

        TeslaWirelessRegistry.unregisterTower(this);

        unregisterTower(this);
        removeRangeNode(ownerTeamUUID);

        netInLastSec = 0;
        inputPerSec = 0;
        netOutLastSec = 0;
        outputPerSec = 0;
        super.onStructureInvalid();
    }

    private static MutableComponent getTimeToFillDrainText(BigInteger timeToFillSeconds) {
        if (timeToFillSeconds.compareTo(BIG_INTEGER_MAX_LONG) > 0) {
            timeToFillSeconds = BIG_INTEGER_MAX_LONG;
        }

        Duration duration = Duration.ofSeconds(timeToFillSeconds.longValue());
        String key;
        long fillTime;
        if (duration.getSeconds() <= 180) {
            fillTime = duration.getSeconds();
            key = "gtceu.multiblock.power_substation.time_seconds";
        } else if (duration.toMinutes() <= 180) {
            fillTime = duration.toMinutes();
            key = "gtceu.multiblock.power_substation.time_minutes";
        } else if (duration.toHours() <= 72) {
            fillTime = duration.toHours();
            key = "gtceu.multiblock.power_substation.time_hours";
        } else if (duration.toDays() <= 730) {
            fillTime = duration.toDays();
            key = "gtceu.multiblock.power_substation.time_days";
        } else if (duration.toDays() / 365 < 1_000_000) {
            fillTime = duration.toDays() / 365;
            key = "gtceu.multiblock.power_substation.time_years";
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }

        return Component.translatable(key, FormattingUtil.formatNumbers(fillTime));
    }

    public String getStored() {
        if (energyBank == null) {
            return "0";
        }
        return FormattingUtil.formatNumbers(energyBank.getStored());
    }

    public String getCapacity() {
        if (energyBank == null) {
            return "0";
        }
        return FormattingUtil.formatNumbers(energyBank.getCapacity());
    }

    @Override
    public EnergyInfo getEnergyInfo() {
        return new EnergyInfo(energyBank.getCapacity(), energyBank.getStored());
    }

    @Override
    public boolean supportsBigIntEnergyValues() {
        return true;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private BigInteger[] storage;
    private BigInteger[] maximums;
    @Setter
    private BigInteger capacity;
    private int index;

    @Override
    public @NotNull ModularUI createUI(Player entityPlayer) {
        return new ModularUI(198, 208, this, entityPlayer).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return getParts().stream().filter(IFancyUIProvider.class::isInstance).map(IFancyUIProvider.class::cast)
                .toList();
    }

    @Override
    public void attachTooltips(TooltipsPanel tooltipsPanel) {
        for (IMultiPart part : getParts()) {
            part.attachFancyTooltipsToController(this, tooltipsPanel);
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag bankTag = energyBank.writeToNBT(new CompoundTag());
        tag.put("energyBank", bankTag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        energyBank.readFromNBT(tag.getCompound("energyBank"));
        updateBatteryTier();
    }

    private void updateBatteryTier() {
        int newTier = energyBank.getHighestTier();
        if (newTier != batteryTier) {
            batteryTier = newTier;
            onChanged();
        }
    }

    @Persisted
    private BlockPos boundTowerPos;

    @Nullable
    private TeslaTowerMachine getBoundTower() {
        if (boundTowerPos == null || !(getLevel() instanceof ServerLevel sl)) return null;
        BlockEntity be = getLevel().getBlockEntity(boundTowerPos);
        if (!(be instanceof IMachineBlockEntity mbe)) return null;
        if (!(mbe.getMetaMachine() instanceof TeslaTowerMachine tower)) return null;
        return tower;
    }

    private TickableSubscription energySyncSub;

    @Override
    public void onUnload() {
        super.onUnload();
        if (energySyncSub != null) {
            energySyncSub.unsubscribe();
            energySyncSub = null;
        }
    }

    public void bindToTower(TeslaTowerMachine tower) {
        if (tower == null) return;
        boundTowerPos = tower.self().getPos();
        self().markDirty();
    }

    private void pushEnergyToSavedData() {
        if (this.getLevel() instanceof ServerLevel server && ownerTeamUUID != null) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(server);
            TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(ownerTeamUUID);

            teamData.stored = this.energyBank.getStored();
            teamData.capacity = this.energyBank.getCapacity();

            data.setDirty();
        }
    }

    private void syncToTeslaSavedData() {
        if (this.getLevel() instanceof ServerLevel serverLevel && ownerTeamUUID != null) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(serverLevel);
            TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(ownerTeamUUID);

            teamData.upsertNode(getRangeNodeType(), serverLevel.dimension(), getPos(),
                    this.energyBank.getCapacity(), isWorkingEnabled() && isFormed() && !isDuplicate);

            BigInteger share = teamData.capacity.signum() > 0 ?
                    teamData.stored.multiply(this.energyBank.getCapacity()).divide(teamData.capacity) :
                    BigInteger.ZERO;
            this.energyBank.setStored(share);

            if (teamData.capacity.signum() > 0) {
                float fill = new java.math.BigDecimal(teamData.stored)
                        .divide(new java.math.BigDecimal(teamData.capacity), 4, java.math.RoundingMode.HALF_UP)
                        .floatValue();
                this.networkFill = Math.round(Math.max(0f, Math.min(1f, fill)) * 32f) / 32f;
            } else {
                this.networkFill = 0f;
            }

            data.setOnline(ownerTeamUUID, teamData.anyTowerWorking());
            data.setDirty();
        }
    }

    private void removeRangeNode(@Nullable UUID team) {
        if (team == null || !(getLevel() instanceof ServerLevel serverLevel)) return;

        TeslaTeamEnergyData data = TeslaTeamEnergyData.get(serverLevel);
        TeslaTeamEnergyData.TeamEnergy teamData = data.getOrCreate(team);
        if (teamData.removeNode(serverLevel.dimension(), getPos())) {
            data.setOnline(team, teamData.anyTowerWorking());
            data.setDirty();
        }
    }

    public static class TeslaEnergyBank extends MachineTrait {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                TeslaEnergyBank.class);
        private static final String NBT_SIZE = "Size";
        private static final String NBT_STORED = "Stored";
        private static final String NBT_MAX = "Max";

        private BigInteger[] storage;
        private BigInteger[] maximums;
        @Getter
        private BigInteger capacity;
        private int index;
        private final List<ITeslaBattery> batteries;

        public TeslaEnergyBank(MetaMachine machine, List<ITeslaBattery> batteries) {
            super(machine);
            this.batteries = new ArrayList<>(batteries);
            storage = new BigInteger[batteries.size()];
            maximums = new BigInteger[batteries.size()];
            capacity = BigInteger.ZERO;
            for (int i = 0; i < batteries.size(); i++) {
                maximums[i] = batteries.get(i).getCapacity();
                storage[i] = BigInteger.ZERO;
                capacity = capacity.add(maximums[i]);
            }
        }

        public void setStored(BigInteger totalAmount) {
            if (totalAmount == null || storage == null || storage.length == 0) return;
            BigInteger remaining = totalAmount.max(BigInteger.ZERO).min(this.capacity);
            for (int i = 0; i < storage.length; i++) {
                BigInteger toPut = remaining.min(maximums[i]);
                storage[i] = toPut;
                remaining = remaining.subtract(toPut);
            }
            this.index = 0;
            while (index < storage.length - 1 && storage[index].equals(maximums[index])) {
                index++;
            }
        }

        public int getHighestTier() {
            if (batteries.isEmpty()) return 0;
            return batteries.stream().mapToInt(ITeslaBattery::getTier).max().orElse(0);
        }

        public void readFromNBT(CompoundTag storageTag) {
            int size = storageTag.getInt(NBT_SIZE);
            storage = new BigInteger[size];
            maximums = new BigInteger[size];
            capacity = BigInteger.ZERO;
            for (int i = 0; i < size; i++) {
                CompoundTag subtag = storageTag.getCompound(String.valueOf(i));
                storage[i] = new BigInteger(
                        subtag.getString(NBT_STORED).isEmpty() ? "0" : subtag.getString(NBT_STORED));
                maximums[i] = new BigInteger(subtag.getString(NBT_MAX).isEmpty() ? "0" : subtag.getString(NBT_MAX));
                capacity = capacity.add(maximums[i]);
            }
        }

        public CompoundTag writeToNBT(CompoundTag compound) {
            compound.putInt(NBT_SIZE, storage.length);
            for (int i = 0; i < storage.length; i++) {
                CompoundTag subtag = new CompoundTag();
                subtag.putString(NBT_STORED, storage[i].toString());
                subtag.putString(NBT_MAX, maximums[i].toString());
                compound.put(String.valueOf(i), subtag);
            }
            return compound;
        }

        public TeslaEnergyBank rebuild(@NotNull List<ITeslaBattery> batteries) {
            TeslaEnergyBank newStorage = new TeslaEnergyBank(this.machine,
                    batteries);
            for (BigInteger stored : storage) {
                newStorage.fill(stored);
            }
            return newStorage;
        }

        public long fill(long amount) {
            BigInteger filled = fill(BigInteger.valueOf(amount));
            return filled.longValue();
        }

        public BigInteger fill(BigInteger amount) {
            if (amount.signum() < 0) return BigInteger.ZERO;

            if (index < storage.length && storage[index].equals(maximums[index])) {
                if (index < storage.length - 1) index++;
            }

            BigInteger space = maximums[index].subtract(storage[index]);
            BigInteger toFill = amount.min(space);

            if (toFill.equals(BigInteger.ZERO) && index == storage.length - 1) {
                return BigInteger.ZERO;
            }

            storage[index] = storage[index].add(toFill);
            BigInteger remaining = amount.subtract(toFill);

            if (remaining.signum() > 0 && index < storage.length - 1) {
                return toFill.add(fill(remaining));
            }

            return toFill;
        }

        public long drain(long amount) {
            BigInteger drained = drain(BigInteger.valueOf(amount));
            return drained.longValue();
        }

        public BigInteger drain(BigInteger amount) {
            if (amount.signum() < 0) return BigInteger.ZERO;

            if (index >= 0 && storage[index].equals(BigInteger.ZERO)) {
                if (index > 0) index--;
            }

            BigInteger toDrain = storage[index].min(amount);

            if (toDrain.equals(BigInteger.ZERO) && index == 0) {
                return BigInteger.ZERO;
            }

            storage[index] = storage[index].subtract(toDrain);
            BigInteger remaining = amount.subtract(toDrain);

            if (remaining.signum() > 0 && index > 0) {
                return toDrain.add(drain(remaining));
            }

            return toDrain;
        }

        public BigInteger getStored() {
            BigInteger total = BigInteger.ZERO;
            for (BigInteger b : storage) total = total.add(b);
            return total;
        }

        public boolean hasEnergy() {
            return getStored().signum() > 0;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }

    @Getter
    public static class BatteryMatchWrapper {

        private final ITeslaBattery partType;
        private int amount;

        public BatteryMatchWrapper(ITeslaBattery partType) {
            this.partType = partType;
        }

        public BatteryMatchWrapper increment() {
            amount++;
            return this;
        }
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixTeslaItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        var tag = binder.getTag();
        if (!getLevel().isClientSide && tag != null && tag.hasUUID("TargetTeam")) {

            UUID previousTeam = this.ownerTeamUUID;
            if (previousTeam != null && !previousTeam.equals(tag.getUUID("TargetTeam"))) {
                unregisterTower(this);
                removeRangeNode(previousTeam);
            }

            this.ownerTeamUUID = tag.getUUID("TargetTeam");

            registerTower(this);

            if (isFormed()) {
                syncToTeslaSavedData();
                self().markDirty();
            }

            player.sendSystemMessage(
                    Component.literal("Tower frequency set to: " + TeamUtils.getTeamName(ownerTeamUUID))
                            .withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    @Override
    public InteractionResult onDataStickShiftUse(Player player, ItemStack binder) {
        if (!binder.is(PhoenixTeslaItems.TESLA_BINDER.get())) return InteractionResult.PASS;

        if (!getLevel().isClientSide) {
            ensureOwnerTeamUUID();
            if (this.ownerTeamUUID != null) {
                var tag = binder.getOrCreateTag();
                tag.putUUID("TargetTeam", this.ownerTeamUUID);
                tag.putString("TeamName", player.getName().getString() + "'s Network");

                if (!tag.contains("OwnerName")) {
                    tag.putString("OwnerName", player.getName().getString());
                }

                syncToTeslaSavedData();
                player.sendSystemMessage(Component.literal("Tower frequency copied to Binder.")
                        .withStyle(ChatFormatting.GREEN));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(getLevel().isClientSide);
    }

    @Override
    public net.minecraft.world.phys.Vec3 getLinkAnchor() {
        return getSpireAxis().add(0, getTowerType().linkAnchorHeight(), 0);
    }

    public net.minecraft.world.phys.Vec3 getSpireAxis() {
        net.minecraft.core.Direction front = getFrontFacing();
        if (front.getAxis().isVertical()) front = net.minecraft.core.Direction.NORTH;
        var back = front.getOpposite();
        var right = front.getClockWise();
        return net.minecraft.world.phys.Vec3.atCenterOf(getPos()).add(
                back.getStepX() * getTowerType().spireBackOffset() + right.getStepX() * 0.5, 0,
                back.getStepZ() * getTowerType().spireBackOffset() + right.getStepZ() * 0.5);
    }

    @DescSynced
    private float networkFill;

    public float getNetworkFill() {
        return networkFill;
    }

    @Persisted
    @DescSynced
    private int batteryTier;
    @Persisted
    private UUID ownerTeamUUID;

    private void ensureOwnerTeamUUID() {
        if (!(getLevel() instanceof ServerLevel sl)) return;

        if (this.ownerTeamUUID != null) return;

        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID != null) {

            this.ownerTeamUUID = TeamUtils.getTeamIdOrPlayerFallback(ownerUUID);

            self().markDirty();

            if (TESLA_DEBUG) {
                PhoenixTeslaNetwork.LOGGER.info("Tesla Tower at {} auto-assigned to Team {}",
                        getPos().toShortString(), ownerTeamUUID);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        if (!isFormed()) {
            textList.add(Component.literal("Tesla Network: Inactive").withStyle(ChatFormatting.RED));
            return;
        }

        Style GOLD = Style.EMPTY.withColor(ChatFormatting.GOLD);
        Style AQUA = Style.EMPTY.withColor(ChatFormatting.AQUA);
        Style GREEN = Style.EMPTY.withColor(ChatFormatting.GREEN);
        Style RED = Style.EMPTY.withColor(ChatFormatting.RED);

        textList.add(Component.literal("Tesla Network: ")
                .append(Component.literal(isWorkingEnabled() ? "ONLINE" : "OFFLINE")
                        .withStyle(isWorkingEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED)));

        textList.add(Component.literal("Tower: ")
                .append(Component.literal(getTowerType().displayName()).withStyle(AQUA)));

        var profile = getTowerType().profile();
        String rangeText;
        if (profile.infiniteRange) {
            rangeText = profile.crossDimension ? "Infinite, all dimensions" : "Infinite, this dimension";
        } else {
            rangeText = profile.rangeBlocks + " blocks" + (profile.crossDimension ? ", all dimensions" : "");
        }
        textList.add(Component.literal("Range: ").append(Component.literal(rangeText).withStyle(GOLD)));

        textList.add(Component.literal("Team: ")
                .append(Component.literal(ownerTeamUUID == null ? "None" : TeamUtils.getTeamName(ownerTeamUUID))
                        .withStyle(AQUA)));

        if (energyBank != null) {
            textList.add(Component.literal("Stored: ")
                    .append(Component
                            .literal(formatTeslaValue(FormattingUtil.formatNumbers(energyBank.getStored()), false) +
                                    " EU")
                            .withStyle(GOLD)));

            textList.add(Component.literal("Capacity: ")
                    .append(Component
                            .literal(formatTeslaValue(FormattingUtil.formatNumbers(energyBank.getCapacity()), false) +
                                    " EU")
                            .withStyle(ChatFormatting.YELLOW)));
        }

        long inputVal = 0;
        long outputVal = 0;

        if (!getLevel().isClientSide && getLevel() instanceof ServerLevel serverLevel && ownerTeamUUID != null) {
            var team = TeslaTeamEnergyData.get(serverLevel).getOrCreate(ownerTeamUUID);
            inputVal = team.lastNetInput;
            outputVal = team.lastNetOutput;
        }

        textList.add(Component.literal("Total Input: ")
                .append(Component
                        .literal("+" + formatTeslaValue(FormattingUtil.formatNumbers(inputVal), false) + " EU/t")
                        .withStyle(GREEN)));

        textList.add(Component.literal("Total Output: ")
                .append(Component
                        .literal("-" + formatTeslaValue(FormattingUtil.formatNumbers(outputVal), false) + " EU/t")
                        .withStyle(RED)));

        if (energyBank != null) {
            textList.add(Component.literal("Battery Tier: ")
                    .append(Component.literal(GTValues.VN[energyBank.getHighestTier()]).withStyle(AQUA)));
        }
    }

    private String formatTeslaValue(String valueStr, boolean forceScientific) {
        if (valueStr == null || valueStr.isEmpty()) return "0";
        try {

            String cleanValue = valueStr.replaceAll("[§][0-9a-fk-or]", "")
                    .replace(",", "")
                    .replace("+", "")
                    .replace("-", "")
                    .replaceAll("[a-zA-Z]", "")
                    .trim();

            double value = Double.parseDouble(cleanValue);
            if (value == 0) return "0";

            if (forceScientific && value >= 1000) {
                return String.format("%.1e", value);
            }

            if (value >= 1_000_000_000_000L) {
                return String.format("%.3e", value);
            }

            return String.format("%,.0f", value);

        } catch (NumberFormatException ignored) {

            return valueStr.replaceAll("[§][0-9a-fk-or]", "");
        }
    }

    @Override
    public @NotNull Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 190, 125);
        var container = new DraggableScrollableWidgetGroup(4, 4, 182, 117)
                .setBackground(getScreenTexture());

        container.addWidget(new ImageWidget(140, 70, 32, 32,
                () -> getTeslaTierTexture(batteryTier)));

        container.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
        container.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText).setMaxWidthLimit(150));

        group.addWidget(container);

        group.setBackground(com.gregtechceu.gtceu.api.gui.GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private com.lowdragmc.lowdraglib.gui.texture.IGuiTexture getTeslaTierTexture(int tier) {
        return switch (tier) {
            case 10 -> PhoenixGuiTextures.BATTERY_BAR_UEV;
            case 11 -> PhoenixGuiTextures.BATTERY_BAR_UIV;
            case 12 -> PhoenixGuiTextures.BATTERY_BAR_UXV;
            case 13 -> PhoenixGuiTextures.BATTERY_BAR_OPV;
            case 14 -> PhoenixGuiTextures.BATTERY_BAR_MAX;
            default -> PhoenixGuiTextures.BATTERY_BAR_UHV;
        };
    }
}
