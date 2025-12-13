package com.facebook;

import java.util.*;

public class Page {

    public static boolean login_page() {
        Main.current = null;
        while (true) {
            System.out.println("__________________________________________");
            System.out.println("          FACEBOOK LOGIN PAGE");
            System.out.println("__________________________________________");
            System.out.println("             1- LOGIN");
            System.out.println("             2- SIGNUP");
            System.out.println("             3- FORGOT PASSWORD");
            System.out.println("             0- Exit");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Main.Login();
                    if (Main.current != null)
                        return true;
                }
                case 2 -> {
                    Main.Create_Account();
                }
                case 3 -> {
                    Main.Password_change();
                }
                case 0 -> {
                    return false;
                }
                default -> System.out.println("Invalid Choice! Enter a Valid Choice");
            }
        }
    }

    public static void profile_page() {
        while (true) {
            System.out.println("Logged in as: " + Main.current.getCredentials().getUsername());
            System.out.println("__________________________________________");
            System.out.println("                  Profile");
            System.out.println("__________________________________________");
            Main.current.Print_profile();
            System.out.println("-----------------------------------------");
            System.out.println("         1- View Friends");
            System.out.println("         2- View Received Friends Requests");
            System.out.println("         3- View Sent Friends Requests");
            System.out.println("         4- View My Posts");
            System.out.println("         0- Return to HomePage");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    View_Friends_List();
                }
                case 2 -> {
                    Main.Load_Recieved_Friend_Requestlist();
                }
                case 3 -> {
                    Main.Load_Sent_Friend_Requestlist();
                }
                case 4 -> {
                    View_My_Posts();
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void View_Friends_List() {
        ArrayList<String> Friends;
        while (true) {
            System.out.println("Logged in as: " + Main.current.getCredentials().getUsername());
            Friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
            System.out.println("=========================================");
            System.out.println("               FRIENDS LIST");
            System.out.println("=========================================");
            Main.Print_Friends_List(Friends);
            System.out.println("=========================================");
            System.out.println("            1- Find Friends");
            System.out.println("            2- Remove a Friend");
            System.out.println("            3- Vist a Friend's Profile");
            System.out.println("            0- Return to Profile");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Find_Friends();
                }
                case 2 -> {
                    if (Friends.isEmpty()) {
                        System.out.println("No friends to remove!");
                        break;
                    }
                    int index = Main.Input_Int("Index of friend to remove");
                    if (index < 1 || index > Friends.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        String friendUsername = Friends.get(--index);
                        User friend = Database.LoadUser(friendUsername);
                        friend.Print_profile();
                        if (Main.Yes_or_No("remove friend?")) {
                            Database.Delete_Friend_Chat(friendUsername);
                            Database.Delete_Friend(friendUsername);
                            System.out.println("Friend removed successfully!");
                        }
                    }
                }
                case 3 -> {

                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static String Chose_Friendo() {
        while (true) {
            ArrayList<String> Friends = Database.Load_Friends(Main.current.getCredentials().getUsername());
            System.out.println("=========================================");
            System.out.println("               FRIENDS LIST");
            System.out.println("=========================================");
            Main.Print_Friends_List(Friends);
            System.out.println("=========================================");
            System.out.println("            1- Find New Friends");
            System.out.println("            2- Chose Friends in list");
            System.out.println("            3- Search Friends");
            System.out.println("            0- Cancel");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Find_Friends();
                }
                case 2 -> {
                    if (Friends.isEmpty()) {
                        System.out.println("No friends to chose!");
                        break;
                    }
                    int index = Main.Input_Int("Index of friend to chose");
                    if (index < 1 || index > Friends.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        return Friends.get(--index);
                    }
                }
                case 3 -> {
                    User temp = Main.Find_Friend_ByNAME("Friend's Name");
                    if (temp == null) {
                        System.out.println("No such Friend Found");
                        break;
                    }
                    return temp.getCredentials().getUsername();
                }
                case 0 -> {
                    return null;
                }
            }
        }
    }

    public static void People_you_may_know() {
        boolean discover = false;

        while (true) {
            List<String> friend_of_friend;
            HashMap<String, List<String>> mutual_frndz = new HashMap<>();

            code: {
                if (!discover) {
                    friend_of_friend = Database.Load_Friend_of_Friends();
                    mutual_frndz = Database.mutual_frndz(friend_of_friend);
                    friend_of_friend = Database.Sort_by_mutual_count(friend_of_friend, mutual_frndz);
                } else {
                    friend_of_friend = Database.Load_Everyone3_6();
                }

                int pageSize = 20;
                int totalPages = (int) Math.ceil((double) friend_of_friend.size() / pageSize);
                int currentPage = 1;

                while (true) {
                    int startIndex = (currentPage - 1) * pageSize;
                    int endIndex = Math.min(startIndex + pageSize, friend_of_friend.size());

                    System.out.println("=========================================");
                    System.out.println("          ALL USERS");
                    System.out.println("     Page " + currentPage + " of " + totalPages);
                    System.out.println("=========================================");

                    for (int i = startIndex; i < endIndex; i++) {
                        User temp = Database.LoadUser(friend_of_friend.get(i));
                        System.out.print((i - startIndex + 1) + " ");
                        temp.Print_profile();
                        System.out.println("- - - - - - - - - - - - - - - - - - - - - -");

                        if (!discover) {
                            List<String> mutuals = mutual_frndz.get(friend_of_friend.get(i));
                            System.out.println("Mutual Friends: " + (mutuals != null ? mutuals.size() : 0));
                            System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
                        }
                    }

                    System.out.println("=========================================");
                    System.out.println("            1- Next Page");
                    System.out.println("            2- Previous Page");
                    System.out.println("            3- Go to Page");
                    System.out.println("            4- Discover more people");
                    System.out.println("            5- Back to People u may know");
                    System.out.println("            6- Choose person");
                    System.out.println("            0- Return");
                    System.out.println("=========================================");

                    int choice = Main.Input_Int("Choice");

                    switch (choice) {
                        case 1 -> {
                            if (currentPage < totalPages)
                                currentPage++;
                            else
                                System.out.println("Already on last page!");
                        }
                        case 2 -> {
                            if (currentPage > 1)
                                currentPage--;
                            else
                                System.out.println("Already on first page!");
                        }
                        case 3 -> {
                            int pageNum = Main.Input_Int("Page Number");
                            if (pageNum >= 1 && pageNum <= totalPages)
                                currentPage = pageNum;
                            else
                                System.out.println("Invalid page number!");
                        }
                        case 4 -> {
                            if (!discover) {
                                discover = true;
                                break code;
                            } else {
                                System.out.println("Already in Discover mode!");
                            }
                        }
                        case 5 -> {
                            if (discover) {
                                discover = false;
                                break code;
                            } else {
                                System.out.println("Already in People u may know");
                            }
                        }
                        case 6 -> {
                            int dex = Main.Input_Int("User Index (from current page)");
                            if (dex >= 1 && dex <= (endIndex - startIndex)) {
                                int globalIndex = startIndex + (dex - 1);
                                User chosen = Database.LoadUser(friend_of_friend.get(globalIndex));
                                if (!discover) {
                                    person_chosen(chosen, mutual_frndz);
                                } else {
                                    person_chosen(chosen, null);
                                }
                            } else {
                                System.out.println("Invalid Index");
                            }
                        }
                        case 0 -> {
                            return;
                        }
                        default -> System.out.println("Invalid Choice!");
                    }
                }
            }
        }
    }

    public static void person_chosen(User user, HashMap<String, List<String>> map) {
        boolean discovery = map == null;

        while (true) {
            System.out.println("=============================================");
            System.out.println("              Chosen person");
            System.out.println("=============================================");
            user.Print_profile();
            System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
            if (!discovery) {
                List<String> mutuals = map.get(user.getCredentials().getUsername());
                System.out.println("Mutual Friends: " + (mutuals != null ? mutuals.size() : 0));
                System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
            }
            System.out.println("=============================================");
            if (!discovery)
                System.out.println("1- View Mutual Friends");
            System.out.println("2- Send Friend Request");
            System.out.println("3- Visit their profile");
            System.out.println("0- Return");

            int choice = Main.Input_Int("Choice");

            switch (choice) {
                case 1 -> {
                    if (!discovery) {
                        System.out.println("=============================================");
                        System.out.println("Mutual Friends:");
                        List<String> mutualList = map.get(user.getCredentials().getUsername());
                        Main.Print_Friends_List(mutualList != null ? mutualList : new ArrayList<>());
                        System.out.println("=============================================");
                    }
                }
                case 2 -> {
                    System.out.println("=============================================");
                    Send_Friend_Request(user);
                }
                case 3 -> {
                    // creamy handle this shit
                }
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    public static void Searching_By_Name(String n) {
        String searchName = Main.Input_String(n);
        List<User> matchingUsers;

        while (true) {
            matchingUsers = Database.Search_Users_By_Name(searchName);
            System.out.println("===========================================");
            System.out.println("           Searching People by Name");
            System.out.println("===========================================");
            if (matchingUsers.isEmpty()) {
                System.out.println("\t\t\tNo Users with that name");
            } else {
                System.out.println("Users with Name: " + searchName);
                for (int i = 0; i < matchingUsers.size(); i++) {
                    System.out.print((i + 1) + ") ");
                    matchingUsers.get(i).Print_profile();
                    System.out.println("- - - - - - - - - - - - - - - - - - - - - -");
                }
            }
            System.out.println("===========================================");
            System.out.println("         1- Search Again");
            System.out.println("         2- Chose from List");
            System.out.println("         0- Return");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    searchName = Main.Input_String(n);
                }
                case 2 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > matchingUsers.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        User temp = matchingUsers.get(--index);
                        temp.Print_profile();
                        boolean friend = Database.Already_Friend(temp);
                        if (friend)
                            System.out.println("Friends 🤝");
                        System.out.println("=========================================");
                        System.out.println("1- Send Friend Request");
                        System.out.println("0- Cancel");
                        System.out.println("=========================================");
                        if (Main.Input_Int("Choice") == 1) {
                            Send_Friend_Request(temp);
                        }
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Send_Friend_Request(User user) {
        boolean friend = Database.Already_Friend(user);
        String username = user.getCredentials().getUsername();
        if (!friend) {
            if (!Database.F_Request_Already_sent(username)) {
                Database.Write_Notification(username,Main.Input_NotificationF());
                Database.WriteFriendRequestRecieved(username);
                Database.WriteFriendRequestSent(username);
                System.out.println("Friend Request Sent Successfully!");
            } else {
                System.out.println("Friend Request Already Sent");
            }
        } else {
            System.out.println("Already Friend!");
        }
    }

    public static void Find_Friends() {
        while (true) {
            System.out.println("__________________________________________");
            System.out.println("              FIND Friends Page");
            System.out.println("__________________________________________");
            System.out.println("              1- Search People by Name");
            System.out.println("              2- People You May Know");
            System.out.println("              0- Return");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Searching_By_Name("Friend's Name");
                }
                case 2 -> {
                    People_you_may_know();
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Privacy_Settings() {
        while (true) {
            System.out.println("""
                    =========================================
                                   Privacy Settings
                    =========================================
                              1- Turn Privacy Mode On
                              2- Turn Privacy Mode OFF
                              0- Return
                    """);
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    System.out.println("Nobody will be able to see ur status as either Online 🟢 or Offline 🔴 ");
                    if (Main.Yes_or_No("Enable Privacy Mode")) {
                        if (Main.current.getPrivacy()) {
                            System.out.println("Privacy Mode Already Enabled");
                        } else {
                            Main.current.Privacy_Mode_On();
                        }
                    }
                }
                case 2 -> {
                    System.out.println("Everyone will be able to see ur  status as either Online 🟢 or Offline 🔴 ");
                    if (Main.Yes_or_No("Disable Privacy Mode")) {
                        if (!Main.current.getPrivacy()) {
                            System.out.println("Privacy Mode Already Disabled");
                        } else {
                            Main.current.Privacy_Mode_OFF();
                        }
                    }
                }
            }
        }
    }

    public static void Settings_Page() {
        while (true) {
            System.out.println("Logged in as: " + Main.current.getCredentials().getUsername());
            System.out.println("=========================================");
            System.out.println("               SETTINGS");
            System.out.println("=========================================");
            System.out.println("        1- Change First Name");
            System.out.println("        2- Change Last Name");
            System.out.println("        3- Change Bio");
            System.out.println("        4- Change Date of Birth");
            System.out.println("        5- Privacy Settings");
            System.out.println("        6- Delete Account");
            System.out.println("        0- Back");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Main.current.setFirstname(Main.Input_String("New First Name"));
                    System.out.println("First name updated.");
                }
                case 2 -> {
                    Main.current.setLastname(Main.Input_String("New Last Name"));
                    System.out.println("Last name updated.");
                }
                case 3 -> {
                    Main.current.setBio(Main.Input_String("New Bio"));
                    System.out.println("Bio updated.");
                }
                case 4 -> {
                    Main.current.setBirth(Main.Input_date());
                    System.out.println("Date of Birth Changed!");
                }
                case 5 -> {
                    Privacy_Settings();
                }
                case 6 -> {
                    if (Main.Yes_or_No("Delete account?")) {
                        Database.Delete_Acc();
                        Main.current = null;
                        System.out.println("Account deleted. Returning to login.");
                        Database.Delete_Acc();
                        Main.System_Start();
                    }
                }
                case 0 -> {
                    return;
                }
            }
            Database.WriteUser(Main.current);
        }
    }

    public static void View_Post_Details(Post post) {
        while (true) {
            boolean hasLiked = Database.Has_Liked(post);
            System.out.println("============================================");
            System.out.println("               POST DETAILS");
            System.out.println("============================================");
            Main.Print_Post(post);
            System.out.println("============================================");
            System.out.println("            1- View Likes");
            System.out.println("            2- View Comments");
            System.out.println("            3- " + (hasLiked ? "Unlike" : "Like"));
            System.out.println("            4- Comment");
            System.out.println("            5- Delete Comment");
            System.out.println("            0- Return");
            System.out.println("============================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    ArrayList<String> likes = Database.Load_Post_Likes(post);
                    System.out.println("-----------------------------------------");
                    System.out.println("👍 Likes: " + likes.size());
                    for (int i = 0; i < likes.size(); i++) {
                        User liker = Database.LoadUser(likes.get(i));
                        System.out.println("👍" + liker.getFirstname() + " " + liker.getLastname());
                    }
                    System.out.println("-----------------------------------------");
                }
                case 2 -> {
                    ArrayList<Comment> comments = Database.Load_Post_Comments(post);
                    Main.Print_Comments(comments);
                }
                case 3 -> {
                    if (hasLiked) {
                        Database.Remove_Like(post);
                        System.out.println("Post unliked!");
                    } else {
                        if (!post.getSender().equals(Main.current.getCredentials().getUsername())) {
                            Database.Write_Notification(post.getSender(), Main.Input_NotificationL());
                        }
                        String timestamp = Database.safeTimestamp(post.getTime());
                        String path = timestamp + post.getSender();
                        for (String f : post.getTagged()) {
                            Database.WriteFeed(path, f, post);
                            Database.Write_Notification(f, Main.Input_NotificationL());
                        }
                        Database.Write_Like(post);
                        System.out.println("Post liked!");
                    }
                }
                case 4 -> {
                    Database.Write_Comment(post, Main.Input_Comment());
                    System.out.println("Comment added!");
                    if (!post.getSender().equals(Main.current.getCredentials().getUsername())) {
                        String timestamp = Database.safeTimestamp(post.getTime());
                        String path = timestamp + post.getSender();
                        for (String f : post.getTagged()) {
                            Database.WriteFeed(path, f, post);
                            Database.Write_Notification(f, Main.Input_NotificationC());
                        }
                        Database.Write_Notification(post.getSender(), Main.Input_NotificationC());
                    }
                }
                case 5 -> {
                    ArrayList<Comment> comments = Database.Load_Post_Comments(post);
                    Main.Print_Comments(comments);
                    if (comments.isEmpty())
                        break;
                    int commentIndex = Main.Input_Int("Comment Index to Delete");
                    if (commentIndex < 1 || commentIndex > comments.size()) {
                        System.out.println("Invalid Index");
                        break;
                    }
                    Comment temp = comments.get(--commentIndex);
                    if (temp.getSender().equals(Main.current.getCredentials().getUsername())) {
                        Database.Delete_Comment(post, temp);
                        System.out.println("Comment deleted!");
                    } else {
                        System.out.println("You can only delete your own comments!");
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void View_My_Posts() {
        ArrayList<Post> posts = Database.Load_User_Posts(Main.current.getCredentials().getUsername());
        while (true) {
            System.out.println("=========================================");
            System.out.println("             MY POSTS");
            System.out.println("=========================================");
            Main.Print_Posts(posts);
            System.out.println("=========================================");
            System.out.println("      1- View Post Details");
            System.out.println("      0- Return to Profile");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Post Index");
                    if (index < 1 || index > posts.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        View_Post_Details(posts.get(--index));
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Create_Group_Chat() {
        final int MAX_SIZE = 1000;
        String groupname, groupdescription;
        ArrayList<String> members = new ArrayList<>();
        members.add(Main.current.getCredentials().getUsername());
        groupname = Main.Input_String("Group Name");
        groupdescription = Main.Input_String("Group Description");
        while (true) {
            System.out.println("=========================================");
            System.out.println("            CREATE GROUP CHAT");
            System.out.println("=========================================");
            System.out.println("           1- Add Friends");
            System.out.println("           2- View Added Members");
            System.out.println("           3- Proceed");
            System.out.println("           0- Cancel");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    String friendo = Chose_Friendo();
                    if (friendo != null) {
                        if (members.size() <= MAX_SIZE) {
                            if (members.contains(friendo)) {
                                System.out.println("Friend already in group!");
                            } else {
                                User temp = Database.LoadUser(friendo);
                                members.add(friendo);
                                System.out.println(temp.getFullName() + " added to group!");
                            }
                        } else {
                            System.out.println("Maximum Member limit Reached!");
                        }
                    }
                }
                case 2 -> {
                    View_Added_Members_creation(members);
                }
                case 3 -> {
                    if (members.size() < 2) {
                        System.out.println("Group must have at least 2 members!");
                        break;
                    }
                    Group_chat groupChat = new Group_chat(groupname, groupdescription, members);
                    for (String member : members) {
                        Database.WriteChat(member, groupChat);
                    }
                    System.out.println("Group chat created successfully!");
                    Group_Chat_Page(groupChat);
                    return;
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void View_Added_Members_creation(List<String> members) {
        while (true) {
            System.out.println("Added Members: " + members.size());
            Main.Print_Friends_List(members);
            System.out.println("=========================================");
            System.out.println("1- Remove Member");
            System.out.println("0- Back");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > members.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        members.remove(--index);
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Inbox_page() {
        ArrayList<Chat> chats = Database.LoadInbox();
        while (true) {
            System.out.println("Logged in as: " + Main.current.getCredentials().getUsername());
            System.out.println("__________________________________________");
            System.out.println("            FACEBOOK Messenger");
            System.out.println("__________________________________________");
            Main.Print_Inbox(chats);
            System.out.println("__________________________________________");
            System.out.println("            1- Open a Chat");
            System.out.println("            2- Create a new DM");
            System.out.println("            3- Create a new GC");
            System.out.println("            4- Search a chat");
            System.out.println("            0- Return to HomePage");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    if (chats.isEmpty()) {
                        System.out.println("No chats to open");
                        break;
                    }
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > chats.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        Chat curr = chats.get(--index);
                        if (curr instanceof DM_chat temp) {
                            DMChat_Page(temp);
                        } else if (curr instanceof Group_chat temp) {
                            Group_Chat_Page(temp);
                        }
                    }
                }
                case 2 -> {
                    String friendo = Chose_Friendo();
                    if (friendo == null)
                        break;
                    boolean found = false;
                    if (!chats.isEmpty()) {
                        for (Chat chat : chats) {
                            if (chat instanceof DM_chat temp) {
                                if (temp.getR_username().equals(friendo)) {
                                    found = true;
                                    DMChat_Page(temp);
                                    break;
                                }
                            }
                        }
                    }
                    if (!found) {
                        DM_chat temp1 = new DM_chat(Main.current.getCredentials().getUsername(), friendo);
                        DM_chat temp2 = new DM_chat(friendo, Main.current.getCredentials().getUsername());
                        Database.WriteChat(Main.current.getCredentials().getUsername(), temp1);
                        Database.WriteChat(friendo, temp2);
                        DMChat_Page(temp1);
                    }
                }
                case 3 -> {
                    Create_Group_Chat();
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void DMChat_Page(DM_chat chat) {

        ArrayList<Message> messages = Database.Load_ALLMessages(chat.getFolder_path());
        ArrayList<Message> newMessages = new ArrayList<>();
        int previousSize = messages.size();
        String fullname = Main.Get_Fullname(chat.getR_username());
        while (true) {
            System.out.println("----------------------------------------------------");
            System.out.println("    " + fullname + "    "
                    + (Database.Check_Online(chat.getR_username()) ? "Online 🟢" : "Offline 🔴"));
            System.out.println("----------------------------------------------------");
            newMessages = Database.Load_New_Messages(chat.getFolder_path(), previousSize);
            if (!newMessages.isEmpty()) {
                messages.addAll(newMessages);
                previousSize = messages.size();
            }

            if (messages.isEmpty()) {
                System.out.println("No Messages yet!");
            } else {
                Main.Print_Entire_Chat(messages);
            }
            System.out.println("__________________________________________");
            System.out.println("        1- Send a Message");
            System.out.println("        2- Refresh Messages");
            System.out.println("        0- Return to Inbox");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Database.WriteMessage(chat.getFolder_path(), Main.Input_Message());
                    if(!Database.Check_Online(chat.getR_username())){
                        Database.Write_Notification(chat.getR_username(), Main.Input_NotificationM());
                    }
                    System.out.println("Message sent!");
                }
                case 2 -> {

                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static String Group_Members_Online(Group_chat chat) {
        int count = 0;
        for (String member : chat.getMembers()) {
            if (Database.Check_Online(member)) {
                count++;
            }
        }
        return "Members Online " + count;
    }

    public static void Group_Chat_Page(Group_chat chat) {
        boolean Admin = Main.current.getCredentials().getUsername().equals(chat.getCreator());
        ArrayList<Message> messages = Database.Load_ALLMessages(chat.getFolder_path());
        int previousSize = messages.size();

        while (true) {
            ArrayList<Message> newMessages = Database.Load_New_Messages(chat.getFolder_path(), previousSize);
            if (!newMessages.isEmpty()) {
                messages.addAll(newMessages);
                previousSize = messages.size();
            }

            System.out.println("----------------------------------------------------");
            System.out.println("          " + chat.getGroupname());
            System.out.println("         " + Group_Members_Online(chat));
            System.out.println("----------------------------------------------------");

            if (messages.isEmpty()) {
                System.out.println("No Messages yet!");
            } else {
                Main.Print_Entire_Chat(messages);
            }
            System.out.println("----------------------------------------------------");
            System.out.println("       1- Send a Message");
            System.out.println("       2- Refresh Messages");
            System.out.println("       3- Manage Members");
            System.out.println("       4- Change Group Descr");
            System.out.println("       5- Delete/Exit Group");
            System.out.println("       0- Return to Inbox");
            System.out.println("----------------------------------------------------");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Message newMessage = Main.Input_Message();
                    Database.WriteMessage(chat.getFolder_path(), newMessage);
                    for (String m : chat.getMembers()) {
                        Database.Write_Notification(m, Main.Input_NotificationM());
                    }
                    System.out.println("Message sent!");
                }
                case 2 -> {

                }
                case 3 -> {
                    Manage_Members(chat);
                }
                case 4 -> {
                    if (Admin) {
                        chat.setGroupdescription(Main.Input_String("New Description: "));
                    } else {
                        System.out.println("You are not the admin");
                    }
                }
                case 5 -> {
                    if (!Admin) {
                        chat.removeMember(Main.current.getCredentials().getUsername());
                        Database.Delete_Chat(chat.getFolder_path());
                        System.out.println("Group Deleted!");
                        return;
                    } else {
                        System.out.println("You must appoint someone else as admin first! ");
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Manage_Members(Group_chat chat) {
        boolean Admin = Main.current.getCredentials().getUsername().equals(chat.getCreator());
        while (true) {
            System.out.println("=========================================");
            System.out.println("          " + chat.getGroupname());
            System.out.println("=========================================");
            System.out.println("Group Description: ");
            System.out.println(chat.getGroupdescription());
            System.out.println("=========================================");
            for (int i = 0; i < chat.getMembers().size(); i++) {
                String display = (i + 1) + "- " + Main.Get_Fullname(chat.getMembers().get(i));
                if (chat.getMembers().get(i).equals(chat.getCreator())) {
                    display += " (ADMIN)";
                }
                System.out.println(display);
            }
            System.out.println("=========================================");
            System.out.println("1- Add Member");
            System.out.println("2- Remove Member");
            System.out.println("3- Make Admin");
            System.out.println("0- Back");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    if (Admin) {
                        String user = Chose_Friendo();
                        if (user != null) {
                            chat.getMembers().add(user);
                            System.out.println("Member Added Successfully!");
                        }
                    } else {
                        System.out.println("You are not the admin");
                    }
                }
                case 2 -> {
                    if (Admin) {
                        int index = Main.Input_Int("Index:");
                        if (index < 1 || index > chat.getMembers().size()) {
                            index--;
                            String curr = chat.getMembers().get(index);
                            if (curr.equals(chat.getCreator())) {
                                System.out.println("Can't Remove Yourself!");
                                continue;
                            }
                            chat.getMembers().remove(index);
                            System.out.println("Member Removed Successfully!");
                        }
                    } else {
                        System.out.println("You are not the admin");
                    }
                }
                case 3 -> {
                    if (Admin) {
                        if (Main.Yes_or_No("Make some else admin, You wont be admin anymore")) {
                            int index = Main.Input_Int("Index:");
                            if (index < 1 || index > chat.getMembers().size()) {
                                index--;
                                String curr = chat.getMembers().get(index);
                                if (curr.equals(chat.getCreator())) {
                                    System.out.println("You are already admin");
                                    continue;
                                }
                                chat.setCreator(curr);
                                System.out.println(Main.Get_Fullname(curr) + " is now an Admin!");
                            }
                        }
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Notifications_Page() {
        Database.Compute_Read_Unread();
        ArrayList<Notification> read;
        ArrayList<Notification> unread;
        while (true) {
            read = Database.Load_Read_Notifications();
            unread = Database.Load_Unread_Notification();
            int Unread = unread.size();
            System.out.println("=========================================");
            System.out.println("            NOTIFICATIONS " + (Unread > 0 ? (Unread + "new") : "No new Messages"));
            System.out.println("=========================================");
            if (unread.isEmpty() && read.isEmpty())
                System.out.println("No notifications Yet!");
            else {
                Main.Print_Notifications(unread);
                Main.Print_Notifications(read);
            }
            System.out.println("=========================================");
            System.out.println("          1- Mark all as read");
            System.out.println("          2- Clear all");
            System.out.println("          0- Back");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Database.Mark_All_Notifications_Read();
                    Database.Compute_Read_Unread();
                    System.out.println("Marked as read.");
                }
                case 2 -> {
                    Database.Delete_Notifications();
                    System.out.println("Cleared notifications.");
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void home_page() {
        while (Main.current != null) {
            Database.Compute_Read_Unread();
            int invites = Database.Load_Game_InvitesSize();
            int Unread = Database.Load_Unread_Notification().size();
            System.out.println("Logged in as: " + Main.current.getCredentials().getUsername());
            System.out.println("__________________________________________");
            System.out.println("           FACEBOOK HOME PAGE");
            System.out.println("__________________________________________");
            System.out.println("            1- Open Profile");
            System.out.println("            2- Open Messenger");
            System.out.println("            3- Find Friends");
            System.out.println("            4- View Feed");
            System.out.println("            5- Create Post");
            System.out.println("            6- Notifications" + (Unread > 0 ? " (" + Unread + " new)" : ""));
            System.out.println("            7- Facebook Games  " + ((invites > 0) ? "(" + invites + ")" : ""));
            System.out.println("            8- Settings");
            System.out.println("            9- Refresh Page");
            System.out.println("------------------------------------------");
            System.out.println("            0- Logout");
            System.out.println("__________________________________________");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    profile_page();
                }
                case 2 -> {
                    Inbox_page();
                }
                case 3 -> {
                    Find_Friends();
                }
                case 4 -> {
                    View_Feed();
                }
                case 5 -> {
                    Main.Input_Post();
                }
                case 6 -> {
                    Notifications_Page();
                }
                case 7 -> {
                    Games_Page();
                }
                case 8 -> {
                    Settings_Page();
                }
                case 9 -> {
                    System.out.println("Refreshing...");
                }
                case 0 -> {
                    Database.Delete_Online();
                    System.out.println("\t\t\t\tLOGGING OUT");
                    return;
                }
            }
        }
    }

    public static void View_Feed() {
        ArrayList<Post> feeds = Database.Load_Feed();
        ;
        while (true) {
            System.out.println("=========================================");
            System.out.println("                 FEED");
            System.out.println("=========================================");
            Main.Print_Posts(feeds);
            System.out.println("=========================================");
            System.out.println("        1- View Post Details");
            System.out.println("        2- Refresh feed");
            System.out.println("        0- Return to homePage");
            System.out.println("=========================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > feeds.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        View_Post_Details(feeds.get(--index));
                    }
                }
                case 2 -> {
                    ArrayList<Post> newfeed = Database.Load_Feed();
                    if (newfeed.size() > feeds.size()) {
                        System.out.println("Feed Refreshed");
                        feeds = newfeed;
                    } else {
                        System.out.println("No new Feed yet!");
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Games_Page() {
        while (true) {
            ArrayList<Game_Invite> invites = Database.Load_Game_Invites();
            System.out.println("================================");
            System.out.println("         Facebook Games");
            System.out.println("================================");
            System.out.println("1- Play Games");
            System.out.println("2- Manage Game Invites " + ((invites.size() > 0) ? "(" + (invites.size()) + ")" : ""));
            System.out.println("0- Return");
            System.out.println("================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    Play_Games_Page();
                }
                case 2 -> {
                    Manage_Game_Invites();
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Play_Games_Page() {
        ArrayList<Game> games = Main.Get_ALL_games();
        while (true) {
            System.out.println("================================");
            System.out.println("            GAMES");
            System.out.println("================================");
            Main.Print_Games(games);
            System.out.println("================================");
            System.out.println("1- Chose Game");
            System.out.println("0- Return");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > games.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        Game game = games.get(--index);
                        game.Game_launch();
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

    public static void Manage_Game_Invites() {
        while (true) {
            ArrayList<Game_Invite> invites = Database.Load_Game_Invites();
            System.out.println("================================");
            System.out.println("          GAME INVITES");
            System.out.println("================================");
            Main.Print_Game_Invites(invites);
            System.out.println("================================");
            System.out.println("1- Chose Game Invite");
            System.out.println("2- Delete Game Invite");
            System.out.println("0- Return");
            System.out.println("================================");
            switch (Main.Input_Int("Choice")) {
                case 1 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > invites.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        Game_Invite invite = invites.get(--index);
                        Database.Delete_Game_invite(invite);
                        if (invite.getGame().equals("TicTacToe")) {
                            TicTacToe toe = new TicTacToe();
                            toe.Online_game_launch(invite.getFilepath());
                        }
                    }
                }
                case 2 -> {
                    int index = Main.Input_Int("Index");
                    if (index < 1 || index > invites.size()) {
                        System.out.println("Invalid Index");
                    } else {
                        Game_Invite invite = invites.get(--index);
                        Database.Delete_Game_invite(invite);
                    }
                }
                case 0 -> {
                    return;
                }
            }
        }
    }

}
