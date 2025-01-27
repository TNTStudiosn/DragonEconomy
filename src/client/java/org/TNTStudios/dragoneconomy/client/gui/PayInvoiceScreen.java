package org.TNTStudios.dragoneconomy.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.Invoice;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;

import java.util.*;

@Environment(EnvType.CLIENT)
public class PayInvoiceScreen extends Screen {
    private final List<Invoice> invoices;
    private final List<CheckboxWidget> checkboxes = new ArrayList<>();
    private ButtonWidget payButton;
    private final Map<UUID, Invoice> invoiceMap = new HashMap<>(); // Ahora almacena objetos Invoice

    public PayInvoiceScreen() {
        super(Text.literal("Pagar Facturas"));
        ClientInvoiceManager.refreshInvoices();
        this.invoices = ClientInvoiceManager.getInvoices();
    }

    @Override
    protected void init() {
        invoiceMap.clear();
        checkboxes.clear();
        invoices.clear();
        invoices.addAll(ClientInvoiceManager.getInvoices());

        int centerX = this.width / 2;
        int startY = 40;

        for (Invoice invoice : invoices) {
            String formattedText = invoice.getTitle() + " - $" + invoice.getAmount(); // ✅ Mostrar título + precio
            CheckboxWidget checkbox = new CheckboxWidget(
                    centerX - 75, startY + (invoices.indexOf(invoice) * 25),
                    20, 20,
                    Text.literal(formattedText),
                    false
            );
            checkboxes.add(checkbox);
            invoiceMap.put(invoice.getInvoiceId(), invoice);
            this.addDrawableChild(checkbox);
        }
    }

    private void paySelectedInvoices() {
        List<UUID> selectedInvoiceIds = new ArrayList<>();
        for (CheckboxWidget checkbox : checkboxes) {
            if (checkbox.isChecked()) {
                for (Map.Entry<UUID, Invoice> entry : invoiceMap.entrySet()) {
                    String formattedTitle = entry.getValue().getTitle() + " - $" + entry.getValue().getAmount();
                    if (formattedTitle.equals(checkbox.getMessage().getString())) {
                        selectedInvoiceIds.add(entry.getKey());
                        break;
                    }
                }
            }
        }

        if (selectedInvoiceIds.isEmpty()) {
            System.out.println("⚠ No se seleccionó ninguna factura.");
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(selectedInvoiceIds.size());
        for (UUID invoiceId : selectedInvoiceIds) {
            buf.writeUuid(invoiceId);
        }

        ClientPlayNetworking.send(new Identifier("dragoneconomy", "pay_multiple_invoices"), buf);
        this.client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        int centerX = this.width / 2;
        context.drawText(this.textRenderer, Text.literal("Selecciona las facturas para pagar").formatted(Formatting.YELLOW), centerX - 75, 20, 0xFFFFFF, false);
        super.render(context, mouseX, mouseY, delta);
    }

    public void updateInvoices() {
        invoices.clear();
        invoices.addAll(ClientInvoiceManager.getInvoices());

        System.out.println("📜 Actualizando GUI con nuevas facturas: " + invoices);

        this.clearChildren();
        checkboxes.clear();

        int centerX = this.width / 2;
        int startY = 40;

        for (Invoice invoice : invoices) {
            String formattedText = invoice.getTitle() + " - $" + invoice.getAmount();
            CheckboxWidget checkbox = new CheckboxWidget(centerX - 75, startY + (invoices.indexOf(invoice) * 25), 20, 20, Text.literal(formattedText), false);
            checkboxes.add(checkbox);
            this.addDrawableChild(checkbox);
        }

        payButton = ButtonWidget.builder(Text.literal("Pagar Seleccionadas"), button -> paySelectedInvoices())
                .dimensions(centerX - 50, startY + (invoices.size() * 25) + 20, 150, 20)
                .build();
        this.addDrawableChild(payButton);
    }
}
