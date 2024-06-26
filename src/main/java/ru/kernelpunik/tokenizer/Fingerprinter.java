package ru.kernelpunik.tokenizer;

import org.treesitter.TSInputEncoding;
import org.treesitter.TSParser;
import org.treesitter.TSTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class Fingerprinter {
    private static final int DEFAULT_K = 20;
    private static final int DEFAULT_WINNOW_LENGTH = 5;
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

    public synchronized Iterator<Integer> getFingerprints(String source) {
        TSTree tree = tsParser.parseStringEncoding(null, source, TSInputEncoding.TSInputEncodingUTF8);
        KGram kGram = new KGram(k);
        return new WinnowingIterator(
                new MapIterator<>(
                        new TSTreeDFS(tree.getRootNode()),
                        (node) -> {
                            kGram.put(node.getType().hashCode());
                            return kGram.getHashCode();
                        }
                ), 5
        );
    }
}
