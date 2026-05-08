package org.renaissance.neo4j.ldbc.model.finbench;

public record AccountRepayLoan(long accountId, long loanId, double amount, java.time.LocalDateTime createTime) {}