package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.gui.PayInvoiceScreen;

import java.util.ArrayList;
import java.util.List;

public class ClientInvoiceManager {
    private static final List<String> invoices = new ArrayList<>();
    public static final Identifier REQUEST_INVOICES = new Identifier("dragoneconomy", "request_invoices");

    public static List<String> getInvoices() {
        System.out.println("🔍 Obteniendo lista de facturas: " + invoices);
        return new ArrayList<>(invoices); // Se devuelve una copia para evitar modificaciones externas
    }

    public static void addInvoice(String invoice) {
        if (!invoices.contains(invoice)) {
            invoices.add(invoice);
            System.out.println("✅ Factura añadida correctamente: " + invoice); // 🔍 Depuración
        } else {
            System.out.println("⚠ La factura ya estaba en la lista: " + invoice);
        }
    }

    public static void removeInvoice(String invoice) {
        invoices.remove(invoice);
        System.out.println("❌ Factura eliminada: " + invoice);
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
            List<String> receivedInvoices = new ArrayList<>();

            for (int i = 0; i < invoiceCount; i++) {
                String title = buf.readString();
                int amount = buf.readInt();
                String description = buf.readString();
                boolean isGovernment = buf.readBoolean();

                String invoiceData = title + " - $" + amount + (isGovernment ? " (Gobierno)" : "");
                receivedInvoices.add(invoiceData);
            }

            client.execute(() -> {
                invoices.clear();
                invoices.addAll(receivedInvoices);

                System.out.println("📜 Recibidas " + invoiceCount + " facturas desde el servidor.");
                for (String invoice : receivedInvoices) {
                    System.out.println("✅ Factura añadida: " + invoice);
                }

                // 🔹 Asegurar que la pantalla de facturas se actualiza si está abierta
                if (client.currentScreen instanceof PayInvoiceScreen) {
                    ((PayInvoiceScreen) client.currentScreen).updateInvoices();
                }
            });
        });

    }
}
