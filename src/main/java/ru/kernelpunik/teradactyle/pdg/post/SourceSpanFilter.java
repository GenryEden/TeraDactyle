package ru.kernelpunik.teradactyle.pdg.post;

import ru.kernelpunik.teradactyle.pdg.match.Match;
import ru.kernelpunik.teradactyle.pdg.engine.Settings;

import java.util.IntSummaryStatistics;

/**
 * Фильтр, проверяющий компактность найденных соответствий по номеру строки,
 * чтобы убедиться, что найденные пары узлов лежат близко друг к другу в исходном коде.
 */
public class SourceSpanFilter {

    /**
     * Допуск на дополнительное число строк вне соответствий при оценке компактности
     */
    private final int tolerance;

    /**
     * @param settings настройки, содержащие tolerance
     */
    public SourceSpanFilter(Settings settings) {
        this.tolerance = settings.getTolerance();
    }

    /**
     * Проверяет, что в обоих графах номера строк добавленных пар узлов
     * лежат в интервале длиной не более match.size() + tolerance.
     * @param match найденное соответствие пар узлов
     * @return true, если соответствие компактно в исходниках
     */
    public boolean accept(Match match) {
        if (match.size() == 0) {
            return false;
        }
        // Проверка для первого графа
        IntSummaryStatistics statsA = match.getPairs().stream()
                .mapToInt(p -> p.getFirst().getLine())
                .summaryStatistics();
        if (statsA.getMax() - statsA.getMin() + 1 > match.size() + tolerance) {
            return false;
        }
        // Проверка для второго графа
        IntSummaryStatistics statsB = match.getPairs().stream()
                .mapToInt(p -> p.getSecond().getLine())
                .summaryStatistics();
        if (statsB.getMax() - statsB.getMin() + 1 > match.size() + tolerance) {
            return false;
        }
        return true;
    }
}
