package org.TNTStudios.dragoneconomy.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;
import org.TNTStudios.dragoneconomy.Invoice;

import java.util.UUID;

public class EconomyClientPacketHandler {
    public static final Identifier SYNC_BALANCE = new Identifier("dragoneconomy", "sync_balance");
    public static final Identifier RECEIVE_INVOICE = new Identifier("dragoneconomy", "receive_invoice");
    public static final Identifier INVOICE_PAID = new Identifier("dragoneconomy", "invoice_paid");

    public static void register() {
        // Sincronizar saldo con el cliente
        ClientPlayNetworking.registerGlobalReceiver(SYNC_BALANCE, (client, handler, buf, responseSender) -> {
            int balance = buf.readInt();
            client.execute(() -> EconomyClientData.setBalance(balance));
        });

        // Recibir facturas enviadas por el servidor
        ClientPlayNetworking.registerGlobalReceiver(RECEIVE_INVOICE, (client, handler, buf, responseSender) -> {
            UUID invoiceId = buf.readUuid(); // Recibir ID único de factura
            UUID senderId = buf.readUuid(); // Recibir ID del remitente
            String title = buf.readString();
            int amount = buf.readInt();
            String description = buf.readString();
            boolean isGovernment = buf.readBoolean();

            Invoice invoice = new Invoice(invoiceId, senderId, client.player.getUuid(), title, amount, description, isGovernment);

            client.execute(() -> {
                System.out.println("📜 Factura recibida en cliente: " + invoice.getTitle());

                // 📌 Ahora almacenamos la factura como un objeto Invoice
                ClientInvoiceManager.addInvoice(invoice);

                // 🔄 Confirmar que la factura se guardó correctamente
                System.out.println("📜 Facturas actuales en ClientInvoiceManager: " + ClientInvoiceManager.getInvoices());

                client.player.sendMessage(
                        net.minecraft.text.Text.literal("📜 Has recibido una nueva factura. Usa la tecla asignada para revisarlas.")
                                .formatted(net.minecraft.util.Formatting.YELLOW),
                        false
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(INVOICE_PAID, (client, handler, buf, responseSender) -> {
            UUID invoiceId = buf.readUuid(); // Ahora el servidor envía el UUID de la factura pagada
            client.execute(() -> {
                System.out.println("📜 Factura pagada en cliente: " + invoiceId);

                // ✅ Ahora eliminamos la factura usando su UUID en lugar de un String
                ClientInvoiceManager.removeInvoice(invoiceId);

                client.player.sendMessage(
                        net.minecraft.text.Text.literal("✔ Has pagado una factura.")
                                .formatted(net.minecraft.util.Formatting.GREEN),
                        false
                );
            });
        });

    }
}
