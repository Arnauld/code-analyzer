package coda.scanner.clazz;

import coda.graph.neo4j.CachedNodeFinder;
import coda.graph.neo4j.ClassMapping;
import coda.graph.neo4j.ClassProperties;
import coda.graph.neo4j.ClassRelations;
import coda.graph.neo4j.PathNames;
import coda.graph.neo4j.rule.CyclicDependencies;
import coda.graph.neo4j.rule.ValueObjectAndFinalFields;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.logging.slf4j.Slf4jLogProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static coda.util.Maps.e;
import static coda.util.Maps.map;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Neo4jUsageTest {

    private static final File dbBaseDir = new File("/Users/Arnauld/Projects/code-analyzer/target");

    private GraphDatabaseService graph;
    private DeclarationFactory declarationFactory;
    private CachedNodeFinder finder;
    private ClassMapping classMapping;

    @After
    public void closeDb() {
        if (graph != null)
            graph.shutdown();
    }

    @Test
    @Ignore("Not a real test - it demonstrates how to...")
    public void usecase() {
        File databaseLocation = new File(dbBaseDir + "/neo4j-db");
        dropNeoDb(databaseLocation);

        graph = openDb(databaseLocation);
        finder = new CachedNodeFinder(graph);
        classMapping = new ClassMapping(graph, finder);

        Node node;
        try (Transaction tx = graph.beginTx()) {
            ClassDeclaration classDeclaration;

            classDeclaration = scanClass(SimpleCase.class.getSimpleName());
            node = classMapping.insert(tx, classDeclaration);

            classDeclaration = scanClass(ParentClass.class.getSimpleName());
            classMapping.insert(tx, classDeclaration);

            classDeclaration = scanClass(MiddleClass.class.getSimpleName());
            classMapping.insert(tx, classDeclaration);
            tx.success();
        }

        System.out.println("-1-------------------------------------------------");
        try (Transaction ignored = graph.beginTx()) {
            TraversalDescription td = graph.traversalDescription()
                    .breadthFirst()
                    .relationships(ClassRelations.DEPENDS_ON)
                    .evaluator(Evaluators.excludeStartPosition());
            for (Path path : td.traverse(node)) {
                System.out.println(">>" + path.endNode().getAllProperties());
            }
        }


        System.out.println("-2-Indexes-----------------------------------------");
        try (Transaction ignored = graph.beginTx()) {
            Schema schema = graph.schema();
            for (IndexDefinition def : schema.getIndexes()) {
                System.out.println(">> '" + def.getLabel().name() + "' [" + def.getPropertyKeys() + "]");
            }
        }

        System.out.println("-2b-Nodes------------------------------------------");
        try (Transaction ignored = graph.beginTx();
             ResourceIterator<Node> nodes = classMapping.findTypes()) {
            while (nodes.hasNext()) {
                Node next = nodes.next();
                System.out.println(">> " + next + " : '" + next.getProperty(ClassProperties.TYPE_NAME) + "'");
            }
        }

        System.out.println("-3-------------------------------------------------");
        try (Transaction ignored = graph.beginTx();
             Result result = graph.execute(
                     "match (n {name: 'coda.scanner.clazz.Neo4jUsageTest$SimpleCase'}) return n, n.name")) {
            while (result.hasNext()) {
                String str = "";
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {

                    str += column.getKey() + ": " + column.getValue() + "; ";
                }
                System.out.println(">> " + str);
            }

        }

        System.out.println("-4-------------------------------------------------");
        long start = System.nanoTime();
        try (Transaction ignored = graph.beginTx();
             Result result = graph.execute(
                     "" +
                             "match p=n-[:DEPENDS_ON*1..4]->n\n" +
                             "where" +
                             " length(p) > 1\n" +
                             "return p;")) {
            while (result.hasNext()) {
                String str = "";
                Map<String, Object> row = result.next();
                for (Map.Entry<String, Object> column : row.entrySet()) {
                    Object value = column.getValue();
                    Path p = (Path) value;
                    str += column.getKey() + ": " + value + "; " + toString(p.nodes());
                }
                System.out.println(">> " + str);
            }
            long elapsed = System.nanoTime() - start;
            System.out.println("... " + elapsed / 1e6 + "ms");
        }
    }

    @Test
    public void should_detect_class_dependency_cycle__simple_case() {
        initDatabase(dbBaseDir, "db_class_cycle_simple_case", true);
        insertClassesInDb(
                Stream.of(SimpleCase.class, ParentClass.class, MiddleClass.class)
                        .map(c -> scanClass(c.getName())));

        List<PathNames> allPathNames = new CyclicDependencies(graph).queryCycles();

        assertThat(allPathNames).hasSize(3);
        assertThat(allPathNames).contains(
                PathNames.of(
                        Stream.of(SimpleCase.class, ParentClass.class, MiddleClass.class, SimpleCase.class)
                                .map(Class::getName)));

    }

    @Test
    public void should_detect_class_dependency_cycle__project_case() {
        initDatabase(dbBaseDir, "db_class_cycle_project", true);
        insertClassesInDb(scanAllClasses());

        List<PathNames> allPathNames = new CyclicDependencies(graph).queryCycles();
        allPathNames.forEach(System.out::println);


        assertThat(allPathNames.size()).isGreaterThanOrEqualTo(3);
        assertThat(allPathNames).contains(
                PathNames.of(
                        Stream.of(SimpleCase.class, ParentClass.class, MiddleClass.class, SimpleCase.class)
                                .map(Class::getName)));

    }

    private void insertClassesInDb(Stream<ClassDeclaration> declarationStream) {
        AtomicInteger count = new AtomicInteger();
        try (Transaction tx = graph.beginTx()) {
            declarationStream
                    .map(cd -> {
                        count.incrementAndGet();
                        return cd;
                    })
                    .forEach(cd -> classMapping.insert(tx, cd));
            tx.success();
        }
        System.out.println("#" + count.get() + " class declarations inserted");
    }

    private void initDatabase(File dbBaseDir, String dbName, boolean drop) {
        File databaseLocation = new File(dbBaseDir + "/" + dbName);
        if (drop)
            dropNeoDb(databaseLocation);

        graph = openDb(databaseLocation);
        finder = new CachedNodeFinder(graph);
        classMapping = new ClassMapping(graph, finder);
    }

    private Result query(int nodeId) {
        return graph.execute(
                "" +
                        "start n=node({ID}), passby=node({ID})\n" +
                        "match p=n-[:DEPENDS_ON*0..4]->m\n" +
                        "where passby in nodes(p)\n" +
                        "return p;", map(e("ID", nodeId)));
    }


    private static String toString(Iterable<Node> nodes) {
        StringBuilder b = new StringBuilder();
        for (Node node : nodes) {
            b.append(node.getProperty("name")).append("..");
        }
        return b.toString();
    }

    private Relationship findRelationship(Iterable<Relationship> relationships, String dependencyType) {
        for (Relationship relationship : relationships) {
            if (relationship.getEndNode().getProperty("name").equals(dependencyType))
                return relationship;
        }
        return null;
    }

    private GraphDatabaseService openDb(File databaseLocation) {
        return new GraphDatabaseFactory()
                .setUserLogProvider(new Slf4jLogProvider())
                .newEmbeddedDatabase(databaseLocation);
    }

    public static void dropNeoDb(final File databaseLocation) {
        if (!databaseLocation.exists())
            return;
        try {
            FileUtils.deleteRecursively(databaseLocation);
        } catch (IOException ioe) {
            throw new RuntimeException("Error deleting directory ", ioe);
        }
    }

    // --

    public ClassDeclaration scanClass(String className) {
        return populateDeclarationFactory()
                .classDeclarations()
                .filter(d -> d.getName().endsWith(className))
                .findFirst()
                .get();
    }

    public Stream<ClassDeclaration> scanAllClasses() {
        return populateDeclarationFactory()
                .classDeclarations();
    }

    private DeclarationFactory populateDeclarationFactory() {
        if (declarationFactory == null) {
            declarationFactory = new DeclarationFactory();
            new ClazzScanner(declarationFactory)
                    .appendSourceTree(new File(dbBaseDir + "/test-classes"))
                    .process();
        }
        return declarationFactory;
    }

    @SuppressWarnings("unused")
    public static class SimpleCase {
        public ParentClass parentClass;
    }

    @SuppressWarnings("unused")
    public static class MiddleClass {
        public SimpleCase simpleCase;
    }

    @SuppressWarnings("unused")
    public static class ParentClass {
        public MiddleClass middleClass;
    }

    @Test
    public void should_detect_non_final_fields_for_value_object_class() {
        initDatabase(dbBaseDir, "non_final_fields", true);
        insertClassesInDb(scanAllClasses());

        Node annotationNode;
        try (Transaction tx = graph.beginTx()) {
            annotationNode = classMapping.getOrCreateTypeNode(tx, ValueObject.class.getName());
        }

        new ValueObjectAndFinalFields(graph)
                .queryNonFinalFields(annotationNode.getId())
                .stream()
                .forEach(System.out::println);
    }

    public @interface ValueObject {
    }

    @Neo4jUsageTest.ValueObject
    public static class Price {
        private BigDecimal value;
        private Currency currency;

        public Price(BigDecimal value) {
            this.value = value;
        }
    }

    @Neo4jUsageTest.ValueObject
    public static class ImmutablePrice {
        private final BigDecimal value;

        public ImmutablePrice(BigDecimal value) {
            this.value = value;
        }
    }

    @Neo4jUsageTest.ValueObject
    public static class PartiallyImmutablePrice {
        private final BigDecimal value;
        private Currency currency;

        public PartiallyImmutablePrice(BigDecimal value) {
            this.value = value;
        }
    }

    @Neo4jUsageTest.ValueObject
    public static class ImmutableListOfPrice {
        private final List<BigDecimal> values;

        public ImmutableListOfPrice(List<BigDecimal> values) {
            this.values = values;
        }
    }

}
