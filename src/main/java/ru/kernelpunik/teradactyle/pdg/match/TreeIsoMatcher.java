package ru.kernelpunik.teradactyle.pdg.match;

import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;
import ru.kernelpunik.teradactyle.pdg.model.Edge;

import java.util.*;

/**
 * Упрощённый алгоритм поиска изоморфизма между двумя подграфами PDG,
 * ориентированного на обнаружение T1/T2-клонов.
 */
public class TreeIsoMatcher {
    public TreeIsoMatcher() {
    }

    /**
     * Пытается найти полный изоморфизм между g1 и g2.
     * @return Match с полным соответствием пар вершин или пустой Match, если изоморфизм не найден
     */
    public Match match(Graph g1, Graph g2) {
        // Быстрая проверка по количеству вершин и рёбер
        if (g1.getNodes().size() != g2.getNodes().size() ||
            g1.getEdges().size() != g2.getEdges().size()) {
            return new Match();
        }

        List<Node> nodes1 = new ArrayList<>(g1.getNodes());
        Map<Node, Node> mapping = new HashMap<>();
        Set<Node> used2 = new HashSet<>();

        if (search(0, nodes1, g1, g2, mapping, used2)) {
            Match match = new Match();
            mapping.forEach(match::add);
            return match;
        }
        return new Match();
    }

    /**
     * Рекурсивный бэктрекинг для подбора соответствия узлов.
     */
    private boolean search(int idx,
                           List<Node> nodes1,
                           Graph g1,
                           Graph g2,
                           Map<Node, Node> mapping,
                           Set<Node> used2) {
        if (idx == nodes1.size()) return true;

        Node n1 = nodes1.get(idx);
        for (Node n2 : g2.getNodes()) {
            if (used2.contains(n2) || !n1.getLabel().equals(n2.getLabel())) continue;

            mapping.put(n1, n2);
            used2.add(n2);

            if (isCompatible(n1, n2, g1, g2, mapping)) {
                if (search(idx + 1, nodes1, g1, g2, mapping, used2)) return true;
            }

            mapping.remove(n1);
            used2.remove(n2);
        }
        return false;
    }

    /**
     * Проверяет согласованность добавленной пары с уже сопоставленными узлами по рёбрам.
     */
    private boolean isCompatible(Node n1,
                                 Node n2,
                                 Graph g1,
                                 Graph g2,
                                 Map<Node, Node> mapping) {
        for (Edge e : g1.getEdges()) {
            // прямые связи
            if (e.getSource().equals(n1) && mapping.containsKey(e.getTarget())) {
                Node mappedTarget = mapping.get(e.getTarget());
                if (!containsEdge(g2.getEdges(), n2, mappedTarget, e.getType())) {
                    return false;
                }
            }
            // обратные связи
            if (e.getTarget().equals(n1) && mapping.containsKey(e.getSource())) {
                Node mappedSource = mapping.get(e.getSource());
                if (!containsEdge(g2.getEdges(), mappedSource, n2, e.getType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Проверяет наличие ребра с указанным типом между двумя узлами.
     */
    private boolean containsEdge(Set<Edge> edges, Node src, Node tgt, String type) {
        for (Edge e : edges) {
            if (e.getSource().equals(src)
             && e.getTarget().equals(tgt)
             && e.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
