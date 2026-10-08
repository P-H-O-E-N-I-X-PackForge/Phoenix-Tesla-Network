package net.phoenix_tesla_network.tesla.client;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderManager;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.client.particle.PhoenixParticles;
import net.phoenix_tesla_network.tesla.client.particles.TeslaSparkParticle;
import net.phoenix_tesla_network.tesla.client.renderer.machine.TeslaLinkRenderer;
import net.phoenix_tesla_network.tesla.client.renderer.machine.TeslaTowerRenderer;

import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PhoenixTeslaClient {

    private PhoenixTeslaClient() {}

    public static void init(IEventBus modBus) {
        DynamicRenderManager.register(PhoenixTeslaNetwork.id("tesla_tower"), TeslaTowerRenderer.TYPE);
        DynamicRenderManager.register(PhoenixTeslaNetwork.id("tesla_link"), TeslaLinkRenderer.TYPE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PhoenixParticles.TESLA_SPARK.get(), TeslaSparkProvider::new);
    }

    public static class TeslaSparkProvider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public TeslaSparkProvider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            TeslaSparkParticle particle = new TeslaSparkParticle(level, x, y, z);

            if (this.sprites != null) {
                particle.pickSprite(this.sprites);
            }

            return particle;
        }
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {

        });
    }
}
