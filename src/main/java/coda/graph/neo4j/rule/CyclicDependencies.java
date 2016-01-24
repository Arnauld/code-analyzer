package coda.graph.neo4j.rule;

import coda.graph.neo4j.Monitor;
import coda.graph.neo4j.PathNames;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class CyclicDependencies {

    private final GraphDatabaseService graph;

    public CyclicDependencies(GraphDatabaseService graph) {
        this.graph = graph;
    }

    public List<PathNames> queryCycles() {
        List<PathNames> allPathNames = new ArrayList<>();

        long start = System.nanoTime();
        try (Transaction ignored = graph.beginTx();
             Result result = graph.execute(
                     "match p=n-[:DEPENDS_ON*]->n\n" +
//                             "where" +
//                             " length(p) > 1\n" +
                             "return p;")) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    Path p = (Path) column.getValue();
                    PathNames pathNames = new PathNames();
                    for (Node node : p.nodes()) {
                        pathNames.add((String) node.getProperty("name"));
                    }
                    allPathNames.add(pathNames);
                }
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Cycle analysis in " + (elapsed / 1e6) + "ms");
        }
        return allPathNames;
    }
}
