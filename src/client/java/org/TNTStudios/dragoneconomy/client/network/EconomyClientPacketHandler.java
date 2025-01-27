package org.TNTStudios.dragoneconomy.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

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
            String invoice = buf.readString(); // Recibir la factura

            client.execute(() -> {
                System.out.println("📜 Factura recibida en cliente: " + invoice); // 🔍 Depuración

                // 📌 Asegurar que se guarde correctamente
                ClientInvoiceManager.addInvoice(invoice);

                // 🔄 Confirmar que la factura se guardó
                System.out.println("📜 Facturas actuales en ClientInvoiceManager: " + ClientInvoiceManager.getInvoices());

                client.player.sendMessage(
                        net.minecraft.text.Text.literal("📜 Has recibido una nueva factura. Usa la tecla asignada para revisarlas.")
                                .formatted(net.minecraft.util.Formatting.YELLOW),
                        false
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(INVOICE_PAID, (client, handler, buf, responseSender) -> {
            String paidInvoice = buf.readString();
            client.execute(() -> {
                System.out.println("📜 Factura pagada en cliente: " + paidInvoice);

                // ✅ Ahora eliminamos la factura después de pagarla
                ClientInvoiceManager.removeInvoice(paidInvoice);

                client.player.sendMessage(
                        net.minecraft.text.Text.literal("✔ Has pagado la factura: " + paidInvoice)
                                .formatted(net.minecraft.util.Formatting.GREEN),
                        false
                );
            });
        });

    }
}
