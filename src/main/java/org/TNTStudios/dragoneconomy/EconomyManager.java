package org.TNTStudios.dragoneconomy;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.TNTStudios.dragoneconomy.network.EconomySyncPacket;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class EconomyManager {
    private static final File STORAGE_FILE = new File("config/dragoneconomy_data.json");
    private static final Gson GSON = new Gson();
    private static Map<UUID, Integer> playerBalances = new HashMap<>();

    public static void init() {
        loadData();

        // Evento cuando un jugador se une al servidor
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();

            // Si el jugador no tiene un balance registrado, darle el inicial
            if (!playerBalances.containsKey(playerUUID)) {
                int initialMoney = determineInitialMoney(player);
                setBalance(playerUUID, initialMoney);

                // Enviar mensaje de bienvenida
                player.sendMessage(Text.literal("¡Bienvenido a DragonCraft! Se te ha otorgado $" + initialMoney + " de inicio.")
                        .styled(style -> style.withColor(Formatting.GREEN)), false);
            }

            // Enviar el balance actual al cliente
            sendBalanceToClient(player);
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
                return 750;
            }
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.rolplayer").asBoolean()) {
                return 750;
            }
            if (user.getCachedData().getPermissionData().checkPermission("DragonEconomy.player").asBoolean()) {
                return 250;
            }
        }
        return 100;
    }


    public static int getBalance(UUID playerUUID) {
        return playerBalances.getOrDefault(playerUUID, 0);
    }

    public static void setBalance(UUID playerUUID, int amount) {
        playerBalances.put(playerUUID, amount);
        saveData();
    }

    public static void addMoney(UUID playerUUID, int amount) {
        int currentBalance = getBalance(playerUUID);
        setBalance(playerUUID, currentBalance + amount);
    }

    public static void resetPlayer(UUID playerUUID, ServerPlayerEntity player) {
        int currentBalance = getBalance(playerUUID);
        int initialMoney = determineInitialMoney(player);

        // En lugar de sobrescribir, sumamos el dinero inicial del rol al actual
        setBalance(playerUUID, currentBalance + initialMoney);

        player.sendMessage(Text.literal("Tu balance ha sido restablecido con $" + initialMoney + " adicionales. sal y entra para aplicar los cambios")
                .styled(style -> style.withColor(Formatting.YELLOW)), false);
    }


    private static void loadData() {
        if (STORAGE_FILE.exists()) {
            try {
                String json = new String(Files.readAllBytes(STORAGE_FILE.toPath()));
                playerBalances = GSON.fromJson(json, new TypeToken<Map<UUID, Integer>>(){}.getType());
            } catch (IOException e) {
                System.err.println("Error al cargar los datos de economía: " + e.getMessage());
            }
        }
    }

    private static void saveData() {
        try {
            Files.write(STORAGE_FILE.toPath(), GSON.toJson(playerBalances).getBytes());
        } catch (IOException e) {
            System.err.println("Error al guardar los datos de economía: " + e.getMessage());
        }
    }

    public static void transferMoney(ServerPlayerEntity sender, ServerPlayerEntity receiver, int amount) {
        UUID senderUUID = sender.getUuid();
        UUID receiverUUID = receiver.getUuid();

        int senderBalance = getBalance(senderUUID);
        if (senderBalance < amount) {
            sender.sendMessage(Text.literal("⚠ No tienes fondos suficientes").formatted(Formatting.RED), false);
            return;
        }

        // ✅ Realizar la transferencia
        setBalance(senderUUID, senderBalance - amount);
        addMoney(receiverUUID, amount);

        // ✅ Enviar saldo actualizado a ambos jugadores
        sendBalanceToClient(sender);
        sendBalanceToClient(receiver);

        // ✅ Enviar mensaje de confirmación dentro de la GUI
        sender.sendMessage(Text.literal("✔ Has enviado $" + amount + " a " + receiver.getName().getString())
                .formatted(Formatting.GREEN), false);
        receiver.sendMessage(Text.literal("✔ Has recibido $" + amount + " de " + sender.getName().getString())
                .formatted(Formatting.GREEN), false);

        // ✅ Guardar la transferencia en historial
        saveTransferHistory(senderUUID, receiver.getName().getString(), amount);
    }

    public static void sendBalanceToClient(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        int balance = getBalance(playerUUID);
        EconomySyncPacket.send(player, balance);
    }



    private static void saveTransferHistory(UUID senderUUID, String receiverName, int amount) {
        File dir = new File("config/dragoneconomy/transferencias/" + senderUUID);
        if (!dir.exists()) dir.mkdirs();

        File historyFile = new File(dir, "historial.json");
        Gson gson = new Gson();

        List<Map<String, Object>> history;
        if (historyFile.exists()) {
            try (Reader reader = new FileReader(historyFile)) {
                history = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>(){}.getType());
            } catch (IOException e) {
                history = new ArrayList<>();
            }
        } else {
            history = new ArrayList<>();
        }

        Map<String, Object> transferRecord = new HashMap<>();
        transferRecord.put("destinatario", receiverName);
        transferRecord.put("cantidad", amount);
        transferRecord.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        history.add(transferRecord);

        try (FileWriter writer = new FileWriter(historyFile)) {
            gson.toJson(history, writer);
        } catch (IOException e) {
            System.err.println("Error al guardar el historial de transferencias: " + e.getMessage());
        }
    }
}
