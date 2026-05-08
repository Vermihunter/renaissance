package org.renaissance.neo4j.ldbc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for IO operations used for loading different resources such as the Query catalog
 */
public final class ResourceIO {
    private ResourceIO() {
    }

    public static String joinResourcePath(String dir, String file) {
        if (dir.endsWith("/") || dir.endsWith("\\")) {
            return dir + file;
        }

        return dir + "/" + file;
    }

    public static boolean resourceExistsFlexible(String resourcePath) {
        try {
            Path filesystemPath = Path.of(resourcePath);

            if (Files.exists(filesystemPath)) {
                return true;
            }
        } catch (Exception ignored) {
            // Fall through to classpath lookup.
        }

        String normalized = normalizeClasspathResourcePath(resourcePath);

        return ResourceIO.class
                .getClassLoader()
                .getResource(normalized) != null;
    }

    public static List<String> readResourceLinesFlexible(String resourcePath) {
        try {
            Path filesystemPath = Path.of(resourcePath);

            if (Files.exists(filesystemPath)) {
                return Files.readAllLines(filesystemPath, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            // Fall through to classpath lookup.
        }

        String normalized = normalizeClasspathResourcePath(resourcePath);

        try (
                InputStream is = ResourceIO.class
                        .getClassLoader()
                        .getResourceAsStream(normalized)
        ) {
            if (is == null) {
                throw new IllegalArgumentException("Missing resource/file: " + resourcePath);
            }

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                List<String> lines = new ArrayList<>();

                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                return lines;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading lines from: " + resourcePath, e);
        }
    }

    public static String readResourceStringFlexible(String resourcePath) {
        try {
            Path filesystemPath = Path.of(resourcePath);

            if (Files.exists(filesystemPath)) {
                return Files.readString(filesystemPath, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
            // Fall through to classpath lookup.
        }

        String normalized = normalizeClasspathResourcePath(resourcePath);

        try (
                InputStream is = ResourceIO.class
                        .getClassLoader()
                        .getResourceAsStream(normalized)
        ) {
            if (is == null) {
                throw new IllegalArgumentException("Missing resource/file: " + resourcePath);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed reading string from: " + resourcePath, e);
        }
    }

    public static String normalizeClasspathResourcePath(String resourcePath) {
        String normalized = resourcePath.replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    public static String resolveDatasetBase(String benchmarkName, String sf) {
        List<String> candidates = new ArrayList<>();

        candidates.add(benchmarkName + "/SF" + sf);

        if (sf.endsWith(".0")) {
            candidates.add(benchmarkName + "/SF" + sf.substring(0, sf.length() - 2));
        } else if (!sf.contains(".")) {
            candidates.add(benchmarkName + "/SF" + sf + ".0");
        }

        for (String candidate : candidates) {
            if (resourceExistsFlexible(candidate + "/Account.csv")) {
                return candidate;
            }
        }

        throw new IllegalArgumentException(
                "Could not find dataset Account.csv. Tried: " + candidates
        );
    }
}
