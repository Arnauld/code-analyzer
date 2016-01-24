package coda.scanner.clazz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class DeclarationFactory {

    private Collection<ClassDeclaration> classes = new ArrayList<>();

    public ClassDeclaration createClassInfo(String name, int access) {
        return store(new ClassDeclaration(name, access));
    }

    private ClassDeclaration store(ClassDeclaration classDeclaration) {
        classes.add(classDeclaration);
        return classDeclaration;
    }

    public MethodDeclaration createMethodInfo(int access, String name, Collection<String> params, String returnType, List<String> exceptionList) {
        return new MethodDeclaration(access, name, params, returnType, exceptionList);
    }

    public FieldDeclaration createFieldInfo(int access, String name, String typeName) {
        return new FieldDeclaration(access, name, typeName);
    }

    public Stream<ClassDeclaration> classDeclarations() {
        return classes.stream();
    }

    public void dump() {
        classes.forEach(System.out::println);
    }

    public AnnotationDeclaration createAnnotationInfo(String typeName, boolean visibleAtRuntime) {
        return new AnnotationDeclaration(typeName, visibleAtRuntime);
    }
}
