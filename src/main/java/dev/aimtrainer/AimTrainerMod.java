package dev.aimtrainer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class AimTrainerMod implements ClientModInitializer {
    public static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        // Кнопка L (можно поменять в настройках управления)
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aimtrainer.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                "category.aimtrainer"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new TrainerScreen());
                }
            }
            Trainer.tick(client);
        });

        // Во время раунда ЛКМ бьёт по виртуальным шарикам (а не по блокам/мобам)
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> Trainer.onAttack(client, player));

        WorldRenderEvents.AFTER_ENTITIES.register(Trainer::render);
        HudRenderCallback.EVENT.register(Trainer::renderHud);
    }
}
