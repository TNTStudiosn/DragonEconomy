package org.TNTStudios.dragoneconomy;

import net.fabricmc.api.ModInitializer;
import org.TNTStudios.dragoneconomy.commands.EconomyCommand;

public class Dragoneconomy implements ModInitializer {

    @Override
    public void onInitialize() {
        EconomyManager.init();
        EconomyCommand.register();
    }
}
