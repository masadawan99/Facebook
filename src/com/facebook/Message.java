package com.facebook;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

public class Message extends Content implements Serializable {
    private static final long serialVersionUID = 1L;

    public Message(String text, String sender) {
        super(text, sender);
    }

    @Override
    public void Print_Content() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        System.out.println(getSender() + " : " + getText());
        System.out.println(getTime().format(formatter));
    }

    public String getContent() {
        return getText();
    }

}
