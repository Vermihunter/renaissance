package org.renaissance.neo4j.ldbc.model.finbench;

public record Account(long id, java.time.OffsetDateTime createTime, boolean blocked,
                      String type, String nickname, String phonenum, String email,
                      String freqLoginType, long lastLoginTime, AccountLevel level) {}
