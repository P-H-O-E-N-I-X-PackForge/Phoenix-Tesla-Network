package net.phoenix_tesla_network.tesla.common.block;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.phoenix_tesla_network.tesla.api.machine.trait.ITeslaBattery;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Getter
@MethodsReturnNonnullByDefault
public class TeslaBatteryBlock extends Block {

    private final ITeslaBattery batteryData;

    public TeslaBatteryBlock(Properties properties, ITeslaBattery batteryData) {
        super(properties);
        this.batteryData = batteryData;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (batteryData.getTier() == -1) {
            tooltip.add(Component.translatable("block.phoenix_tesla_network.tesla_battery.tooltip_empty"));
        } else {
            tooltip.add(Component.translatable("block.phoenix_tesla_network.tesla_battery.tooltip_filled",
                    FormattingUtil.formatNumbers(batteryData.getCapacity())));
        }
    }

    public enum TeslaBatteryType implements ITeslaBattery {

        LV(1, true),
        MV(2, true),
        HV(3, true),
        EV(4, true),
        IV(5, true),
        LuV(6, true),
        ZPM(7, true),
        UV(8, true),

        UHV(9, BigInteger.valueOf(10_000_000_000L).multiply(BigInteger.valueOf(100))),
        UEV(10, BigInteger.valueOf(50_000_000_000L).multiply(BigInteger.valueOf(100))),
        UIV(11, BigInteger.valueOf(250_000_000_000L).multiply(BigInteger.valueOf(100))),
        UXV(12, BigInteger.valueOf(1_000_000_000_000L).multiply(BigInteger.valueOf(100))),
        OPV(13, BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(400))),
        MAX(14, BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(900000000)));

        private final int tier;
        private final Supplier<BigInteger> capacity;
        private final boolean placeholderTexture;
        private BigInteger stored = BigInteger.ZERO;

        TeslaBatteryType(int tier, BigInteger capacity) {
            this.tier = tier;
            this.capacity = () -> capacity;
            this.placeholderTexture = false;
        }

        TeslaBatteryType(int tier, boolean scaledCapacity) {
            this.tier = tier;
            this.capacity = () -> TeslaBatteryCapacities.capacityFor(tier);
            this.placeholderTexture = true;
        }

        public boolean usesPlaceholderTexture() {
            return placeholderTexture;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public BigInteger getCapacity() {
            return capacity.get();
        }

        @Override
        public String getBatteryName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public BigInteger getStored() {
            return stored != null ? stored : BigInteger.ZERO;
        }

        @Override
        public void setStored(BigInteger amount) {
            stored = amount != null ? amount : BigInteger.ZERO;
        }

        @Override
        public BigInteger getMaxInput() {
            return BigInteger.valueOf(Long.MAX_VALUE);
        }

        @Override
        public BigInteger getMaxOutput() {
            return BigInteger.valueOf(Long.MAX_VALUE);
        }
    }
}
