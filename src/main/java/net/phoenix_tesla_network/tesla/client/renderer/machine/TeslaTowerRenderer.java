package net.phoenix_tesla_network.tesla.client.renderer.machine;

import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.phoenix_tesla_network.tesla.client.PhoenixRenderTypes;
import net.phoenix_tesla_network.tesla.client.particle.PhoenixParticles;
import net.phoenix_tesla_network.tesla.common.machine.multiblock.electric.TeslaTowerMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class TeslaTowerRenderer extends DynamicRender<TeslaTowerMachine, TeslaTowerRenderer> {

    public static final TeslaTowerRenderer INSTANCE = new TeslaTowerRenderer();
    public static final Codec<TeslaTowerRenderer> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<TeslaTowerMachine, TeslaTowerRenderer> TYPE = new DynamicRenderType<>(CODEC);

    private TeslaTowerRenderer() {}

    public record ArcStyle(float or, float og, float ob, float oa, float ir, float ig, float ib, float ia) {

        public static final ArcStyle DEFAULT = new ArcStyle(1.0f, 0.7f, 0.8f, 0.3f, 0.6f, 0.1f, 0.9f, 1.0f);

        private static final float[][] OUTER = { { 0.55f, 0.85f, 1.0f }, { 1.0f, 0.7f, 0.8f },
                { 1.0f, 0.95f, 0.85f } };
        private static final float[][] INNER = { { 0.2f, 0.5f, 1.0f }, { 0.6f, 0.1f, 0.9f },
                { 1.0f, 0.35f, 0.75f } };

        public static ArcStyle forCharge(float fill) {
            float[] outer = blend(OUTER, fill);
            float[] inner = blend(INNER, fill);
            return new ArcStyle(outer[0], outer[1], outer[2], 0.3f, inner[0], inner[1], inner[2], 1.0f);
        }

        private static float[] blend(float[][] stops, float fill) {
            float f = Math.max(0f, Math.min(1f, fill)) * 2f;
            int i = Math.min(1, (int) f);
            float t = f - i;
            return new float[] {
                    stops[i][0] + (stops[i + 1][0] - stops[i][0]) * t,
                    stops[i][1] + (stops[i + 1][1] - stops[i][1]) * t,
                    stops[i][2] + (stops[i + 1][2] - stops[i][2]) * t };
        }
    }

    private static final Map<TeslaTowerMachine, float[]> RING_STATE = new WeakHashMap<>();

    @Override
    public @NotNull DynamicRenderType<TeslaTowerMachine, TeslaTowerRenderer> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRenderOffScreen(TeslaTowerMachine m) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(TeslaTowerMachine m) {
        return new AABB(m.getPos()).inflate(40);
    }

    @Override
    public boolean shouldRender(TeslaTowerMachine machine, @NotNull Vec3 cameraPos) {
        return machine.isFormed() && machine.isActive();
    }

    @Override
    public void render(TeslaTowerMachine machine, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        long time = machine.getLevel().getGameTime();
        VertexConsumer vc = buffer.getBuffer(PhoenixRenderTypes.LIGHT_RING());

        float fill = machine.getNetworkFill();
        ArcStyle style = ArcStyle.forCharge(fill);
        float now = time + partialTick;
        float targetSpeed = 0.35f + 1.65f * fill;
        float[] ring = RING_STATE.computeIfAbsent(machine, m -> new float[] { 0f, now, targetSpeed });
        float dt = now - ring[1];
        if (dt < 0f || dt > 10f) dt = 0f;
        ring[1] = now;
        ring[2] += (targetSpeed - ring[2]) * Math.min(1f, dt * 0.1f);
        ring[0] += dt * ring[2];
        float ringPhase = ring[0];

        float RING_RADIUS = 6.5f;
        float TEEPEE_DROP = 2.5f;
        int ARC_POINTS = 30;

        float[] yPositions = new float[] { 5.5f, 14.5f, 22.5f };

        Vec3 axis = machine.getSpireAxis().subtract(Vec3.atLowerCornerOf(machine.getPos()));
        float axisX = (float) axis.x;
        float axisZ = (float) axis.z;

        poseStack.pushPose();

        for (int ringIndex = 0; ringIndex < yPositions.length; ringIndex++) {
            float xBase = axisX;
            float yBase = yPositions[ringIndex];
            float zBase = axisZ;
            Vec3 currentTopCenter = new Vec3(xBase, yBase, zBase);

            for (int i = 0; i < ARC_POINTS; i++) {
                double rotationSpeed = ringPhase * (0.02 + (ringIndex * 0.01));
                double angle = (2 * Math.PI * i / ARC_POINTS) + rotationSpeed;

                double x = xBase + Math.cos(angle) * RING_RADIUS;
                double z = zBase + Math.sin(angle) * RING_RADIUS;
                double pulse = Math.sin((time + ringIndex * 10 + i) * 0.1) * 0.2;
                double y = yBase - TEEPEE_DROP + pulse;

                Vec3 targetPos = new Vec3(x, y, z);

                if ((time + i * 7 + ringIndex * 13) % 12 < 5) {
                    drawArc(poseStack, vc, currentTopCenter, targetPos, time, 5, 0.18f, 1.0f, 0L, style);

                    if (machine.getLevel().isClientSide && machine.getLevel().random.nextFloat() < 0.05f) {
                        machine.getLevel().addParticle(
                                PhoenixParticles.TESLA_SPARK.get(),
                                targetPos.x + machine.getPos().getX(),
                                targetPos.y + machine.getPos().getY(),
                                targetPos.z + machine.getPos().getZ(),
                                0.0, 0.0, 0.0);
                    }
                }
            }
        }
        poseStack.popPose();
    }

    private void generateLightningPath(List<Vec3> points, Vec3 start, Vec3 end, int depth, float jitter, long time) {
        if (depth == 0) {
            points.add(end);
            return;
        }

        Vec3 mid = start.add(end).scale(0.5);

        float seed = (float) (time + start.x + start.y + start.z);

        mid = mid.add(
                (sinNoise(seed) - 0.5) * jitter * depth,
                (sinNoise(seed + 1) - 0.5) * jitter * depth,
                (sinNoise(seed + 2) - 0.5) * jitter * depth);

        generateLightningPath(points, start, mid, depth - 1, jitter, time);
        generateLightningPath(points, mid, end, depth - 1, jitter, time);
    }

    private void drawVolumetricArc(VertexConsumer vc, Matrix4f pose, List<Vec3> path, float width, float r, float g,
                                   float b, float a) {
        for (int i = 0; i < path.size() - 1; i++) {
            Vec3 s = path.get(i);
            Vec3 e = path.get(i + 1);

            float dx = (float) (e.x - s.x);
            float dy = (float) (e.y - s.y);
            float dz = (float) (e.z - s.z);

            float nx = -dz * width;
            float nz = dx * width;

            vc.vertex(pose, (float) s.x - nx, (float) s.y, (float) s.z - nz).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) s.x + nx, (float) s.y, (float) s.z + nz).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) e.x + nx, (float) e.y, (float) e.z + nz).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) e.x - nx, (float) e.y, (float) e.z - nz).color(r, g, b, a).endVertex();
        }
    }

    private void drawTeslaLine(VertexConsumer vc, Matrix4f pose, Vec3 s, Vec3 e,
                               int depth, boolean isCore, long time) {
        if (depth == 0) {
            float r, g, b, a;
            if (isCore) {
                r = 160 / 255f;
                g = 32 / 255f;
                b = 240 / 255f;
                a = 1.0f;
            } else {
                r = 255 / 255f;
                g = 183 / 255f;
                b = 197 / 255f;
                a = 0.5f;
            }
            vc.vertex(pose, (float) s.x, (float) s.y, (float) s.z).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) e.x, (float) e.y, (float) e.z).color(r, g, b, a).endVertex();
            return;
        }

        Vec3 mid = s.add(e).scale(0.5);

        float seed = (float) (time + s.x * 31 + s.y * 17 + s.z);
        float jitter = 0.25f * depth;

        double jX = (sinNoise(seed) - 0.5) * jitter;
        double jY = (sinNoise(seed * 1.2f) - 0.5) * jitter;
        double jZ = (sinNoise(seed * 1.5f) - 0.5) * jitter;

        mid = mid.add(jX, jY, jZ);

        drawTeslaLine(vc, pose, s, mid, depth - 1, isCore, time);
        drawTeslaLine(vc, pose, mid, e, depth - 1, isCore, time);
    }

    private static final Vec3[] POINT_CACHE = new Vec3[260];
    static {
        for (int i = 0; i < POINT_CACHE.length; i++) POINT_CACHE[i] = Vec3.ZERO;
    }

    public static void drawArc(PoseStack stack, VertexConsumer vc, Vec3 start, Vec3 end, long time, int depth,
                               float jitter, float widthScale, long seedOffset) {
        drawArc(stack, vc, start, end, time, depth, jitter, widthScale, seedOffset, ArcStyle.DEFAULT);
    }

    public static void drawArc(PoseStack stack, VertexConsumer vc, Vec3 start, Vec3 end, long time, int depth,
                               float jitter, float widthScale, long seedOffset, ArcStyle style) {
        Matrix4f pose = stack.last().pose();

        float smoothTime = time * 0.05f + seedOffset * 7.3f;
        long snapTime = time / 4 + seedOffset * 31;

        POINT_CACHE[0] = start;
        int totalPoints = generatePath(start, end, 1, Math.min(depth, 7), jitter, snapTime, smoothTime);
        POINT_CACHE[totalPoints - 1] = end;

        drawRibbon(vc, pose, totalPoints, 0.08f * widthScale, style.or(), style.og(), style.ob(), style.oa(), time);
        drawRibbon(vc, pose, totalPoints, 0.02f * widthScale, style.ir(), style.ig(), style.ib(), style.ia(), time);
    }

    private static int generatePath(Vec3 s, Vec3 e, int index, int depth, float jitter, long snapTime,
                                    float smoothTime) {
        if (depth == 0) {
            POINT_CACHE[index] = e;
            return index + 1;
        }

        Vec3 mid = s.add(e).scale(0.5);

        float seed = (float) (snapTime + (index * 1.5f) + smoothTime);

        float currentJitter = jitter * (float) Math.sqrt(depth);

        mid = mid.add(
                (sinNoise(seed) - 0.5) * currentJitter,
                (sinNoise(seed + 12) - 0.5) * currentJitter,
                (sinNoise(seed + 24) - 0.5) * currentJitter);

        int nextIndex = generatePath(s, mid, index, depth - 1, jitter, snapTime, smoothTime);
        return generatePath(mid, e, nextIndex, depth - 1, jitter, snapTime, smoothTime);
    }

    private static void drawRibbon(VertexConsumer vc, Matrix4f pose, int count, float width, float r, float g, float b,
                                   float a, long time) {
        for (int i = 0; i < count - 1; i++) {
            Vec3 s = POINT_CACHE[i];
            Vec3 e = POINT_CACHE[i + 1];

            float dx = (float) (e.x - s.x);
            float dy = (float) (e.y - s.y);
            float dz = (float) (e.z - s.z);

            float twistSpeed = time * 0.1f;
            float roll = (i * 0.5f) + twistSpeed;

            float angleX = (float) Math.cos(roll) * width;
            float angleY = (float) Math.sin(roll) * width;

            float nx = -dz * angleX;
            float ny = angleY;
            float nz = dx * angleX;

            vc.vertex(pose, (float) s.x - nx, (float) s.y - ny, (float) s.z - nz).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) s.x + nx, (float) s.y + ny, (float) s.z + nz).color(r, g, b, a).endVertex();

            float nextRoll = ((i + 1) * 0.5f) + twistSpeed;
            float nnx = -dz * (float) Math.cos(nextRoll) * width;
            float nny = (float) Math.sin(nextRoll) * width;
            float nnz = dx * (float) Math.cos(nextRoll) * width;

            vc.vertex(pose, (float) e.x + nnx, (float) e.y + nny, (float) e.z + nnz).color(r, g, b, a).endVertex();
            vc.vertex(pose, (float) e.x - nnx, (float) e.y - nny, (float) e.z - nnz).color(r, g, b, a).endVertex();
        }
    }

    private static float sinNoise(float n) {
        return (float) (Math.sin(n * 2137.123) * 43758.5453) % 1.0f;
    }
}
