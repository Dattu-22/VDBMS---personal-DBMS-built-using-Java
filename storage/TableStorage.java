package storage;

import model.Table;
import model.Row;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TableStorage {
    public static void saveTable(Table table, String userDir) throws IOException {
        String fileName = userDir + "/" + table.getName() + ".csv";
        Files.createDirectories(Paths.get(userDir));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write(String.join(",", table.getColumns()));
            bw.newLine();
            for (Row row : table.getRows()) {
                bw.write(String.join(",", row.getValues()));
                bw.newLine();
            }
        }
    }

    public static Table loadTable(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String headerLine = br.readLine();
            if (headerLine == null) throw new IOException("Empty file: " + fileName);
            List<String> columns = Arrays.asList(headerLine.split(","));
            String tableName = new File(fileName).getName().replace(".csv", "");
            Table table = new Table(tableName, columns);
            String rowLine;
            while ((rowLine = br.readLine()) != null) {
                List<String> values = Arrays.asList(rowLine.split(","));
                table.insertRow(values);
            }
            return table;
        }
    }

    public static void deleteTableFile(String userDir, String tableName) {
        Path file = Paths.get(userDir, tableName + ".csv");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.out.println("Failed to delete file: " + e.getMessage());
        }
    }

    public static void renameTableFile(String userDir, String oldName, String newName) {
        Path oldFile = Paths.get(userDir, oldName + ".csv");
        Path newFile = Paths.get(userDir, newName + ".csv");
        try {
            Files.move(oldFile, newFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to rename file: " + e.getMessage());
        }
    }
}
