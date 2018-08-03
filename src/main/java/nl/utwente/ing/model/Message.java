package nl.utwente.ing.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

public class Message {

    private int id;
    private String message;
    private LocalDateTime date;
    private boolean read;
    private MessageType type;

    public Message(int id, String message, String date, String type) {
        this.id = id;
        this.message = message;
        this.date = LocalDateTime.parse(date.replace("Z", ""));
        this.read = false;
        this.type = MessageType.valueOf(type);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setType(String type) {
        this.type = MessageType.valueOf(type);
    }

    public boolean isRead() {
        return read;
    }

    public MessageType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setDate(String date) {
        this.date = LocalDateTime.parse(date);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getDate() {
        return date.toString();
    }

    public boolean validMessage() {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
            timeFormatter.parse(date.toString());
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }
}
