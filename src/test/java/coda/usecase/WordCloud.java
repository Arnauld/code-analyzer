package coda.usecase;

import coda.scanner.java.JavaFileLoggerListener;
import coda.scanner.java.JavaFileScanner;
import coda.scanner.java.JavaFileScannerListener;
import coda.scanner.java.JavaFileWordCloudListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class WordCloud {
    public static void main(String[] args) {
        String source = "/Users/Arnauld/Projects/etai/coaching_ddd_etai_salto/kart/kart/" +
                "src/main/java/fr/etai/kart/domain";

        List<String> words = new ArrayList<>();
        JavaFileWordCloudListener listener = new JavaFileWordCloudListener(words::add);
        JavaFileLoggerListener logger = new JavaFileLoggerListener();
        new JavaFileScanner()
                .appendSourceTree(new File(source))
                .process(JavaFileScannerListener.compose(logger, listener));

        words.stream().forEach(System.out::println);
    }
}
