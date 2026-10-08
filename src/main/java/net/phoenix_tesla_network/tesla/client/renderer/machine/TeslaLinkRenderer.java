package net.phoenix_tesla_network.tesla.client.renderer.machine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.phoenix_tesla_network.tesla.api.range.ITeslaLinkNode;
import net.phoenix_tesla_network.tesla.client.PhoenixRenderTypes;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaRelayMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public class TeslaLinkRenderer extends DynamicRender<TeslaRelayMachine, TeslaLinkRenderer> {

    public static final TeslaLinkRenderer INSTANCE = new TeslaLinkRenderer();
    public static final Codec<TeslaLinkRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<TeslaRelayMachine, TeslaLinkRenderer> TYPE = new DynamicRenderType<>(CODEC);

    private TeslaLinkRenderer() {}

    @Override
    public @NotNull DynamicRenderType<TeslaRelayMachine, TeslaLinkRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(TeslaRelayMachine machine, @NotNull Vec3 cameraPos) {
        return machine.getLinkedPos() != null;
    }

    @Override
    public boolean shouldRenderOffScreen(TeslaRelayMachine machine) {
        return machine.getLinkedPos() != null;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(TeslaRelayMachine machine) {
        BlockPos linked = machine.getLinkedPos();
        AABB box = new AABB(machine.getPos()).inflate(8);
        return linked == null ? box : box.minmax(new AABB(linked).inflate(60));
    }

    @Override
    public void render(TeslaRelayMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BlockPos linked = machine.getLinkedPos();
        if (linked == null || machine.getLevel() == null) return;

        if (!(MetaMachine.getMachine(machine.getLevel(), linked) instanceof ITeslaLinkNode parent)) return;

        Vec3 origin = Vec3.atLowerCornerOf(machine.getPos());
        Vec3 start = machine.getLinkAnchor().subtract(origin);
        Vec3 end = parent.getLinkAnchor().subtract(origin);

        double length = end.subtract(start).length();
        int depth = length < 12 ? 5 : length < 32 ? 6 : 7;
        float load = machine.getLoadLevel();
        long time = machine.getLevel().getGameTime();

        float pulse = 1f + 0.3f * load * (float) Math.sin((time + partialTick) * (0.25 + 0.6 * load));
        float jitter = (float) Math.min(0.18 + length * 0.012, 1.6) * (0.7f + 0.8f * load);
        float width = (float) Math.min(1.0 + length * 0.02, 3.0) * (0.55f + 1.45f * load) * pulse;

        var vc = buffer.getBuffer(PhoenixRenderTypes.LIGHT_RING());

        TeslaTowerRenderer.drawArc(poseStack, vc, start, end, time, depth, jitter, width, 0L);
        TeslaTowerRenderer.drawArc(poseStack, vc, start, end, time, depth, jitter * 1.4f, width * 0.6f, 1L);
    }
}
