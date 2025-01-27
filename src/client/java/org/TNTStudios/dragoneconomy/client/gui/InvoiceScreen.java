package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

@Environment(EnvType.CLIENT)
public class InvoiceScreen extends Screen {
    private TextFieldWidget recipientField;
    private TextFieldWidget titleField;
    private TextFieldWidget amountField;
    private ButtonWidget sendButton;
    private CheckboxWidget governmentPaymentCheckBox;

    public InvoiceScreen() {
        super(Text.literal("Enviar Factura"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = Math.max(20, (int) (this.height * 0.15)); // Ajuste dinámico

        // Campos de texto
        recipientField = new TextFieldWidget(textRenderer, centerX - 75, startY + 10, 150, 20, Text.literal(""));
        titleField = new TextFieldWidget(textRenderer, centerX - 75, startY + 50, 150, 20, Text.literal(""));
        amountField = new TextFieldWidget(textRenderer, centerX - 75, startY + 90, 150, 20, Text.literal(""));

        this.addDrawableChild(recipientField);
        this.addDrawableChild(titleField);
        this.addDrawableChild(amountField);

        // Checkbox de pago al gobierno
        governmentPaymentCheckBox = new CheckboxWidget(centerX - 75, startY + 135, 20, 20, Text.literal("Pago al Gobierno"), false);
        this.addDrawableChild(governmentPaymentCheckBox);

        // Botón de enviar
        sendButton = ButtonWidget.builder(Text.literal("Enviar"), button -> sendInvoice())
                .dimensions(centerX - 50, startY + 165, 100, 20)
                .build();
        this.addDrawableChild(sendButton);
    }

    private void sendInvoice() {
        String recipient = recipientField.getText().trim();
        String title = titleField.getText().trim();
        String amount = amountField.getText().trim();
        String description = ""; // 🔹 Siempre se enviará vacío
        boolean isGovernmentPayment = governmentPaymentCheckBox.isChecked();

        if (recipient.isEmpty() || title.isEmpty() || amount.isEmpty()) {
            return;
        }

        try {
            int amountValue = Integer.parseInt(amount);
            if (amountValue <= 0) {
                return;
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeString(recipient);
            buf.writeString(title);
            buf.writeInt(amountValue);
            buf.writeString(description); // 🔹 Siempre vacío
            buf.writeBoolean(isGovernmentPayment);

            ClientPlayNetworking.send(new Identifier("dragoneconomy", "send_invoice"), buf);
            this.client.setScreen(null);
        } catch (NumberFormatException e) {
            // Evitar valores inválidos
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        int startY = Math.max(20, (int) (this.height * 0.15));

        // Título
        context.drawText(this.textRenderer, Text.literal("Enviar Factura").formatted(Formatting.GOLD), centerX - 40, startY - 30, 0xFFFFFF, false);

        // Etiquetas alineadas justo encima de los campos
        context.drawText(this.textRenderer, Text.literal("Destinatario:"), centerX - 75, startY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Título:"), centerX - 75, startY + 40, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Cantidad:"), centerX - 75, startY + 80, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }
}
