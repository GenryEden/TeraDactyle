package ru.kernelpunik.tokenizer;

import org.antlr.v4.runtime.misc.MurmurHash;
import org.treesitter.TSInputEncoding;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.yaml.snakeyaml.util.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class Fingerprinter {
    private static final int HASH_SEED = 1337;
    private static final int DEFAULT_K = 10;
    private static final int DEFAULT_WINNOW_LENGTH = 20;
    public static final AtomicInteger CNT = new AtomicInteger();
    private final TSParser tsParser;
    private final int k;
    private final int winnowLength;


    public Fingerprinter(TSParser tsParser) {
        this(tsParser, DEFAULT_K, DEFAULT_WINNOW_LENGTH);
    }

    public Fingerprinter(TSParser tsParser, int k, int winnowLength) {
        this.tsParser = tsParser;
        this.k = k;
        this.winnowLength = winnowLength;
    }

    private static String readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder ans = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                ans.append(line);
                ans.append("\n");
            }
            return ans.toString();
        }
    }

    public Iterator<Integer> getFingerprints(File file) throws IOException {
        return getFingerprints(readFile(file));
    }

    public Iterator<Integer> getFingerprints(String source) {
        TSParser tsParser1 = new TSParser();
        tsParser1.setLanguage(tsParser.getLanguage());
        TSTree tree = tsParser1.parseStringEncoding(null, source, TSInputEncoding.TSInputEncodingUTF8);
        KGram kGram = new KGram(k);
        return new WinnowingIterator(
                new MapIterator<>(
                        new TSTreeDFS(tree.getRootNode()),
                        (node) -> {
                            CNT.incrementAndGet();
                            String type = node.getType();
                            Byte[] bytes = new Byte[type.length()];
                            for (int i = 0; i < type.length(); i++) {
                                bytes[i] = (byte) type.charAt(i);
                            }
                            kGram.put(MurmurHash.hashCode(bytes, HASH_SEED));
                            return kGram.getHashCode();
                        }
                ), winnowLength
        );
    }
}
