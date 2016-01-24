package coda.scanner.clazz;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ClassDeclaration implements HasAnnotations {
    private final String name;
    private final int access;
    private final List<MethodDeclaration> methods = new ArrayList<>();
    private final List<FieldDeclaration> fields = new ArrayList<>();
    private final List<AnnotationDeclaration> annotations = new ArrayList<>();
    //
    private final List<String> interfaceNames = new ArrayList<>();
    private String superClassName;

    public ClassDeclaration(String name, int access) {
        this.name = name;
        this.access = access;
    }

    public String getName() {
        return name;
    }

    public void addMethod(MethodDeclaration methodDeclaration) {
        methods.add(methodDeclaration);
    }

    public void addField(FieldDeclaration fieldDeclaration) {
        fields.add(fieldDeclaration);
    }

    public Stream<FieldDeclaration> fields() {
        return fields.stream();
    }

    @Override
    public void addAnnotation(AnnotationDeclaration annotationDeclaration) {
        annotations.add(annotationDeclaration);
    }

    @Override
    public Stream<AnnotationDeclaration> annotations() {
        return annotations.stream();
    }


    public void setSuperClass(String superClassName) {
        this.superClassName = superClassName;
    }

    public void addInterface(String interfaceName) {
        this.interfaceNames.add(interfaceName);
    }

    public Stream<MethodDeclaration> methods() {
        return methods.stream();
    }

    public Set<String> dependsOn() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add(superClassName);
        dependencies.addAll(interfaceNames);
        fields.forEach(f -> dependencies.addAll(f.dependsOn()));
        methods.forEach(f -> dependencies.addAll(f.dependsOn()));
        annotations.forEach(f -> dependencies.addAll(f.dependsOn()));
        return dependencies;
    }

    @Override
    public String toString() {
        return "ClassDeclaration{" +
                "name='" + name + '\'' +
                ", access=" + access +
                ", methods=" + methods +
                ", fields=" + fields +
                ", annotations=" + annotations +
                ", interfaceNames=" + interfaceNames +
                ", superClassName='" + superClassName + '\'' +
                '}';
    }

}
