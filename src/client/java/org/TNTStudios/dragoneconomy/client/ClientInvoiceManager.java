package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.Invoice;
import org.TNTStudios.dragoneconomy.client.gui.PayInvoiceScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientInvoiceManager {
    private static final List<Invoice> invoices = new ArrayList<>();
    public static final Identifier REQUEST_INVOICES = new Identifier("dragoneconomy", "request_invoices");

    public static List<Invoice> getInvoices() {
        System.out.println("🔍 Obteniendo lista de facturas:");
        for (Invoice invoice : invoices) {
            System.out.println("✅ Factura en lista: " + invoice.getTitle() + " (ID: " + invoice.getInvoiceId() + ")");
        }
        return new ArrayList<>(invoices);
    }


    public static void addInvoice(Invoice invoice) {
        if (!invoices.contains(invoice)) {
            invoices.add(invoice);
            System.out.println("✅ Factura añadida correctamente: " + invoice.getTitle());
        } else {
            System.out.println("⚠ La factura ya estaba en la lista: " + invoice.getTitle());
        }
    }

    public static void removeInvoice(Invoice invoice) {
        invoices.remove(invoice);
        System.out.println("❌ Factura eliminada: " + invoice.getTitle());
    }

    public static void clearInvoices() {
        invoices.clear();
        System.out.println("🗑 Lista de facturas limpiada.");
    }

    public static void refreshInvoices() {
        invoices.clear();
        System.out.println("🔄 Solicitando facturas al servidor...");
        ClientPlayNetworking.send(REQUEST_INVOICES, PacketByteBufs.create());
    }

    public static void registerReceivers() {
        // Recibir facturas desde el servidor
        ClientPlayNetworking.registerGlobalReceiver(REQUEST_INVOICES, (client, handler, buf, responseSender) -> {
            int invoiceCount = buf.readInt();
            List<Invoice> receivedInvoices = new ArrayList<>();

            for (int i = 0; i < invoiceCount; i++) {
                UUID invoiceId = buf.readUuid(); // ✅ Leer el UUID correcto.
                UUID senderId = buf.readUuid();
                String title = buf.readString();
                int amount = buf.readInt();
                String description = buf.readString();
                boolean isGovernment = buf.readBoolean();

                Invoice invoice = new Invoice(invoiceId, senderId, client.player.getUuid(), title, amount, description, isGovernment);
                receivedInvoices.add(invoice);
            }

            client.execute(() -> {
                invoices.clear();
                invoices.addAll(receivedInvoices);

                System.out.println("📜 Recibidas " + invoiceCount + " facturas desde el servidor.");
                for (Invoice invoice : receivedInvoices) {
                    System.out.println("✅ Factura añadida: " + invoice.getTitle() + " (ID: " + invoice.getInvoiceId() + ")");
                }

                if (client.currentScreen instanceof PayInvoiceScreen) {
                    ((PayInvoiceScreen) client.currentScreen).updateInvoices();
                    System.out.println("🔄 Actualización forzada de la pantalla de pago.");
                }
            });

        });


    }


    public static void removeInvoice(UUID invoiceId) {
        invoices.removeIf(invoice -> invoice.getInvoiceId().equals(invoiceId));
        System.out.println("❌ Factura eliminada con ID: " + invoiceId);
    }

}
