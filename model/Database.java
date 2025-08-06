package model;

import storage.TableStorage;

import java.util.*;
import java.io.*;
//import java.nio.file.*;

public class Database {
    private Map<String, Table> tables;

    public Database() {
        tables = new HashMap<>();
    }

    public void createTable(String name, List<String> columns, String userDir) {
        if (tables.containsKey(name)) {
            System.out.println("Warning: Table '" + name + "' already exists. Overwriting.");
        }
        Table table = new Table(name, columns);
        tables.put(name, table);
        try {
            TableStorage.saveTable(table, userDir);
        } catch (IOException e) {
            System.out.println("Error saving table: " + e.getMessage());
        }
    }

    public Table getTable(String name) {
        return tables.get(name);
    }

    public Set<String> getTableNames() {
        return tables.keySet();
    }

    public boolean deleteTable(String tableName, String userDir) {
        Table removed = tables.remove(tableName);
        if (removed == null) return false;
        TableStorage.deleteTableFile(userDir, tableName);
        return true;
    }

    public boolean renameTable(String oldName, String newName, String userDir) {
        if (!tables.containsKey(oldName) || tables.containsKey(newName)) return false;
        Table table = tables.remove(oldName);
        table.setName(newName);
        tables.put(newName, table);
        TableStorage.renameTableFile(userDir, oldName, newName);
        return true;
    }

    // Loads all tables for user
    public void loadAllTables(String userDir) {
        tables.clear();
        File dir = new File(userDir);
        File[] csvFiles = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (csvFiles == null) return;
        for (File file : csvFiles) {
            try {
                Table t = TableStorage.loadTable(file.getPath());
                tables.put(t.getName(), t);
            } catch (IOException e) {
                System.out.println("Failed to load table: " + e.getMessage());
            }
        }
    }

    public void saveAllTables(String userDir) {
        for (Table t : tables.values()) {
            try {
                TableStorage.saveTable(t, userDir);
            } catch (IOException e) {
                System.out.println("Failed to save table: " + e.getMessage());
            }
        }
    }
}
