// Parameter generator for tsr-3.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tsr-3.cypher.
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
     0.0 AS threshold
MATCH (blockedSrc:Account {isBlocked: true})-[edge1:transfer]->(dst:Account)
WHERE start_time < edge1.timestamp AND edge1.timestamp < end_time
  AND edge1.amount > threshold
RETURN
  dst.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  threshold AS threshold,
  count(edge1) AS _matchingRows
ORDER BY _matchingRows DESC, id ASC
LIMIT 15
