package org.renaissance.neo4j.ldbc;

import java.util.List;
import java.util.Map;

/**
 * Represents a cypher query with all the loaded/generated parameter sets
 * that are configured to execute the query with
 * @param name a.k.a. identifier for the query
 * @param cypher the query itself represented as a string
 * @param parameterSets List of parameter sets to execute the query with
 */
public record CypherQuery(
        String name,
        String cypher,
        List<Map<String, Object>> parameterSets
) {
}
