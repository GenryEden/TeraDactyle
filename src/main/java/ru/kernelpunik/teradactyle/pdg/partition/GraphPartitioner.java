package ru.kernelpunik.teradactyle.pdg.partition;

import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;
import ru.kernelpunik.teradactyle.pdg.model.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Разбиение Program Dependence Graph (PDG) на единицы сравнения (ЕС)
 * по алгоритму минимизации пересекающихся рёбер.
 */
public class GraphPartitioner {

    private final int unitSize;
    private final int tolerance;

    /**
     * @param unitSize желаемое количество строк (вершин) в каждой части
     * @param tolerance максимальное отклонение (в вершинах) при поиске оптимальной границы
     */
    public GraphPartitioner(int unitSize, int tolerance) {
        this.unitSize = unitSize;
        this.tolerance = tolerance;
    }

    /**
     * Разбивает граф на фрагменты.
     * @param graph исходный PDG
     * @return список индукцированных подграфов-ЕС
     */
    public List<Graph> split(Graph graph) {
        // Сортируем вершины по номеру строки в исходном коде
        List<Node> sorted = graph.getNodes().stream()
                .sorted(Comparator.comparingInt(Node::getLine))
                .collect(Collectors.toList());

        List<Graph> parts = new ArrayList<>();

        while (!sorted.isEmpty()) {
            // Вычисляем лучшую границу
            int splitIndex = argMinCrossEdges(graph, sorted, unitSize, tolerance);
            // Формируем фрагмент из 0..splitIndex
            List<Node> chunk = new ArrayList<>(sorted.subList(0, splitIndex + 1));
            parts.add(graph.inducedSubgraph(new HashSet<>(chunk)));
            // Оставляем для обработки остаток
            sorted = new ArrayList<>(sorted.subList(splitIndex + 1, sorted.size()));
        }

        return parts;
    }

    /**
     * Находит индекс вершины в списке sorted, до которого должен идти фрагмент,
     * чтобы минимизировать число пересекающихся рёбер.
     * @param graph полный граф
     * @param sorted отсортированный по строкам список вершин
     * @param unitSize ориентиpное число вершин
     * @param tolerance допуск при поиске оптимальной границы
     * @return индекс вершины, включённой в фрагмент
     */
    private int argMinCrossEdges(Graph graph, List<Node> sorted,
                                 int unitSize, int tolerance) {
        int minIndex = Math.min(unitSize, sorted.size()) - 1;
        int maxIndex = Math.min(sorted.size(), unitSize + tolerance) - 1;
        long minCross = Long.MAX_VALUE;
        int bestIdx = minIndex;

        Set<Edge> edges = graph.getEdges();

        for (int i = minIndex; i <= maxIndex; i++) {
            Set<Node> chunkSet = new HashSet<>(sorted.subList(0, i + 1));
            long crossCount = countCrossEdges(edges, chunkSet);
            if (crossCount < minCross) {
                minCross = crossCount;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * Считает количество рёбер, у которых один конец в chunk, другой вне его.
     */
    private long countCrossEdges(Set<Edge> edges, Set<Node> chunk) {
        return edges.stream()
                .filter(e -> (chunk.contains(e.getSource()) && !chunk.contains(e.getTarget()))
                        || (!chunk.contains(e.getSource()) && chunk.contains(e.getTarget())))
                .count();
    }
}
