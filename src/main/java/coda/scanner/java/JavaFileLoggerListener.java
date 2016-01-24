package coda.scanner.java;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class JavaFileLoggerListener implements JavaFileScannerListener {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void aboutToParsePackages(Collection<JavaPackage> packages) {
        log.info("About to parse packages {}", packages.stream().map(JavaPackage::getName).collect(toList()));
    }

    @Override
    public void aboutToParseClasses(Collection<JavaClass> classes) {
        log.info("About to parse classes {}", classes.stream().map(JavaClass::getName).collect(toList()));
    }

    @Override
    public void enteringPackage(JavaPackage pkg) {
        log.info("Entering package {}", pkg.getName());
    }

    @Override
    public void exitingPackage(JavaPackage pkg) {
        log.info("Exiting package {}", pkg.getName());
    }

    @Override
    public void enteringClass(JavaClass klazz) {
        log.info("Entering class {}", klazz.getName());
    }

    @Override
    public void exitingClass(JavaClass klazz) {
        log.info("Exiting class {}", klazz.getName());
    }

    @Override
    public void enteringMethod(JavaMethod method) {
        log.info("Entering method {}", method.getCallSignature());
    }

    @Override
    public void exitingMethod(JavaMethod method) {
        log.info("Exiting method {}", method.getCallSignature());
    }
}
