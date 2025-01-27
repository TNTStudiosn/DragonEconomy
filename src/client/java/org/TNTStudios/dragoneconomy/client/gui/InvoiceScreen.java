package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class InvoiceScreen extends Screen {
    private TextFieldWidget recipientField;
    private TextFieldWidget titleField;
    private TextFieldWidget amountField;
    private TextFieldWidget descriptionField;
    private ButtonWidget sendButton;
    private CheckboxWidget governmentPaymentCheckBox;

    public InvoiceScreen() {
        super(Text.literal("Enviar Factura"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int screenHeight = this.height;
        int startY = Math.max(20, (int) (screenHeight * 0.15)); // 🔹 Se ajusta dinámicamente para no estar demasiado abajo

        // Campos de texto
        recipientField = new TextFieldWidget(textRenderer, centerX - 75, startY + 10, 150, 20, Text.literal(""));
        titleField = new TextFieldWidget(textRenderer, centerX - 75, startY + 50, 150, 20, Text.literal(""));
        amountField = new TextFieldWidget(textRenderer, centerX - 75, startY + 90, 150, 20, Text.literal(""));
        descriptionField = new TextFieldWidget(textRenderer, centerX - 75, startY + 130, 150, 40, Text.literal(""));

        this.addDrawableChild(recipientField);
        this.addDrawableChild(titleField);
        this.addDrawableChild(amountField);
        this.addDrawableChild(descriptionField);

        // Checkbox de pago al gobierno
        governmentPaymentCheckBox = new CheckboxWidget(centerX - 75, startY + 185, 20, 20, Text.literal("Pago al Gobierno"), false);
        this.addDrawableChild(governmentPaymentCheckBox);

        // Botón de enviar
        sendButton = ButtonWidget.builder(Text.literal("Enviar"), button -> sendInvoice())
                .dimensions(centerX - 50, startY + 215, 100, 20)
                .build();
        this.addDrawableChild(sendButton);
    }

    private void sendInvoice() {
        String recipient = recipientField.getText();
        String title = titleField.getText();
        String amount = amountField.getText();
        String description = descriptionField.getText();
        boolean isGovernmentPayment = governmentPaymentCheckBox.isChecked();

        if (recipient.isEmpty() || title.isEmpty() || amount.isEmpty()) {
            return;
        }

        // Lógica de envío de factura aquí...
        this.client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        int screenHeight = this.height;
        int startY = Math.max(20, (int) (screenHeight * 0.15)); // 🔹 Se ajusta dinámicamente según la pantalla

        // Título
        context.drawText(this.textRenderer, Text.literal("Enviar Factura").formatted(Formatting.GOLD), centerX - 40, startY - 30, 0xFFFFFF, false);

        // Etiquetas alineadas justo encima de los campos
        context.drawText(this.textRenderer, Text.literal("Destinatario:"), centerX - 75, startY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Título:"), centerX - 75, startY + 40, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Cantidad:"), centerX - 75, startY + 80, 0xFFFFFF, false);
        context.drawText(this.textRenderer, Text.literal("Descripción:"), centerX - 75, startY + 120, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }
}
