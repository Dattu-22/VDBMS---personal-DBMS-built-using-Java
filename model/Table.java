package model;

import java.util.*;

public class Table {
    private String name;
    private List<String> columns;
    private List<Row> rows;
    private BPlusTree bptIndex;

    public Table(String name, List<String> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>();
        this.bptIndex = new BPlusTree();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void insertRow(List<String> values) {
        Row newRow = new Row(values);
        rows.add(newRow);
        if (!values.isEmpty()) bptIndex.insert(values.get(0), newRow);
    }

    public List<Row> getRows() {
        return rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getName() {
        return name;
    }

    public void addColumn(String columnName) {
        columns.add(columnName);
        for (Row row : rows) {
            row.getValues().add("");
        }
    }

    public boolean dropColumn(String columnName) {
        int idx = columns.indexOf(columnName);
        if (idx == -1) return false;
        columns.remove(idx);
        for (Row row : rows) {
            row.getValues().remove(idx);
        }
        return true;
    }

    public List<Row> selectRows(String whereColumn, String whereValue) {
        if (whereColumn == null) return new ArrayList<>(rows);

        int colIndex = columns.indexOf(whereColumn);
        if (colIndex == -1) return Collections.emptyList();

        if (colIndex == 0) {
            Row row = bptIndex.search(whereValue);
            return row != null ? Arrays.asList(row) : Collections.emptyList();
        }

        List<Row> filtered = new ArrayList<>();
        for (Row row : rows) {
            if (row.getValues().get(colIndex).equals(whereValue)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    public List<Row> selectRange(String column, String low, String high) {
        if (column == null) return new ArrayList<>(rows);
        int colIndex = columns.indexOf(column);
        if (colIndex == -1) return Collections.emptyList();

        if (colIndex == 0) {
            return bptIndex.searchRange(low, high);
        }
        List<Row> filtered = new ArrayList<>();
        for (Row row : rows) {
            String val = row.getValues().get(colIndex);
            if (val.compareTo(low) >= 0 && val.compareTo(high) <= 0) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    public int updateRows(String whereColumn, String whereValue, String updateColumn, String updateValue) {
        int whereIdx = columns.indexOf(whereColumn);
        int updateIdx = columns.indexOf(updateColumn);
        if (whereIdx == -1 || updateIdx == -1) return 0;
        int count = 0;
        if (whereIdx == 0) {
            Row row = bptIndex.search(whereValue);
            if (row != null) {
                row.getValues().set(updateIdx, updateValue);
                count = 1;
            }
        } else {
            for (Row row : rows) {
                if (row.getValues().get(whereIdx).equals(whereValue)) {
                    row.getValues().set(updateIdx, updateValue);
                    count++;
                }
            }
        }
        return count;
    }

    public int deleteRows(String whereColumn, String whereValue) {
        int idx = columns.indexOf(whereColumn);
        if (idx == -1) return 0;
        int count = 0;
        Iterator<Row> iter = rows.iterator();
        while (iter.hasNext()) {
            Row row = iter.next();
            if (row.getValues().get(idx).equals(whereValue)) {
                iter.remove();
                count++;
            }
        }
        return count;
    }
}
