package ru.kernelpunik.teradactyle.pdg.filter;

import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;
import ru.kernelpunik.teradactyle.pdg.engine.Settings;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Фильтр, проверяющий пересечение множеств меток вершин двух подграфов.
 * Требует, чтобы доля общих меток была не меньше порога из настроек.
 */
public class LabelIntersectFilter {
    /**
     * Порог относительного количества пересекающихся меток (в диапазоне [0.0, 1.0]).
     */
    private final double threshold;

    /**
     * @param settings объект настроек, содержащий параметр labelSimilarityThreshold
     */
    public LabelIntersectFilter(Settings settings) {
        this.threshold = settings.getLabelSimilarityThreshold();
    }

    /**
     * Выполняет фильтрацию: проверяет, что число общих меток >= threshold * min(|A|,|B|).
     * @param a первый подграф
     * @param b второй подграф
     * @return true, если фильтр пройден
     */
    public boolean pass(Graph a, Graph b) {
        Set<String> labelsA = a.getNodes().stream()
                .map(Node::getLabel)
                .collect(Collectors.toSet());
        Set<String> labelsB = b.getNodes().stream()
                .map(Node::getLabel)
                .collect(Collectors.toSet());

        long common = labelsA.stream()
                .filter(labelsB::contains)
                .count();

        int minSize = Math.min(labelsA.size(), labelsB.size());
        // Сравниваем с пороговым значением
        return common >= minSize * threshold;
    }
}
