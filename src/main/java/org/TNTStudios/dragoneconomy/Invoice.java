package org.TNTStudios.dragoneconomy;

import java.util.UUID;

public class Invoice {
    private UUID sender;
    private UUID recipient;
    private String title;
    private int amount;
    private String description;
    private boolean isGovernmentPayment;

    public Invoice(UUID sender, UUID recipient, String title, int amount, String description, boolean isGovernmentPayment) {
        this.sender = sender;
        this.recipient = recipient;
        this.title = title;
        this.amount = amount;
        this.description = description;
        this.isGovernmentPayment = isGovernmentPayment;
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getRecipient() {
        return recipient;
    }

    public String getTitle() {
        return title;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGovernmentPayment() {
        return isGovernmentPayment;
    }
}
