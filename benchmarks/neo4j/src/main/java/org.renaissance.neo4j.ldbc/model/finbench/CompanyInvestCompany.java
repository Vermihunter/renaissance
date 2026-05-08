package org.renaissance.neo4j.ldbc.model.finbench;

public record CompanyInvestCompany(long investorId, long companyId, double ratio, java.time.LocalDateTime createTime) {
}
