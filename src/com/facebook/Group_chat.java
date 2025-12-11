package com.facebook;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Group_chat extends Chat implements Serializable {
    private static final long serialVersionUID = 1L;
    private String groupname;
    private String groupdescription;
    private ArrayList<String> members;
    private String creator;

    public Group_chat(String groupname, String groupdescription, ArrayList<String> members) {
        this.creator = Main.current.getCredentials().getUsername();
        this.groupname = groupname;
        this.groupdescription = groupdescription;
        this.members = new ArrayList<>(members);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        filepathinitilize(groupname + creator + timestamp);
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGroupdescription() {
        return groupdescription;
    }

    public void setGroupdescription(String groupdescription) {
        this.groupdescription = groupdescription;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isMember(String username) {
        return members.contains(username);
    }

    public void addMember(String username) {
        if (!members.contains(username)) {
            members.add(username);
        }
    }

    public void removeMember(String username) {
        members.remove(username);
    }

    @Override
    public void Print_Chat_Outside() {
        System.out.println(groupname);
    }

    @Override
    public void filepathinitilize(String path) {
        setFolder_path(path);
    }

    public String getGroupName() {
        return groupname;
    }

}
