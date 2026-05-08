package org.renaissance.neo4j.ldbc;

/**
 * Data class containing results of the execution of a query
 * @param successfulExecutions
 * @param failedExecutions
 * @param totalRows
 */
public record QueryStats(
        long successfulExecutions,
        long failedExecutions,
        long totalRows
) {
    public QueryStats plus(QueryStats other) {
        return new QueryStats(
                successfulExecutions + other.successfulExecutions,
                failedExecutions + other.failedExecutions,
                totalRows + other.totalRows
        );
    }

    public static QueryStats zero() {
        return new QueryStats(0, 0, 0);
    }
}
