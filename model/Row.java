package model;

import java.util.*;

public class Row {
    private List<String> values;

    public Row(List<String> values) {
        this.values = new ArrayList<>(values);
    }

    public List<String> getValues() {
        return values;
    }
}
