package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PayInvoiceScreen extends Screen {
    private List<String> invoices;

    public PayInvoiceScreen() {
        super(Text.literal("Pagar Facturas"));
    }

    @Override
    protected void init() {
        invoices = ClientInvoiceManager.getInvoices(); // Obtiene las facturas del cliente

        // 🔍 Depuración para verificar si la lista está vacía
        System.out.println("📜 Lista de facturas obtenida en PayInvoiceScreen: " + invoices);

        int centerX = this.width / 2;
        int startY = Math.max(20, (int) (this.height * 0.2));

        if (invoices.isEmpty()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("No hay facturas pendientes"), button -> this.client.setScreen(null))
                    .dimensions(centerX - 75, startY, 150, 20)
                    .build());
        } else {
            for (int i = 0; i < invoices.size(); i++) {
                String invoice = invoices.get(i);
                this.addDrawableChild(ButtonWidget.builder(Text.literal(invoice), button -> openInvoiceDetails(invoice))
                        .dimensions(centerX - 75, startY + (i * 25), 150, 20)
                        .build());
            }
        }
    }

    private void openInvoiceDetails(String invoice) {
        if (invoice == null || invoice.isEmpty()) {
            System.out.println("⚠ Error: Factura nula o vacía, no se puede abrir detalles.");
            return;
        }
        this.client.setScreen(new PayInvoiceDetailsScreen(invoice, this));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawText(this.textRenderer, Text.literal("Pagar Facturas").formatted(Formatting.GOLD), this.width / 2 - 40, 20, 0xFFFFFF, false);
        super.render(context, mouseX, mouseY, delta);
    }
}
