package net.phoenix_tesla_network.tesla.client.ui;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.phoenix_tesla_network.tesla.client.TeslaText;
import net.phoenix_tesla_network.tesla.network.packet.C2STeslaTowerRequestPacket;
import net.phoenix_tesla_network.tesla.network.packet.S2CTeslaTowerPacket;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * The Tesla Tower's full-screen UI. It opens in the dark with a storm building, tells you what your network holds, and
 * then lightning strikes the spire and the tower powers up in front of you: it fills with the network's charge, its
 * rings glow, and arcs jump from it to every machine drawing from it. The numbers sit in fading panels on either side.
 *
 * <p>
 * Behind the {@code features.fullScreenTowerUi} config, off by default.
 */
@OnlyIn(Dist.CLIENT)
public class TeslaTowerScreen extends Screen {

    private static final float INTRO_SECONDS = 8.5f;
    private static final float T_STRIKE = 5.4f;
    private static final float[] RUMBLES = { 0.8f, 2.0f, 3.1f, 4.2f };

    private static final int CYAN = 0x59D8FF;
    private static final int VIOLET = 0xA070FF;
    private static final int GOLD = 0xFFD25A;
    private static final int GREEN = 0x6BE890;
    private static final int RED = 0xFF6B6B;
    private static final int TEXT = 0xFFCCD6EE;
    private static final int DIM = 0xFF6C7794;

    private S2CTeslaTowerPacket state;

    private float seconds;
    private long lastNanos = -1L;
    private float shownFill;
    private float flash;
    private int rumbleIndex;
    private boolean struck;
    private int ticks;

    // The storm's sound, kept quiet: rain, distant thunder after each sheet of lightning, and crackle from the tower.
    private static final float RAIN_VOLUME = 0.16f;
    private static final float THUNDER_VOLUME = 0.2f;
    private static final float CRACKLE_VOLUME = 0.12f;
    private float nextRain;
    private float nextCrackle;
    private float thunderAt = -1f;
    private long lastSheet = -1L;
    private final Random soundRng = new Random();

    private final ArrayDeque<Long> inHistory = new ArrayDeque<>();
    private final ArrayDeque<Long> outHistory = new ArrayDeque<>();
    private final List<Hit> hits = new ArrayList<>();
    private final UiProfiler prof = new UiProfiler("tesla");

    private record Hit(int x, int y, int w, int h, Runnable action) {

        boolean contains(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public TeslaTowerScreen(S2CTeslaTowerPacket state) {
        super(Component.literal(TeslaText.str("block.phoenix_tesla_network.tesla_tower")));
        this.state = state;
        sample();
    }

    public BlockPos pos() {
        return state.pos();
    }

    public void update(S2CTeslaTowerPacket next) {
        this.state = next;
        sample();
    }

    private void sample() {
        inHistory.addLast(state.inPerSec());
        outHistory.addLast(state.outPerSec());
        while (inHistory.size() > 60) inHistory.pollFirst();
        while (outHistory.size() > 60) outHistory.pollFirst();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void renderBlurredBackground(float partialTick) {}

    @Override
    public void tick() {
        // Fresh numbers about once a second while the screen is open.
        if (++ticks % 20 == 0) {
            PacketDistributor.sendToServer(
                    new C2STeslaTowerRequestPacket(state.pos(), C2STeslaTowerRequestPacket.REFRESH));
        }
    }

    // ------------------------------------------------------------ drawing helpers

    private static int argb(int rgb, float alpha) {
        return (Mth.clamp((int) (alpha * 255f), 0, 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    private static float noise(float a, float b) {
        double v = Math.sin(a * 12.9898 + b * 78.233) * 43758.5453;
        return (float) (v - Math.floor(v));
    }

    private BufferBuilder begin(boolean additive) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    private void end(BufferBuilder buf) {
        // A pass with nothing in it (no leaves yet, no bolt this frame) has no mesh to draw.
        MeshData mesh = buf.build();
        if (mesh != null) {
            prof.count(TeslaText.str("phoenix_tesla_network.tesla_tower.vertices"), mesh.drawState().vertexCount());
            prof.count(TeslaText.str("phoenix_tesla_network.tesla_tower.draws"), 1);
            BufferUploader.drawWithShader(mesh);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
    }

    private void line(BufferBuilder buf, Matrix4f pose, float x0, float y0, float x1, float y1, float w0, float w1,
                      int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.01f) return;
        float nx = -dy / len;
        float ny = dx / len;
        buf.addVertex(pose, x0 + nx * w0, y0 + ny * w0, 0).setColor(color);
        buf.addVertex(pose, x0 - nx * w0, y0 - ny * w0, 0).setColor(color);
        buf.addVertex(pose, x1 - nx * w1, y1 - ny * w1, 0).setColor(color);
        buf.addVertex(pose, x1 + nx * w1, y1 + ny * w1, 0).setColor(color);
    }

    private void ellipse(BufferBuilder buf, Matrix4f pose, float cx, float cy, float rx, float ry, float thick,
                         int color) {
        int segments = Math.max(24, (int) (rx / 2f));
        for (int i = 0; i < segments; i++) {
            float a0 = Mth.TWO_PI * i / segments;
            float a1 = Mth.TWO_PI * (i + 1) / segments;
            float k = thick / Math.max(1f, rx);
            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);
            buf.addVertex(pose, cx + c0 * rx * (1 - k), cy + s0 * ry * (1 - k), 0).setColor(color);
            buf.addVertex(pose, cx + c0 * rx * (1 + k), cy + s0 * ry * (1 + k), 0).setColor(color);
            buf.addVertex(pose, cx + c1 * rx * (1 + k), cy + s1 * ry * (1 + k), 0).setColor(color);
            buf.addVertex(pose, cx + c1 * rx * (1 - k), cy + s1 * ry * (1 - k), 0).setColor(color);
        }
    }

    /** A jagged bolt from one point to another: a wide soft pass and a thin bright core, with the odd fork. */
    private void bolt(BufferBuilder buf, Matrix4f pose, float x0, float y0, float x1, float y1, float jitter,
                      long seed, int color, float alpha, boolean fork) {
        Random rng = new Random(seed);
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 4f) return;
        int segments = Math.max(5, (int) (len / 20f));
        float nx = -dy / len;
        float ny = dx / len;
        float px = x0;
        float py = y0;
        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float off = i == segments ? 0f : (rng.nextFloat() - 0.5f) * 2f * jitter * (float) Math.sin(Math.PI * t);
            float x = x0 + dx * t + nx * off;
            float y = y0 + dy * t + ny * off;
            line(buf, pose, px, py, x, y, 2.6f, 2.6f, argb(color, alpha * 0.16f));
            line(buf, pose, px, py, x, y, 0.7f, 0.7f, argb(0xFFFFFF, alpha));
            if (fork && rng.nextFloat() < 0.14f && i < segments - 1) {
                float fx = x + (rng.nextFloat() - 0.5f) * len * 0.3f;
                float fy = y + rng.nextFloat() * len * 0.18f;
                bolt(buf, pose, x, y, fx, fy, jitter * 0.6f, rng.nextLong(), color, alpha * 0.7f, false);
            }
            px = x;
            py = y;
        }
    }

    private void play(SoundEvent sound, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
    }

    // ------------------------------------------------------------ numbers

    private static String compact(String digits) {
        try {
            BigInteger v = new BigInteger(digits);
            if (v.compareTo(BigInteger.valueOf(1_000_000_000_000L)) >= 0) {
                return String.format(Locale.ROOT, "%.3e", new BigDecimal(v).doubleValue());
            }
            return String.format(Locale.ROOT, "%,d", v);
        } catch (NumberFormatException e) {
            return digits;
        }
    }

    private static String rate(long perSecond) {
        return String.format(Locale.ROOT, "%,d", perSecond);
    }

    // ------------------------------------------------------------ frame

    /** What the screen is scaled by to fit a small window or a high GUI scale; mouse positions are divided by it. */
    private float fitK = 1f;

    @Override
    public void render(GuiGraphics g, int rawMouseX, int rawMouseY, float partialTick) {
        // laid out on a stage of at least 480 by 270, and scaled down to fit a smaller window
        var window = Minecraft.getInstance().getWindow();
        int realW = window.getGuiScaledWidth();
        int realH = window.getGuiScaledHeight();
        fitK = Mth.clamp(Math.min(realW / 480f, realH / 270f), 0.5f, 1f);
        this.width = (int) Math.ceil(realW / fitK);
        this.height = (int) Math.ceil(realH / fitK);
        g.pose().pushPose();
        g.pose().scale(fitK, fitK, 1f);
        renderStage(g, (int) (rawMouseX / fitK), (int) (rawMouseY / fitK), partialTick);
        g.pose().popPose();
    }

    private void renderStage(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        float dt = lastNanos < 0 ? 0.016f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;
        seconds += dt;
        flash = Math.max(0f, flash - dt * 1.6f);
        prof.frame(dt);
        long renderStart = prof.start();
        hits.clear();

        tickCinematic();
        tickAmbience(power0());
        float power = easeOut(Mth.clamp((seconds - T_STRIKE) / 2.4f, 0f, 1f));
        float ui = Mth.clamp((seconds - (T_STRIKE + 1.4f)) / 2f, 0f, 1f);
        shownFill += (state.fill() * power - shownFill) * Math.min(1f, dt * 1.6f);

        g.fillGradient(0, 0, width, height, 0xFF04050B, 0xFF0A1122);
        timed("storm", () -> drawStorm(g, power));
        timed("tower", () -> drawTower(g, power));
        g.fillGradient(0, 0, width, (int) (height * 0.2f), 0xBB000000, 0x00000000);
        g.fillGradient(0, (int) (height * 0.82f), width, height, 0x00000000, 0xBB000000);

        int bars = (int) (height * 0.06f * easeOut(Mth.clamp(seconds / 1.2f, 0f, 1f)));
        g.fill(0, 0, width, bars, 0xFF000000);
        g.fill(0, height - bars, width, height, 0xFF000000);

        if (ui > 0f) {
            timed("leftPanel", () -> drawLeft(g, ui, mouseX, mouseY));
            timed("rightPanel", () -> drawRight(g, ui));
            drawFooter(g, ui, mouseX, mouseY);
        }
        if (flash > 0f) g.fill(0, 0, width, height, argb(0xDDF4FF, 0.85f * flash));
        if (seconds < INTRO_SECONDS) timed("intro", () -> drawIntro(g));
        prof.end("render", renderStart);
        prof.draw(g, font);
        super.render(g, mouseX, mouseY, partialTick);
    }

    /** How far the tower has powered up, 0 to 1, for the sound as well as the picture. */
    private float power0() {
        return easeOut(Mth.clamp((seconds - T_STRIKE) / 2.4f, 0f, 1f));
    }

    /** Rain all the way through, thunder rolling in a moment after each distant flash, and crackle once it is live. */
    private void tickAmbience(float power) {
        if (seconds >= nextRain) {
            play(SoundEvents.WEATHER_RAIN, 0.9f + soundRng.nextFloat() * 0.25f, RAIN_VOLUME);
            nextRain = seconds + 1.1f + soundRng.nextFloat() * 0.4f;
        }
        // The sheet lightning in the sky repeats every 6.7 seconds; its thunder follows about a second later.
        long sheet = (long) (seconds / 6.7f);
        if (sheet != lastSheet) {
            if (lastSheet >= 0 && sheet >= 1) thunderAt = seconds + 0.9f + soundRng.nextFloat() * 0.8f;
            lastSheet = sheet;
        }
        if (thunderAt >= 0f && seconds >= thunderAt) {
            play(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.55f + soundRng.nextFloat() * 0.3f, THUNDER_VOLUME);
            thunderAt = -1f;
        }
        if (power > 0.15f && seconds >= nextCrackle) {
            float live = Mth.clamp(power * (0.35f + 0.65f * shownFill), 0f, 1f);
            play(SoundEvents.FIRE_AMBIENT, 1.7f + soundRng.nextFloat() * 0.5f, CRACKLE_VOLUME * (0.4f + live));
            if (soundRng.nextFloat() < 0.25f * live) {
                play(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.9f + soundRng.nextFloat() * 0.3f, CRACKLE_VOLUME * 0.5f);
            }
            nextCrackle = seconds + 0.25f + soundRng.nextFloat() * 0.7f / (0.4f + live);
        }
    }

    private void tickCinematic() {
        if (seconds >= INTRO_SECONDS) return;
        while (rumbleIndex < RUMBLES.length && seconds >= RUMBLES[rumbleIndex]) {
            play(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.55f + 0.1f * rumbleIndex, 0.12f);
            rumbleIndex++;
        }
        if (!struck && seconds >= T_STRIKE) {
            struck = true;
            flash = 1f;
            play(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.4f, 0.4f);
            play(SoundEvents.LIGHTNING_BOLT_IMPACT, 1.1f, 0.5f);
            play(SoundEvents.BEACON_ACTIVATE, 1.3f, 0.6f);
        }
    }

    /** Clouds, rain and the odd sheet of distant lightning. */
    private void drawStorm(GuiGraphics g, float power) {
        Matrix4f pose = g.pose().last().pose();
        BufferBuilder buf = begin(true);
        float seed = seconds;
        // Cloud banks: wide soft bands across the top, drifting.
        for (int k = 0; k < 4; k++) {
            float y = height * (0.02f + 0.07f * k);
            float off = (seed * (6f + k * 3f)) % width;
            int dim = argb(VIOLET, 0.05f);
            int clear = argb(VIOLET, 0f);
            buf.addVertex(pose, -off, y, 0).setColor(clear);
            buf.addVertex(pose, -off, y + 60f, 0).setColor(dim);
            buf.addVertex(pose, width * 2f - off, y + 60f, 0).setColor(dim);
            buf.addVertex(pose, width * 2f - off, y, 0).setColor(clear);
        }
        // Rain: thin slanted streaks falling fast.
        for (int i = 0; i < 90; i++) {
            float x = noise(i, 1f) * (width + 60f);
            float speed = 320f + noise(i, 2f) * 260f;
            float y = ((seconds * speed) + noise(i, 3f) * height) % (height + 40f) - 20f;
            float len = 8f + noise(i, 4f) * 16f;
            line(buf, pose, x, y, x - 3f, y + len, 0.4f, 0.2f, argb(0xBFD8FF, 0.1f + 0.12f * noise(i, 5f)));
        }
        // A distant strike now and then: a flash across the sky and a bolt to the ground.
        float cycle = seconds % 6.7f;
        if (cycle < 0.18f && seconds > 1f) {
            float f = 1f - cycle / 0.18f;
            long n = (long) (seconds / 6.7f);
            float bx = width * (0.1f + 0.8f * noise(n, 9f));
            if (Math.abs(bx - width / 2f) < width * 0.12f) bx += width * 0.25f;
            bolt(buf, pose, bx, 0, bx + (noise(n, 4f) - 0.5f) * 80f, height * 0.84f, 38f, n * 977L, CYAN, 0.5f * f,
                    true);
            g.fill(0, 0, width, height, argb(0xCFE6FF, 0.05f * f));
        }
        end(buf);
    }

    // ------------------------------------------------------------ the tower

    private void drawTower(GuiGraphics g, float power) {
        if (power <= 0f && seconds < T_STRIKE - 1.5f) return;
        Matrix4f pose = g.pose().last().pose();
        int kind = state.kind();
        float cx = width / 2f;
        float baseY = height * 0.86f;
        float h = height * (kind == 0 ? 0.56f : 0.7f);
        float scale = kind == 0 ? 0.85f : 1f;
        float fill = Mth.clamp(shownFill, 0f, 1f);
        float pulse = 0.85f + 0.15f * (float) Math.sin(seconds * 7f);

        // The spire: a dark tapering body with a lit edge.
        BufferBuilder body = begin(false);
        float bw = h * 0.045f;
        float tw = h * 0.008f;
        float alpha = Mth.clamp(0.2f + power, 0f, 1f);
        int dark = argb(0x0F1626, alpha);
        body.addVertex(pose, cx - bw, baseY, 0).setColor(dark);
        body.addVertex(pose, cx + bw, baseY, 0).setColor(dark);
        body.addVertex(pose, cx + tw, baseY - h, 0).setColor(dark);
        body.addVertex(pose, cx - tw, baseY - h, 0).setColor(dark);
        end(body);

        BufferBuilder glow = begin(true);
        // The network's charge, rising through the spire from the ground.
        float top = baseY - h * fill;
        float widthAtTop = bw + (tw - bw) * fill;
        int lo = argb(CYAN, 0.55f * alpha);
        int hi = argb(0xEAFBFF, 0.95f * alpha);
        glow.addVertex(pose, cx - bw * 0.7f, baseY, 0).setColor(lo);
        glow.addVertex(pose, cx + bw * 0.7f, baseY, 0).setColor(lo);
        glow.addVertex(pose, cx + widthAtTop * 0.7f, top, 0).setColor(hi);
        glow.addVertex(pose, cx - widthAtTop * 0.7f, top, 0).setColor(hi);
        // A wider, fainter halo around the lit part.
        int halo = argb(CYAN, 0.1f * alpha * pulse);
        int none = argb(CYAN, 0f);
        glow.addVertex(pose, cx - bw * 2.6f, baseY, 0).setColor(halo);
        glow.addVertex(pose, cx + bw * 2.6f, baseY, 0).setColor(halo);
        glow.addVertex(pose, cx + widthAtTop * 2.6f, top, 0).setColor(none);
        glow.addVertex(pose, cx - widthAtTop * 2.6f, top, 0).setColor(none);

        // Rings, brighter as the tower fills.
        float[] heights = { 0.17f, 0.46f, 0.72f };
        for (int i = 0; i < heights.length; i++) {
            float ry0 = baseY - h * heights[i];
            float rx = h * (0.10f + 0.016f * i) * scale;
            float lit = 0.25f + 0.65f * fill;
            ellipse(glow, pose, cx, ry0, rx, rx * 0.22f, 2.2f, argb(CYAN, lit * alpha * pulse));
            ellipse(glow, pose, cx, ry0, rx * 1.08f, rx * 0.24f, 1f, argb(VIOLET, 0.35f * alpha));
        }
        end(glow);

        // The orb at the tip, swelling with the charge.
        float tipY = baseY - h;
        float orb = h * (0.014f + 0.03f * fill) * (0.9f + 0.1f * (float) Math.sin(seconds * 9f));
        BufferBuilder discs = begin(true);
        for (int k = 5; k >= 1; k--) {
            float r = orb * k * 0.9f;
            ring(discs, pose, cx, tipY, r * 0.5f, r * 0.5f,
                    argb(k == 1 ? 0xFFFFFF : CYAN, (k == 1 ? 0.9f : 0.14f) * alpha));
        }
        end(discs);

        // Arcs: orb to the rings, the rings to the ground, and out to every machine drawing from the network.
        if (power > 0.05f) {
            long frame = (long) (seconds * 16f);
            BufferBuilder arcs = begin(true);
            float intensity = power * (0.35f + 0.65f * fill);
            for (int i = 0; i < heights.length; i++) {
                float ry0 = baseY - h * heights[i];
                float rx = h * (0.10f + 0.016f * i) * scale;
                bolt(arcs, pose, cx, tipY, cx - rx, ry0, 22f, frame * 31L + i, CYAN, 0.55f * intensity, true);
                bolt(arcs, pose, cx, tipY, cx + rx, ry0, 22f, frame * 37L + i, CYAN, 0.55f * intensity, true);
                bolt(arcs, pose, cx - rx, ry0, cx - rx * 1.5f, baseY, 14f, frame * 41L + i, VIOLET, 0.4f * intensity,
                        false);
                bolt(arcs, pose, cx + rx, ry0, cx + rx * 1.5f, baseY, 14f, frame * 43L + i, VIOLET, 0.4f * intensity,
                        false);
            }
            // Out to the network: a node for each link, and an arc to the ones that are drawing right now.
            int nodes = Math.min(28, state.hatches());
            float busy = 0.2f + 0.5f * fill + (state.inPerSec() + state.outPerSec() > 0 ? 0.25f : 0f);
            for (int i = 0; i < nodes; i++) {
                float t = nodes == 1 ? 0.5f : i / (float) (nodes - 1);
                boolean left = i % 2 == 0;
                float ang = (float) (Math.PI * (0.08 + 0.84 * t));
                float nx = cx + (left ? -1 : 1) * (float) Math.cos(ang * 0.5f) * width * (0.22f + 0.1f * noise(i, 6f));
                float ny = baseY - (float) Math.sin(ang) * h * (0.2f + 0.45f * noise(i, 7f));
                boolean soul = i < state.soulLinked();
                int color = soul ? VIOLET : CYAN;
                ring(arcs, pose, nx, ny, 3.5f, 3.5f, argb(color, 0.7f * power));
                ring(arcs, pose, nx, ny, 8f, 1f, argb(color, 0.25f * power));
                if (soul) ring(arcs, pose, nx, ny, 13f, 0.7f, argb(VIOLET, 0.3f * power * pulse));
                if (noise(i, frame / 5f) < busy) {
                    bolt(arcs, pose, cx, tipY + h * 0.05f, nx, ny, 26f, frame * 53L + i, color, 0.45f * intensity,
                            false);
                }
            }
            for (int i = 0; i < 2; i++) {
                float ang = (noise(frame, i) - 0.5f) * 1.6f;
                bolt(arcs, pose, cx, tipY, cx + (float) Math.sin(ang) * h * 0.5f,
                        tipY - (float) Math.cos(ang) * h * 0.4f, 30f, frame * 59L + i, 0xBFD8FF, 0.35f * intensity,
                        true);
            }
            end(arcs);
        }
    }

    private void ring(BufferBuilder buf, Matrix4f pose, float cx, float cy, float r, float thick, int color) {
        ellipse(buf, pose, cx, cy, r, r, thick, color);
    }

    // ------------------------------------------------------------ the opening

    private record Line(String text, float start, float end, float scale, float y, int color) {}

    private void drawIntro(GuiGraphics g) {
        g.flush();
        float black = seconds < T_STRIKE ? 1f : Math.max(0f, 1f - (seconds - T_STRIKE) / 0.7f);
        if (black > 0f) g.fill(0, 0, width, height, argb(0x000000, black));

        // Before the strike: small blue sparks crawling in the dark.
        if (seconds < T_STRIKE) {
            Matrix4f pose = g.pose().last().pose();
            BufferBuilder buf = begin(true);
            long frame = (long) (seconds * 14f);
            for (int i = 0; i < 3; i++) {
                float sx = width * (0.2f + 0.6f * noise(frame, i));
                float sy = height * (0.3f + 0.4f * noise(i, frame));
                float ex = sx + (noise(frame, i + 9) - 0.5f) * 120f;
                float ey = sy + (noise(i + 9, frame) - 0.5f) * 80f;
                float a = noise(frame, i + 3) < 0.35f ? 0.4f * Math.min(1f, seconds / 2f) : 0f;
                if (a > 0f) bolt(buf, pose, sx, sy, ex, ey, 10f, frame * 13L + i, CYAN, a, false);
            }
            end(buf);
        }

        Minecraft mc = Minecraft.getInstance();
        String name = mc.player == null ? "You" : mc.player.getName().getString();
        List<Line> lines = new ArrayList<>();
        float h = height;
        lines.add(new Line(name, 0.5f, 5.0f, 2.2f, h * 0.36f, CYAN));
        lines.add(new Line("Your network.", 1.5f, 5.0f, 1f, h * 0.36f + 34, 0xCCD6EE));
        lines.add(new Line(state.hatches() + (state.hatches() == 1 ? " machine draws from it." :
                " machines draw from it."), 2.4f, 5.0f, 1f, h * 0.36f + 50, 0x9AA6C4));
        lines.add(new Line("It holds " + compact(state.stored()) + " EU.", 3.3f, 5.0f, 1f, h * 0.36f + 66, 0x9AA6C4));
        lines.add(new Line("Charge.", 4.1f, 5.3f, 2.4f, h * 0.5f, GOLD));
        for (Line line : lines) {
            if (seconds < line.start() || seconds > line.end()) continue;
            float typed = (seconds - line.start()) * 24f;
            String text = line.text().substring(0, Math.min(line.text().length(), (int) typed));
            float a = Mth.clamp((seconds - line.start()) / 0.3f, 0f, 1f) *
                    Mth.clamp((line.end() - seconds) / 0.4f, 0f, 1f);
            if (a <= 0f || text.isEmpty()) continue;
            float jitter = ((int) (seconds * 33f) % 19 == 0) ? 2f : 0f;
            g.pose().pushPose();
            g.pose().translate(width / 2f + jitter, line.y(), 0);
            g.pose().scale(line.scale(), line.scale(), 1f);
            int w = font.width(text);
            g.drawString(font, text, (int) (-w / 2f) - 1, 0, argb(0xFF3050, a * 0.3f), false);
            g.drawString(font, text, (int) (-w / 2f) + 1, 0, argb(0x30D0FF, a * 0.3f), false);
            g.drawString(font, text, (int) (-w / 2f), 0, argb(line.color(), a), false);
            g.pose().popPose();
        }
        g.drawCenteredString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.click_to_skip"), width / 2,
                height - 18,
                argb(0x777799, 0.5f * Mth.clamp(1f - seconds / INTRO_SECONDS, 0f, 1f)));
    }

    // ------------------------------------------------------------ panels

    private int panelW() {
        return Mth.clamp((int) (width * 0.27f), 150, 210);
    }

    private void fadePanel(GuiGraphics g, int x, int w, boolean leftEdge, float ui) {
        int strip = 3;
        for (int i = 0; i < w; i += strip) {
            float t = i / (float) w;
            float a = 0.82f * (float) Math.pow(1f - t, 1.4f) * ui;
            int sx = leftEdge ? x + i : x + w - i - strip;
            g.fill(sx, 0, sx + strip, height, argb(0x03050C, a));
        }
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String label, int mx, int my, boolean enabled,
                        Runnable action) {
        boolean over = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
        g.fill(x, y, x + w, y + h, over ? argb(CYAN, 0.28f) : argb(0x0A0F1C, 0.85f));
        g.renderOutline(x, y, w, h, enabled ? argb(CYAN, 0.8f) : 0xFF2A3350);
        g.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, enabled ? TEXT : 0xFF2A3350);
        if (enabled) hits.add(new Hit(x, y, w, h, action));
    }

    private void drawLeft(GuiGraphics g, float ui, int mx, int my) {
        int pw = panelW();
        fadePanel(g, 0, pw, true, ui);
        int x = 14;
        int y = bar() + 14;
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.tesla_network"), x, y, argb(CYAN, ui),
                false);
        y += 14;
        boolean online = state.online();
        float blink = online ? 0.6f + 0.4f * (float) Math.sin(seconds * 4f) : 1f;
        g.fill(x, y + 2, x + 5, y + 7, argb(online ? GREEN : RED, ui * blink));
        g.drawString(font,
                online ? TeslaText.str("phoenix_tesla_network.tesla_tower.online") :
                        TeslaText.str("phoenix_tesla_network.tesla_tower.offline"),
                x + 10, y, argb(online ? GREEN : RED, ui), false);
        y += 22;
        y = row(g, x, y, "Tower", state.towerName(), ui);
        y = row(g, x, y, "Range", state.rangeText(), ui);
        y = row(g, x, y, "Team", state.teamName(), ui);
        y = row(g, x, y, "Towers on team", Integer.toString(state.towers()), ui);
        y += 10;
        button(g, x, y, pw - 28, 16, online ? "Power: ON  (switch off)" : "Power: OFF (switch on)", mx, my, true,
                () -> PacketDistributor.sendToServer(
                        new C2STeslaTowerRequestPacket(state.pos(), C2STeslaTowerRequestPacket.TOGGLE_POWER)));
    }

    private int row(GuiGraphics g, int x, int y, String label, String value, float ui) {
        g.drawString(font, label, x, y, argb(0x6C7794, ui), false);
        y += 10;
        g.drawString(font, font.plainSubstrByWidth(value, panelW() - 24), x, y, argb(0xCCD6EE, ui), false);
        return y + 15;
    }

    private void drawRight(GuiGraphics g, float ui) {
        int pw = panelW();
        fadePanel(g, width - pw, pw, false, ui);
        int x = width - pw + 12;
        int w = pw - 26;
        int y = bar() + 14;
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.stored"), x, y, argb(0x6C7794, ui), false);
        y += 11;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(1.5f, 1.5f, 1f);
        g.drawString(font, font.plainSubstrByWidth(compact(state.stored()), (int) (w / 1.5f)), 0, 0, argb(GOLD, ui),
                false);
        g.pose().popPose();
        y += 18;
        g.drawString(font, "of " + compact(state.capacity()) + " EU", x, y, argb(0x6C7794, ui), false);
        y += 11;
        g.drawString(font, String.format(Locale.ROOT, "%.1f%% full", state.fill() * 100f), x, y, argb(CYAN, ui), false);
        y += 18;

        long in = state.inPerSec();
        long out = state.outPerSec();
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.input"), x, y, argb(0x6C7794, ui), false);
        g.drawString(font, "+" + rate(in) + " EU/s", x + 40, y, argb(GREEN, ui), false);
        y += 11;
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.output"), x, y, argb(0x6C7794, ui), false);
        g.drawString(font, "-" + rate(out) + " EU/s", x + 40, y, argb(RED, ui), false);
        y += 11;
        long net = in - out;
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.net"), x, y, argb(0x6C7794, ui), false);
        g.drawString(font, (net >= 0 ? "+" : "") + rate(net) + " EU/s", x + 40, y, argb(net >= 0 ? GREEN : RED, ui),
                false);
        y += 18;
        g.drawString(font,
                TeslaText.str("phoenix_tesla_network.tesla_tower.battery_tier",
                        GTValues.VN[Math.min(GTValues.VN.length - 1, state.batteryTier())]),
                x, y,
                argb(CYAN, ui), false);
        y += 11;
        g.drawString(font,
                TeslaText.str("phoenix_tesla_network.tesla_tower.links_soul_linked", state.hatches(),
                        state.soulLinked()),
                x, y,
                argb(VIOLET, ui), false);
        y += 20;
        drawSpark(g, x, y, w, 46, ui);
    }

    /** The last minute of input (green) and output (red). */
    private void drawSpark(GuiGraphics g, int x, int y, int w, int h, float ui) {
        g.fill(x, y, x + w, y + h, argb(0x0A0F1C, 0.7f * ui));
        g.renderOutline(x, y, w, h, argb(0x2A3350, ui));
        long max = 1;
        for (long v : inHistory) max = Math.max(max, v);
        for (long v : outHistory) max = Math.max(max, v);
        Matrix4f pose = g.pose().last().pose();
        BufferBuilder buf = begin(true);
        plot(buf, pose, inHistory, x, y, w, h, max, GREEN, ui);
        plot(buf, pose, outHistory, x, y, w, h, max, RED, ui);
        end(buf);
        g.drawString(font, TeslaText.str("phoenix_tesla_network.tesla_tower.last_minute"), x + 3, y + h + 3,
                argb(0x6C7794, ui), false);
    }

    private void plot(BufferBuilder buf, Matrix4f pose, ArrayDeque<Long> data, int x, int y, int w, int h, long max,
                      int color, float ui) {
        if (data.size() < 2) return;
        float step = w / 59f;
        float px = 0;
        float py = 0;
        int i = 0;
        int offset = 60 - data.size();
        for (long v : data) {
            float fx = x + (offset + i) * step;
            float fy = y + h - 2 - (h - 4) * (v / (float) max);
            if (i > 0) line(buf, pose, px, py, fx, fy, 0.7f, 0.7f, argb(color, 0.85f * ui));
            px = fx;
            py = fy;
            i++;
        }
    }

    private int bar() {
        return (int) (height * 0.06f);
    }

    private void drawFooter(GuiGraphics g, float ui, int mx, int my) {
        int cx = width / 2;
        int barW = Math.min(260, width - panelW() * 2 - 40);
        int y = height - bar() - 20;
        g.drawCenteredString(font, String.format(Locale.ROOT, "%.1f%%", state.fill() * 100f), cx, y - 12,
                argb(CYAN, ui));
        g.fill(cx - barW / 2, y, cx + barW / 2, y + 5, argb(0x1A2238, ui));
        int filled = (int) (barW * Mth.clamp(shownFill, 0f, 1f));
        g.fill(cx - barW / 2, y, cx - barW / 2 + filled, y + 5, argb(CYAN, ui));
        // A bright band running along the charge.
        int shimmer = (int) ((seconds * 90f) % Math.max(1, filled + 40)) - 20;
        if (filled > 0) {
            int a = Math.max(0, cx - barW / 2 + shimmer);
            int b = Math.min(cx - barW / 2 + filled, cx - barW / 2 + shimmer + 24);
            if (b > a) g.fill(a, y, b, y + 5, argb(0xFFFFFF, 0.55f * ui));
        }
        button(g, width - panelW() + 12, height - bar() - 24, 56, 15, "Leave", mx, my, true, this::onClose);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == UiProfiler.TOGGLE_KEY) {
            prof.toggle();
            return true;
        }
        return super.keyPressed(key, scan, modifiers);
    }

    private void timed(String section, Runnable r) {
        long t = prof.start();
        r.run();
        prof.end(section, t);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        mx /= fitK;
        my /= fitK;
        if (seconds < INTRO_SECONDS) {
            seconds = INTRO_SECONDS;
            struck = true;
            rumbleIndex = RUMBLES.length;
            shownFill = state.fill();
            return true;
        }
        if (button == 0) {
            for (Hit h : new ArrayList<>(hits)) {
                if (h.contains(mx, my)) {
                    h.action().run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
