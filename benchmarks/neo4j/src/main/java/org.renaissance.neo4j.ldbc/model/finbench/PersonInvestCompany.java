package org.renaissance.neo4j.ldbc.model.finbench;

public record PersonInvestCompany(long investorId, long companyId, double ratio, java.time.LocalDateTime createTime) {
}
