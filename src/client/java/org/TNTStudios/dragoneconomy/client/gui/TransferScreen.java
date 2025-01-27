package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.network.client.TransferPacketClient;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class TransferScreen extends Screen {

    private TextFieldWidget recipientField;
    private TextFieldWidget amountField;
    private ButtonWidget sendButton;
    private Text message = Text.empty();
    private int balance;
    private int suggestionIndex = 0;
    private List<String> playerNames;

    private final int boxWidth = 300;
    private final int boxHeight = 160;

    public TransferScreen() {
        super(Text.literal("Transferencias"));
        this.balance = EconomyClientData.getBalance();
    }

    @Override
    protected void init() {
        int centerX = (this.width - boxWidth) / 2;
        int centerY = (this.height - boxHeight) / 2;

        // Obtener nombres de jugadores en línea para autocompletado con TAB
        this.playerNames = MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream()
                .map(entry -> entry.getProfile().getName())
                .collect(Collectors.toList());

        // Campo de texto para el destinatario
        recipientField = new TextFieldWidget(textRenderer, centerX + 140, centerY + 30, 150, 20, Text.literal(""));
        recipientField.setMaxLength(16);
        this.addDrawableChild(recipientField);

        // Campo de texto para el monto
        amountField = new TextFieldWidget(textRenderer, centerX + 140, centerY + 60, 150, 20, Text.literal(""));
        amountField.setMaxLength(10);
        this.addDrawableChild(amountField);

        // Botón de transferencia
        sendButton = ButtonWidget.builder(Text.literal("ENVIAR").formatted(Formatting.GREEN), button -> processTransfer())
                .dimensions(centerX + 100, centerY + 100, 100, 20)
                .build();
        this.addDrawableChild(sendButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = (this.width - boxWidth) / 2;
        int centerY = (this.height - boxHeight) / 2;

        // Dibujar el fondo de la ventana sin doble borde
        context.fill(centerX, centerY, centerX + boxWidth, centerY + boxHeight, 0xAA000000);

        // Borde exterior blanco
        context.fill(centerX, centerY, centerX + boxWidth, centerY + 2, 0xFFFFFFFF);
        context.fill(centerX, centerY + boxHeight - 2, centerX + boxWidth, centerY + boxHeight, 0xFFFFFFFF);
        context.fill(centerX, centerY, centerX + 2, centerY + boxHeight, 0xFFFFFFFF);
        context.fill(centerX + boxWidth - 2, centerY, centerX + boxWidth, centerY + boxHeight, 0xFFFFFFFF);

        // Título
        context.drawText(this.textRenderer, Text.literal("TRANSFERENCIAS").formatted(Formatting.GOLD, Formatting.BOLD),
                centerX + (boxWidth / 2) - (this.textRenderer.getWidth("TRANSFERENCIAS") / 2) - 8, // 🔹 Movemos más a la izquierda
                centerY - 15, 0xFFFFFF, false);



        // Saldo actual (actualizado en tiempo real)
        context.drawText(this.textRenderer, Text.literal("Saldo: $" + balance).formatted(Formatting.YELLOW),
                centerX + (boxWidth / 2) - (this.textRenderer.getWidth("Saldo: $" + balance) / 2), centerY + 5, 0xFFFFFF, false);


        // Etiqueta "Destinatario"
        context.drawText(this.textRenderer, Text.literal("Destinatario").formatted(Formatting.WHITE),
                centerX + 10, centerY + 35, 0xFFFFFF, false);

        // Etiqueta "Monto"
        context.drawText(this.textRenderer, Text.literal("Monto").formatted(Formatting.WHITE),
                centerX + 10, centerY + 65, 0xFFFFFF, false);

        // Mostrar mensaje de error o confirmación
        if (!message.getString().isEmpty()) {
            context.drawText(this.textRenderer, message, centerX + 15, centerY + 140, 0xFFAA00, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258) { // Código de tecla TAB para autocompletar
            autoCompleteRecipient();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void autoCompleteRecipient() {
        if (playerNames.isEmpty()) return;

        String currentText = recipientField.getText().trim();
        if (currentText.isEmpty()) {
            recipientField.setText(playerNames.get(0));
            suggestionIndex = 0;
        } else {
            suggestionIndex = (suggestionIndex + 1) % playerNames.size();
            recipientField.setText(playerNames.get(suggestionIndex));
        }

        // 🔊 Reproducir sonido cuando se autocompleta
    }

    private void processTransfer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String targetPlayer = recipientField.getText().trim();
        if (targetPlayer.isEmpty()) {
            message = Text.literal("⚠ Ingresa el nombre del jugador").formatted(Formatting.RED);
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            message = Text.literal("⚠ Ingresa un monto válido").formatted(Formatting.RED);
            return;
        }

        try {
            int amount = Integer.parseInt(amountText);
            if (amount <= 0) {
                message = Text.literal("⚠ El monto debe ser mayor a 0").formatted(Formatting.RED);
                return;
            }

            if (amount > balance) {
                message = Text.literal("❌ No tienes fondos suficientes").formatted(Formatting.RED);
                return;
            }

            // 🔊 Reproducir sonido al enviar la transferencia
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);

            // Enviar paquete de transferencia al servidor
            TransferPacketClient.send(targetPlayer, amount);

            // Actualizar saldo restando el monto enviado
            balance -= amount;

            // Mostrar mensaje de confirmación
            message = Text.literal("✔ Transferencia enviada a " + targetPlayer + " por $" + amount)
                    .formatted(Formatting.GREEN);

        } catch (NumberFormatException e) {
            message = Text.literal("⚠ Monto inválido, ingresa un número").formatted(Formatting.RED);
        }
    }


    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
