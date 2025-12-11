package com.facebook;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    public static User current;

    public static void main(String[] args) {
        test_data();
        if (Database.Check_Database()) {
            System_Start();
        }
    }

    public static Gender Input_Gender() {
        while (true) {
            System.out.print("Enter Gender (MALE/FEMALE): ");
            String gender = scanner.next();
            if (gender.equals("MALE") || gender.equals("FEMALE")) {
                return Gender.valueOf(gender);
            }
            System.out.println("Invalid Gender");
        }
    }

    public static void System_Start() {
        current = null;
        while (true) {
            if (Page.login_page()) {
                Database.Write_Online();
                Page.home_page();
            } else
                return;
        }
    }

    public static void test_data() {
        // testing data
        User one = new User("creamy", "man", LocalDate.now(), "...", new Credentials("creamyxo", "12345678"),
                Gender.MALE);
        User two = new User("mike", "tyson", LocalDate.now(), "...", new Credentials("mikey123", "12345678"),
                Gender.MALE);
        User three = new User("asad", "awan", LocalDate.now(), "...", new Credentials("asad-awan", "12345678"),
                Gender.MALE);
        User four = new User("shan", "snake", LocalDate.now(), "...", new Credentials("Shansnake", "12345678"),
                Gender.MALE);
        Database.Write_new_account(one);
        Database.Write_new_account(two);
        Database.Write_new_account(three);
        Database.Write_new_account(four);
        Main.current = one;
        Database.WriteFriend("asad-awan");
        Database.WriteFriend("mikey123");
        Database.WriteFriend("Shansnake");
    }

    public static boolean Yes_or_No(String n) {
        char choice = ' ';
        System.out.print("Are u sure you want to " + n + " (Y/N): ");
        choice = scanner.next().charAt(0);
        return (choice == 'Y' || choice == 'y');
    }

    public static void Print_Friend_Requests(ArrayList<String> requests) {
        if (requests.isEmpty()) {
            System.out.println("NO Friend Requests");
            return;
        }
        for (int i = 0; i < requests.size(); i++) {
            User requester = Database.LoadUser(requests.get(i));
            System.out.print((i + 1) + " ");
            requester.Print_profile();
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
        }
    }

    public static void Load_Recieved_Friend_Requestlist() {
        List<String> requests;
        while (true) {
            requests = Database.Load_Requests_Recieved();
            System.out.println("=========================================");
            System.out.println("        RECEIVED FRIEND REQUESTS");
            System.out.println("=========================================");
            Print_Friend_Requests((ArrayList<String>) requests);
            System.out.println("=========================================");
            System.out.println("1- Accept friend Request");
            System.out.println("2- Delete friend Request");
            System.out.println("0- Return to Profile");
            switch (Input_Int("Choice")) {
                case 1 -> {
                    int index = Input_Int("Index");
                    if (index < 1 || index > requests.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        String username = requests.get(--index);
                        User temp = Database.LoadUser(username);
                        temp.Print_profile();
                        System.out.println("--------------------------------------------");
                        if (Yes_or_No("Accept friend request?")) {
                            Database.WriteFriend(username);
                            Database.Delete_FriendRequest_Recieved(username);
                            System.out.println("Friend Added Successfully!");
                        }
                    }
                }
                case 2 -> {
                    int index = Input_Int("Index");
                    if (index < 1 || index > requests.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        int dex = --index;
                        String username = requests.get(dex);
                        User temp = Database.LoadUser(username);
                        temp.Print_profile();
                        if (Yes_or_No("Delete friend request?")) {
                            Database.Delete_FriendRequest_Recieved(username);
                            System.out.println("Friend request Deleted Successfully!");
                        }
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Load_Sent_Friend_Requestlist() {
        while (true) {
            ArrayList<String> requests = Database.Load_Requests_Sent();
            System.out.println("=========================================");
            System.out.println("        SENT FRIEND REQUESTS");
            System.out.println("=========================================");
            Print_Friend_Requests(requests);
            System.out.println("=========================================");
            System.out.println("1- Cancel friend Request");
            System.out.println("0- Return to Profile");
            switch (Input_Int("Choice")) {
                case 1 -> {
                    int index = Input_Int("Index");
                    if (index < 1 || index > requests.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        String username = requests.get(--index);
                        User temp = Database.LoadUser(username);
                        temp.Print_profile();
                        if (Yes_or_No("Cancel friend request?")) {
                            Database.Delete_FriendRequest_Sent(username);
                            System.out.println("Friend request Cancelled Successfully!");
                        }
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static Credentials Input_Credentials() {
        return new Credentials(Input_Username("User Name"), Input_Password("Password"));
    }

    public static String Input_Username(String n) {
        String u;
        while (true) {
            u = Input_String(n).trim();

            if (u.length() < 8 || u.length() > 12) {
                System.out.println("Username must be 8–12 characters long.");
                continue;
            }
            if (u.contains(" ")) {
                System.out.println("No spaces allowed in username!");
                continue;
            }
            if (Username_Already_Exists(u)) {
                System.out.println("Username already exists!");
            } else {
                return u;
            }
        }
    }

    public static boolean Username_Already_Exists(String u) {
        User temp = Database.LoadUser(u);
        return temp != null;
    }

    public static String Input_Password(String n) {
        String p, c;
        while (true) {
            p = Input_String(n).trim();
            if (p.length() < 8 || p.length() > 15) {
                System.out.println("Password must be at 8 to 15 characters long.");
                continue;
            }
            if (p.contains(" ")) {
                System.out.println("Password cannot contain spaces.");
                continue;
            }
            c = Input_String("Confirm Password");
            if (p.equals(c)) {
                return p;
            } else {
                System.out.println("Passwords don't Match");
            }
        }
    }

    public static User Input_User() {
        String f_n = Main.Input_String("First Name");
        String l_n = Main.Input_String("Last Name");
        Gender gender = Input_Gender();
        System.out.println("Date of Birth");
        LocalDate date = Input_date();
        String bio = Main.Input_String("Bio");
        Credentials credentials = Input_Credentials();

        return new User(f_n, l_n, date, bio, credentials, gender);
    }

    public static LocalDate Input_date() {
        LocalDate date;
        while (true) {
            try {
                date = LocalDate.of(Main.Input_Int("Year"), Main.Input_Int("Month"), Main.Input_Int("Day"));
                return date;
            } catch (Exception e) {
                System.out.println("Invlaid Input of Date!");
            }
        }
    }

    public static String Input_String(String n) {
        String alpha = "";
        do {
            try {
                System.out.print("Enter " + n + " : ");
                alpha = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Exception occurred");
                scanner.nextLine();
            }

        } while (alpha.isBlank());
        return alpha;
    }

    public static Message Input_Message() {
        String sender = Main.current.getFirstname();
        return new Message(Input_String("Message"), sender);
    }

    public static Comment Input_Comment() {
        String sender = Main.current.getFirstname() + " " + Main.current.getLastname();
        return new Comment(Input_String("Comment"), sender);
    }

    public static int Input_Int(String n) {
        int a;
        while (true) {
            try {
                System.out.print("Enter " + n + " : ");
                a = scanner.nextInt();
                break;
            } catch (Exception e) {
                System.out.println("Invalid Input! Enter a Valid Integer");
                scanner.nextLine();
            }
        }
        scanner.nextLine();
        return a;
    }

    public static User Find_Friend_ByNAME(String name) {
        User temp;
        ArrayList<String> friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
        ArrayList<User> matches = new ArrayList<>();
        for (String username : friends) {
            temp = Database.LoadUser(username);
            String fullname = temp.getFirstname() + " " + temp.getLastname();
            if (temp.getFirstname().equalsIgnoreCase(name) || temp.getLastname().equalsIgnoreCase(name)
                    || fullname.equalsIgnoreCase(name)) {
                matches.add(temp);
            }
        }
        if (matches.isEmpty())
            return null;
        if (matches.size() == 1)
            return matches.getFirst();
        while (true) {
            System.out.println(matches.size() + " Matches Found!");
            System.out.println("=============================================");
            for (int i = 0; i < matches.size(); i++) {
                System.out.print((i + 1) + " ");
                matches.get(i).Print_profile();
            }
            System.out.println("=============================================");
            System.out.println("1- Chose Friend");
            System.out.println("2- Cancel Search");
            switch (Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Index of friend to remove");
                    if (index < 1 || index > matches.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        return matches.get(--index);
                    }
                }
                case 2 -> {
                    return null;
                }
            }
        }
    }

    public static void Password_change() {
        String u, p, c;
        u = Main.Input_String("Username");
        if (Username_Already_Exists(u)) {
            User temp = Database.LoadUser(u);
            System.out.println("Prove Your Identity!");
            String f_n = Main.Input_String("First Name");
            String l_n = Main.Input_String("Last Name");
            System.out.println("Date of Birth");
            LocalDate date = LocalDate.of(Main.Input_Int("Year"), Main.Input_Int("Month"), Main.Input_Int("Day"));
            if (temp.getFirstname().equalsIgnoreCase(f_n) && temp.getLastname().equalsIgnoreCase(l_n)
                    && temp.getBirth().equals(date)) {
                System.out.println("Identity Proved");
                for (int i = 0; i < 3; i++) {
                    p = Main.Input_Password("NEW Password");
                    c = Main.Input_Password("CONFIRM NEW Password");
                    if (p.equals(c)) {
                        temp.getCredentials().setPassword(p);
                        System.out.println("Password Changed Successfully");
                        return;
                    } else
                        System.out.println("Passwords don't Match!");
                }
                System.out.println("Password Change Unsuccesfull!");
            } else {
                System.out.println("You could not Prove Your Identity! Try Again Later");
            }
        } else {
            System.out.println("Invalid Username!");
        }
    }

    public static void Login() {
        String u, p;
        u = Main.Input_String("Username");
        if (Username_Already_Exists(u)) {
            User temp = Database.LoadUser(u);
            for (int i = 3; i > 0; i--) {
                p = Main.Input_String("Password");
                if (temp.getCredentials().p_Verify(p)) {
                    Main.current = temp;
                    return;
                }
                System.out.println("Invalid Password!");
                System.out.println("Attempts Remaining :" + (i - 1));
            }
        } else {
            System.out.println("Invalid Username!");
        }
    }

    public static void Create_Account() {
        System.out.println("---------------------------------------------");
        System.out.println("             Account Creation");
        System.out.println("---------------------------------------------");
        User temp = Main.Input_User();
        Database.Write_new_account(temp);
    }

    public static void Print_Friends_List(List<String> friendo) {
        if (friendo.isEmpty()) {
            System.out.println("\t\t\t\tNo Friends Yet");
        } else {
            int i = 0;
            for (String f : friendo) {
                User temp = Database.LoadUser(f);
                System.out.print((i + 1) + " ");
                temp.Print_profile();
                System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
                i++;
            }
        }
    }

    public static void Print_Entire_Chat(ArrayList<Message> messages) {
        for (Message message : messages) {
            message.Print_Content();
            System.out.println("----------------------------------------------------");
        }
    }

    public static void Print_Inbox(ArrayList<Chat> inbox) {
        if (inbox.isEmpty()) {
            System.out.println("Inbox is Empty");
            return;
        }
        for (int i = 0; i < inbox.size(); i++) {
            System.out.print((i + 1) + " ");
            inbox.get(i).Print_Chat_Outside();
            System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
        }
    }

    public static void Print_Post(Post post) {
        post.Print_Content();
        ArrayList<String> likes = Database.Load_Post_Likes(post);
        ArrayList<Comment> comments = Database.Load_Post_Comments(post);
        System.out.println("Likes: " + likes.size() + " | Comments: " + comments.size());
    }

    public static void Print_Posts(ArrayList<Post> posts) {
        if (posts.isEmpty()) {
            System.out.println("You haven't posted anything yet!");
            return;
        }
        for (int i = 0; i < posts.size(); i++) {
            System.out.println("Index = " + (i + 1));
            Print_Post(posts.get(i));
            System.out.println("- - - - - - - - - - - - - - - - - - - - - - - -");
        }
    }

    public static void Print_Comments(ArrayList<Comment> comments) {
        if (comments.isEmpty()) {
            System.out.println("No comments to delete!");
            return;
        }
        for (int i = 0; i < comments.size(); i++) {
            System.out.print((i + 1) + " ");
            comments.get(i).Print_Content();
            System.out.println("- - - - - - - - - - - - - - - - - - - - - -- - - - - - - - - - - --");
        }
    }

    public static void Print_Notifications(ArrayList<Notification> notifications) {
        for (int i = 0; i < notifications.size(); i++) {
            notifications.get(i).Print_Notificaton();
            System.out.println("----------------------------------------------------");
        }
    }

    public static String Get_Fullname(String username) {
        User user = Database.LoadUser(username);
        return user.getFullName();
    }

    public static void Input_Post() {
        String content = Input_String("Post content");
        String curr = Main.current.getCredentials().getUsername();
        ArrayList<String> tagged = new ArrayList<>();
        while (true) {
            System.out.println("=========================================");
            System.out.println("            CREATE POST");
            System.out.println("=========================================");
            System.out.println(content);
            if (!tagged.isEmpty()) {
                System.out.println("Tagged");
                Print_Friends_List(tagged);
            }
            System.out.println();
            System.out.println("=========================================");
            System.out.println("             1- Post");
            System.out.println("             2- Change content");
            System.out.println("             3- Tag a Friend");
            System.out.println("             4- Remove a tag");
            System.out.println("             0- Cancel");
            System.out.println("=========================================");
            switch (Input_Int("Choice")) {
                case 1 -> {
                    Post post = new Post(content, curr);
                    post.setTagged(tagged);
                    String path = Database.Write_Post(post);
                    Database.WriteFeed(path, curr, post);
                    Add_in_Feed(tagged, path, post, true);
                    System.out.println("Posted!");
                    boolean running = true;
                    while (running) {
                        running = false;
                        System.out.println("=========================================");
                        System.out.println("Chose whom do you want the post to see?");
                        System.out.println("1- Friends only");
                        System.out.println("2- Friends of Friends ");
                        System.out.println("3- Everyone");
                        System.out.println("=========================================");
                        switch (Input_Int("Choice")) {
                            case 1 -> {
                                Add_in_Feed(Database.Load_Friends(curr), path, post, false);
                            }
                            case 2 -> {
                                Add_in_Feed(Database.Load_everyone(2), path, post, false);
                            }
                            case 3 -> {
                                Add_in_Feed(Database.Load_everyone(6), path, post, false);
                            }
                            default -> {
                                System.out.println("Invalid Choice!");
                                running = true;
                            }
                        }
                    }
                    return;
                }
                case 2 -> {
                    content = Input_String("Post content");
                }
                case 3 -> {
                    String user = Page.Chose_Friendo();
                    if (user != null) {
                        tagged.add(user);
                        System.out.println("Friend Added");
                    }
                }
                case 4 -> {
                    int index = Input_Int("Index");
                    if (index < 1 || index > tagged.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        tagged.remove(--index);
                        System.out.println("Tagged friend removed");
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Add_in_Feed(List<String> friends, String path, Post post, boolean tagged) {
        for (String f : friends) {
            Database.WriteFeed(path, f, post);
            if (tagged) {
                Database.Write_Notification(f, Input_NotificationT());
            }
        }
    }

    public static Notification Input_NotificationT() {
        return new Notification(Notification.Type.TAG, current.getFullName() + " Tagged you in a post");
    }

    public static Notification Input_NotificationM() {
        return new Notification(Notification.Type.MESSAGE, current.getFullName() + " Messaged you");
    }

    public static Notification Input_NotificationC() {
        return new Notification(Notification.Type.COMMENT, current.getFullName() + " Commented on ur post");
    }

    public static Notification Input_NotificationL() {
        return new Notification(Notification.Type.LIKE, current.getFullName() + " Liked your post");
    }

    public static ArrayList<Game> Get_ALL_games() {
        ArrayList<Game> games = new ArrayList<>();
        games.add(new TicTacToe());
        games.add(new Hangman());
        games.add(new SnakeGame());
        return games;
    }

    public static void Print_Games(ArrayList<Game> games) {
        for (int i = 0; i < games.size(); i++) {
            System.out.println((i + 1));
            games.get(i).Print_Game_data();
            System.out.println("-----------------------------------------");
        }
    }

    public static void Print_Game_Invites(ArrayList<Game_Invite> invites) {
        for (int i = 0; i < invites.size(); i++) {
            System.out.println((i + 1));
            invites.get(i).Print_Invite();
            System.out.println("-----------------------------------------");
        }
    }

}