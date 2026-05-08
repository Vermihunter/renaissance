package org.renaissance.neo4j.ldbc.model.finbench;

public record Medium(long mediumId, String mediumType, boolean isBlocked, java.time.LocalDateTime createTime,
                     long lastLoginTime, String riskLevel) {
}
