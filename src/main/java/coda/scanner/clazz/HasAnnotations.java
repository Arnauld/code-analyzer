package coda.scanner.clazz;

import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface HasAnnotations {


    void addAnnotation(AnnotationDeclaration annotationDeclaration);

    Stream<AnnotationDeclaration> annotations();
}
