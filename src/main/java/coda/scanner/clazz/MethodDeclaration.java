package coda.scanner.clazz;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class MethodDeclaration {
    private final int access;
    private final String name;
    private final Collection<String> parameterTypes;
    private final String returnType;
    private final List<String> exceptionList;
    private final Set<String> bodyDependencies;

    public MethodDeclaration(int access, String name, Collection<String> parameterTypes, String returnType, List<String> exceptionList) {
        this.access = access;
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.exceptionList = exceptionList;
        this.bodyDependencies = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Set<String> dependsOn() {
        Set<String> deps = new HashSet<>();
        deps.addAll(parameterTypes);
        deps.add(returnType);
        deps.addAll(exceptionList);
        deps.addAll(bodyDependencies);

        return deps;
    }


    public void declareBodyDependency(String objectType) {
        bodyDependencies.add(objectType);
    }

    @Override
    public String toString() {
        return "MethodDeclaration{" +
                "access=" + access +
                ", name='" + name + '\'' +
                ", parameterTypes=" + parameterTypes +
                ", returnType='" + returnType + '\'' +
                ", exceptionList=" + exceptionList +
                ", bodyDependencies=" + bodyDependencies +
                '}';
    }
}
