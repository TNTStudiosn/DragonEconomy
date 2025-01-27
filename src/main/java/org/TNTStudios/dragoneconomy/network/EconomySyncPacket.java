package org.TNTStudios.dragoneconomy.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EconomySyncPacket {
    public static final Identifier ID = new Identifier("dragoneconomy", "sync_balance");

    public static void send(ServerPlayerEntity player, int balance) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(balance);
        ServerPlayNetworking.send(player, ID, buf);
    }
}
