package com.facebook;

import java.io.Serializable;

public class DM_chat extends Chat implements Serializable {
    private static final long serialVersionUID = 1L;
    private String R_username;

    public DM_chat(String sender, String reciever) {
        this.R_username = reciever;
        filepathinitilize(sender);
    }

    public void Print_Chat_Outside() {
        User temp = Database.LoadUser(R_username);
        System.out.println(temp.getFirstname() + " " + temp.getLastname());
    }

    @Override
    public void filepathinitilize(String sender) {
        setFolder_path(Database.Alphabetizefilename(sender, R_username));
    }

    public String getR_username() {
        return R_username;
    }

    public void setR_username(String r_username) {
        this.R_username = r_username;
    }
}
