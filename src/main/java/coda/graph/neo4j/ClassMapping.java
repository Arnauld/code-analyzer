package coda.graph.neo4j;

import coda.scanner.clazz.AnnotationDeclaration;
import coda.scanner.clazz.ClassDeclaration;
import coda.scanner.clazz.FieldDeclaration;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import java.util.Set;
import java.util.stream.Stream;

import static coda.graph.neo4j.ClassProperties.*;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ClassMapping {
    private final GraphDatabaseService graph;
    private final CachedNodeFinder finder;

    public ClassMapping(GraphDatabaseService graph, CachedNodeFinder finder) {
        this.graph = graph;
        this.finder = finder;
    }

    public ClassMapping initConstraints() {
        finder.createUniqueConstraintIfMissing(graph, TYPE, TYPE_NAME);
        finder.createUniqueConstraintIfMissing(graph, PACKAGE, PACKAGE_NAME);
        return this;
    }


    public Node getOrCreateTypeNode(Transaction tx, String typeName) {
        return finder.getOrCreateNode(tx, TYPE, TYPE_NAME, typeName);
    }

    public Node getOrCreatePackageNode(Transaction tx, String packageName) {
        return finder.getOrCreateNode(tx, PACKAGE, PACKAGE_NAME, packageName);
    }

    public Node createFieldNode(Transaction tx, String fieldName) {
        Label label = DynamicLabel.label(FIELD);
        Node typeNode = graph.createNode(label);
        typeNode.setProperty(FIELD_NAME, fieldName);
        return typeNode;
    }

    public Node createAnnotationNode(Transaction tx, String name) {
        Label label = DynamicLabel.label(ANNOTATION);
        Node typeNode = graph.createNode(label);
        typeNode.setProperty(ANNOTATION_NAME, name);
        return typeNode;
    }


    public Node insert(Transaction tx, ClassDeclaration classDeclaration) {
        Node classNode = getOrCreateTypeNode(tx, classDeclaration.getName());
        fillDependsOn(tx, classNode, classDeclaration.dependsOn());
        fillAnnotations(tx, classNode, classDeclaration.annotations());

        classDeclaration.fields().forEach(f -> insertField(tx, classNode, f));

        return classNode;
    }

    private void insertField(Transaction tx, Node classNode, FieldDeclaration f) {
        Node fieldNode = createFieldNode(tx, f.getName());
        fieldNode.setProperty(FIELD_FINAL, f.isFinal());
        fieldNode.setProperty(FIELD_STATIC, f.isStatic());
        fieldNode.setProperty(FIELD_VISIBILITY, f.visibility().id());
        classNode.createRelationshipTo(fieldNode, ClassRelations.WITH_FIELD);

        f.typeNames().forEach(typeName -> {
            Node fieldTypeNode = getOrCreateTypeNode(tx, typeName);
            fieldNode.createRelationshipTo(fieldTypeNode, ClassRelations.OF_TYPE);
        });

        fillDependsOn(tx, fieldNode, f.dependsOn());
        fillAnnotations(tx, fieldNode, f.annotations());
    }

    private void fillAnnotations(Transaction tx, Node node, Stream<AnnotationDeclaration> annotations) {
        annotations.forEach(a -> {
            Node annotationNode = createAnnotationNode(tx, a.getName());

            node.createRelationshipTo(annotationNode, ClassRelations.ANNOTATED_WITH);
            fillDependsOn(tx, annotationNode, a.dependsOn());

            Node annotationTypeNode = getOrCreateTypeNode(tx, a.getName());
            annotationNode.createRelationshipTo(annotationTypeNode, ClassRelations.OF_TYPE);
            fillAnnotations(tx, annotationNode, a.annotations());
        });
    }

    private void fillDependsOn(Transaction tx, Node node, Set<String> dependencies) {
        dependencies.stream().forEach(dep -> {
            Node dependencyNode = getOrCreateTypeNode(tx, dep);
            node.createRelationshipTo(dependencyNode, ClassRelations.DEPENDS_ON);
        });
    }

    private String toPackageName(String typeName) {
        int idx = typeName.lastIndexOf(".");
        return idx == -1 ? "default" : typeName.substring(0, idx).replace('/', '.');
    }

    public ResourceIterator<Node> findTypes() {
        return graph.findNodes(DynamicLabel.label(TYPE));
    }
}
