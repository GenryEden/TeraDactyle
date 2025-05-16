package ru.kernelpunik.tokenizer;

import org.springframework.dao.DataIntegrityViolationException;
import org.treesitter.TSParser;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ComponentProcessor {
    final Map<Language, Fingerprinter> fingerprinterMap = new ConcurrentHashMap<>();
    final FingerprintRepository fingerprintRepository;
    final String processorID = String.format("%04d", System.nanoTime() % 10_000);
    final AtomicInteger threadCount = new AtomicInteger();
    final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2 * Runtime.getRuntime().availableProcessors(),
             2 * Runtime.getRuntime().availableProcessors(),
             1,
             TimeUnit.MINUTES,
             new ArrayBlockingQueue<>(1024),
            r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                String threadNumber = String.format("%02d", threadCount.incrementAndGet());
                thread.setName("component-processor-" + processorID + "-" + threadNumber);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public ComponentProcessor(FingerprintRepository fingerprintRepository) {
        this.fingerprintRepository = fingerprintRepository;
    }

    public CompletableFuture<Void> processComponent(long componentId, File directory) throws IOException {
        Language language = Language.getByFile(directory);
        if (language != null) {
            return processComponentFile(componentId, directory, language).handleAsync(
                    (aBoolean, throwable) -> null
            );
        } else if (directory.isDirectory()) {
            var fileList = directory.listFiles();
            if (fileList == null) {
                return CompletableFuture.completedFuture(null);
            }
            List<CompletableFuture<Void>> futures = new ArrayList<>(fileList.length);
            for (File file : fileList) {
                futures.add(processComponent(componentId, file));
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        }
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Boolean> processComponentFile(long componentId, File file, Language language) throws IOException {
        Fingerprinter fingerprinter = fingerprinterMap.getOrDefault(language, null);
        if (fingerprinter == null) {
            synchronized (fingerprinterMap) {
                if (fingerprinterMap.containsKey(language)) {
                    fingerprinter = fingerprinterMap.get(language);
                } else {
                    TSParser tsParser = new TSParser();
                    tsParser.setLanguage(language.tsLanguage);
                    fingerprinter = new Fingerprinter(tsParser);
                    fingerprinterMap.put(language, fingerprinter);
                }
            }
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Fingerprinter finalFingerprinter = fingerprinter;
        executor.execute(() -> {
            Iterator<Integer> fingerprints = null;
            try {
                fingerprints = finalFingerprinter.getFingerprints(file);
            } catch (IOException e) {
                future.complete(false);
                return;
            }
            while (fingerprints.hasNext()) {
                int fingerprint = fingerprints.next();
                try {
                    fingerprintRepository.save(new Fingerprint(fingerprint, componentId, language.id, null));
                } catch (DataIntegrityViolationException e) {
                    // pass
                }
            }
            future.complete(true);
        });
        return future;
    }
}
