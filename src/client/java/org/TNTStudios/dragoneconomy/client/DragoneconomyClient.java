package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.TNTStudios.dragoneconomy.client.gui.InvoiceScreen;
import org.TNTStudios.dragoneconomy.client.gui.PayInvoiceScreen;
import org.TNTStudios.dragoneconomy.client.gui.TransferScreen;
import org.TNTStudios.dragoneconomy.client.network.EconomyClientPacketHandler;
import org.lwjgl.glfw.GLFW;

public class DragoneconomyClient implements ClientModInitializer {
    private static KeyBinding openTransferScreenKey;
    private static KeyBinding openPayInvoiceScreenKey;
    private static KeyBinding openInvoiceScreenKey;

    @Override
    public void onInitializeClient() {
        HudOverlay.register();
        EconomyClientPacketHandler.register();

        openTransferScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragoneconomy.open_transfer_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.dragoneconomy"
        ));

        openPayInvoiceScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragoneconomy.open_pay_invoice_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.dragoneconomy"
        ));

        openInvoiceScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.dragoneconomy.open_invoice_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.dragoneconomy"
        ));

        // Registrar evento para detectar cuando una tecla es presionada
        ClientTickEvents.END_CLIENT_TICK.register(client -> DragoneconomyClient.checkKeyPress());
    }

    public static void checkKeyPress() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) return;

        // Verificar si la tecla de transferencias fue presionada
        if (openTransferScreenKey.wasPressed()) {
            client.setScreen(new TransferScreen());
        }

        // Verificar si la tecla de pago de facturas fue presionada
        if (openPayInvoiceScreenKey.wasPressed()) {
            client.setScreen(new PayInvoiceScreen());
        }

        if (openInvoiceScreenKey.wasPressed()) {
            client.setScreen(new InvoiceScreen());
        }
    }
}
