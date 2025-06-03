package ru.kernelpunik.teradactyle.pdg.match;

import ru.kernelpunik.teradactyle.pdg.engine.Settings;
import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;
import ru.kernelpunik.teradactyle.pdg.model.Edge;

import java.util.*;

public class SlicingMatcher {
    private final Settings settings;

    public SlicingMatcher(Settings settings) {
        this.settings = settings;
    }

    /**
     * Основной метод: находит наилучшее совпадение между двумя графами.
     * @param g1 первый подграф
     * @param g2 второй подграф
     * @return объект Match с набранными парами вершин
     */
    public Match match(Graph g1, Graph g2) {
        // Инициализируем очередь стартовых пар
        Queue<Pair<Node, Node>> seeds = new LinkedList<>(bestSeedPairs(g1, g2));
        Match best = new Match();

        while (!seeds.isEmpty()) {
            Pair<Node, Node> seed = seeds.poll();
            Match current = grow(seed.getFirst(), seed.getSecond(), new Match(), g1, g2);
            if (current.size() > best.size()) {
                best = current;
            }
        }
        return best;
    }

    private List<Pair<Node, Node>> bestSeedPairs(Graph g1, Graph g2) {
        List<Pair<Node, Node>> seeds = new ArrayList<>();
        for (Node n1 : g1.getNodes()) {
            for (Node n2 : g2.getNodes()) {
                if (n1.getLabel().equals(n2.getLabel())) {
                    seeds.add(new Pair<>(n1, n2));
                }
            }
        }
        // Сортируем по убыванию числа совпадающих меток соседей
        seeds.sort((p1, p2) -> Integer.compare(
                score(p2, g1, g2), score(p1, g1, g2)
        ));
        return seeds;
    }

    // Оцениваем пару по числу совпадений меток соседних узлов
    private int score(Pair<Node, Node> p, Graph g1, Graph g2) {
        Set<String> neigh1 = new HashSet<>();
        for (Edge e : g1.getEdges()) {
            if (e.getSource().equals(p.getFirst())) neigh1.add(e.getTarget().getLabel());
            if (e.getTarget().equals(p.getFirst())) neigh1.add(e.getSource().getLabel());
        }
        Set<String> neigh2 = new HashSet<>();
        for (Edge e : g2.getEdges()) {
            if (e.getSource().equals(p.getSecond())) neigh2.add(e.getTarget().getLabel());
            if (e.getTarget().equals(p.getSecond())) neigh2.add(e.getSource().getLabel());
        }
        neigh1.retainAll(neigh2);
        return neigh1.size();
    }

    // Расширение совпадения из начальной пары
    private Match grow(Node n1, Node n2, Match match, Graph g1, Graph g2) {
        match.add(n1, n2);
        boolean expanded;
        do {
            expanded = false;
            Set<Pair<Node, Node>> toAdd = new HashSet<>();
            for (Match.Pair pair : match.getPairs()) {
                // прямое расширение (succ)
                for (Node a : successors(pair.getFirst(), g1)) {
                    for (Node b : successors(pair.getSecond(), g2)) {
                        if (a.getLabel().equals(b.getLabel()) && !match.contains(a, b)) {
                            toAdd.add(new Pair<>(a, b));
                        }
                    }
                }
                // обратное расширение (pred)
                for (Node a : predecessors(pair.getFirst(), g1)) {
                    for (Node b : predecessors(pair.getSecond(), g2)) {
                        if (a.getLabel().equals(b.getLabel()) && !match.contains(a, b)) {
                            toAdd.add(new Pair<>(a, b));
                        }
                    }
                }
            }
            if (!toAdd.isEmpty()) {
                expanded = true;
                for (Pair<Node, Node> p : toAdd) {
                    match.add(p.getFirst(), p.getSecond());
                }
            }
        } while (expanded);
        return match;
    }

    // Получить соседей по исходящим рёбрам
    private Set<Node> successors(Node n, Graph g) {
        Set<Node> s = new HashSet<>();
        for (Edge e : g.getEdges()) {
            if (e.getSource().equals(n)) s.add(e.getTarget());
        }
        return s;
    }

    // Получить соседей по входящим рёбрам
    private Set<Node> predecessors(Node n, Graph g) {
        Set<Node> s = new HashSet<>();
        for (Edge e : g.getEdges()) {
            if (e.getTarget().equals(n)) s.add(e.getSource());
        }
        return s;
    }

    // Вспомогательный класс-пара для узлов
    private static class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        A getFirst() { return first; }
        B getSecond() { return second; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
}
