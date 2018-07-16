package nl.utwente.ing.model;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class PaymentRequest {

    private int id;
    private String description;
    private String due_date;
    private double amount;
    private int number_of_requests;
    private boolean filled;
    private Transaction[] transactions;


    public PaymentRequest(int id, String description, String dueDate, double amount, int nbOfRequests) {
        this.id = id;
        this.description = description;
        this.due_date = dueDate;
        this.amount = amount;
        this.number_of_requests = nbOfRequests;
        this.filled = false;
        this.transactions = new Transaction[nbOfRequests];
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFilled() {
        return filled;
    }

    public double getAmount() {
        return amount;
    }

    public int getNumber_of_requests() {
        return number_of_requests;
    }

    public String getDescription() {
        return description;
    }

    public String getDue_date() {
        return due_date;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public void setNumber_of_requests(int number_of_requests) {
        this.number_of_requests = number_of_requests;
    }

    public void setTransactions(Transaction[] transactions) {
        this.transactions = transactions;
    }

    public boolean checkValidPaymentRequest() {
        if (getAmount() <= 0 || getNumber_of_requests() <= 0 || getDescription() == null || getId() < 0) {
            return false;
        }

        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
            timeFormatter.parse(getDue_date());
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }
}
