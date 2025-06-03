package ru.kernelpunik.teradactyle.pdg.model;

import java.util.Objects;

/**
 * Рёбро Program Dependence Graph (PDG).
 */
public class Edge {
    private final Node source;
    private final Node target;
    private final String type;

    /**
     * @param source исходная вершина
     * @param target целевая вершина
     * @param type тип зависимости (например, "data", "control")
     */
    public Edge(Node source, Node target, String type) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    /**
     * @return исходная вершина
     */
    public Node getSource() {
        return source;
    }

    /**
     * @return целевая вершина
     */
    public Node getTarget() {
        return target;
    }

    /**
     * @return тип зависимости
     */
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return source.equals(edge.source) &&
               target.equals(edge.target) &&
               type.equals(edge.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, type);
    }

    @Override
    public String toString() {
        return "Edge{" + source + " -> " + target + ", type='" + type + "'}";
    }
}
