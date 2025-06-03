package ru.kernelpunik.teradactyle.pdg.engine;

import org.springframework.stereotype.Component;
import ru.kernelpunik.teradactyle.pdg.frontend.PdgBuilder;
import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.match.Match;
import ru.kernelpunik.teradactyle.pdg.filter.LabelIntersectFilter;
import ru.kernelpunik.teradactyle.pdg.filter.CharVectorFilter;
import ru.kernelpunik.teradactyle.pdg.match.SlicingMatcher;
import ru.kernelpunik.teradactyle.pdg.match.TreeIsoMatcher;
import ru.kernelpunik.teradactyle.pdg.post.SourceSpanFilter;
import ru.kernelpunik.teradactyle.pdg.partition.GraphPartitioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Основной компонент для поиска зависимостей-клонов в проекте.
 * Использует четыре фазы: разбиение, фильтрация, сопоставление и пост-фильтрацию.
 */
@Component
public class DependencySearchEngine {

    private final Settings settings;
    private final PdgBuilder pdgBuilder;
    private final GraphPartitioner partitioner;
    private final LabelIntersectFilter labelFilter;
    private final CharVectorFilter charFilter;
    private final SlicingMatcher slicingMatcher;
    private final TreeIsoMatcher treeIsoMatcher;
    private final SourceSpanFilter spanFilter;

    public DependencySearchEngine(Settings settings, PdgBuilder pdgBuilder) {
        this.settings = settings;
        this.pdgBuilder = pdgBuilder;
        this.partitioner = new GraphPartitioner(settings.getUnitSize(), settings.getTolerance());
        this.labelFilter = new LabelIntersectFilter(settings);
        this.charFilter = new CharVectorFilter(settings);
        this.slicingMatcher = new SlicingMatcher(settings);
        this.treeIsoMatcher = new TreeIsoMatcher();
        this.spanFilter = new SourceSpanFilter(settings);
    }

    /**
     * Запускает процесс поиска для проекта в указанной папке.
     * @param projectRoot путь к корню исходников
     * @return список найденных соответствий пар подграфов
     */
    public List<Match> run(String projectRoot) {
        // Строим PDG из исходников
        Graph graph = pdgBuilder.build(projectRoot);
        return search(graph);
    }

    /**
     * Поиск соответствий внутри уже построенного графа PDG.
     * @param graph PDG проекта
     * @return список найденных соответствений
     */
    public List<Match> search(Graph graph) {
        List<Graph> parts = partitioner.split(graph);
        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < parts.size(); i++) {
            for (int j = i + 1; j < parts.size(); j++) {
                Graph a = parts.get(i);
                Graph b = parts.get(j);
                if (!labelFilter.pass(a, b)) continue;
                if (!charFilter.pass(a, b)) continue;

                // Пробуем сначала быстрейший алгоритм
                Match match = slicingMatcher.match(a, b);
                if (match.size() < settings.getUnitSize() / 2) {
                    Match exact = treeIsoMatcher.match(a, b);
                    if (exact.size() > match.size()) {
                        match = exact;
                    }
                }

                if (spanFilter.accept(match)) {
                    matches.add(match);
                }
            }
        }
        return matches;
    }
}
