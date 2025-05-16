package ru.kernelpunik.tokenizer;

import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectProcessor {
    private final static double MIN_PERCENT = 0.5; // TODO: move it to config and parameters
    private final static Logger LOG = Logger.getLogger(ProjectProcessor.class.getName());
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
                thread.setName("project-processor-" + processorID + "-" + threadNumber);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public ProjectProcessor(FingerprintRepository fingerprintRepository) {
        this.fingerprintRepository = fingerprintRepository;
    }

    public CompletableFuture<CollisionReport> processSource(File file) {
        CompletableFuture<CollisionReport> future = new CompletableFuture<>();
        executor.execute(() -> {
            Language language = Language.getByFile(file);
            if (language == null) {
                future.complete(new CollisionReport());
            }
            CollisionReport result = new CollisionReport(file.toString());
            Iterator<Integer> fingerprints;
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
            try {
                fingerprints = fingerprinter.getFingerprints(file);
            } catch (IOException e) {
                LOG.log(Level.WARNING,"Error happened while getting fingerprints from " + file, e);
                future.completeExceptionally(e);
                return;
            }
            while (fingerprints.hasNext()) {
                int fingerprint = fingerprints.next();
                result.addFingerprints();
                List<Fingerprint> founds = fingerprintRepository.findByValueAndLanguageId(fingerprint, language.id);
                for (Fingerprint found: founds) {
                    result.addCollisionWith(found.getComponent());
                }
            }
            future.complete(result);
        });

        return future;
    }

    public CompletableFuture<TreeNode<CollisionReport>> processTree(File file) {
        if (file.isFile()) {
            return processSource(file).thenApplyAsync(
                    (CollisionReport collisionReport) ->
                            new TreeNode<>(collisionReport, new ArrayList<>())
            );
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) {
                return null;
            }
            ConcurrentLinkedQueue<TreeNode<CollisionReport>> childrenNodes = new ConcurrentLinkedQueue<>();
            List<CompletableFuture<TreeNode<CollisionReport>>> futures = new ArrayList<>(children.length);
            AtomicLong totalTokens = new AtomicLong();
            ConcurrentHashMap<Component, Long> enoughEverywhere = new ConcurrentHashMap<>();
            ConcurrentHashMap<Component, Long> notEnoughEverywhere = new ConcurrentHashMap<>();
            for (File child: children) {
                futures.add(
                        processTree(child).thenApplyAsync(
                                (TreeNode<CollisionReport> treeNode) -> {
                                    if (treeNode == null) return null;
                                    childrenNodes.add(treeNode);
                                    for (Component component: treeNode.getValue().getCollisions().keySet()) {
                                        enoughEverywhere.put(component, 0L);
                                        notEnoughEverywhere.put(component, 0L);
                                    }
                                    totalTokens.addAndGet(treeNode.getValue().getTotalFingerprints());
                                    return treeNode;
                                },
                                executor
                        )
                );
            }
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApplyAsync(
                    unused -> {
                        Map<Component, Long> total = new HashMap<>();
                        for (TreeNode<CollisionReport> child : childrenNodes) {
                            long totalFingerprints = child.getValue().getTotalFingerprints();
                            for (var collision : child.getValue().getCollisions().entrySet()) {
                                total.put(
                                        collision.getKey(),
                                        total.getOrDefault(collision.getKey(), 0L) + collision.getValue()
                                );
                                if (collision.getValue() * 1.0f / totalFingerprints >= MIN_PERCENT) {
                                    notEnoughEverywhere.remove(collision.getKey());
                                    if (enoughEverywhere.containsKey(collision.getKey())) {
                                        enoughEverywhere.put(
                                                collision.getKey(),
                                                enoughEverywhere.get(collision.getKey())
                                                        + collision.getValue()
                                        );
                                    }
                                } else {
                                    enoughEverywhere.remove(collision.getKey());
                                    if (notEnoughEverywhere.containsKey(collision.getKey())) {
                                        notEnoughEverywhere.put(
                                                collision.getKey(),
                                                notEnoughEverywhere.get(collision.getKey())
                                                        + collision.getValue()
                                        );
                                    }
                                    child.getValue().getCollisions().remove(collision.getKey());
                                }
                            }
                        }
                        CollisionReport report = new CollisionReport(file.toString());
                        report.addAllCollisions(enoughEverywhere);
                        report.addAllCollisions(notEnoughEverywhere);
                        report.addFingerprints(totalTokens.get());
                        for (var collision : total.entrySet()) {
                            if (!enoughEverywhere.containsKey(collision.getKey())
                                    && !notEnoughEverywhere.containsKey(collision.getKey())) {
                                report.addCollisionWith(collision.getKey(), collision.getValue());
                            }
                        }
                        for (var child : childrenNodes) {
                            child.getValue().removeBelow(MIN_PERCENT);
                            child.getValue().removeAll(enoughEverywhere.keySet());
                        }
                        return new TreeNode<>(report, childrenNodes.stream().toList());
                    }
            );
    } else {
        return CompletableFuture.completedFuture(null);
    }
}
}
