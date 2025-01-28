package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.EconomyManager;

import java.util.UUID;

public class EconomyPacketHandler {
    public static final Identifier ADD_MONEY_PACKET = new Identifier("dragoneconomy", "add_money");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ADD_MONEY_PACKET, (server, player, handler, buf, responseSender) -> {
            UUID playerUUID = buf.readUuid();
            int amount = buf.readInt();

            server.execute(() -> {
                EconomyManager.addMoney(playerUUID, amount);
                EconomyManager.sendBalanceToClient(player);

                player.sendMessage(Text.literal("💰 Recibiste $" + amount + " por completar un trabajo.")
                        .formatted(Formatting.GREEN), false);
            });
        });
    }
}
