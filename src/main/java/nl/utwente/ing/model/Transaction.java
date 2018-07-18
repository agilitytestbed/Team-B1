package nl.utwente.ing.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction {
	private int id;
	
	private String externalIBAN;
	private double amount;
	private LocalDateTime date;
	private TransactionType type;
	private Category category = null;
	private String description;
	private double balance;
	
	public Transaction(int id, String date, double amount, String externalIBAN, String type, String description) {
		setId(id);
		setAmount(amount);
		setDate(LocalDateTime.parse(date.replace("Z", "")));
		setType(TransactionType.valueOf(type));
		setExternalIBAN(externalIBAN);
		this.description = description;
		this.balance = 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getExternalIBAN() {
		return externalIBAN;
	}

	public void setExternalIBAN(String external_iban) {
		this.externalIBAN = external_iban;
	}

	public TransactionType getType() {
		return type;
	}

	public void setType(TransactionType type) {
		this.type = type;
	}

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean validTransaction() {
		
		// if a value is null
		if (externalIBAN == null || date == null || type == null) {
			return false;
		}

		// if amount is negative or zero
		if (amount <= 0) {
			return false;
		}
		
		// if the date is not valid date-time
		
		return true;
		
	}
}
