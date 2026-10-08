package net.phoenix_tesla_network.tesla.client.coverage;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoenix_tesla_network.tesla.PhoenixTeslaNetwork;
import net.phoenix_tesla_network.tesla.client.PhoenixRenderTypes;
import net.phoenix_tesla_network.tesla.common.data.item.PhoenixTeslaItems;
import net.phoenix_tesla_network.tesla.network.packet.S2CCoverageSyncPacket;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

@Mod.EventBusSubscriber(modid = PhoenixTeslaNetwork.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CoverageRenderer {

    private static final int SEGMENTS = 96;

    private static final double CLIP_DISTANCE = 0.2;

    private static double camX, camY, camZ, lookX, lookY, lookZ;

    private static final float[] LOSS_LEVELS = { 1f, 2.5f, 5f, 10f, 20f, 35f, 50f };

    private static final float UNLIMITED_SHELL_REACH = 1024f;

    private static final float MAX_DRAWN_RADIUS = 2048f;

    private CoverageRenderer() {}

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!isHoldingBinder(mc)) return;

        var entries = ClientCoverageData.fresh();
        if (entries.isEmpty()) return;

        PoseStack stack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        camX = camera.x;
        camY = camera.y;
        camZ = camera.z;
        var look = event.getCamera().getLookVector();
        lookX = look.x();
        lookY = look.y();
        lookZ = look.z();
        var buffers = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffers.getBuffer(PhoenixRenderTypes.COVERAGE_LINES());

        var loss = ClientCoverageData.loss();
        float pulse = 0.5f + 0.12f * (float) Math.sin((mc.level.getGameTime() + event.getPartialTick()) * 0.12);

        stack.pushPose();
        stack.translate(-camera.x, -camera.y, -camera.z);
        for (S2CCoverageSyncPacket.Entry entry : entries) {
            float[] color = colorFor(entry.kind());
            double cx = entry.pos().getX() + 0.5;
            double cy = entry.pos().getY() + 0.5;
            double cz = entry.pos().getZ() + 0.5;
            float alpha = color[3] * (pulse / 0.5f);

            boolean unlimited = entry.radius() < 0 || entry.radius() > MAX_DRAWN_RADIUS;
            if (unlimited) {
                drawUnlimitedMarker(stack, vc, cx, cy, cz, color, alpha);
            } else {
                drawSphere(stack, vc, cx, cy, cz, entry.radius(), color, alpha);
            }

            if (loss.enabled() && (entry.kind() == S2CCoverageSyncPacket.TOWER ||
                    entry.kind() == S2CCoverageSyncPacket.RELAY_ACTIVE)) {
                float reach = unlimited ? UNLIMITED_SHELL_REACH : entry.radius();
                for (float level : LOSS_LEVELS) {
                    float distance = loss.distanceFor(level);
                    if (distance <= 0.5f || distance >= reach) continue;

                    float t = Math.min(1f, level / 50f);
                    float[] shell = { 0.3f + 0.7f * t, 1.0f - 0.75f * t, 0.4f - 0.2f * t };
                    drawShell(stack, vc, cx, cy, cz, distance, shell, alpha * 0.7f);
                }
            }
        }
        stack.popPose();

        buffers.endBatch(PhoenixRenderTypes.COVERAGE_LINES());
    }

    private static boolean isHoldingBinder(Minecraft mc) {
        return mc.player.getMainHandItem().is(PhoenixTeslaItems.TESLA_BINDER.get()) ||
                mc.player.getOffhandItem().is(PhoenixTeslaItems.TESLA_BINDER.get());
    }

    private static float[] colorFor(byte kind) {
        return switch (kind) {
            case S2CCoverageSyncPacket.TOWER -> new float[] { 0.72f, 0.35f, 1.0f, 0.9f };
            case S2CCoverageSyncPacket.RELAY_ACTIVE -> new float[] { 0.3f, 0.9f, 1.0f, 0.8f };
            case S2CCoverageSyncPacket.RELAY_IDLE -> new float[] { 1.0f, 0.3f, 0.3f, 0.6f };
            default -> new float[] { 0.6f, 0.6f, 0.6f, 0.5f };
        };
    }

    private static void drawSphere(PoseStack stack, VertexConsumer vc, double cx, double cy, double cz, double radius,
                                   float[] c, float alpha) {
        for (int lat = -2; lat <= 2; lat++) {
            double phi = lat * Math.PI / 6.0;
            double ringRadius = radius * Math.cos(phi);
            double y = cy + radius * Math.sin(phi);
            double px = cx + ringRadius;
            double pz = cz;
            for (int i = 1; i <= SEGMENTS; i++) {
                double a = 2 * Math.PI * i / SEGMENTS;
                double nx = cx + Math.cos(a) * ringRadius;
                double nz = cz + Math.sin(a) * ringRadius;
                line(stack, vc, px, y, pz, nx, y, nz, c, alpha);
                px = nx;
                pz = nz;
            }
        }

        for (int k = 0; k < 6; k++) {
            double theta = k * Math.PI / 6.0;
            double dx = Math.cos(theta);
            double dz = Math.sin(theta);
            double px = cx + radius * dx;
            double py = cy;
            double pz = cz + radius * dz;
            for (int i = 1; i <= SEGMENTS; i++) {
                double t = 2 * Math.PI * i / SEGMENTS;
                double horizontal = radius * Math.cos(t);
                double nx = cx + horizontal * dx;
                double ny = cy + radius * Math.sin(t);
                double nz = cz + horizontal * dz;
                line(stack, vc, px, py, pz, nx, ny, nz, c, alpha);
                px = nx;
                py = ny;
                pz = nz;
            }
        }
    }

    private static void drawShell(PoseStack stack, VertexConsumer vc, double cx, double cy, double cz, double radius,
                                  float[] c, float alpha) {
        double[] prev = null;
        for (int axis = 0; axis < 3; axis++) {
            prev = null;
            for (int i = 0; i <= SEGMENTS; i++) {
                double a = 2 * Math.PI * i / SEGMENTS;
                double u = Math.cos(a) * radius;
                double v = Math.sin(a) * radius;
                double[] p = switch (axis) {
                    case 0 -> new double[] { cx + u, cy, cz + v };
                    case 1 -> new double[] { cx + u, cy + v, cz };
                    default -> new double[] { cx, cy + u, cz + v };
                };
                if (prev != null) line(stack, vc, prev[0], prev[1], prev[2], p[0], p[1], p[2], c, alpha);
                prev = p;
            }
        }
    }

    private static void drawUnlimitedMarker(PoseStack stack, VertexConsumer vc, double cx, double cy, double cz,
                                            float[] c, float alpha) {
        line(stack, vc, cx, cy - 8, cz, cx, cy + 128, cz, c, alpha);

        double px = cx + 4;
        double pz = cz;
        for (int i = 1; i <= 32; i++) {
            double a = 2 * Math.PI * i / 32;
            double nx = cx + Math.cos(a) * 4;
            double nz = cz + Math.sin(a) * 4;
            line(stack, vc, px, cy, pz, nx, cy, nz, c, alpha);
            px = nx;
            pz = nz;
        }
    }

    private static void line(PoseStack stack, VertexConsumer vc, double x1, double y1, double z1, double x2,
                             double y2, double z2, float[] c, float alpha) {
        double d1 = (x1 - camX) * lookX + (y1 - camY) * lookY + (z1 - camZ) * lookZ - CLIP_DISTANCE;
        double d2 = (x2 - camX) * lookX + (y2 - camY) * lookY + (z2 - camZ) * lookZ - CLIP_DISTANCE;
        if (d1 < 0 && d2 < 0) return;
        if (d1 < 0) {
            double t = d2 / (d2 - d1);
            x1 = x2 + (x1 - x2) * t;
            y1 = y2 + (y1 - y2) * t;
            z1 = z2 + (z1 - z2) * t;
        } else if (d2 < 0) {
            double t = d1 / (d1 - d2);
            x2 = x1 + (x2 - x1) * t;
            y2 = y1 + (y2 - y1) * t;
            z2 = z1 + (z2 - z1) * t;
        }

        var last = stack.last();
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-5f) return;
        nx /= len;
        ny /= len;
        nz /= len;

        vc.vertex(last.pose(), (float) x1, (float) y1, (float) z1).color(c[0], c[1], c[2], alpha)
                .normal(last.normal(), nx, ny, nz).endVertex();
        vc.vertex(last.pose(), (float) x2, (float) y2, (float) z2).color(c[0], c[1], c[2], alpha)
                .normal(last.normal(), nx, ny, nz).endVertex();
    }
}
