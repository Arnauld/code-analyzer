package coda.scanner.java;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class JavaFileScanner {

    private JavaProjectBuilder builder = new JavaProjectBuilder();

    public JavaFileScanner appendSourceFile(File sourceFile) {
        try {
            builder.addSource(sourceFile);
            return this;
        } catch (IOException e) {
            throw new ScanningException("Invalid source file <" + sourceFile + ">");
        }
    }

    public JavaFileScanner appendSourceTree(File sourceTree) {
        builder.addSourceTree(sourceTree);
        return this;
    }

    public void process(JavaFileScannerListener listener) {
        Collection<JavaPackage> packages = builder.getPackages();
        listener.aboutToParsePackages(packages);
        for (JavaPackage pkg : packages) {
            analyzePackage(pkg, listener);
        }

        Collection<JavaClass> classes = builder.getClasses();
        listener.aboutToParseClasses(classes);
        for (JavaClass klazz : classes) {
            analyzeClass(klazz, listener);
        }
    }

    protected void analyzePackage(JavaPackage pkg, JavaFileScannerListener listener) {
        listener.enteringPackage(pkg);
        for (JavaClass klazz : pkg.getClasses()) {
            analyzeClass(klazz, listener);
        }
        listener.exitingPackage(pkg);
    }

    protected void analyzeClass(JavaClass klazz, JavaFileScannerListener listener) {
        listener.enteringClass(klazz);
        for (JavaMethod method : klazz.getMethods()) {
            analyzeMethod(method, listener);
        }
        listener.exitingClass(klazz);
    }

    protected void analyzeMethod(JavaMethod method, JavaFileScannerListener listener) {
        listener.enteringMethod(method);
        listener.exitingMethod(method);
    }


}
