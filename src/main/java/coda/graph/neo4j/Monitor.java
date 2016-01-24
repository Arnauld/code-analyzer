package coda.graph.neo4j;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public interface Monitor {
    interface Key {}
    Key startProcessing(String name);
    void endProcessing(Key key, String message);
}
