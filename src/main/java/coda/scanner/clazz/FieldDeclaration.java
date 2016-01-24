package coda.scanner.clazz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class FieldDeclaration implements HasAnnotations {
    private final int access;
    private final String name;
    private final Set<String> typeNames;
    private final List<AnnotationDeclaration> annotations = new ArrayList<>();

    public FieldDeclaration(int access, String name, String typeName) {
        this.access = access;
        this.name = name;
        this.typeNames = new HashSet<>();
        typeNames.add(typeName);
    }

    public Access.Visibility visibility() {
        return Access.visibility(access);
    }

    public boolean isFinal() {
        return Access.isFinal(access);
    }

    public boolean isStatic() {
        return Access.isStatic(access);
    }

    public Stream<String> typeNames() {
        return typeNames.stream();
    }

    public void declareTypes(Iterable<String> types) {
        types.forEach(typeNames::add);
    }

    public Set<String> dependsOn() {
        Set<String> deps = new HashSet<>();
        deps.addAll(typeNames);
        annotations.forEach(f -> deps.addAll(f.dependsOn()));
        return deps;
    }

    public String getName() {
        return name;
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
