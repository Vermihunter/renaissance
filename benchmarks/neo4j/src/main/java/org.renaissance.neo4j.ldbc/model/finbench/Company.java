package org.renaissance.neo4j.ldbc.model.finbench;


public record Company(long companyId, String companyName, boolean blocked, java.time.LocalDateTime createTime,
                      String country, String city, String business, String description, String url) {}

