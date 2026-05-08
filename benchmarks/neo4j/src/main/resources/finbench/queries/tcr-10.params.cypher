// Parameter generator for tcr-10.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-10.cypher.
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
  (p1:Person)-[e1:invest]->(common:Company)<-[e2:invest]-(p2:Person)
WHERE p1.id < p2.id
  AND start_time < e1.timestamp AND e1.timestamp < end_time
  AND start_time < e2.timestamp AND e2.timestamp < end_time
WITH p1, p2, start_time, end_time, count(DISTINCT common) AS _commonCompanies
WHERE _commonCompanies >= 1
RETURN
  p1.id AS id1,
  p2.id AS id2,
  start_time AS start_time,
  end_time AS end_time,
  _commonCompanies
ORDER BY _commonCompanies DESC, id1 ASC, id2 ASC
LIMIT 15
