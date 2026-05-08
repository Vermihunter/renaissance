package org.renaissance.neo4j.ldbc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.renaissance.Benchmark;
import org.renaissance.Benchmark.*;
import org.renaissance.BenchmarkContext;
import org.renaissance.BenchmarkResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Name("neo4j-ldbc")
@Group("database")
@Group("neo4j")
@Summary("Executes Neo4j graph queries against an LDBC FinBench-style Cypher workload.")
@RequiresJvm("17")
@Repetitions(20)
@Parameter(name = "ldbc_benchmark", defaultValue = "finbench")
@Parameter(name = "ldbc_sf", defaultValue = "1.0")
@Parameter(name = "ldbc_query_dir", defaultValue = "finbench/queries")
@Parameter(name = "ldbc_query_catalog", defaultValue = "catalog.txt")
@Parameter(name = "ldbc_param_cache_dir", defaultValue = "finbench/queries/params")
@Parameter(name = "ldbc_regenerate_params", defaultValue = "false")
@Parameter(name = "ldbc_verbose", defaultValue = "false")
@Parameter(name = "ldbc_continue_on_query_error", defaultValue = "false")
@Parameter(name = "ldbc_tcr_threads", defaultValue = "1")
@Parameter(name = "ldbc_tcr_repeats", defaultValue = "2")
@Parameter(name = "ldbc_tsr_threads", defaultValue = "1")
@Parameter(name = "ldbc_tsr_repeats", defaultValue = "2")
@Parameter(name = "ldbc_tw_threads", defaultValue = "1")
@Parameter(name = "ldbc_tw_repeats", defaultValue = "1")
public final class Neo4JLDBCAnalytics implements Benchmark {
    private final EmbeddedNeo4j embeddedNeo4j = new EmbeddedNeo4j();

    private GraphDatabaseService db;
    private boolean verbose;
    private boolean continueOnQueryError;

    private List<CypherQuery> cypherQueries = List.of();
    private Map<String, List<Map<String, Object>>> paramsByQuery = Map.of();

    @Override
    public void setUpBeforeAll(BenchmarkContext c) {
        try {
            verbose = boolParam(c, "ldbc_verbose");
            continueOnQueryError = boolParam(c, "ldbc_continue_on_query_error");

            Path graphDbDir = c.scratchDirectory().resolve("graphdb").normalize();
            db = embeddedNeo4j.createGraphDatabase(graphDbDir);

            String benchmarkName = c.parameter("ldbc_benchmark").value();
            String sf = c.parameter("ldbc_sf").value();

            String base = ResourceIO.resolveDatasetBase(benchmarkName, sf);
            System.out.println("Loading LDBC dataset from resources: " + base);
            System.out.println("Account.csv URL = " +
                    Thread.currentThread().getContextClassLoader().getResource(base + "/Account.csv"));

            new FinBenchDataLoader(db).loadDataset(base);

            String queryDir = c.parameter("ldbc_query_dir").value();
            String queryCatalog = c.parameter("ldbc_query_catalog").value();
            boolean regenerateParams = boolParam(c, "ldbc_regenerate_params");

            Path paramCacheDir = resolveParamCacheDir(c, c.parameter("ldbc_param_cache_dir").value());

            FinBenchQueryCatalog catalog = new FinBenchQueryCatalog(db);

            paramsByQuery = catalog.loadParamsForCatalog(
                    queryDir,
                    queryCatalog,
                    paramCacheDir,
                    regenerateParams
            );

            cypherQueries = catalog.loadCypherQueries(queryDir, paramsByQuery);

            System.out.printf("Loaded %,d active Cypher query file(s)%n", cypherQueries.size());
            for (CypherQuery q : cypherQueries) {
                System.out.printf("Active query %s has %,d parameter set(s)%n",
                        q.name(), q.parameterSets().size());
            }

        } catch (Exception e) {
            try {
                embeddedNeo4j.shutdownDatabaseOnce();
            } catch (Exception shutdownError) {
                e.addSuppressed(shutdownError);
            }

            throw new RuntimeException("Failed during benchmark setup", e);
        }
    }

    @Override
    public void setUpBeforeEach(BenchmarkContext context) {
    }

    @Override
    public BenchmarkResult run(BenchmarkContext c) {
        QueryStats total = new ParallelQueryExecutor(
                db,
                cypherQueries,
                verbose,
                continueOnQueryError
        ).run(
                intParam(c, "ldbc_tcr_threads"),
                intParam(c, "ldbc_tcr_repeats"),
                intParam(c, "ldbc_tsr_threads"),
                intParam(c, "ldbc_tsr_repeats"),
                intParam(c, "ldbc_tw_threads"),
                intParam(c, "ldbc_tw_repeats")
        );

        return BenchmarkResult.Validators.simple(
                "neo4j-ldbc-successful-query-invocations",
                total.successfulExecutions(),
                total.successfulExecutions()
        );
    }

    @Override
    public void tearDownAfterEach(BenchmarkContext context) {
    }

    @Override
    public void tearDownAfterAll(BenchmarkContext c) {
        embeddedNeo4j.shutdownDatabaseOnce();
    }

    private Path resolveParamCacheDir(BenchmarkContext c, String cacheDirValue) {
        String sf = c.parameter("ldbc_sf").value();
        String sfDir = sf.startsWith("SF") ? sf : "SF" + sf;

        String baseResourcePath = (cacheDirValue != null && !cacheDirValue.isBlank())
                ? cacheDirValue
                : "finbench/queries/params";

        String resourcePath = Path.of(baseResourcePath, sfDir)
                .toString()
                .replace('\\', '/');

        List<Path> filesystemCandidates = List.of(
                Path.of("resources").resolve(resourcePath).normalize(),
                Path.of("src/main/resources").resolve(resourcePath).normalize(),
                Path.of("benchmarks/neo4j/src/main/resources").resolve(resourcePath).normalize()
        );

        for (Path candidate : filesystemCandidates) {
            if (Files.isDirectory(candidate)) {
                System.out.println("Parameter cache directory = " + candidate.toAbsolutePath());
                return candidate;
            }
        }

        var resourceUrl = Thread.currentThread()
                .getContextClassLoader()
                .getResource(resourcePath);

        if (resourceUrl != null && "file".equals(resourceUrl.getProtocol())) {
            try {
                Path cacheDir = Path.of(resourceUrl.toURI()).normalize();

                if (Files.isDirectory(cacheDir)) {
                    System.out.println("Parameter cache directory = " + cacheDir.toAbsolutePath());
                    return cacheDir;
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed resolving parameter cache resource directory: " + resourcePath,
                        e
                );
            }
        }

        throw new RuntimeException(
                "Parameter cache directory not found. Tried filesystem paths: "
                        + filesystemCandidates
                        + " and classpath resource: "
                        + resourcePath
                        + ". Resource URL was: "
                        + resourceUrl
        );
    }

    private int intParam(BenchmarkContext context, String name) {
        return Integer.parseInt(context.parameter(name).value());
    }

    private boolean boolParam(BenchmarkContext context, String name) {
        return Boolean.parseBoolean(context.parameter(name).value());
    }
}
