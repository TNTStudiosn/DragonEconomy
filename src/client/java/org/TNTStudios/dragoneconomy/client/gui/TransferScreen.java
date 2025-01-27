package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.network.client.TransferPacketClient;

@Environment(EnvType.CLIENT)
public class TransferScreen extends Screen {

    private TextFieldWidget recipientField;
    private TextFieldWidget amountField;
    private final int boxWidth = 220;
    private final int boxHeight = 140;

    public TransferScreen() {
        super(Text.literal("Transferencias"));
    }

    @Override
    protected void init() {
        int centerX = (this.width - boxWidth) / 2;
        int centerY = (this.height - boxHeight) / 2;

        // Campo para ingresar el nombre del destinatario
        recipientField = new TextFieldWidget(textRenderer, centerX + 20, centerY + 30, 180, 20, Text.literal("Nombre del jugador"));
        recipientField.setMaxLength(16);
        this.addDrawableChild(recipientField);

        // Campo para ingresar el monto
        amountField = new TextFieldWidget(textRenderer, centerX + 20, centerY + 60, 180, 20, Text.literal("Ingrese monto"));
        amountField.setMaxLength(10);
        this.addDrawableChild(amountField);

        // Botón de transferencia
        this.addDrawableChild(ButtonWidget.builder(Text.literal("ENVIAR").formatted(Formatting.GREEN), button -> processTransfer())
                .dimensions(centerX + 60, centerY + 100, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        int centerX = (this.width - boxWidth) / 2;
        int centerY = (this.height - boxHeight) / 2;

        // Dibujar el fondo de la ventana
        context.fill(centerX, centerY, centerX + boxWidth, centerY + boxHeight, 0xAA000000);

        // Bordes blancos alrededor de la ventana
        context.fill(centerX, centerY, centerX + boxWidth, centerY + 2, 0xFFFFFFFF);
        context.fill(centerX, centerY + boxHeight - 2, centerX + boxWidth, centerY + boxHeight, 0xFFFFFFFF);
        context.fill(centerX, centerY, centerX + 2, centerY + boxHeight, 0xFFFFFFFF);
        context.fill(centerX + boxWidth - 2, centerY, centerX + boxWidth, centerY + boxHeight, 0xFFFFFFFF);

        // Título
        context.drawText(this.textRenderer, Text.literal("TRANSFERENCIAS").formatted(Formatting.GOLD, Formatting.BOLD),
                this.width / 2 - this.textRenderer.getWidth("TRANSFERENCIAS") / 2, centerY + 10, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void processTransfer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Obtener el nombre del destinatario
        String targetPlayer = recipientField.getText().trim();
        if (targetPlayer.isEmpty()) {
            client.player.sendMessage(Text.literal("Ingresa el nombre del jugador").formatted(Formatting.RED), false);
            return;
        }

        // Validar cantidad ingresada
        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            client.player.sendMessage(Text.literal("Ingresa un monto").formatted(Formatting.RED), false);
            return;
        }

        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                client.player.sendMessage(Text.literal("El monto debe ser mayor a 0").formatted(Formatting.RED), false);
                return;
            }

            // ✅ Llamar a `TransferPacketClient.send()`
            TransferPacketClient.send(targetPlayer, amount);

            // Confirmación de transferencia
            client.player.sendMessage(Text.literal("Transferencia enviada a " + targetPlayer + " por $" + amount)
                    .formatted(Formatting.GREEN), false);

            // Cerrar la pantalla después de la transferencia
            client.setScreen(null);

        } catch (NumberFormatException e) {
            client.player.sendMessage(Text.literal("Monto inválido, ingresa un número").formatted(Formatting.RED), false);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
