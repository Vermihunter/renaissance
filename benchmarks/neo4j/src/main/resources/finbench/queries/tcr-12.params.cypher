// Parameter generator for tcr-12.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-12.cypher.
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
MATCH path=(p1:Person)-[:guarantee*1..3]->(pX:Person)
WHERE all(e IN relationships(path) WHERE start_time < e.timestamp AND e.timestamp < end_time)
WITH p1, start_time, end_time, count(*) AS _paths
MATCH (p1)-[:apply]->(:Loan)
RETURN
  p1.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  _paths AS _matchingRows
ORDER BY _matchingRows DESC, id ASC
LIMIT 15
