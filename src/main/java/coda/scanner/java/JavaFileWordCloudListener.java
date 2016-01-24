package coda.scanner.java;

import coda.util.CamelCaseSplitter;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMember;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class JavaFileWordCloudListener implements JavaFileScannerListener {

    private final Function<String, String[]> toWords;
    private final Consumer<String> wordConsumer;

    public JavaFileWordCloudListener(Consumer<String> wordConsumer) {
        this(CamelCaseSplitter::split, wordConsumer);
    }

    public JavaFileWordCloudListener(Function<String, String[]> splitToWords, Consumer<String> wordConsumer) {
        this.toWords = splitToWords;
        this.wordConsumer = wordConsumer;
    }

    @Override
    public void enteringPackage(JavaPackage pkg) {
    }

    @Override
    public void enteringClass(JavaClass klazz) {
        splitWords(klazz.getName());
        processQualifiedName(klazz
                .getAnnotations()
                .stream()
                .map((a) -> a.getType().getName()));
        processQualifiedName(klazz
                .getImplements()
                .stream()
                .map(JavaType::getCanonicalName));
        processQualifiedName(klazz
                .getFields()
                .stream()
                .map(JavaMember::getName));
    }

    @Override
    public void enteringMethod(JavaMethod method) {
        splitWords(method.getName());
    }

    private void processQualifiedName(Stream<String> qualifiedName) {
        qualifiedName
                .map(this::removePackageName)
                .forEach(this::splitWords);
    }

    private String removePackageName(String s) {
        int index = s.lastIndexOf('.');
        if (index >= 0)
            return s.substring(index + 1);
        return s;
    }

    private void splitWords(String name) {
        Stream.of(name.split("(\\s+|\\.)"))
                .map(toWords)
                .flatMap(Stream::of)
                .map(String::toLowerCase)
                .forEach(wordConsumer);
    }
}
