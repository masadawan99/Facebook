package com.facebook;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public enum Type {
        MESSAGE, LIKE, COMMENT, TAG, GAME
    }

    private String sender;
    private Type type;
    private String text;
    private LocalDateTime createdAt;
    private boolean read;

    public Notification(Type type, String text) {
        this.type = type;
        this.text = text;
        this.createdAt = LocalDateTime.now();
        this.read = false;
        sender = Main.current.getCredentials().getUsername();
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }

    public void Print_Notificaton() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        System.out.println("| " + getType() + " | " + getText());
        System.out.println("| " + getCreatedAt().format(formatter) + " | " + (isRead() ? "(Read)" : "(Unread)"));
    }

    public String getMessage() {
        return text;
    }
}
