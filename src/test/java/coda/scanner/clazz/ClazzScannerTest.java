package coda.scanner.clazz;

import org.junit.Ignore;
import org.junit.Test;

import javax.swing.JPanel;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ClazzScannerTest {

    @Test
    public void usecase() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/classes"))
                .process();

        declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith("ClassDeclaration"))
                .forEach(d ->
                        d.dependsOn().stream().forEach(System.out::println));
    }

    @Test
    public void should_detect_new_multi_array_in_method_body() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/test-classes"))
                .process();

        ClassDeclaration clazz = declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith("CaseMultiArray"))
                .findFirst()
                .get();

        assertThat(clazz
                .methods()
                .filter(m -> m.getName().equals("doo"))
                .findFirst()
                .get()
                .dependsOn()).contains("void", "java.io.PrintStream", "java.lang.System", "javax.swing.JPanel");
    }

    @SuppressWarnings("unused")
    public static class CaseMultiArray {
        public void doo() {
            Object o = new JPanel[][]{{}};
            System.out.println(o);
        }
    }

    @Test
    public void should_detect_annotation_value_type() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/test-classes"))
                .process();

        ClassDeclaration clazz = declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith(CaseClassAnnotation.class.getSimpleName()))
                .findFirst()
                .get();
        System.out.println("ClazzScannerTest.should_detect_annotation_value_type:: " + clazz);
    }


    @SuppressWarnings("unused")
    @Ignore
    @Timeout(unit = TimeUnit.DAYS, components = "eds", type = JPanel.class)
    public static class CaseClassAnnotation {
    }

    @SuppressWarnings("unused")
    public @interface Timeout {
        String name() default "Zog";

        String[] components();

        TimeUnit unit();

        Class<?> type();
    }

    @Test
    public void should_not_consider_itself_as_field_owner_in_dependsOn() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/test-classes"))
                .process();

        ClassDeclaration clazz = declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith(ClassWithFieldDependsOnCase.class.getSimpleName()))
                .findFirst()
                .get();

        assertThat(clazz.dependsOn()).doesNotContain(clazz.getName());
        assertThat(clazz.dependsOn()).contains("void", "java.lang.Object", "java.lang.String");
    }

    @SuppressWarnings("unused")
    private static class ClassWithFieldDependsOnCase {
        public String field;
    }

    @Test
    public void should_detect_type_of_field_for_list_of_type() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/test-classes"))
                .process();

        ClassDeclaration clazz = declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith(CaseFieldWithGeneric.class.getSimpleName()))
                .findFirst()
                .get();

        FieldDeclaration field = clazz.fields().filter(f -> f.getName().equals("names")).findFirst().get();
        assertThat(field.dependsOn())
                .containsOnly("java.util.List",
                        "java.lang.String");
    }

    @SuppressWarnings("unused")
    public static class CaseFieldWithGeneric {
        public List<String> names;
    }

    @Test
    public void should_detect_type_of_field_with_generic_of_gerics_type() {
        DeclarationFactory declarationFactory = new DeclarationFactory();
        new ClazzScanner(declarationFactory)
                .appendSourceTree(new File("/Users/Arnauld/Projects/code-analyzer/target/test-classes"))
                .process();

        ClassDeclaration clazz = declarationFactory
                .classDeclarations()
                .filter(d -> d.getName().endsWith(CaseFieldWithGenericOfGeneric.class.getSimpleName()))
                .findFirst()
                .get();

        FieldDeclaration field = clazz.fields().filter(f -> f.getName().equals("names")).findFirst().get();
        assertThat(field.typeNames().collect(Collectors.toList()))
                .containsOnly("java.util.List",
                        "java.util.Map",
                        "java.util.Set",
                        "java.lang.String",
                        "java.lang.Number");
    }

    @SuppressWarnings("unused")
    public static class CaseFieldWithGenericOfGeneric {
        public List<Map<? extends Number, Set<String[]>>> names;
    }

}