package org.renaissance.neo4j.ldbc.model.finbench;

public record Person(long personId, String personName, boolean isBlocked, java.time.LocalDateTime createTime,
                     String gender, java.time.LocalDate birthday, String country, String city) {
}
