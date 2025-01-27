package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.EconomyManager;

public class TransferPacket {
    public static final Identifier ID = new Identifier("dragoneconomy", "transfer_money");

    public static void send(ServerPlayerEntity sender, String targetPlayer, int amount) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(targetPlayer);
        buf.writeInt(amount);
        ServerPlayNetworking.send(sender, ID, buf);
    }


    public static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            String targetPlayerName = buf.readString();
            int amount = buf.readInt();

            server.execute(() -> {
                ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(targetPlayerName);
                if (targetPlayer != null) {
                    EconomyManager.transferMoney(player, targetPlayer, amount);
                } else {
                    player.sendMessage(Text.literal("Jugador no encontrado").formatted(Formatting.RED), false);
                }
            });
        });
    }

}
