package ru.kernelpunik.teradactyle.pdg.services;

import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.pdg.engine.Settings;
import ru.kernelpunik.teradactyle.pdg.filter.CharVectorFilter;
import ru.kernelpunik.teradactyle.pdg.filter.LabelIntersectFilter;
import ru.kernelpunik.teradactyle.pdg.frontend.PdgBuilder;
import ru.kernelpunik.teradactyle.pdg.match.Match;
import ru.kernelpunik.teradactyle.pdg.match.SlicingMatcher;
import ru.kernelpunik.teradactyle.pdg.post.SourceSpanFilter;
import ru.kernelpunik.teradactyle.pdg.partition.GraphPartitioner;
import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.repositories.ProjectRepository;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * ProjectService с поддержкой PDG-анализа библиотечных зависимостей.
 */
//@Service
public class ProjectService extends ru.kernelpunik.teradactyle.services.ProjectService {

    private final PdgBuilder pdgBuilder;
    private final LibraryService libraryService;

    private final GraphPartitioner partitioner;
    private final LabelIntersectFilter labelFilter;
    private final CharVectorFilter charFilter;
    private final SlicingMatcher slicingMatcher;
    private final SourceSpanFilter spanFilter;

    public ProjectService(ProjectRepository projectRepository,
                          FingerprintRepository fingerprintRepository,
                          PdgBuilder pdgBuilder,
                          Settings settings,
                          LibraryService libraryService) {
        super(projectRepository, fingerprintRepository);
        this.pdgBuilder = pdgBuilder;
        this.libraryService = libraryService;

        this.partitioner = new GraphPartitioner(settings.getUnitSize(), settings.getTolerance());
        this.labelFilter = new LabelIntersectFilter(settings);
        this.charFilter = new CharVectorFilter(settings);
        this.slicingMatcher = new SlicingMatcher(settings);
        this.spanFilter = new SourceSpanFilter(settings);
    }

    @Override
    public CompletableFuture<TreeNode<CollisionReport>> analyzeProject(File projectDir) {
        return CompletableFuture.supplyAsync(() -> {
            // базовая токенизация (может быть асинхронной внутри BaseProjectService)
            try { super.analyzeProject(projectDir).join(); } catch (Exception ignored) {}

            // формируем PDG проекта
            Graph projectGraph = pdgBuilder.build(projectDir.getAbsolutePath());

            // корневой отчёт по всему проекту
            CollisionReport rootReport = new CollisionReport(projectDir.getName());
            TreeNode<CollisionReport> rootNode = new TreeNode<>(rootReport);

            // перебираем сохранённые библиотеки
            Set<Long> libIds = libraryService.getIds();
            for (Long libId : libIds) {
                Graph libGraph = libraryService.getLibraryGraph(libId);
                Component component = libraryService.getComponent(libId);

                List<Match> matches = findCollisions(projectGraph, libGraph);
                long collisionsCount = matches.stream().mapToLong(Match::size).sum();

                if (collisionsCount > 0) {
                    // обновляем корневой отчёт
                    rootReport.addFingerprints(collisionsCount);
                    rootReport.addCollisionWith(component, collisionsCount);

                    // создаём дочерний отчёт
                    CollisionReport childReport = new CollisionReport(component.getName());
                    childReport.addFingerprints(collisionsCount);
                    childReport.addCollisionWith(component, collisionsCount);
                    rootNode.addChild(new TreeNode<>(childReport));
                }
            }
            return rootNode;
        });
    }

    private List<Match> findCollisions(Graph project, Graph lib) {
        List<Match> results = new ArrayList<>();
        List<Graph> projParts = partitioner.split(project);
        List<Graph> libParts = partitioner.split(lib);
        for (Graph a : projParts) {
            for (Graph b : libParts) {
                if (!labelFilter.pass(a, b)) continue;
                if (!charFilter.pass(a, b)) continue;
                Match m = slicingMatcher.match(a, b);
                if (spanFilter.accept(m) && m.size() > 0) {
                    results.add(m);
                }
            }
        }
        return results;
    }
}
