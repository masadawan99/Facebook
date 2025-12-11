package com.facebook;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comment extends Content implements Serializable {
    private static final long serialVersionUID = 1L;

    public Comment(String text, String sender) {
        super(text, sender);
    }

    @Override
    public void Print_Content() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        System.out.println(getSender() + ": " + getText() + "         " + getTime().format(formatter));
    }

    public String getContent() {
        return getText();
    }

}
