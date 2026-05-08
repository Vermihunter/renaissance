package org.renaissance.neo4j.ldbc;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Loads queries and their parameters:
 * - There is a catalog of queries that will run in the benchmark (index.txt) - other queries are ignored
 * - Each query is executed with different params (ID / threshold / start+end time / ...)
 * - The parameters are selected from the original source / selected cache directory / are generated
 * - If the parameters are generated, another set of cypher queries are ran for each query
 *  - Each parameter generating query is a cypher query that looks for "nice" values to measure the queries against
 *  - The parameter generating queries are called ${QUERY_NAME}.params.cypher and are located in finbench/queries
 */
public final class FinBenchQueryCatalog {
    private final GraphDatabaseService db;

    public FinBenchQueryCatalog(GraphDatabaseService db) {
        this.db = db;
    }

    public Map<String, List<Map<String, Object>>> loadParamsForCatalog(
            String queryDirResourcePath,
            String catalogFileName,
            Path paramCacheDir,
            boolean regenerate
    ) {
        String catalogPath = ResourceIO.joinResourcePath(queryDirResourcePath, catalogFileName);

        if (!ResourceIO.resourceExistsFlexible(catalogPath)) {
            System.out.printf(
                    "No query catalog found at %s. Parameter generation will be limited to active index.txt entries.%n",
                    catalogPath
            );

            catalogPath = ResourceIO.joinResourcePath(queryDirResourcePath, "index.txt");
        }

        List<String> queryNames = ResourceIO.readResourceLinesFlexible(catalogPath);

        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

        for (String rawLine : queryNames) {
            String queryName = parseIndexQueryName(rawLine);

            if (queryName == null) {
                continue;
            }

            List<Map<String, Object>> params = loadParamsForQuery(
                    queryDirResourcePath,
                    queryName,
                    paramCacheDir,
                    regenerate
            );

            result.put(queryName, params);

            System.out.printf(
                    "Prepared %,d parameter set(s) for %s%n",
                    params.size(),
                    queryName
            );
        }

        return Map.copyOf(result);
    }

    public List<CypherQuery> loadCypherQueries(
            String queryDirResourcePath,
            Map<String, List<Map<String, Object>>> paramsByQuery
    ) {
        String indexPath = ResourceIO.joinResourcePath(queryDirResourcePath, "index.txt");

        List<String> indexLines = ResourceIO.readResourceLinesFlexible(indexPath);
        List<CypherQuery> result = new ArrayList<>();

        for (String line : indexLines) {
            String queryFileName = parseIndexQueryName(line);

            if (queryFileName == null) {
                continue;
            }

            String[] parts = line.trim().split("\\s*\\|\\s*", -1);
            String queryPath = ResourceIO.joinResourcePath(queryDirResourcePath, queryFileName);

            String cypher = ResourceIO.readResourceStringFlexible(queryPath).trim();

            if (cypher.endsWith(";")) {
                cypher = cypher.substring(0, cypher.length() - 1).trim();
            }

            List<Map<String, Object>> parameterSets = List.of();

            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                String explicitParamPath = ResourceIO.joinResourcePath(queryDirResourcePath, parts[1].trim());
                if (ResourceIO.resourceExistsFlexible(explicitParamPath)) {
                    parameterSets = ParameterCsv.loadParameterSets(explicitParamPath);
                }
            }

            if (parameterSets.isEmpty()) {
                parameterSets = paramsByQuery.getOrDefault(queryFileName, List.of());
            }

            if (parameterSets.isEmpty()) {
                parameterSets = List.of(Map.of());
            }

            System.out.printf(
                    "Loaded workload query %s with %,d parameter set(s)%n",
                    queryFileName,
                    parameterSets.size()
            );

            result.add(new CypherQuery(queryFileName, cypher, parameterSets));
        }

        return List.copyOf(result);
    }

    private List<Map<String, Object>> loadParamsForQuery(
            String queryDirResourcePath,
            String queryName,
            Path paramCacheDir,
            boolean regenerate
    ) {
        String baseName = stripCypherSuffix(queryName);
        Path generatedCachePath = paramCacheDir.resolve(baseName + ".params.generated.csv");

        if (!regenerate && Files.exists(generatedCachePath)) {
            List<Map<String, Object>> cached = readGeneratedParamsCache(generatedCachePath);
            if (!cached.isEmpty()) {
                return cached;
            }
        }

        String staticCsvPath = ResourceIO.joinResourcePath(queryDirResourcePath, baseName + ".params.csv");

        if (ResourceIO.resourceExistsFlexible(staticCsvPath)) {
            List<Map<String, Object>> staticParams = ParameterCsv.loadParameterSets(staticCsvPath);

            if (!staticParams.isEmpty()) {
                return staticParams;
            }
        }

        String generatorPath = ResourceIO.joinResourcePath(queryDirResourcePath, baseName + ".params.cypher");

        if (ResourceIO.resourceExistsFlexible(generatorPath)) {
            List<Map<String, Object>> generated = generateParamsForQuery(generatorPath);

            if (!generated.isEmpty()) {
                writeGeneratedParamsCache(generatedCachePath, generated);
            }

            return generated;
        }

        return List.of();
    }

    private List<Map<String, Object>> generateParamsForQuery(String generatorResourcePath) {
        String cypher = ResourceIO.readResourceStringFlexible(generatorResourcePath).trim();

        if (cypher.endsWith(";")) {
            cypher = cypher.substring(0, cypher.length() - 1).trim();
        }

        System.out.println("Generating params using " + generatorResourcePath);

        long start = System.nanoTime();
        List<Map<String, Object>> result = new ArrayList<>();

        try (Transaction tx = db.beginTx()) {
            Result rows = tx.execute(cypher);

            while (rows.hasNext()) {
                Map<String, Object> row = rows.next();
                Map<String, Object> params = new LinkedHashMap<>();

                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String key = entry.getKey();

                    if (!key.startsWith("_")) {
                        params.put(key, entry.getValue());
                    }
                }

                result.add(params);
            }

            tx.commit();
        }

        double elapsedMillis = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf(
                "Generated %,d parameter set(s) using %s in %.3f ms%n",
                result.size(),
                generatorResourcePath,
                elapsedMillis
        );

        return List.copyOf(result);
    }

    private void writeGeneratedParamsCache(
            Path cachePath,
            List<Map<String, Object>> parameterSets
    ) {
        try {
            Path parent = cachePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (parameterSets.isEmpty()) {
                Files.writeString(cachePath, "", StandardCharsets.UTF_8);
                return;
            }

            List<String> keys = new ArrayList<>(parameterSets.get(0).keySet());

            try (BufferedWriter writer = Files.newBufferedWriter(cachePath, StandardCharsets.UTF_8)) {
                for (int i = 0; i < keys.size(); i++) {
                    if (i > 0) {
                        writer.write("|");
                    }

                    String key = keys.get(i);
                    Object sampleValue = firstNonNullValue(parameterSets, key);

                    writer.write(key);
                    writer.write(":");
                    writer.write(ParameterCsv.paramTypeForValue(sampleValue));
                }

                writer.newLine();

                for (Map<String, Object> params : parameterSets) {
                    for (int i = 0; i < keys.size(); i++) {
                        if (i > 0) {
                            writer.write("|");
                        }

                        Object value = params.get(keys.get(i));

                        if (value != null) {
                            writer.write(value.toString());
                        }
                    }

                    writer.newLine();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed writing generated params cache: " + cachePath, e);
        }
    }

    private Object firstNonNullValue(List<Map<String, Object>> parameterSets, String key) {
        for (Map<String, Object> params : parameterSets) {
            Object value = params.get(key);

            if (value != null) {
                return value;
            }
        }

        return "";
    }

    private List<Map<String, Object>> readGeneratedParamsCache(Path cachePath) {
        try {
            if (!Files.exists(cachePath) || Files.size(cachePath) == 0) {
                return List.of();
            }

            return ParameterCsv.loadParameterSetsFromLines(Files.readAllLines(cachePath, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed reading generated params cache: " + cachePath, e);
        }
    }

    public static String parseIndexQueryName(String rawLine) {
        String trimmed = rawLine.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null;
        }

        String[] parts = trimmed.split("\\s*\\|\\s*", -1);
        String queryName = parts[0].trim();

        if (queryName.isEmpty()) {
            return null;
        }

        return queryName;
    }

    private String stripCypherSuffix(String queryName) {
        if (queryName.endsWith(".cypher")) {
            return queryName.substring(0, queryName.length() - ".cypher".length());
        }

        return queryName;
    }
}
