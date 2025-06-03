package ru.kernelpunik.teradactyle.pdg.match;

import ru.kernelpunik.teradactyle.pdg.model.Node;

import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Класс для хранения соответствий пар вершин двух графов.
 */
public class Match {
    private final Set<Pair> pairs;

    /**
     * Создаёт пустой объект соответствий.
     */
    public Match() {
        this.pairs = new HashSet<>();
    }

    /**
     * Добавляет новую пару соответствия.
     * @param first вершина из первого графа
     * @param second вершина из второго графа
     */
    public void add(Node first, Node second) {
        pairs.add(new Pair(first, second));
    }

    /**
     * Проверяет, содержит ли уже эта пара совпадение.
     * @param first вершина из первого графа
     * @param second вершина из второго графа
     * @return true, если пара уже добавлена
     */
    public boolean contains(Node first, Node second) {
        return pairs.contains(new Pair(first, second));
    }

    /**
     * @return число найденных соответствий
     */
    public int size() {
        return pairs.size();
    }

    /**
     * @return неизменяемое множество пар соответствий
     */
    public Set<Pair> getPairs() {
        return Set.copyOf(pairs);
    }

    @Override
    public String toString() {
        return "Match{size=" + size() + ", pairs=" + pairs + '}';
    }

    /**
     * Вложенный класс для пары вершин.
     */
    public static class Pair {
        private final Node first;
        private final Node second;

        public Pair(Node first, Node second) {
            this.first = Objects.requireNonNull(first, "first must not be null");
            this.second = Objects.requireNonNull(second, "second must not be null");
        }

        public Node getFirst() {
            return first;
        }

        public Node getSecond() {
            return second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair pair = (Pair) o;
            return first.equals(pair.first) && second.equals(pair.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "(" + first + " <-> " + second + ")";
        }
    }
}
