package org.renaissance.neo4j.ldbc;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.io.ByteUnit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Embedded Neo4J instance inspired by the Neo4JAnalytics benchmark
 */
public final class EmbeddedNeo4j {
    private final AtomicReference<DatabaseManagementService> dbms = new AtomicReference<>();

    public GraphDatabaseService createGraphDatabase(Path graphDbDir) {
        try {
            System.out.println("Creating graph database at: " + graphDbDir);
            Files.createDirectories(graphDbDir);

            dbms.set(
                    new DatabaseManagementServiceBuilder(graphDbDir)
                            .setConfig(GraphDatabaseSettings.pagecache_memory, ByteUnit.mebiBytes(512))
                            .build()
            );

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownDatabaseOnce));

            return dbms.get().database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create graph database", e);
        }
    }

    public void shutdownDatabaseOnce() {
        DatabaseManagementService service = dbms.getAndSet(null);
        if (service != null) {
            service.shutdown();
        }
    }
}
