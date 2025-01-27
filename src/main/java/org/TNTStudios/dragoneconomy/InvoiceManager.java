package org.TNTStudios.dragoneconomy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class InvoiceManager {
    private static final File STORAGE_FILE = new File("config/dragoneconomy_invoices.json");
    private static final Gson GSON = new Gson();
    private static Map<UUID, List<Invoice>> invoices = new HashMap<>();

    public static void init() {
        loadData();
    }

    private static void loadData() {
        if (STORAGE_FILE.exists()) {
            try (FileReader reader = new FileReader(STORAGE_FILE)) {
                Type type = new TypeToken<Map<UUID, List<Invoice>>>(){}.getType();
                invoices = GSON.fromJson(reader, type);
                if (invoices == null) invoices = new HashMap<>();
            } catch (IOException e) {
                System.err.println("Error al cargar las facturas: " + e.getMessage());
            }
        }
    }

    private static void saveData() {
        try (FileWriter writer = new FileWriter(STORAGE_FILE)) {
            GSON.toJson(invoices, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar las facturas: " + e.getMessage());
        }
    }

    public static void createInvoice(UUID sender, UUID recipient, String title, int amount, String description, boolean isGovernment) {
        Invoice invoice = new Invoice(sender, recipient, title, amount, description, isGovernment);
        invoices.computeIfAbsent(recipient, k -> new ArrayList<>()).add(invoice);
        saveData();
    }

    public static List<Invoice> getInvoices(UUID recipient) {
        return invoices.getOrDefault(recipient, new ArrayList<>());
    }

    public static boolean payInvoice(ServerPlayerEntity payer, Invoice invoice) {
        UUID payerUUID = payer.getUuid();

        int balance = EconomyManager.getBalance(payerUUID);
        if (balance < invoice.getAmount()) {
            payer.sendMessage(Text.literal("⚠ No tienes fondos suficientes para pagar esta factura.").formatted(Formatting.RED), false);
            return false;
        }

        if (!invoice.isGovernmentPayment()) {
            EconomyManager.addMoney(invoice.getSender(), invoice.getAmount());
        }

        EconomyManager.setBalance(payerUUID, balance - invoice.getAmount());
        invoices.get(payerUUID).remove(invoice);
        saveData();

        payer.sendMessage(Text.literal("✔ Has pagado la factura con éxito.").formatted(Formatting.GREEN), false);
        return true;
    }
}
