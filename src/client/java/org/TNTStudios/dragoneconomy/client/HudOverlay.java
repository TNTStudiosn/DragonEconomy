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
        if (client.player == null) return;

        TextRenderer textRenderer = client.textRenderer;

        int balance = EconomyClientData.getBalance(); // Obtener balance del cliente
        String balanceText = "Cuenta: $" + balance;

        int x = 10;
        int y = drawContext.getScaledWindowHeight() - 20;
        int color = Formatting.GREEN.getColorValue();

        drawContext.drawTextWithShadow(textRenderer, Text.literal(balanceText), x, y, color);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new HudOverlay());
    }
}
