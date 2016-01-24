package coda.graph.neo4j;

import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MapNodeCache implements NodeCache {
    private final Map<String, Node> typeNodes = new ConcurrentHashMap<>(1000);
    private AtomicInteger hits = new AtomicInteger();

    public Node getCachedNode(final String typeName) {
        final Node node = typeNodes.get(typeName);
        if (node != null)
            hits.incrementAndGet();
        return node;
    }

    public void cacheNode(final String typeName, final Node newTypeNode) {
        typeNodes.put(typeName, newTypeNode);
    }

    @Override
    public String toString() {
        return "size " + typeNodes.size() + " hits " + hits.get();
    }
}
