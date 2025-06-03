package ru.kernelpunik.teradactyle.pdg.model;

import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Представление Program Dependence Graph (PDG).
 */
public class Graph {
    private final Set<Node> nodes;
    private final Set<Edge> edges;

    /**
     * Конструктор графа.
     * @param nodes множество вершин
     * @param edges множество рёбер
     */
    public Graph(Set<Node> nodes, Set<Edge> edges) {
        this.nodes = new HashSet<>(nodes);
        this.edges = new HashSet<>(edges);
    }

    /**
     * @return множество вершин графа
     */
    public Set<Node> getNodes() {
        return nodes;
    }

    /**
     * @return множество рёбер графа
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * Строит индуцированный подграф по заданному множеству вершин.
     * @param subset вершины, входящие в подграф
     * @return новый Graph, содержащий только ребра между вершинами из subset
     */
    public Graph inducedSubgraph(Set<Node> subset) {
        Set<Edge> subEdges = edges.stream()
            .filter(e -> subset.contains(e.getSource()) && subset.contains(e.getTarget()))
            .collect(Collectors.toSet());
        return new Graph(subset, subEdges);
    }

    @Override
    public String toString() {
        return "Graph{nodes=" + nodes.size() + ", edges=" + edges.size() + '}';
    }
}
