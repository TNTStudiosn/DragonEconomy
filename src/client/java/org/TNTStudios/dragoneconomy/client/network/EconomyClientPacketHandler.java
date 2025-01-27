package org.TNTStudios.dragoneconomy.client.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.client.ClientInvoiceManager;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

public class EconomyClientPacketHandler {
    public static final Identifier SYNC_BALANCE = new Identifier("dragoneconomy", "sync_balance");
    public static final Identifier RECEIVE_INVOICE = new Identifier("dragoneconomy", "receive_invoice");

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
                ClientInvoiceManager.addInvoice(invoice); // Guardar factura en el cliente
                client.player.sendMessage(
                        net.minecraft.text.Text.literal("📜 Has recibido una nueva factura. Usa la tecla asignada para revisarlas.")
                                .formatted(net.minecraft.util.Formatting.YELLOW),
                        false
                );
            });
        });
    }
}
