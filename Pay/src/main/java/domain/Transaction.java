package domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    private TransactionType type;
    private String category;
    private double amount;
    private LocalDateTime dateTime;

    public Transaction(TransactionType type, String category, double amount) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                ", dateTime=" + dateTime +
                '}';
    }
}
