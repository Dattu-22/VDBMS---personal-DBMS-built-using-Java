package storage;

import java.io.*;
import java.nio.file.*;
//import java.util.*;

public class UserManager {
    private static final String USERS_DIR = "Users";
    private static final String USERS_FILE = USERS_DIR + "/users.csv";
    private String currentUser = null;

    public UserManager() {
        File dir = new File(USERS_DIR);
        if (!dir.exists()) dir.mkdir();
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(usersFile, true))) {
                // Just create the file if missing
            } catch (IOException e) {
                System.out.println("Could not initialize user system: " + e.getMessage());
            }
        }
    }

    public boolean register(String username, String password) {
        // Check if user exists
        if (userExists(username)) return false;
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            pw.println(username + "," + password);
            // Create user's folder
            Path userDir = Paths.get(USERS_DIR, username);
            Files.createDirectories(userDir);
            return true;
        } catch (IOException e) {
            System.out.println("Could not register: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", 2);
                if (arr.length == 2 && arr[0].equals(username) && arr[1].equals(password)) {
                    // Check user directory
                    Path userDir = Paths.get(USERS_DIR, username);
                    if (!Files.exists(userDir)) Files.createDirectories(userDir);
                    currentUser = username;
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not login: " + e.getMessage());
        }
        return false;
    }

    public boolean userExists(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",", 2);
                if (arr.length == 2 && arr[0].equals(username)) return true;
            }
        } catch (IOException e) {
            // Ignore for existence check
        }
        return false;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public String getCurrentUserDir() {
        if (currentUser == null) return null;
        return USERS_DIR + "/" + currentUser;
    }
}
