package org.TNTStudios.dragoneconomy;

import net.fabricmc.api.ModInitializer;
import org.TNTStudios.dragoneconomy.commands.EconomyCommand;
import org.TNTStudios.dragoneconomy.network.TransferPacket;

public class Dragoneconomy implements ModInitializer {

    @Override
    public void onInitialize() {
        EconomyManager.init();
        EconomyCommand.register();
        TransferPacket.registerReceiver();
    }
}
