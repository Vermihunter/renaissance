package org.renaissance.neo4j.ldbc.model.finbench;

public record AccountWithdrawAccount(long fromId, long toId, double amount, java.time.LocalDateTime createTime) {
}
