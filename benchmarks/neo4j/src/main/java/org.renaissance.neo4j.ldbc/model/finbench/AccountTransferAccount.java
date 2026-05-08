package org.renaissance.neo4j.ldbc.model.finbench;

public record AccountTransferAccount(long fromId, long toId, java.time.LocalDateTime createTime,
                                     long orderNum, String comment, String goodsType) {
}
