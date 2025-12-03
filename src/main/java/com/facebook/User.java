package com.facebook;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User implements Serializable {

    private String Firstname;
    private String Lastname;
    private LocalDate birth;
    private String bio;
    private Credentials credentials;
    private boolean Privacy;

    public User(String firstname, String lastname, LocalDate birth, String bio, Credentials credentials) {
        Firstname = firstname;
        Lastname = lastname;
        this.birth = birth;
        this.bio = bio;
        this.credentials = credentials;
        Privacy = false;
    }

    public void Print_profile(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String format = birth.format(formatter);
        System.out.println(Firstname+" "+ Lastname);
        System.out.println("Date of Birth: "+format);
        System.out.println("BIO: "+bio);
        if(!Privacy){
            System.out.println(Database.Check_Online(credentials.getUsername())? "Online 🟢": "Offline 🔴");
        }
    }

    public String getFullName(){
        return Firstname+" "+Lastname;
    }

    public String getFirstname() {
        return Firstname;
    }

    public void setFirstname(String firstname) {
        Firstname = firstname;
    }

    public String getLastname() {
        return Lastname;
    }

    public void setLastname(String lastname) {
        Lastname = lastname;
    }

    public LocalDate getBirth() {
        return birth;
    }

    public void setBirth(LocalDate birth) {
        this.birth = birth;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void Privacy_Mode_On(){
        Privacy = true;
        System.out.println("Privacy Mode Turned ON Successfully!");
    }

    public void Privacy_Mode_OFF(){
        Privacy = false;
        System.out.println("Privacy Mode Turned OFF Successfully!");
    }

    public boolean getPrivacy() {
        return Privacy;
    }

}
