package ru.kernelpunik.teradactyle.pdg.model;

import java.util.Objects;

/**
 * Вершина Program Dependence Graph (PDG).
 */
public class Node {
    private final String id;
    private final String label;
    private final int line;

    /**
     * @param id уникальный идентификатор вершины
     * @param label текстовая метка (например, оператор или выражение)
     * @param line номер строки в исходнике
     */
    public Node(String id, String label, int line) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.label = Objects.requireNonNull(label, "label must not be null");
        this.line = line;
    }

    /**
     * @return уникальный идентификатор вершины
     */
    public String getId() {
        return id;
    }

    /**
     * @return текстовая метка вершины
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return номер строки в исходном коде
     */
    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{id='" + id + "', label='" + label + "', line=" + line + '}';
    }
}
