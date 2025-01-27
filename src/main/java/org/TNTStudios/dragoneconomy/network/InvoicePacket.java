package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.EconomyManager;
import org.TNTStudios.dragoneconomy.Invoice;
import org.TNTStudios.dragoneconomy.InvoiceManager;

import java.util.UUID;

public class InvoicePacket {
    public static final Identifier SEND_INVOICE = new Identifier("dragoneconomy", "send_invoice");
    public static final Identifier PAY_INVOICE = new Identifier("dragoneconomy", "pay_invoice");

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
                    InvoiceManager.createInvoice(player.getUuid(), recipient.getUuid(), title, amount, description, isGovernment);
                    player.sendMessage(Text.literal("✔ Factura enviada a " + recipientName).formatted(Formatting.GREEN), false);
                    recipient.sendMessage(Text.literal("📩 Has recibido una nueva factura. ve a facturas para verla.").formatted(Formatting.YELLOW), false);
                } else {
                    player.sendMessage(Text.literal("⚠ No se encontró al jugador.").formatted(Formatting.RED), false);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PAY_INVOICE, (server, player, handler, buf, responseSender) -> {
            String invoiceTitle = buf.readString();
            server.execute(() -> {
                UUID payerUUID = player.getUuid();
                Invoice targetInvoice = null;

                for (Invoice invoice : InvoiceManager.getInvoices(payerUUID)) {
                    if (invoice.getTitle().equals(invoiceTitle)) {
                        targetInvoice = invoice;
                        break;
                    }
                }

                if (targetInvoice != null) {
                    InvoiceManager.payInvoice(player, targetInvoice);
                } else {
                    player.sendMessage(Text.literal("⚠ Factura no encontrada.").formatted(Formatting.RED), false);
                }
            });
        });
    }
}
