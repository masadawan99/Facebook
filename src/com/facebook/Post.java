package com.facebook;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Post extends Content implements Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<String> tagged;

    public Post(String text, String sender) {
        super(text, sender);
    }

    @Override
    public void Print_Content() {
        User temp = Database.LoadUser(getSender());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        System.out.println("=========================================");
        System.out.println(temp.getFirstname() + " " + temp.getLastname());
        System.out.println("-----------------------------------------");
        System.out.println(getText());
        System.out.println("-----------------------------------------");
        System.out.println(getTime().format(formatter));
        System.out.println("=========================================");
    }

    public ArrayList<String> getTagged() {
        return tagged;
    }

    public void setTagged(ArrayList<String> tagged) {
        this.tagged = tagged;
    }

    public String getContent() {
        return getText();
    }
}
