package org.TNTStudios.dragoneconomy.client;

import java.util.ArrayList;
import java.util.List;

public class ClientInvoiceManager {
    private static final List<String> invoices = new ArrayList<>();

    public static List<String> getInvoices() {
        return new ArrayList<>(invoices);
    }

    public static void addInvoice(String invoice) {
        invoices.add(invoice);
    }

    public static void removeInvoice(String invoice) {
        invoices.remove(invoice);
    }

    public static void clearInvoices() {
        invoices.clear();
    }
}
