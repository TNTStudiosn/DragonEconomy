package org.TNTStudios.dragoneconomy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

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

        System.out.println("📜 Factura creada: " + title + " - $" + amount + " (Destino: " + recipient + ")");
    }


    public static List<Invoice> getInvoices(UUID recipient) {
        return invoices.getOrDefault(recipient, new ArrayList<>());
    }


    public static void removeInvoice(UUID recipientUUID, Invoice invoice) {
        List<Invoice> userInvoices = invoices.get(recipientUUID);
        if (userInvoices != null) {
            userInvoices.remove(invoice);
            if (userInvoices.isEmpty()) {
                invoices.remove(recipientUUID); // Eliminar la lista si está vacía
            }
            saveData(); // Guardar los cambios
        }
    }


}
