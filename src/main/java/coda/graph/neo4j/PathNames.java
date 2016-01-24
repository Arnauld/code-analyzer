package coda.graph.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class PathNames {
    private final List<String> names = new ArrayList<>();

    public void add(String name) {
        names.add(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PathNames pathNames = (PathNames) o;
        return names.equals(pathNames.names);
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }

    @Override
    public String toString() {
        return "PathNames{" + names + '}';
    }

    public static PathNames of(Stream<String> names) {
        PathNames pathNames = new PathNames();
        names.forEach(pathNames::add);
        return pathNames;
    }

}
