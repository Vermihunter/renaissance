package org.renaissance.neo4j.ldbc.model.finbench;

public record PersonGuaranteePerson(long fromId, long toId, java.time.LocalDateTime createTime, String relation) {
}
