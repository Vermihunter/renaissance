package org.renaissance.neo4j.ldbc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * The class that actually runs the queries in parallel
 * - There are 3 types of queries:
 *  - TCR = Transaction Complex Read
 *  - TSR = Transaction Simple Read
 *  - TW  = Transaction Writ
 * - Each of them are configured separately (number of threads + number of repeats)
 * - This class is inspired by the Neo4JAnalytics class with the query execution
 */
public final class ParallelQueryExecutor {
    private final GraphDatabaseService db;
    private final List<CypherQuery> cypherQueries;
    private final boolean verbose;
    private final boolean continueOnQueryError;

    public ParallelQueryExecutor(
            GraphDatabaseService db,
            List<CypherQuery> cypherQueries,
            boolean verbose,
            boolean continueOnQueryError
    ) {
        this.db = db;
        this.cypherQueries = cypherQueries;
        this.verbose = verbose;
        this.continueOnQueryError = continueOnQueryError;
    }

    public QueryStats run(
            int tcrThreads,
            int tcrRepeats,
            int tsrThreads,
            int tsrRepeats,
            int twThreads,
            int twRepeats
    ) {
        List<QueryWorkload> workloads = List.of(
                new QueryWorkload(
                        "tcr",
                        tcrThreads,
                        queriesWithPrefix("tcr-"),
                        tcrRepeats,
                        i -> 3 * i
                ),
                new QueryWorkload(
                        "tsr",
                        tsrThreads,
                        queriesWithPrefix("tsr-"),
                        tsrRepeats,
                        i -> i
                ),
                new QueryWorkload(
                        "tw",
                        twThreads,
                        queriesWithPrefix("tw-"),
                        twRepeats,
                        i -> i
                )
        );

        List<QueryWorker> workers = new ArrayList<>();

        for (QueryWorkload workload : workloads) {
            if (workload.threadCount() <= 0 || workload.repeats() <= 0 || workload.queries().isEmpty()) {
                continue;
            }

            for (int i = 0; i < workload.threadCount(); i++) {
                workers.add(new QueryWorker(workload, i));
            }
        }

        for (QueryWorker worker : workers) {
            worker.start();
        }

        for (QueryWorker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for query worker", e);
            }
        }

        QueryStats total = QueryStats.zero();

        for (QueryWorker worker : workers) {
            if (worker.failure() != null && !continueOnQueryError) {
                throw worker.failure();
            }

            if (worker.failure() != null) {
                System.err.printf(
                        "Worker %s failed: %s%n",
                        worker.getName(),
                        worker.failure().getMessage()
                );
            }

            total = total.plus(worker.stats());
        }

        System.out.printf(
                "Parallel run completed: workers=%d, successful=%d, failed=%d, totalRows=%d%n",
                workers.size(),
                total.successfulExecutions(),
                total.failedExecutions(),
                total.totalRows()
        );

        return total;
    }

    private record QueryWorkload(
            String name,
            int threadCount,
            List<CypherQuery> queries,
            int repeats,
            IntUnaryOperator offset
    ) {
    }

    private final class QueryWorker extends Thread {
        private final QueryWorkload workload;
        private final int workerIndex;

        private QueryStats stats = QueryStats.zero();
        private RuntimeException failure;

        QueryWorker(QueryWorkload workload, int workerIndex) {
            super("neo4j-ldbc-" + workload.name() + "-" + workerIndex);
            this.workload = workload;
            this.workerIndex = workerIndex;
        }

        @Override
        public void run() {
            try {
                stats = runQueries(
                        workload.queries(),
                        workload.repeats(),
                        workload.offset().applyAsInt(workerIndex)
                );
            } catch (RuntimeException e) {
                failure = e;
            }
        }

        QueryStats stats() {
            return stats;
        }

        RuntimeException failure() {
            return failure;
        }
    }

    private QueryStats runQueries(
            List<CypherQuery> queries,
            int repeats,
            int offset
    ) {
        long successfulExecutions = 0;
        long failedExecutions = 0;
        long totalRows = 0;

        List<CypherQuery> rotatedQueries = rotate(queries, offset);

        for (int repeat = 0; repeat < repeats; repeat++) {
            for (CypherQuery query : rotatedQueries) {
                int parameterSetIndex = 0;

                for (Map<String, Object> params : query.parameterSets()) {
                    parameterSetIndex++;

                    try {
                        long rows = executeAndConsume(query, params, parameterSetIndex);
                        successfulExecutions++;
                        totalRows += rows;
                    } catch (RuntimeException e) {
                        failedExecutions++;

                        if (!continueOnQueryError) {
                            throw e;
                        }

                        System.err.printf(
                                "[%s] Continuing after failed query %s params #%d: %s%n",
                                Thread.currentThread().getName(),
                                query.name(),
                                parameterSetIndex,
                                e.getMessage()
                        );
                    }
                }
            }
        }

        return new QueryStats(successfulExecutions, failedExecutions, totalRows);
    }

    private long executeAndConsume(
            CypherQuery query,
            Map<String, Object> params,
            int parameterSetIndex
    ) {
        long rows = 0;
        long start = System.nanoTime();

        try (Transaction tx = db.beginTx()) {
            Result result = tx.execute(query.cypher(), params);

            while (result.hasNext()) {
                result.next();
                rows++;
            }

            tx.commit();

            if (verbose) {
                double elapsedMillis = (System.nanoTime() - start) / 1_000_000.0;
                System.out.printf(
                        "[%s] Query %s params #%d %s returned %,d rows in %.3f ms%n",
                        Thread.currentThread().getName(),
                        query.name(),
                        parameterSetIndex,
                        params,
                        rows,
                        elapsedMillis
                );
            }

            return rows;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed executing query: " + query.name()
                            + ", parameter set #" + parameterSetIndex
                            + ", params=" + params
                            + "\n\nCypher:\n" + query.cypher(),
                    e
            );
        }
    }

    private List<CypherQuery> queriesWithPrefix(String prefix) {
        List<CypherQuery> result = new ArrayList<>();

        for (CypherQuery query : cypherQueries) {
            if (query.name().startsWith(prefix)) {
                result.add(query);
            }
        }

        return List.copyOf(result);
    }

    private List<CypherQuery> rotate(List<CypherQuery> queries, int offset) {
        if (queries.isEmpty()) {
            return queries;
        }

        int normalizedOffset = Math.floorMod(offset, queries.size());

        if (normalizedOffset == 0) {
            return queries;
        }

        List<CypherQuery> result = new ArrayList<>(queries.size());
        result.addAll(queries.subList(normalizedOffset, queries.size()));
        result.addAll(queries.subList(0, normalizedOffset));

        return List.copyOf(result);
    }
}
