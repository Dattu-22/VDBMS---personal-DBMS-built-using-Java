package cli;

import model.*;
import parser.QueryParser;
import storage.TableStorage;
import storage.UserManager;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserManager userMgr = new UserManager();
        Database db = new Database();

        boolean running = true;

        outerLoop:
        while (running) {
            String userDir = null;

            // Login/Register/Exit prompt loop
            while (true) {
                System.out.print("Register with cmd 'CREATE USER <username> <password>' or Login with cmd 'LOGIN <username> <password>' or 'EXIT' to quit: ");
                String cmd = scanner.nextLine();
                String[] toks = QueryParser.parse(cmd);

                if (toks.length == 1 && toks[0].equalsIgnoreCase("EXIT")) {
                    System.out.println("Goodbye!");
                    break outerLoop; // Exit whole program
                } 
                else if (toks.length == 4 && toks[0].equalsIgnoreCase("CREATE") && toks[1].equalsIgnoreCase("USER")) {
                    String uname = toks[2], pwd = toks[3];
                    if (userMgr.register(uname, pwd)) {
                        System.out.println("User registered! Now login.");
                    } else {
                        System.out.println("User already exists. Try again.");
                    }
                } 
                else if (toks.length == 3 && toks[0].equalsIgnoreCase("LOGIN")) {
                    String uname = toks[1], pwd = toks[2];
                    if (userMgr.login(uname, pwd)) {
                        System.out.println("Login successful! Welcome " + uname);
                        userDir = userMgr.getCurrentUserDir();
                        db.loadAllTables(userDir);
                        break;
                    } else {
                        System.out.println("Incorrect username or password. Try again.");
                    }
                } 
                else {
                    System.out.println("Unknown command for login/registration.");
                }
            }

            if (!running) break; 

            System.out.println("Welcome to VDBMS " + userMgr.getCurrentUser() + "!");

            // Main DBMS command loop
            while (true) {
                System.out.print("vd> ");
                String input = scanner.nextLine();
                if (input.trim().isEmpty()) continue;

                String[] tokens = QueryParser.parse(input);
                String command = tokens[0].toUpperCase();

                if (command.equals("LOGOUT")) {
                    db.saveAllTables(userDir);
                    userMgr.logout();
                    System.out.println("Logged out.");
                    break; // Break to login/register prompt loop

                } else if (command.equals("EXIT")) {
                    db.saveAllTables(userDir);
                    System.out.println("Goodbye!");
                    running = false;
                    break; // Exit entire program
                }

                else if (tokens.length >= 4 && command.equals("CREATE") && tokens[1].equalsIgnoreCase("TABLE")) {
                    String tableName = tokens[2];
                    List<String> columns = Arrays.asList(tokens).subList(3, tokens.length);
                    db.createTable(tableName, columns, userDir);
                    System.out.println("Table " + tableName + " created with columns " + columns + ".");

                } else if (tokens.length >= 4 && command.equals("INSERT") && tokens[1].equalsIgnoreCase("INTO")) {
                    String tableName = tokens[2];
                    Table table = db.getTable(tableName);
                    if (table != null) {
                        List<String> values = Arrays.asList(tokens).subList(3, tokens.length);
                        if (values.size() != table.getColumns().size()) {
                            System.out.println("Error: Column count does not match value count.");
                        } else {
                            table.insertRow(values);
                            try { TableStorage.saveTable(table, userDir); } catch (Exception e) {}
                            System.out.println("Row inserted into " + tableName + ".");
                        }
                    } else {
                        System.out.println("Error: Table '" + tableName + "' not found.");
                    }

                } else if (tokens.length >= 4 && command.equals("SELECT") && tokens[1].equals("*") && tokens[2].equalsIgnoreCase("FROM")) {
                    String tableName = tokens[3];
                    Table table = db.getTable(tableName);

                    if (table == null) {
                        System.out.println("Error: Table '" + tableName + "' not found.");
                        continue;
                    }

                    if (tokens.length == 4) {
                        printRows(table, table.getRows());

                    } else if (tokens.length == 7 && tokens[4].equalsIgnoreCase("WHERE")) {
                        String whereColumn = tokens[5];
                        String whereValue = tokens[6];
                        List<Row> results = table.selectRows(whereColumn, whereValue);
                        printRows(table, results);

                    } else if (tokens.length == 8 && tokens[4].equalsIgnoreCase("WHERE") && tokens[6].equals("=")) {
                        String whereColumn = tokens[5];
                        String whereValue = tokens[7];
                        List<Row> results = table.selectRows(whereColumn, whereValue);
                        printRows(table, results);

                    } else if (tokens.length == 10 && tokens[4].equalsIgnoreCase("WHERE")
                            && tokens[6].equalsIgnoreCase("BETWEEN") && tokens[8].equalsIgnoreCase("AND")) {
                        String whereColumn = tokens[5];
                        String low = tokens[7];
                        String high = tokens[9];
                        List<Row> results = table.selectRange(whereColumn, low, high);
                        printRows(table, results);

                    } else {
                        System.out.println("Unrecognized command or syntax error.");
                    }

                } else if (tokens.length >= 9 && command.equals("UPDATE")) {
                    String tableName = tokens[1];
                    Table table = db.getTable(tableName);
                    if (table == null) {
                        System.out.println("Error: Table '" + tableName + "' not found.");
                        continue;
                    }

                    boolean validSyntax = false;
                    String updateColumn = null, updateValue = null, whereColumn = null, whereValue = null;

                    if (tokens.length == 9 && tokens[2].equalsIgnoreCase("SET") && tokens[4].equals("=")
                            && tokens[6].equalsIgnoreCase("WHERE")) {
                        validSyntax = true;
                        updateColumn = tokens[3];
                        updateValue = tokens[5];
                        whereColumn = tokens[7];
                        whereValue = tokens[8];
                    } else if (tokens.length == 10 && tokens[2].equalsIgnoreCase("SET") && tokens[4].equals("=")
                            && tokens[6].equalsIgnoreCase("WHERE") && tokens[8].equals("=")) {
                        validSyntax = true;
                        updateColumn = tokens[3];
                        updateValue = tokens[5];
                        whereColumn = tokens[7];
                        whereValue = tokens[9];
                    }

                    if (!validSyntax) {
                        System.out.println("Unrecognized command or syntax error.");
                        continue;
                    }

                    int updatedCount = table.updateRows(whereColumn, whereValue, updateColumn, updateValue);
                    try { TableStorage.saveTable(table, userDir); } catch (Exception e) {}
                    System.out.println(updatedCount + " row(s) updated.");

                } else if (tokens.length >= 6 && command.equals("DELETE") && tokens[1].equalsIgnoreCase("FROM")) {
                    String tableName = tokens[2];
                    Table table = db.getTable(tableName);
                    if (table == null) {
                        System.out.println("Error: Table '" + tableName + "' not found.");
                        continue;
                    }
                    if (tokens.length == 6 && tokens[3].equalsIgnoreCase("WHERE")) {
                        String whereColumn = tokens[4];
                        String whereValue = tokens[5];
                        int deletedCount = table.deleteRows(whereColumn, whereValue);
                        try { TableStorage.saveTable(table, userDir); } catch (Exception e) {}
                        System.out.println(deletedCount + " row(s) deleted.");
                    } else if (tokens.length == 7 && tokens[3].equalsIgnoreCase("WHERE") && tokens[5].equals("=")) {
                        String whereColumn = tokens[4];
                        String whereValue = tokens[6];
                        int deletedCount = table.deleteRows(whereColumn, whereValue);
                        try { TableStorage.saveTable(table, userDir); } catch (Exception e) {}
                        System.out.println(deletedCount + " row(s) deleted.");
                    } else {
                        System.out.println("Unrecognized command or syntax error.");
                    }

                } else if (tokens.length == 3 && command.equals("DELETE") && tokens[1].equalsIgnoreCase("TABLE")) {
                    String tableName = tokens[2];
                    if (db.deleteTable(tableName, userDir)) {
                        System.out.println("Table '" + tableName + "' deleted.");
                    } else {
                        System.out.println("Table '" + tableName + "' does not exist.");
                    }

                } else if (tokens.length == 5 && command.equals("RENAME") && tokens[1].equalsIgnoreCase("TABLE")
                        && tokens[3].equalsIgnoreCase("TO")) {
                    String oldName = tokens[2];
                    String newName = tokens[4];
                    if (db.renameTable(oldName, newName, userDir)) {
                        System.out.println("Table renamed from '" + oldName + "' to '" + newName + "'.");
                    } else {
                        System.out.println("Rename failed: check if old table exists or new name is already taken.");
                    }

                } else if (tokens.length == 5 && command.equals("ALTER") && tokens[1].equalsIgnoreCase("TABLE")
                        && tokens[3].equalsIgnoreCase("ADD")) {
                    String tableName = tokens[2];
                    String columnName = tokens[4];
                    Table table = db.getTable(tableName);
                    if (table != null) {
                        table.addColumn(columnName);
                        System.out.println("Column '" + columnName + "' added to table '" + tableName + "'.");
                        try { TableStorage.saveTable(table, userDir); } catch (Exception ignored) {}
                    } else {
                        System.out.println("Table '" + tableName + "' not found.");
                    }

                } else if (tokens.length == 5 && command.equals("ALTER") && tokens[1].equalsIgnoreCase("TABLE")
                        && tokens[3].equalsIgnoreCase("DROP")) {
                    String tableName = tokens[2];
                    String columnName = tokens[4];
                    Table table = db.getTable(tableName);
                    if (table != null) {
                        boolean success = table.dropColumn(columnName);
                        if (success) {
                            System.out.println("Column '" + columnName + "' dropped from table '" + tableName + "'.");
                            try { TableStorage.saveTable(table, userDir); } catch (Exception ignored) {}
                        } else {
                            System.out.println("Column '" + columnName + "' does not exist in table '" + tableName + "'.");
                        }
                    } else {
                        System.out.println("Table '" + tableName + "' not found.");
                    }

                } else {
                    System.out.println("Unrecognized command or syntax error.");
                }
            }
        }
        scanner.close();
    }

    private static void printRows(Table table, List<Row> rows) {
        if (rows.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println(String.join("\t", table.getColumns()));
        for (Row row : rows) {
            System.out.println(String.join("\t", row.getValues()));
        }
    }
}
