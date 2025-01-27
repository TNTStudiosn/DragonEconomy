package org.TNTStudios.dragoneconomy.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.network.TransferPacket;

import java.util.List;

public class TransferScreen extends BaseOwoScreen<FlowLayout> {

    private TextFieldWidget recipientField;
    private TextFieldWidget amountField;
    private final List<String> onlinePlayers;

    public TransferScreen(List<String> onlinePlayers) {
        super(Text.literal("Transferencias"));
        this.onlinePlayers = onlinePlayers;
    }

    @Override
    protected OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout root) {
        root.verticalAlignment(VerticalAlignment.CENTER);

        // Título de la pantalla
        root.child(Components.label(Text.literal("Transferencias").formatted(Formatting.GOLD)));

        // Campo para ingresar el nombre del destinatario
        recipientField = new TextFieldWidget(textRenderer, 0, 0, 150, 20, Text.literal("Nombre del jugador"));
        root.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).child(Components.wrapVanillaWidget(recipientField)));

        // Campo para ingresar el monto
        amountField = new TextFieldWidget(textRenderer, 0, 0, 150, 20, Text.literal("Ingrese monto"));
        root.child(Containers.verticalFlow(Sizing.content(), Sizing.content()).child(Components.wrapVanillaWidget(amountField)));

        // Botón de transferencia
        root.child(Components.button(Text.literal("Enviar").formatted(Formatting.GREEN), button -> {
            processTransfer();
        }));
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

            // Enviar paquete de transferencia
            TransferPacket.send(client.player.getUuid().toString(), targetPlayer, amount);
            UISounds.playInteractionSound();

            // Confirmación de transferencia
            client.player.sendMessage(Text.literal("Transferencia enviada a " + targetPlayer + " por $" + amount)
                    .formatted(Formatting.GREEN), false);

        } catch (NumberFormatException e) {
            client.player.sendMessage(Text.literal("Monto inválido, ingresa un número").formatted(Formatting.RED), false);
        }
    }
}
