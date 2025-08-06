package model;

import java.util.*;

public class BPlusTree {
    private final int T = 3; // Order of the tree
    private class Node {
        boolean leaf = true;
        List<String> keys = new ArrayList<>();
        List<Row> values = new ArrayList<>(); // only for leaf nodes
        List<Node> children = new ArrayList<>();
        Node next = null; // pointer to next leaf node (for range queries)
    }
    private Node root = new Node();

    public Row search(String key) {
        Node current = root;
        while (!current.leaf) {
            int i = 0;
            while (i < current.keys.size() && key.compareTo(current.keys.get(i)) >= 0) {
                i++;
            }
            current = current.children.get(i);
        }
        for (int i = 0; i < current.keys.size(); i++) {
            if (current.keys.get(i).equals(key)) {
                return current.values.get(i);
            }
        }
        return null;
    }

    public void insert(String key, Row row) {
        Node r = root;
        if (r.keys.size() == 2 * T - 1) {
            Node s = new Node();
            s.leaf = false;
            s.children.add(r);
            splitChild(s, 0, r);
            root = s;
        }
        insertNonFull(root, key, row);
    }

    private void insertNonFull(Node node, String key, Row row) {
        int i = node.keys.size() - 1;
        if (node.leaf) {
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) i--;
            node.keys.add(i + 1, key);
            node.values.add(i + 1, row);
        } else {
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) i--;
            i++;
            if (node.children.get(i).keys.size() == 2 * T - 1) {
                splitChild(node, i, node.children.get(i));
                if (key.compareTo(node.keys.get(i)) > 0) i++;
            }
            insertNonFull(node.children.get(i), key, row);
        }
    }

    private void splitChild(Node parent, int idx, Node full) {
        Node sibling = new Node();
        sibling.leaf = full.leaf;
        int mid = T - 1;

        parent.keys.add(idx, full.keys.get(mid));
        if (!full.leaf) {
            sibling.children.addAll(full.children.subList(T, full.children.size()));
            full.children.subList(T, full.children.size()).clear();
        }
        sibling.keys.addAll(full.keys.subList(T, full.keys.size()));
        full.keys.subList(mid, full.keys.size()).clear();

        if (full.leaf) {
            sibling.values.addAll(full.values.subList(T, full.values.size()));
            full.values.subList(T, full.values.size()).clear();

            sibling.next = full.next;
            full.next = sibling;
        }

        parent.children.add(idx + 1, sibling);
    }

    // New method for range search (inclusive)
    public List<Row> searchRange(String low, String high) {
        List<Row> result = new ArrayList<>();
        Node current = root;

        // Descend to leaf node where low should be
        while (!current.leaf) {
            int i = 0;
            while (i < current.keys.size() && low.compareTo(current.keys.get(i)) >= 0) {
                i++;
            }
            current = current.children.get(i);
        }

        // Traverse leaf linked list collecting rows within [low, high]
        while (current != null) {
            for (int i = 0; i < current.keys.size(); i++) {
                String k = current.keys.get(i);
                if (k.compareTo(low) >= 0 && k.compareTo(high) <= 0) {
                    result.add(current.values.get(i));
                } else if (k.compareTo(high) > 0) {
                    return result;
                }
            }
            current = current.next;
        }

        return result;
    }
}
