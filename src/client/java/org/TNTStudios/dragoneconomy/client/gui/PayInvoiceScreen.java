package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PayInvoiceScreen extends Screen {
    private final List<String> invoices;

    public PayInvoiceScreen(List<String> invoices) {
        super(Text.literal("Pagar Facturas"));
        this.invoices = invoices;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        if (invoices.isEmpty()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("No hay facturas pendientes"), button -> this.client.setScreen(null))
                    .dimensions(centerX - 75, centerY, 150, 20)
                    .build());
        } else {
            for (int i = 0; i < invoices.size(); i++) {
                String invoice = invoices.get(i);
                this.addDrawableChild(ButtonWidget.builder(Text.literal(invoice), button -> payInvoice(invoice))
                        .dimensions(centerX - 75, centerY - 40 + (i * 25), 150, 20)
                        .build());
            }
        }
    }

    private void payInvoice(String invoice) {
        invoices.remove(invoice);
        this.client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawText(this.textRenderer, Text.literal("Pagar Facturas"), this.width / 2 - 40, 20, Formatting.GOLD.getColorValue(), false);
        super.render(context, mouseX, mouseY, delta);
    }

}
