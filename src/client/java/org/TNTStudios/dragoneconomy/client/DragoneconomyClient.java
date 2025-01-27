package org.TNTStudios.dragoneconomy.client;

import net.fabricmc.api.ClientModInitializer;
import org.TNTStudios.dragoneconomy.client.network.EconomyClientPacketHandler;

public class DragoneconomyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudOverlay.register();
        EconomyClientPacketHandler.register();
    }
}
