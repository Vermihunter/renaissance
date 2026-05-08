package org.renaissance.neo4j.ldbc.model.finbench;

public record Loan(long id, double amount, java.time.LocalDateTime createTime, String loanUsage, double interestRate) {}
