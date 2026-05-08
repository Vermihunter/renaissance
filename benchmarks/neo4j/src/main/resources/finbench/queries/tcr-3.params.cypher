// Parameter generator for tcr-3.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-3.cypher.
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
MATCH p=(src:Account)-[edge:transfer*1..3]->(dst:Account)
WHERE src.id <> dst.id
  AND all(e IN edge WHERE start_time < e.timestamp AND e.timestamp < end_time)
WITH src, dst, start_time, end_time, min(length(p)) AS _distance, count(*) AS _matchingRows
RETURN
  src.id AS id1,
  dst.id AS id2,
  start_time AS start_time,
  end_time AS end_time,
  _distance,
  _matchingRows
ORDER BY _distance DESC, _matchingRows DESC, id1 ASC, id2 ASC
LIMIT 15
