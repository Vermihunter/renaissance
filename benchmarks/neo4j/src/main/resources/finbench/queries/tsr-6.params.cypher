// Parameter generator for tsr-6.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tsr-6.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 1640995200000 AS start_time, 1672531200000 AS end_time
MATCH
  (src:Account)<-[e1:transfer]-(mid:Account)-[e2:transfer]->(dst:Account {isBlocked: true})
WHERE src.id <> dst.id
  AND start_time < e1.timestamp AND e1.timestamp < end_time
  AND start_time < e2.timestamp AND e2.timestamp < end_time
RETURN
  src.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  count(DISTINCT dst) AS _blockedDstCount
ORDER BY _blockedDstCount DESC, id ASC
LIMIT 15
