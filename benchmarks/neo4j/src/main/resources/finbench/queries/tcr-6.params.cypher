// Parameter generator for tcr-6.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-6.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 1640995200000 AS start_time, 1672531200000 AS end_time,
     0.0 AS threshold1, 0.0 AS threshold2
MATCH
  (src1:Account)-[edge1:transfer]->(mid:Account)-[edge2:withdraw]->(dstCard:Account {type: "card"})
WHERE start_time < edge1.timestamp AND edge1.timestamp < end_time
  AND edge1.amount > threshold1
  AND start_time < edge2.timestamp AND edge2.timestamp < end_time
  AND edge2.amount > threshold2
RETURN
  dstCard.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  threshold1 AS threshold1,
  threshold2 AS threshold2,
  count(*) AS _matchingRows,
  sum(edge1.amount) AS _sumTransfer,
  sum(edge2.amount) AS _sumWithdraw
ORDER BY _matchingRows DESC, _sumWithdraw DESC, id ASC
LIMIT 15
