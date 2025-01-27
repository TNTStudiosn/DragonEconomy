package org.TNTStudios.dragoneconomy.network;

public class EconomyClientData {
    private static int balance = 0;

    public static void setBalance(int newBalance) {
        balance = newBalance;
    }

    public static int getBalance() {
        return balance;
    }
}
