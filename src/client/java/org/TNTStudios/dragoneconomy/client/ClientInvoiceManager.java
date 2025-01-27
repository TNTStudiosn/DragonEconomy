package org.TNTStudios.dragoneconomy.client;

import java.util.ArrayList;
import java.util.List;

public class ClientInvoiceManager {
    private static final List<String> invoices = new ArrayList<>();

    public static List<String> getInvoices() {
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
    }

    public static void clearInvoices() {
        invoices.clear();
    }
}
