package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PayInvoiceScreen extends Screen {
    private final List<String> invoices;
    private int selectedInvoiceIndex = -1;
    private ButtonWidget payButton;

    public PayInvoiceScreen() {
        super(Text.literal("Pagar Facturas"));
        ClientInvoiceManager.refreshInvoices(); // 🔄 Asegurar que la lista esté actualizada antes de obtenerla
        this.invoices = ClientInvoiceManager.getInvoices();
        System.out.println("📜 Facturas disponibles en GUI: " + invoices);
    }

    @Override
    protected void init() {
        ClientInvoiceManager.refreshInvoices(); // Solicitar facturas antes de iniciar la GUI

        invoices.clear();
        invoices.addAll(ClientInvoiceManager.getInvoices());

        System.out.println("📜 Facturas disponibles en GUI: " + invoices);

        int centerX = this.width / 2;
        int startY = 40;

        this.clearChildren();

        for (int i = 0; i < invoices.size(); i++) {
            int index = i;
            System.out.println("🔘 Agregando botón para factura: " + invoices.get(i));
            this.addDrawableChild(ButtonWidget.builder(Text.literal(invoices.get(i)), button -> selectedInvoiceIndex = index)
                    .dimensions(centerX - 75, startY + (i * 25), 150, 20)
                    .build());
        }

        payButton = ButtonWidget.builder(Text.literal("Pagar"), button -> payInvoice())
                .dimensions(centerX - 50, startY + (invoices.size() * 25) + 20, 100, 20)
                .build();

        this.addDrawableChild(payButton);
    }


    private void payInvoice() {
        if (selectedInvoiceIndex == -1 || invoices.isEmpty()) {
            System.out.println("⚠ No hay facturas seleccionadas o disponibles para pagar.");
            return;
        }

        String invoice = invoices.get(selectedInvoiceIndex);
        System.out.println("💰 Procesando pago para factura: " + invoice);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(invoice);

        ClientPlayNetworking.send(new Identifier("dragoneconomy", "pay_invoice"), buf);

        // Remover factura pagada y actualizar la pantalla
        invoices.remove(selectedInvoiceIndex);
        selectedInvoiceIndex = -1;
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;

        context.drawText(this.textRenderer, Text.literal("Selecciona una factura para pagar").formatted(Formatting.YELLOW), centerX - 75, 20, 0xFFFFFF, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
