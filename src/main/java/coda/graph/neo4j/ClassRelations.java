package coda.graph.neo4j;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public enum ClassRelations implements RelationshipType {
    METHOD_OF,
    OF_TYPE,
    RETURN_TYPE,
    PARAM_TYPE,
    FIELD_TYPE,
    WITH_FIELD,
    ALL_TYPES,
    TYPE,
    INTERFACE_TYPE,
    SUPER_TYPE,
    TYPE_ARRAY,
    THROWS,
    ALL_PACKAGES,
    PACKAGE_TREE,
    IN_PACKAGE,
    //
    DEPENDS_ON,
    ANNOTATED_WITH
}