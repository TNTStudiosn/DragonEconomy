package org.TNTStudios.dragoneconomy.network.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class TransferPacketClient {
    public static final Identifier ID = new Identifier("dragoneconomy", "transfer_money");

    public static void send(String targetPlayer, int amount) {
        if (MinecraftClient.getInstance().player == null) return; // Verificar si el jugador está presente

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(MinecraftClient.getInstance().player.getUuid().toString()); // Enviar UUID como String
        buf.writeString(targetPlayer);
        buf.writeInt(amount);

        ClientPlayNetworking.send(ID, buf); // Enviar paquete desde el cliente
    }
}
