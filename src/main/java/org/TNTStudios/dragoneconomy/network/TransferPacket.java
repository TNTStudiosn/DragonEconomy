package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.EconomyManager;

import java.util.UUID;

public class TransferPacket {
    public static final Identifier ID = new Identifier("dragoneconomy", "transfer_money");

    public static void registerReceiver() {
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
    }


}
