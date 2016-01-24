package coda.graph.neo4j.rule;

import coda.graph.neo4j.ClassProperties;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static coda.util.Maps.e;
import static coda.util.Maps.map;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ValueObjectAndFinalFields {

    private final GraphDatabaseService graph;

    public ValueObjectAndFinalFields(GraphDatabaseService graph) {
        this.graph = graph;
    }

    public List<NonFinalField> queryNonFinalFields(long annotationNodeId) {
        List<NonFinalField> found = new ArrayList<>();

        long start = System.nanoTime();
        try (Transaction tx = graph.beginTx();
             Result result = graph.execute(
                     "" +
                             "start t=node({ANNOTATION_ID})\n" +
                             "match n-[:ANNOTATED_WITH]->a,\n" +
                             "      a-[:OF_TYPE]->t,\n" +
                             "      n-[:WITH_FIELD]->f,\n" +
                             "      f-[:OF_TYPE*]->ft\n" +
                             "where \n" +
                             "      f.is_final = false\n" +
                             "return n, f, collect(ft) as ts",
                     map(e("ANNOTATION_ID", annotationNodeId)))) {
            while (result.hasNext()) {
                Map<String, Object> row = result.next();

                String typeNode = (String) ((Node) row.get("n")).getProperty(ClassProperties.TYPE_NAME);
                String fieldNode = (String) ((Node) row.get("f")).getProperty(ClassProperties.FIELD_NAME);
                List<String> types = ((Collection<Node>) row.get("ts"))
                        .stream()
                        .map(n -> (String) n.getProperty(ClassProperties.TYPE_NAME))
                        .collect(Collectors.toList());

                found.add(new NonFinalField(typeNode, fieldNode, types));
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("Field analysis in " + (elapsed / 1e6) + "ms");
        }
        return found;
    }

    public static class NonFinalField {
        public final String typeName;
        public final String fieldName;
        public final List<String> fieldTypes;

        public NonFinalField(String typeName, String fieldName, List<String> fieldTypes) {
            this.typeName = typeName;
            this.fieldName = fieldName;
            this.fieldTypes = fieldTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass())
                return false;

            NonFinalField that = (NonFinalField) o;

            return typeName.equals(that.typeName)
                    && fieldName.equals(that.fieldName)
                    && fieldTypes.equals(that.fieldTypes);
        }

        @Override
        public int hashCode() {
            int result = typeName.hashCode();
            result = 31 * result + fieldName.hashCode();
            result = 31 * result + fieldTypes.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "NonFinalField{" +
                    "typeName='" + typeName + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", fieldTypes=" + fieldTypes +
                    '}';
        }
    }

}
