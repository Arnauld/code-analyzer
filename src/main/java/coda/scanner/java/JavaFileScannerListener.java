package coda.scanner.java;

import coda.util.ProxyUtils;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;

import java.util.Collection;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface JavaFileScannerListener {

    static JavaFileScannerListener compose(JavaFileScannerListener listener, JavaFileScannerListener... listeners) {
        return ProxyUtils.broadcast(JavaFileScannerListener.class, listener, listeners);
    }

    default void aboutToParsePackages(Collection<JavaPackage> packages) {
    }

    default void aboutToParseClasses(Collection<JavaClass> classes) {
    }

    default void enteringPackage(JavaPackage pkg) {
    }

    default void exitingPackage(JavaPackage pkg) {
    }

    default void enteringClass(JavaClass klazz) {
    }

    default void exitingClass(JavaClass klazz) {
    }

    default void enteringMethod(JavaMethod method) {
    }

    default void exitingMethod(JavaMethod method) {
    }
}
