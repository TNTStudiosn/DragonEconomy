package org.TNTStudios.dragoneconomy.client.network;


import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.TNTStudios.dragoneconomy.network.EconomyClientData;

public class EconomyClientPacketHandler {
    public static final Identifier ID = new Identifier("dragoneconomy", "sync_balance");

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ID, (client, handler, buf, responseSender) -> {
            int balance = buf.readInt();
            client.execute(() -> EconomyClientData.setBalance(balance));
        });
    }
}
