package coda.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Files {
    private List<File> sources = new ArrayList<>();
    private List<File> sourceTrees = new ArrayList<>();
    private final Predicate<File> filter;

    public Files(Predicate<File> filter) {
        this.filter = filter;
    }

    public void addSource(File sourceFile) {
        sources.add(sourceFile);
    }

    public void addSourceTree(File sourceTree) {
        sourceTrees.add(sourceTree);
    }

    public void traverse(Consumer<File> visitor) {
        sources.forEach(visitor);
        sourceTrees.forEach((dir) -> recursivelyTraverse(dir, visitor));
    }

    private void recursivelyTraverse(File dir, Consumer<File> visitor) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory())
                recursivelyTraverse(file, visitor);
            else if (filter.test(file))
                visitor.accept(file);
        }
    }

}
