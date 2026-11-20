package dev.aimtrainer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Вся логика тренера. Шарики существуют только на клиенте и никак не влияют на мир/сервер. */
public final class Trainer {
    // Настройки (меняются в меню)
    public static boolean running = false;
    public static boolean moving = false;
    public static float radius = 0.35f;
    public static int count = 3;
    public static int duration = 60;

    private static int ticksLeft, hits, shots;
    private static Vec3d anchor = Vec3d.ZERO;
    private static float anchorYaw;
    private static final List<Target> targets = new ArrayList<>();
    private static final Random RND = new Random();

    private static final class Target {
        float yaw, pitch, dist, vyaw, vpitch;
    }

    private Trainer() {}

    private static float rnd(float a, float b) {
        return a + RND.nextFloat() * (b - a);
    }

    private static Target newTarget() {
        Target t = new Target();
        t.yaw = rnd(-50, 50);
        t.pitch = rnd(-15, 25);
        t.dist = rnd(9, 14);
        if (moving) {
            t.vyaw = (RND.nextBoolean() ? 1 : -1) * rnd(0.4f, 1.2f);
            t.vpitch = (RND.nextBoolean() ? 1 : -1) * rnd(0.0f, 0.4f);
        }
        return t;
    }

    private static Vec3d pos(Target t, float delta) {
        float yaw = anchorYaw + t.yaw + t.vyaw * delta;
        float pitch = t.pitch + t.vpitch * delta;
        return anchor.add(Vec3d.fromPolar(pitch, yaw).multiply(t.dist));
    }

    public static void start(MinecraftClient c) {
        if (c.player == null) return;
        anchor = c.player.getEyePos();
        anchorYaw = c.player.getYaw();
        hits = 0;
        shots = 0;
        ticksLeft = duration * 20;
        targets.clear();
        for (int i = 0; i < count; i++) targets.add(newTarget());
        running = true;
    }

    public static void stop(MinecraftClient c, boolean finished) {
        if (!running) return;
        running = false;
        targets.clear();
        if (c.player != null) {
            int acc = shots == 0 ? 0 : Math.round(100f * hits / shots);
            String prefix = finished ? "Время вышло! " : "Остановлено. ";
            c.player.sendMessage(Text.literal("[Aim] " + prefix + "Попаданий: " + hits
                    + ", выстрелов: " + shots + ", точность: " + acc + "%"), false);
        }
    }

    public static void tick(MinecraftClient c) {
        if (!running) return;
        if (c.player == null || c.world == null) {
            running = false;
            targets.clear();
            return;
        }
        if (moving) {
            for (Target t : targets) {
                t.yaw += t.vyaw;
                t.pitch += t.vpitch;
                if (Math.abs(t.yaw) > 50) t.vyaw = -t.vyaw;
                if (t.pitch < -15 || t.pitch > 25) t.vpitch = -t.vpitch;
            }
        }
        if (--ticksLeft <= 0) stop(c, true);
    }

    /** true = отменить обычный удар. */
    public static boolean onAttack(MinecraftClient c, ClientPlayerEntity p) {
        if (!running) return false;
        shots++;
        float d = c.getRenderTickCounter().getTickDelta(false);
        Vec3d eye = p.getCameraPosVec(d);
        Vec3d dir = p.getRotationVec(d);

        int hitIdx = -1;
        double bestT = Double.MAX_VALUE;
        for (int i = 0; i < targets.size(); i++) {
            Vec3d oc = pos(targets.get(i), d).subtract(eye);
            double tt = oc.dotProduct(dir);
            if (tt < 0) continue;
            double d2 = oc.lengthSquared() - tt * tt;
            if (d2 <= radius * radius && tt < bestT) {
                bestT = tt;
                hitIdx = i;
            }
        }
        if (hitIdx >= 0) {
            targets.set(hitIdx, newTarget());
            hits++;
            c.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.6f, 1.0f));
        }
        return true;
    }

    // ---------- Рендер ----------

    public static void render(WorldRenderContext ctx) {
        if (!running || targets.isEmpty()) return;
        VertexConsumerProvider vcp = ctx.consumers();
        if (vcp == null || ctx.matrixStack() == null) return;

        float delta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
        Vec3d cam = ctx.camera().getPos();
        Matrix4f mat = ctx.matrixStack().peek().getPositionMatrix();
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getDebugQuads());

        for (Target t : targets) {
            Vec3d p = pos(t, delta).subtract(cam);
            sphere(mat, vc, (float) p.x, (float) p.y, (float) p.z, radius);
        }
        if (vcp instanceof VertexConsumerProvider.Immediate imm) {
            imm.draw(RenderLayer.getDebugQuads());
        }
    }

    private static void sphere(Matrix4f m, VertexConsumer vc, float cx, float cy, float cz, float r) {
        final int stacks = 10, slices = 16;
        for (int i = 0; i < stacks; i++) {
            double p0 = Math.PI * i / stacks - Math.PI / 2;
            double p1 = Math.PI * (i + 1) / stacks - Math.PI / 2;
            for (int j = 0; j < slices; j++) {
                double t0 = 2 * Math.PI * j / slices;
                double t1 = 2 * Math.PI * (j + 1) / slices;
                float[][] q = {
                        pt(p0, t0), pt(p0, t1), pt(p1, t1), pt(p1, t0)
                };
                // двусторонний квад (на случай отключённого/включённого culling)
                for (int pass = 0; pass < 2; pass++) {
                    for (int k = 0; k < 4; k++) {
                        float[] v = q[pass == 0 ? k : 3 - k];
                        float shade = 0.65f + 0.35f * v[1]; // псевдо-освещение по высоте
                        vc.vertex(m, cx + v[0] * r, cy + v[1] * r, cz + v[2] * r)
                                .color((int) (255 * shade), (int) (60 * shade), (int) (60 * shade), 255);
                    }
                }
            }
        }
    }

    private static float[] pt(double phi, double theta) {
        return new float[]{
                (float) (Math.cos(phi) * Math.cos(theta)),
                (float) Math.sin(phi),
                (float) (Math.cos(phi) * Math.sin(theta))
        };
    }

    public static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!running) return;
        MinecraftClient c = MinecraftClient.getInstance();
        int acc = shots == 0 ? 0 : Math.round(100f * hits / shots);
        String s = "Очки: " + hits + "   Точность: " + acc + "%   Время: " + (ticksLeft / 20 + 1) + "с";
        ctx.drawTextWithShadow(c.textRenderer, Text.literal(s), 10, 10, 0xFFFFFF);
    }
}
