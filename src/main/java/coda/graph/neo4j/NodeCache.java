package coda.graph.neo4j;
import org.neo4j.graphdb.Node;

public interface NodeCache {
    Node getCachedNode(String typeName);

    void cacheNode(String typeName, Node newTypeNode);
}