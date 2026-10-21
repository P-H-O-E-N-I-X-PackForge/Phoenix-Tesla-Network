package net.phoenix_tesla_network.tesla.api.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.saveddata.TeslaTeamEnergyData;

@EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID)
public class TeslaLinkEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos brokenPos = event.getPos();
        MetaMachine machine = MetaMachine.getMachine(level, brokenPos);

        if (machine instanceof TieredEnergyMachine) {
            TeslaTeamEnergyData data = TeslaTeamEnergyData.get(level);

            data.removeMachineFromAllTeams(brokenPos);
        }
    }
}
