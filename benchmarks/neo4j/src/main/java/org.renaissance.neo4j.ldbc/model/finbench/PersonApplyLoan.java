package org.renaissance.neo4j.ldbc.model.finbench;

public record PersonApplyLoan(long personId, long loanId, java.time.LocalDateTime creatTime, String org) {
}
