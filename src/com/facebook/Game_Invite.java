package com.facebook;

import java.io.Serializable;
import java.util.List;

public class Game_Invite implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String filepath;
    private String game;

    Game_Invite(String game, String path, String sender) {
        this.game = game;
        filepath = path;
        this.sender = sender;
    }

    public String getGame() {
        return game;
    }

    public String getFilepath() {
        return filepath;
    }

    public void Print_Invite() {

        System.out.println("Invite from: " + Main.Get_Fullname(sender));
        System.out.println("Game: " + game);
    }
}
