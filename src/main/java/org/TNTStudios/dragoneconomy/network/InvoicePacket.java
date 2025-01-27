// 📌 PAQUETE DE ENVÍO DE FACTURAS (Servidor envía facturas a los clientes)
package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.Invoice;
import org.TNTStudios.dragoneconomy.InvoiceManager;

public class InvoicePacket {
    public static final Identifier SEND_INVOICE = new Identifier("dragoneconomy", "send_invoice");
    public static final Identifier RECEIVE_INVOICE = new Identifier("dragoneconomy", "receive_invoice");

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SEND_INVOICE, (server, player, handler, buf, responseSender) -> {
            String recipientName = buf.readString();
            String title = buf.readString();
            int amount = buf.readInt();
            String description = buf.readString();
            boolean isGovernment = buf.readBoolean();

            server.execute(() -> {
                ServerPlayerEntity recipient = server.getPlayerManager().getPlayer(recipientName);
                if (recipient != null) {
                    Invoice invoice = new Invoice(player.getUuid(), recipient.getUuid(), title, amount, description, isGovernment);
                    InvoiceManager.createInvoice(
                            invoice.getSender(),
                            invoice.getRecipient(),
                            invoice.getTitle(),
                            invoice.getAmount(),
                            invoice.getDescription(),
                            invoice.isGovernmentPayment()
                    );


                    // Al enviar facturas al cliente
                    PacketByteBuf invoiceBuf = PacketByteBufs.create();
                    invoiceBuf.writeUuid(invoice.getInvoiceId()); // Enviar UUID
                    invoiceBuf.writeUuid(invoice.getSender()); // Enviar ID del remitente
                    invoiceBuf.writeString(invoice.getTitle());
                    invoiceBuf.writeInt(invoice.getAmount());
                    invoiceBuf.writeString(invoice.getDescription());
                    invoiceBuf.writeBoolean(invoice.isGovernmentPayment());

                    ServerPlayNetworking.send(recipient, RECEIVE_INVOICE, invoiceBuf);

                    recipient.sendMessage(Text.literal("📜 Has recibido una nueva factura.").formatted(Formatting.YELLOW), false);
                } else {
                    player.sendMessage(Text.literal("⚠ No se encontró al jugador.").formatted(Formatting.RED), false);
                }
            });
        });
    }

}
