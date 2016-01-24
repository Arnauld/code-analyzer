package coda.scanner.clazz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class AnnotationDeclaration implements HasAnnotations {

    private final String name;
    private final boolean visibleAtRuntime;
    private final Set<String> dependencies;
    private final List<AnnotationDeclaration> annotations = new ArrayList<>();

    public AnnotationDeclaration(String name, boolean visibleAtRuntime) {
        this.name = name;
        this.visibleAtRuntime = visibleAtRuntime;
        this.dependencies = new HashSet<>();
    }

    public Set<String> dependsOn() {
        Set<String> deps = new HashSet<>();
        deps.addAll(dependencies);
        annotations.forEach(f -> deps.addAll(f.dependsOn()));
        return deps;
    }

    public void declareDependency(String objectType) {
        dependencies.add(objectType);
    }

    public String getName() {
        return name;
    }

    public boolean visibleAtRuntime() {
        return visibleAtRuntime;
    }

    @Override
    public void addAnnotation(AnnotationDeclaration annotationDeclaration) {
        annotations.add(annotationDeclaration);
    }

    @Override
    public Stream<AnnotationDeclaration> annotations() {
        return annotations.stream();
    }
}
