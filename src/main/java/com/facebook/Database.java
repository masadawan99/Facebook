package com.facebook;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class Database {

    private static final File Dadyfolder = new File("G:\\Shared drives\\Facebook\\Database");
    private static final File Userfolder= new File(Dadyfolder,"Users");
    private static final File Inboxfolder= new File(Dadyfolder,"Inboxes");
    private static final File Messagesfolder = new File(Dadyfolder,"Messages");
    private static final File Friendsfolder = new File(Dadyfolder,"Friends");
    private static final File Postsfolder = new File(Dadyfolder,"Posts");
    private static final File Notificationsfolder = new File(Dadyfolder, "Notifications");
    private static final File Feedsfolder = new File(Dadyfolder, "Feeds");
    private static final File Onlinefolder = new File(Dadyfolder, "Online");

    private static final File FriendRequestsSentfolder = new File(Friendsfolder,"FriendRequestsSent");
    private static final File FriendRequestsRecievedfolder = new File(Friendsfolder,"FriendRequestsRecieved");
    private static ArrayList<Notification> read;
    private static ArrayList<Notification> unread;

    private Database() {
    }

    public static boolean Check_Database(){
        if(Dadyfolder.exists()) return true;
        else System.out.println("Database cannot be Found! \n\t._. ERROR 404 ._.");
        return false;
    }

    public static boolean Check_Online(String username){
        File file = new File(Onlinefolder,username);
        return file.exists();
    }

    public static void Delete_Online(){
        File file = new File(Onlinefolder,Main.current.getCredentials().getUsername());
        file.delete();
    }

    public static void Write_Online(){
        String curr = Main.current.getCredentials().getUsername();
        File file = new File(Onlinefolder,curr);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(curr);
        } catch (Exception s){
            System.out.println("Error Writing comment: " + s.getMessage());
        }
    }

    public static void Write_Comment(Post post, Comment comment){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File commentsFolder = new File(postFolder, "Comments");
        if (!commentsFolder.exists()) {
            commentsFolder.mkdirs();
        }
        String commentTimestamp = safeTimestamp(comment.getTime());
        File file = new File(commentsFolder, commentTimestamp+comment.getSender());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(comment);
        } catch (Exception s){
            System.out.println("Error Writing comment: " + s.getMessage());
        }
    }

    public static void Delete_Comment(Post post, Comment comment){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File commentsFolder = new File(postFolder, "Comments");
        String commentTimestamp = safeTimestamp(comment.getTime());
        File commentFile = new File(commentsFolder, commentTimestamp+comment.getSender());
        commentFile.delete();
    }

    public static void Write_Like(Post post){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File likesFolder = new File(postFolder, "Likes");
        if (!likesFolder.exists()) {
            likesFolder.mkdirs();
        }

        File file = new File(likesFolder, Main.current.getCredentials().getUsername());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(Main.current.getCredentials().getUsername());
        } catch (Exception s){
            System.out.println("Error Writing like: ");
        }
    }

    public static String Alphabetizefilename(String a, String b){
        if (a.compareTo(b) < 0) {
            return a +"-"+ b;
        } else {
            return b +"-"+ a;
        }
    }

    public static void Create_Post_fldr(String username){
        File folder = new File(Postsfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_Notifications_fldr(String username){
        File folder = new File(Notificationsfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_Feeds_fldr(String username){
        File folder = new File(Feedsfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_Inbox_fldr(String username){
        File folder = new File(Inboxfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_Friends_fldr(String username){
        File folder = new File(Friendsfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_FriendRequestsRecieved_fldr(String username){
        File folder = new File(FriendRequestsRecievedfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Create_FriendRequestsSent_fldr(String username){
        File folder = new File(FriendRequestsSentfolder,username);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void Write_new_account(User user){
        String username = user.getCredentials().getUsername();
        WriteUser(user);
        Create_Inbox_fldr(username);
        Create_Friends_fldr(username);
        Create_FriendRequestsRecieved_fldr(username);
        Create_FriendRequestsSent_fldr(username);
        Create_Post_fldr(username);
        Create_Notifications_fldr(username);
        Create_Feeds_fldr(username);
    }

    public static void WriteUser(User user){
        File file = new File(Userfolder, user.getCredentials().getUsername()+".dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(user);
        }catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteFriendRequestSent(String reciever){
        File file = new File(FriendRequestsSentfolder, Main.current.getCredentials().getUsername());
        File cfile = new File(file,reciever);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cfile))) {
            out.writeObject(reciever);
        }catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteFriendRequestRecieved(String reciever){
        File file = new File(FriendRequestsRecievedfolder, reciever);
        File cfile = new File(file,Main.current.getCredentials().getUsername());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cfile))) {
            out.writeObject(Main.current.getCredentials().getUsername());
        }catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteChat(String username, Chat chat){
        File folder = new File( Inboxfolder, username );
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder,chat.getFolder_path()+".dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(chat);
        } catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteFriend(String username){
        File folder = new File( Friendsfolder, Main.current.getCredentials().getUsername() );

        File file = new File(folder,username);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(username);
        } catch (Exception s){
            System.out.println("Error Writing file");
        }
        WriteFriendinFriend(username);
    }

    public static void WriteFriendinFriend(String username){
        File folder = new File( Friendsfolder, username);

        File file = new File(folder,Main.current.getCredentials().getUsername());
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(Main.current.getCredentials().getUsername());
        } catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteMessage(String foldern, Message message) {
        File folder = new File(Messagesfolder , foldern);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String timestamp = safeTimestamp(message.getTime());
        File file = new File(folder, timestamp + message.getSender());

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(message);
        } catch (Exception s){
            System.out.println("Error Writing file");
        }
    }

    public static void WriteFeed(String path,String username,Post post) {
        String timestamp = safeTimestamp(post.getTime());
        File folder = new File(Feedsfolder,username);
        File  file = new File(folder, timestamp+post.getSender());
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(file))) {
            o.writeObject(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String Write_Post(Post post){
        String path = "";
        File userFolder = new File(Postsfolder,post.getSender());
        String timestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, timestamp);
        if (!postFolder.exists()) {
            postFolder.mkdirs();
        }
        File file = new File(postFolder, "post.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(post);
            path = file.getCanonicalPath();
        } catch (Exception s){
            System.out.println("Error Writing file: " + s.getMessage());
        }
        File commentsFolder = new File(postFolder, "Comments");
        File likesFolder = new File(postFolder, "Likes");
        commentsFolder.mkdirs();
        likesFolder.mkdirs();
        return path;
    }

    public static User LoadUser(String username){
        File file = new File(Userfolder,username + ".dat");
        if(file.exists()){
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                User user = ((User) in.readObject());
                return user;
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
                return null;
            }
        }else return null;
    }

    public static ArrayList<User> LoadUsers() {
        ArrayList<User> users = new ArrayList<>();
        File[] files = Userfolder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return users;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                users.add((User) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return users;
    }

    public static ArrayList<String> Load_Requests_Recieved(){
        File folder =  new File(FriendRequestsRecievedfolder, Main.current.getCredentials().getUsername());
        ArrayList<String> Friend_requests = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return Friend_requests;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Friend_requests.add((String) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return Friend_requests;
    }

    public static ArrayList<String> Load_Requests_Sent(){
        File folder =  new File(FriendRequestsSentfolder, Main.current.getCredentials().getUsername());
        ArrayList<String> Friend_requests = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return Friend_requests;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Friend_requests.add((String) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return Friend_requests;
    }

    public static ArrayList<String> Load_Friends(String username){
        File folder =  new File(Friendsfolder, username);
        ArrayList<String> friends = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return friends;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                friends.add((String)in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return friends;
    }

    public static HashSet<String> Load_Friends_Hash(String username){
        File folder =  new File(Friendsfolder, username);
        HashSet<String> friends = new HashSet<>();
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return friends;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                friends.add((String)in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return friends;
    }

    public static ArrayList<Post> Load_Feed(){
        ArrayList<Post> posts = new ArrayList<>();
        File folder = new File(Feedsfolder,Main.current.getCredentials().getUsername());
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if(files==null) return posts;
        for (int i = files.length - 1; i >= 0; i--) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[i]))) {
                String post = (String) in.readObject();
                try (ObjectInputStream en = new ObjectInputStream(new FileInputStream(post))) {
                    posts.add((Post) en.readObject());
                } catch (Exception e) { }
            } catch (Exception e) { }
        }
        return posts;
    }

    public static ArrayList<Post> Load_User_Posts(String username){
        ArrayList<Post> posts = new ArrayList<>();
        File folder = new File(Postsfolder, username);
        if (!folder.exists()) {
            return posts;
        }
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return posts;
        }
        for (int i = files.length - 1; i >= 0; i--) {
            File post = new File(files[i],"post.dat");
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(post))) {
                posts.add((Post) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading post: " + post.getName());
            }
        }
        return posts;
    }

    public static ArrayList<String> Load_Post_Likes(Post post){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File likesFolder = new File(postFolder, "Likes");
        if (!likesFolder.exists()) {
            return new ArrayList<>();
        }
        File[] files = likesFolder.listFiles((dir, name) -> !name.endsWith(".ini"));
        ArrayList<String> likes = new ArrayList<>();
        if (files == null) {
            return likes;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                likes.add((String) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading like: " + file.getName());
            }
        }
        return likes;
    }

    public static ArrayList<Comment> Load_Post_Comments(Post post){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File commentsFolder = new File(postFolder, "Comments");
        if (!commentsFolder.exists()) {
            return new ArrayList<>();
        }
        File[] files = commentsFolder.listFiles((dir, name) -> !name.endsWith(".ini"));
        ArrayList<Comment> comments = new ArrayList<>();
        if (files == null) {
            return comments;
        }
        for (int i = files.length - 1; i >= 0; i--) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[i]))) {
                comments.add((Comment) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading comment: " + files[i].getName());
            }
        }
        return comments;
    }

    public static ArrayList<Chat> LoadInbox() {
        File folder =  new File(Inboxfolder, Main.current.getCredentials().getUsername());
        ArrayList<Chat> chats = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) {
            return chats;
        }
        for (File file : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                chats.add((Chat) in.readObject());
            } catch (Exception e) {
                System.out.println("Error reading file: " + file.getName());
            }
        }
        return chats;
    }

    public static void Delete_Chat(String path){
        File file = new File(Inboxfolder,Main.current.getCredentials().getUsername());
        new File(file,path+".dat").delete();
    }

    private static void Delete_F_Request_Sent(String username1,String username2){
        File folder = new File(FriendRequestsSentfolder, username1);
        File sent = new File(folder, username2);
        sent.delete();
    }

    public static void Delete_F_Request_Recieved(String username1,String username2){
        File folder = new File(FriendRequestsRecievedfolder, username1);
        File recieved = new File(folder,username2);
        recieved.delete();
    }

    public static void Delete_FriendRequest_Recieved(String username){
        Delete_F_Request_Recieved(Main.current.getCredentials().getUsername(),username);
        Delete_F_Request_Sent(username,Main.current.getCredentials().getUsername());
    }

    public static void Delete_FriendRequest_Sent(String username){
        Delete_F_Request_Recieved(username,Main.current.getCredentials().getUsername());
        Delete_F_Request_Sent(Main.current.getCredentials().getUsername(),username);
    }

    public static void Delete_Friend(String u){
        File folder = new File(Friendsfolder,Main.current.getCredentials().getUsername());
        File file = new File(folder,u);
        file.delete();

        File otherFolder = new File(Friendsfolder, u);
        File otherFile = new File(otherFolder, Main.current.getCredentials().getUsername());
        otherFile.delete();
    }

    public static boolean Already_Friend(User user){
        String username = user.getCredentials().getUsername();
        File folder = new File(Friendsfolder, Main.current.getCredentials().getUsername());
        File file = new File(folder,username);
        return file.exists();
    }

    public static boolean F_Request_Already_sent(String username){
        File folder = new File(FriendRequestsSentfolder, Main.current.getCredentials().getUsername());
        File file = new File(folder,username);
        return file.exists();
    }

    public static void Friend_Request_Sent(String username){
        WriteFriendRequestSent(username);
        WriteFriendRequestRecieved(username);
    }

    public static String safeTimestamp(LocalDateTime time){
        return time.toString().replace(":", "-").replace(".", "-");
    }

    public static boolean Has_Liked(Post post){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File likesFolder = new File(postFolder, "Likes");
        if (!likesFolder.exists()) {
            return false;
        }
        File likeFile = new File(likesFolder, Main.current.getCredentials().getUsername());
        return likeFile.exists();
    }

    public static void Remove_Like(Post post){
        File userFolder = new File(Postsfolder, post.getSender());
        String postTimestamp = safeTimestamp(post.getTime());
        File postFolder = new File(userFolder, postTimestamp);
        File likesFolder = new File(postFolder, "Likes");
        File likeFile = new File(likesFolder, Main.current.getCredentials().getUsername());
        likeFile.delete();
    }

    public static void Delete_frnd_Filz( ArrayList<String> friends, File file){
        String u = Main.current.getCredentials().getUsername();
        for(String f: friends){
            File frndF = new File(file,f);
            new File(frndF,u).delete();
        }
    }

    public static void Delete_Acc(){

        String u = Main.current.getCredentials().getUsername();
        new File(Userfolder, u + ".dat").delete();

        Delete_frnd_Filz(Load_Friends(u),Friendsfolder);
        Delete_frnd_Filz(Load_Requests_Recieved(),FriendRequestsRecievedfolder);
        Delete_frnd_Filz(Load_Requests_Sent(),FriendRequestsSentfolder);
        Delete_Online();
        deleteFolderRecursive(new File(Friendsfolder, u));
        deleteFolderRecursive(new File(FriendRequestsRecievedfolder, u));
        deleteFolderRecursive(new File(FriendRequestsSentfolder, u));
        deleteFolderRecursive(new File(Postsfolder, u));
        deleteFolderRecursive(new File(Inboxfolder, u));
        deleteFolderRecursive(new File(Notificationsfolder, u));
    }

    private static void deleteFolderRecursive(File folder){
        if (!folder.exists()) return;
        File[] files = folder.listFiles();
        if (files != null){
            for (File f : files){
                if (f.isDirectory()) deleteFolderRecursive(f);
                else f.delete();
            }
        }
        folder.delete();
    }

    public static ArrayList<Message> Load_ALLMessages(String foldern){
        File folder = new File(Messagesfolder,foldern);
        if (!folder.exists()) {
            System.out.println("Inbox folder not found!");
            return new ArrayList<>();
        }
        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        ArrayList<Message> msgs = new ArrayList<>();
        if (files == null || files.length == 0) {
            System.out.println("No .dat files found.");
            return new ArrayList<>();
        }
        for (int i = 0; i < files.length; i++) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[i]))) {
                msgs.add((Message) in.readObject());
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error reading file: " + files[i].getName());
            }
        }

        return msgs;
    }

    public static ArrayList<Message> Load_New_Messages(String foldern, int size) {
        File folder = new File(Messagesfolder,foldern);

        File[] files = folder.listFiles((dir, name) -> !name.endsWith(".ini"));
        ArrayList<Message> msgs = new ArrayList<>();
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        if (files.length == size) {
        }else if(files.length > size) {
            for (int i = size; i < files.length ; i++) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[i]))) {
                    msgs.add((Message) in.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error reading file: " + files[i].getName());
                }
            }
        }
        return msgs;
    }

    public static ArrayList<User> Search_Users_By_Name(String name){
        ArrayList<User> allUsers = LoadUsers();
        if(allUsers.isEmpty()){
            return new ArrayList<>();
        }
        ArrayList<User> matchingUsers = new ArrayList<>();
        for(User user : allUsers){
            String fullname = Main.Get_Full_Name(user);
            if(user.getCredentials().getUsername().equals(Main.current.getCredentials().getUsername())) continue;
            if(user.getFirstname().equalsIgnoreCase(name) || user.getLastname().equalsIgnoreCase(name) || fullname.equalsIgnoreCase(name)){
                matchingUsers.add(user);
            }
        }
        return matchingUsers;
    }

    public  static List<String> Mutual_Friends(String username, HashSet<String> main){
        HashSet<String> U_Friends = Database.Load_Friends_Hash(username);
        if(U_Friends.contains(username)) return null;
        main.retainAll(U_Friends);
        return new ArrayList<>(U_Friends);
    }

    public static HashMap<String, List<String>> mutual_frndz(List<String> people){
        HashMap<String, List<String>> mutual = new HashMap<>();
        HashSet<String> main = Database.Load_Friends_Hash(Main.current.getCredentials().getUsername());
        for(String P: people ){
            List<String> mutuals = Mutual_Friends(P,main);
            if(mutuals!=null){
                mutual.putIfAbsent(P,mutuals);
            }
        }
        return mutual;
    }

    public static List<String> Load_Friend_of_Friends(){
        String curr = Main.current.getCredentials().getUsername();
        HashSet<String> friends_of_friends = new HashSet<>();
        HashSet<String> main = Load_Friends_Hash(curr);
        for(String f: main){
            HashSet<String> friends_frndz = Load_Friends_Hash(f);
            for(String frnd: friends_frndz){
                if(frnd.equals(curr)) continue;
                if(!main.contains(frnd)) {
                    if(!friends_of_friends.contains(frnd)){
                        friends_of_friends.add(frnd);
                    }
                }
            }
        }
        return new ArrayList<>(friends_of_friends);
    }

    public static List<String> Sort_by_mutual_count(List<String> people, HashMap<String,List<String>> mutuals){
        for (int i = 0; i < people.size()-1; i++) {
            for (int j = i; j < people.size()-1 - i ; j++) {
                int m_count1 = mutuals.get(people.get(j)).size();
                int m_count2 = mutuals.get(people.get(j+1)).size();
                if(m_count1<m_count2){
                    String temp = people.get(j);
                    people.set(j,people.get(j+1));
                    people.set(j+1,temp);
                }
            }
        }
        return people;
    }

    public static void Write_Notification(String username,Notification notification){
        File userFolder = new File(Notificationsfolder, username);
        if (!userFolder.exists()) userFolder.mkdirs();
        String ts = safeTimestamp(notification.getCreatedAt());
        File file = new File(userFolder, ts  + notification.getSender()+".dat");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(notification);
        } catch (Exception s){
            System.out.println("Error Writing notification");
        }
    }
    public static void Compute_Read_Unread(){
        read  = new ArrayList<>();
        unread = new ArrayList<>();
        File userFolder = new File(Notificationsfolder, Main.current.getCredentials().getUsername());
        File[] files = userFolder.listFiles((dir, name) -> !name.endsWith(".ini"));
        if (files == null) return;
        for (int i = files.length - 1; i >= 0; i--) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(files[i]))) {
                Notification n = (Notification) in.readObject();
                if (!n.isRead()) unread.add(n);
                else read.add(n);
            } catch (Exception e){
                System.out.println("Error reading notification: " + files[i].getName());
            }
        }
    }

    public static ArrayList<Notification> Load_Unread_Notification(){
        if(unread.isEmpty()) return new ArrayList<>();
        else return unread;
    }

    public static ArrayList<Notification> Load_Read_Notifications(){
        if(read.isEmpty()) return new ArrayList<>();
        else return read;
    }

    public static void Mark_All_Notifications_Read(){
        File userFolder = new File(Notificationsfolder, Main.current.getCredentials().getUsername());
        for (Notification r: unread){
            String ts = safeTimestamp(r.getCreatedAt());
            File file = new File(userFolder, ts  + r.getSender()+".dat");
            r.markRead();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
                out.writeObject(r);
            } catch (Exception s){
                System.out.println("Error Writing notification");
            }
        }
    }

    public static void Delete_Notifications(){
        File userFolder = new File(Notificationsfolder, Main.current.getCredentials().getUsername());
        File[] files = userFolder.listFiles();
        if (files == null) return;
        for (File f : files) f.delete();
        read = new ArrayList<>();
        unread = new ArrayList<>();
    }

}
