package org.TNTStudios.dragoneconomy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class EconomyManager {
    private static final File STORAGE_FILE = new File("config/dragoneconomy_data.json");
    private static final Gson GSON = new Gson();
    private static Set<UUID> receivedMoneyPlayers = new HashSet<>();

    public static void init() {
        loadData();

        // Evento cuando un jugador se une al servidor
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();

            // Verificar si el jugador ya recibió el dinero inicial
            if (!receivedMoneyPlayers.contains(playerUUID)) {
                int money = determineInitialMoney(player);
                if (money > 0) {
                    giveMoney(player, money);
                    receivedMoneyPlayers.add(playerUUID);
                    saveData();
                }
            }
        });

        // Guardar datos cuando el servidor se apaga
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveData());
    }

    private static int determineInitialMoney(ServerPlayerEntity player) {
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUuid());

        if (user != null) {
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.admin").asBoolean()) {
                return 1000;
            }
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.mod").asBoolean()) {
                return 500;
            }
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.rolplayer").asBoolean()) {
                return 600;
            }
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.player").asBoolean()) {
                return 100;
            }
        }
        return 0; // Si el jugador no tiene permisos, no recibe dinero inicial
    }

    private static void giveMoney(ServerPlayerEntity player, int amount) {
        player.sendMessage(Text.literal("Has recibido $" + amount + " de inicio!")
                .styled(style -> style.withColor(Formatting.GREEN)), false);
        // Aquí puedes implementar la lógica para agregar el dinero a la cuenta del jugador en tu sistema de economía
    }

    private static void loadData() {
        if (STORAGE_FILE.exists()) {
            try {
                String json = new String(Files.readAllBytes(STORAGE_FILE.toPath()));
                receivedMoneyPlayers = GSON.fromJson(json, new TypeToken<Set<UUID>>(){}.getType());
            } catch (IOException e) {
                System.err.println("Error al cargar los datos de economía: " + e.getMessage());
            }
        }
    }

    private static void saveData() {
        try {
            Files.write(STORAGE_FILE.toPath(), GSON.toJson(receivedMoneyPlayers).getBytes());
        } catch (IOException e) {
            System.err.println("Error al guardar los datos de economía: " + e.getMessage());
        }
    }

    public static void resetPlayer(UUID playerUUID) {
        if (receivedMoneyPlayers.contains(playerUUID)) {
            receivedMoneyPlayers.remove(playerUUID);
            saveData();
            System.out.println("El jugador con UUID " + playerUUID + " ha sido restablecido para recibir dinero inicial nuevamente.");
        }
    }
}
