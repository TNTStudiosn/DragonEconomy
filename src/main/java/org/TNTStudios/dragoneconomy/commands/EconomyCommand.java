package org.TNTStudios.dragoneconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.EconomyManager;

import java.util.UUID;

public class EconomyCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("DragonEconomy")
                    .then(CommandManager.literal("reset")
                            .requires(source -> hasAdminPermission(source))
                            .then(CommandManager.argument("jugador", StringArgumentType.string())
                                    .executes(context -> {
                                        String playerName = StringArgumentType.getString(context, "jugador");
                                        ServerPlayerEntity targetPlayer = context.getSource().getServer().getPlayerManager().getPlayer(playerName);

                                        if (targetPlayer != null) {
                                            UUID playerUUID = targetPlayer.getUuid();
                                            EconomyManager.resetPlayer(playerUUID, targetPlayer);
                                            context.getSource().sendMessage(Text.literal("El balance de " + playerName + " ha sido ajustado.")
                                                    .styled(style -> style.withColor(Formatting.YELLOW)));
                                        } else {
                                            context.getSource().sendMessage(Text.literal("El jugador no está en línea.")
                                                    .styled(style -> style.withColor(Formatting.RED)));
                                        }
                                        return 1;
                                    })
                            )
                    )

            );
        });
    }

    private static boolean hasAdminPermission(ServerCommandSource source) {
        if (source.getPlayer() == null) return false;

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(source.getPlayer().getUuid());

        return user != null && user.getCachedData().getPermissionData().checkPermission("DragonEconomy.admin").asBoolean();
    }
}
