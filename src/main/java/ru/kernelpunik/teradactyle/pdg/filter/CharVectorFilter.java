package ru.kernelpunik.teradactyle.pdg.filter;

import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;
import ru.kernelpunik.teradactyle.pdg.engine.Settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Фильтр, проверяющий схожесть двух графов по характеристическим векторам
 * (нормализованные частоты меток) с использованием евклидова расстояния.
 * Принимает пары, расстояние между векторами <= (1.0 - threshold).
 */
public class CharVectorFilter {
    private final double threshold;

    /**
     * @param settings объект настроек, содержащий параметр charVectorSimilarityThreshold
     */
    public CharVectorFilter(Settings settings) {
        this.threshold = settings.getCharVectorSimilarityThreshold();
    }

    /**
     * Выполняет фильтрацию: строит нормализованные векторы частот меток узлов
     * для двух графов и проверяет, что их евклидово расстояние <= (1.0 - threshold).
     * @param a первый подграф
     * @param b второй подграф
     * @return true, если фильтр пройден
     */
    public boolean pass(Graph a, Graph b) {
        // Получаем частоты меток в каждом графе
        Map<String, Integer> freqA = countLabels(a);
        Map<String, Integer> freqB = countLabels(b);
        int totalA = a.getNodes().size();
        int totalB = b.getNodes().size();

        // Собираем все метки
        Set<String> allLabels = new HashSet<>();
        allLabels.addAll(freqA.keySet());
        allLabels.addAll(freqB.keySet());

        // Вычисляем евклидово расстояние между нормализованными векторами
        double sumSquares = 0.0;
        for (String label : allLabels) {
            double vA = freqA.getOrDefault(label, 0) / (double) totalA;
            double vB = freqB.getOrDefault(label, 0) / (double) totalB;
            double diff = vA - vB;
            sumSquares += diff * diff;
        }
        double distance = Math.sqrt(sumSquares);

        // Проверяем условие схожести
        return distance <= (1.0 - threshold);
    }

    /**
     * Считает частоту меток узлов в графе.
     */
    private Map<String, Integer> countLabels(Graph g) {
        Map<String, Integer> freq = new HashMap<>();
        for (Node node : g.getNodes()) {
            freq.merge(node.getLabel(), 1, Integer::sum);
        }
        return freq;
    }
}
