package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.EconomyManager;
import org.TNTStudios.dragoneconomy.Invoice;
import org.TNTStudios.dragoneconomy.InvoiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransferPacket {
    public static final Identifier ID = new Identifier("dragoneconomy", "transfer_money");
    public static final Identifier INVOICE_ID = new Identifier("dragoneconomy", "send_invoice");
    public static final Identifier PAY_INVOICE_ID = new Identifier("dragoneconomy", "pay_invoice");
    public static final Identifier REQUEST_INVOICES = new Identifier("dragoneconomy", "request_invoices");

    public static void registerReceiver() {
        // Manejador de transferencia de dinero estándar
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            String senderUUID = buf.readString();
            String targetPlayerName = buf.readString();
            int amount = buf.readInt();

            server.execute(() -> {
                ServerPlayerEntity sender = server.getPlayerManager().getPlayer(UUID.fromString(senderUUID));
                ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(targetPlayerName);

                if (sender == null) {
                    return;
                }

                if (targetPlayer != null) {
                    if (EconomyManager.getBalance(sender.getUuid()) >= amount) {
                        EconomyManager.transferMoney(sender, targetPlayer, amount);

                        // Enviar confirmación al jugador emisor
                        sender.sendMessage(Text.literal("✔ Has enviado $" + amount + " a " + targetPlayerName)
                                .formatted(Formatting.GREEN), false);

                        // Enviar confirmación al jugador receptor
                        targetPlayer.sendMessage(Text.literal("✔ Has recibido $" + amount + " de " + sender.getName().getString())
                                .formatted(Formatting.GREEN), false);

                    } else {
                        sender.sendMessage(Text.literal("⚠ No tienes fondos suficientes").formatted(Formatting.RED), false);
                    }
                } else {
                    sender.sendMessage(Text.literal("⚠ Jugador no encontrado").formatted(Formatting.RED), false);
                }
            });
        });

        // Manejador de envío de facturas
        ServerPlayNetworking.registerGlobalReceiver(INVOICE_ID, (server, player, handler, buf, responseSender) -> {
            String recipientName = buf.readString();
            String title = buf.readString();
            int amount = buf.readInt();
            String description = buf.readString();
            boolean isGovernment = buf.readBoolean();

            server.execute(() -> {
                ServerPlayerEntity recipient = server.getPlayerManager().getPlayer(recipientName);
                if (recipient != null) {
                    InvoiceManager.createInvoice(player.getUuid(), recipient.getUuid(), title, amount, description, isGovernment);

                    // Confirmación para el remitente
                    player.sendMessage(Text.literal("✔ Factura enviada a " + recipientName)
                            .formatted(Formatting.GREEN), false);

                    // Notificación para el destinatario
                    recipient.sendMessage(Text.literal("📜 Has recibido una nueva factura.")
                            .formatted(Formatting.YELLOW), false);
                } else {
                    player.sendMessage(Text.literal("⚠ No se encontró al jugador.").formatted(Formatting.RED), false);
                }
            });
        });

        // Manejador de pago de facturas
        ServerPlayNetworking.registerGlobalReceiver(new Identifier("dragoneconomy", "pay_multiple_invoices"), (server, player, handler, buf, responseSender) -> {
            int count = buf.readInt();
            List<UUID> invoicesToPay = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                invoicesToPay.add(buf.readUuid());
            }

            System.out.println("📜 Servidor recibió solicitud para pagar " + count + " facturas.");

            server.execute(() -> {
                UUID payerUUID = player.getUuid();

                for (UUID invoiceId : invoicesToPay) {
                    Invoice targetInvoice = InvoiceManager.getInvoiceById(payerUUID, invoiceId);

                    if (targetInvoice != null) {
                        System.out.println("✅ Factura encontrada en servidor: " + targetInvoice.getTitle());

                        int amount = targetInvoice.getAmount();
                        UUID senderUUID = targetInvoice.getSender();

                        if (EconomyManager.getBalance(payerUUID) >= amount) {
                            // Restar dinero al jugador que paga la factura
                            EconomyManager.setBalance(payerUUID, EconomyManager.getBalance(payerUUID) - amount);
                            EconomyManager.sendBalanceToClient(player);

                            if (!targetInvoice.isGovernmentPayment()) {
                                // Si no es un pago gubernamental, añadir dinero al emisor
                                EconomyManager.addMoney(senderUUID, amount);

                                ServerPlayerEntity sender = server.getPlayerManager().getPlayer(senderUUID);
                                if (sender != null) {
                                    sender.sendMessage(Text.literal("💰 Tu factura '" + targetInvoice.getTitle() + "' ha sido pagada. Has recibido $" + amount)
                                            .formatted(Formatting.GREEN), false);
                                    EconomyManager.sendBalanceToClient(sender);
                                }
                            } else {
                                // Notificar al emisor que su factura al gobierno ha sido pagada
                                ServerPlayerEntity sender = server.getPlayerManager().getPlayer(senderUUID);
                                if (sender != null) {
                                    sender.sendMessage(Text.literal("🏛 Tu factura '" + targetInvoice.getTitle() + "' ha sido pagada al gobierno.")
                                            .formatted(Formatting.YELLOW), false);
                                }
                            }

                            // Remover la factura pagada
                            InvoiceManager.removeInvoice(payerUUID, targetInvoice);

                            // Notificar al jugador que pagó
                            player.sendMessage(Text.literal("✔ Has pagado la factura: " + targetInvoice.getTitle() + " por $" + amount)
                                    .formatted(Formatting.GREEN), false);
                        } else {
                            player.sendMessage(Text.literal("⚠ No tienes fondos suficientes para pagar esta factura.")
                                    .formatted(Formatting.RED), false);
                        }
                    } else {
                        player.sendMessage(Text.literal("⚠ Factura no encontrada.").formatted(Formatting.RED), false);
                    }
                }
            });
        });



        // Manejador para solicitud de facturas
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_INVOICES, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                UUID playerUUID = player.getUuid();
                List<Invoice> playerInvoices = InvoiceManager.getInvoices(playerUUID);

                PacketByteBuf responseBuf = PacketByteBufs.create();
                responseBuf.writeInt(playerInvoices.size()); // 🔹 Enviar número total de facturas

                for (Invoice invoice : playerInvoices) {
                    responseBuf.writeUuid(invoice.getInvoiceId()); // 🔹 Enviar UUID único
                    responseBuf.writeUuid(invoice.getSender()); // 🔹 Enviar ID del remitente
                    responseBuf.writeString(invoice.getTitle()); // 🔹 Enviar título de factura
                    responseBuf.writeInt(invoice.getAmount()); // 🔹 Enviar monto
                    responseBuf.writeString(invoice.getDescription()); // 🔹 Enviar descripción
                    responseBuf.writeBoolean(invoice.isGovernmentPayment()); // 🔹 Enviar si es pago gubernamental
                }

                ServerPlayNetworking.send(player, REQUEST_INVOICES, responseBuf);
            });
        });
    } // 🔹 Se cierra correctamente `registerReceiver()`
}
