package ru.kernelpunik.teradactyle.pdg.services;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.pdg.engine.DependencySearchEngine;
import ru.kernelpunik.teradactyle.pdg.engine.Settings;
import ru.kernelpunik.teradactyle.pdg.frontend.PdgBuilder;
import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.services.ILibraryService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Примерная реализация ILibraryService.
 * <p>
 * 1. Копирует загруженную библиотеку (директорию с .class или .jar) во временную папку.
 * 2. Строит PDG библиотеки с помощью {@link PdgBuilder}.
 * 3. Кеширует результат в памяти, возвращая id.
 * <p>
 * В дальнейшем {@link DependencySearchEngine} сможет использовать сохранённые PDG
 * при анализе проектов.
 */
//@Service
public class LibraryService implements ILibraryService {

    private final PdgBuilder pdgBuilder;
    private final Settings settings;

    // Хранилище PDG библиотек по id
    private final Map<Long, Graph> libraryStore = new HashMap<>();
    private final Map<Long, Component> metaStore = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public LibraryService(PdgBuilder pdgBuilder, Settings settings) {
        this.pdgBuilder = pdgBuilder;
        this.settings = settings;
    }

    @Override
    public long addLibrary(Component component, File fileTree) throws IOException {
        long id = idGenerator.getAndIncrement();

        // 1. Скопируем/распакуем библиотеку во временный каталог для анализа
        Path tempDir = Files.createTempDirectory("lib-" + id + "-");
        copyRecursively(fileTree.toPath(), tempDir);

        // 2. Построим PDG
        Graph pdg = pdgBuilder.build(tempDir.toAbsolutePath().toString());

        // 3. Сохраним в памяти (можно заменить на БД или кеш на диске)
        libraryStore.put(id, pdg);
        metaStore.put(id, component);

        return id;
    }

    /**
     * Рекурсивно копирует файл/директорию в dest.
     */
    private void copyRecursively(Path src, Path dest) throws IOException {
        if (Files.isDirectory(src)) {
            if (Files.notExists(dest)) Files.createDirectories(dest);
            try (var paths = Files.list(src)) {
                for (Path p : paths.toList()) {
                    copyRecursively(p, dest.resolve(p.getFileName()));
                }
            }
        } else {
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Возвращает PDG библиотеки по id (для ProjectService).
     */
    public Graph getLibraryGraph(long id) {
        return libraryStore.get(id);
    }

    /**
     * Возвращает метаданные компонента по id.
     */
    public Component getComponent(long id) {
        return metaStore.get(id);
    }

    public Set<Long> getIds() {
        return metaStore.keySet();
    }
}
