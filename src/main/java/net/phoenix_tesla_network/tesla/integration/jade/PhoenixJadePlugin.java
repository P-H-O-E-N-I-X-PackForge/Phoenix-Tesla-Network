package net.phoenix_tesla_network.tesla.integration.jade;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import net.minecraft.world.level.block.Block;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.integration.jade.provider.TeslaNetworkProvider;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PhoenixJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        PhoenixTeslaNetwork.LOGGER.info("[PhoenixJade] register(common) called");

        registration.registerBlockDataProvider(new TeslaNetworkProvider(), MetaMachine.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        PhoenixTeslaNetwork.LOGGER.info("[PhoenixJade] register(client) called");

        registration.registerBlockComponent(new TeslaNetworkProvider(), Block.class);
    }
}
