package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.TNTStudios.dragoneconomy.client.network.EconomyClientPacketHandler;
import org.lwjgl.glfw.GLFW;

public class DragoneconomyClient implements ClientModInitializer {
    private static KeyBinding openTransferScreenKey;

    @Override
    public void onInitializeClient() {
        HudOverlay.register();
        EconomyClientPacketHandler.register();

        openTransferScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragoneconomy.open_transfer_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y, // Usa "Y" como tecla predeterminada
                "category.dragoneconomy"
        ));
    }

    public static KeyBinding getOpenTransferScreenKey() {
        return openTransferScreenKey;
    }
}
