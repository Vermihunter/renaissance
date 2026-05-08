package org.renaissance.neo4j.ldbc.model.finbench;

public record LoanDepositAccount(long loanId, long accountId, double amount, java.time.LocalDateTime createTime) {
}
