package coda.graph.neo4j;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.util.concurrent.TimeUnit;

public class CachedNodeFinder {

    private final NodeCache nodeCache = new MapNodeCache();
    private final GraphDatabaseService graph;

    public CachedNodeFinder(final GraphDatabaseService graph) {
        this.graph = graph;
    }

    public ConstraintDefinition createUniqueConstraintIfMissing(GraphDatabaseService graph,
                                                                String labelName,
                                                                String propertyName) {
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();

            Label label = DynamicLabel.label(labelName);
            ConstraintDefinition def = findConstraintOnProperty(schema, label, propertyName);

            if (def == null)
                graph.schema()
                        .constraintFor(label)
                        .assertPropertyIsUnique(propertyName)
                        .create();
            tx.success();
            return def;
        }
    }

    private ConstraintDefinition findConstraintOnProperty(Schema schema,
                                                          Label label,
                                                          String propertyName) {
        for (ConstraintDefinition def : schema.getConstraints(label)) {
            int nb = 0;
            boolean found = false;
            for (String s : def.getPropertyKeys()) {
                nb++;
                if (s.equals(propertyName))
                    found = true;
            }

            if (found && nb == 1)
                return def;
        }
        return null;
    }

    private IndexDefinition createIndex(GraphDatabaseService graph, String labelName, String propertyName) {
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();

            Label label = DynamicLabel.label(labelName);
            IndexDefinition def = findIndexOnProperty(schema, label, propertyName);
            if (def == null)
                def = schema.indexFor(label).on(propertyName).create();
            tx.success();
            return def;
        }
    }

    private static IndexDefinition findIndexOnProperty(Schema schema,
                                                       Label label,
                                                       String propertyName) {
        for (IndexDefinition def : schema.getIndexes(label)) {
            int nb = 0;
            boolean found = false;
            for (String s : def.getPropertyKeys()) {
                nb++;
                if (s.equals(propertyName))
                    found = true;
            }

            if (found && nb == 1)
                return def;
        }
        return null;
    }

    private void awaitIndexPopulationIsComplete(IndexDefinition indexDefinition) {
        try (Transaction tx = graph.beginTx()) {
            Schema schema = graph.schema();
            schema.awaitIndexOnline(indexDefinition, 10, TimeUnit.SECONDS);
        }
    }

    public Node getOrCreateNode(Transaction tx,
                                String nodeType,
                                String propertyName,
                                String propertyValue) {
        String cacheKey = nodeType + ":" + propertyValue;
        final Node cachedNode = nodeCache.getCachedNode(cacheKey);
        if (cachedNode != null) {
            return cachedNode;
        }

        Label label = DynamicLabel.label(nodeType);

        final Node createdNode = findNode(label, propertyName, propertyValue);
        if (createdNode != null) {
            return createdNode;
        }

        Node typeNode = graph.createNode(label);
        typeNode.setProperty(propertyName, propertyValue);
        nodeCache.cacheNode(cacheKey, typeNode);
        return typeNode;
    }

    private Node findNode(Label label, String propertyName, String value) {
        try (ResourceIterator<Node> users =
                     graph.findNodes(label, propertyName, value)) {
            if (users.hasNext())
                return users.next();
            return null;
        }
    }

}
