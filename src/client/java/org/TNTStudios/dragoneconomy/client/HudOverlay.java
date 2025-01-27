package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

@Environment(EnvType.CLIENT)
public class HudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return; // Evita errores si no hay un jugador

        TextRenderer textRenderer = client.textRenderer;

        // Obtener el balance real del jugador desde la variable sincronizada
        int balance = EconomyClientData.getBalance();

        // Definir el texto con color verde y sombra
        String balanceText = "Cuenta: $" + balance;
        int x = 10; // Posición en el eje X
        int y = drawContext.getScaledWindowHeight() - 20; // Posición en el eje Y
        int color = Formatting.GREEN.getColorValue(); // Color verde

        drawContext.drawTextWithShadow(textRenderer, Text.literal(balanceText), x, y, color);
    }

    // Método para registrar el HUD
    public static void register() {
        HudRenderCallback.EVENT.register(new HudOverlay());
    }
}
