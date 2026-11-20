package dev.aimtrainer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TrainerScreen extends Screen {
    private static final float[] SIZES = {0.2f, 0.35f, 0.5f, 0.75f};
    private static final int[] COUNTS = {1, 3, 5, 8};
    private static final int[] TIMES = {30, 60, 120};

    public TrainerScreen() {
        super(Text.literal("Аим-тренер"));
    }

    private static Text modeLabel() {
        return Text.literal("Режим: " + (Trainer.moving ? "движущиеся" : "статичные"));
    }

    private static Text sizeLabel() {
        return Text.literal("Размер шарика: " + Trainer.radius);
    }

    private static Text countLabel() {
        return Text.literal("Шариков одновременно: " + Trainer.count);
    }

    private static Text timeLabel() {
        return Text.literal("Время раунда: " + Trainer.duration + " с");
    }

    private static Text startLabel() {
        return Text.literal(Trainer.running ? "Остановить" : "Старт");
    }

    private static <T> int next(int[] arr, int cur) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == cur) return arr[(i + 1) % arr.length];
        return arr[0];
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y = height / 2 - 75;

        addDrawableChild(ButtonWidget.builder(startLabel(), b -> {
            MinecraftClient c = MinecraftClient.getInstance();
            if (Trainer.running) Trainer.stop(c, false);
            else Trainer.start(c);
            close();
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 26;

        addDrawableChild(ButtonWidget.builder(modeLabel(), b -> {
            Trainer.moving = !Trainer.moving;
            b.setMessage(modeLabel());
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 26;

        addDrawableChild(ButtonWidget.builder(sizeLabel(), b -> {
            int idx = 0;
            for (int i = 0; i < SIZES.length; i++) if (SIZES[i] == Trainer.radius) idx = i;
            Trainer.radius = SIZES[(idx + 1) % SIZES.length];
            b.setMessage(sizeLabel());
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 26;

        addDrawableChild(ButtonWidget.builder(countLabel(), b -> {
            Trainer.count = next(COUNTS, Trainer.count);
            b.setMessage(countLabel());
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 26;

        addDrawableChild(ButtonWidget.builder(timeLabel(), b -> {
            Trainer.duration = next(TIMES, Trainer.duration);
            b.setMessage(timeLabel());
        }).dimensions(cx - 100, y, 200, 20).build());
        y += 30;

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(cx - 100, y, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 100, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (AimTrainerMod.openKey.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
