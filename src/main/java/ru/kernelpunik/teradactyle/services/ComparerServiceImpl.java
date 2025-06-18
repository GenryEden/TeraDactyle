package ru.kernelpunik.teradactyle.services;

import org.springframework.stereotype.Service;
import org.treesitter.TSParser;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.tokenizer.Fingerprinter;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ComparerServiceImpl implements IComparerService {
    private final String processorID = String.format("%04d", System.nanoTime() % 10_000);
    private final AtomicInteger threadCount = new AtomicInteger();
    protected final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2 * Runtime.getRuntime().availableProcessors(),
            2 * Runtime.getRuntime().availableProcessors(),
            0,
            TimeUnit.MILLISECONDS,
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
    private final ThreadLocalFingerprinterMap fingerprinterMap = new ThreadLocalFingerprinterMap();
    @Override
    public double compareSolutions(String input, String reference, Language language) throws ExecutionException, InterruptedException {
        Future<Set<Integer>> referenceFuture = getFingerprintsFuture(reference, language).thenApplyAsync(
                (iterator) -> {
                    Set<Integer> ans = new HashSet<>();
                    while (iterator.hasNext()) {
                        ans.add(iterator.next());
                    }
                    return ans;
                }, executor
        );;
        Future<Iterator<Integer>> inputFuture = getFingerprintsFuture(input, language);
        Set<Integer> referenceFingerprints = referenceFuture.get();
        Iterator<Integer> inputFingerprints = inputFuture.get();
        long total = 0;
        long collisions = 0;
        while (inputFingerprints.hasNext()) {
            total++;
            if (referenceFingerprints.contains(inputFingerprints.next())) {
                collisions++;
            }
        }
        return ((double) collisions) / total;
    }



    public CompletableFuture<Iterator<Integer>> getFingerprintsFuture(String input, Language language) {
        CompletableFuture<Iterator<Integer>> future = new CompletableFuture<>();
        future.completeAsync(() -> {
            return getFingerprintsNotThreaded(input, language);
        }, executor);
        return future;
    }

    public Iterator<Integer> getFingerprintsNotThreaded(String input, Language language) {
        Fingerprinter fingerprinter = getLocalFingerprinter(language);
        return fingerprinter.getFingerprints(input);
    }

    private Fingerprinter getLocalFingerprinter(Language language) {
        Map<Language, Fingerprinter> langMap = fingerprinterMap.get();
        Fingerprinter fingerprinter = langMap.get(language);
        if (fingerprinter == null) {
            TSParser tsParser = new TSParser();
            tsParser.setLanguage(language.tsLanguage);
            fingerprinter = new Fingerprinter(tsParser, 20, 5);
            langMap.put(language, fingerprinter);
        }
        return fingerprinter;
    }

    private class ThreadLocalFingerprinterMap extends ThreadLocal<Map<Language, Fingerprinter>> {
        @Override
        protected Map<Language, Fingerprinter> initialValue() {
            return new HashMap<>();
        }
    }
}
