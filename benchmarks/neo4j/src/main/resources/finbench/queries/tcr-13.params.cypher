// Parameter generator for tcr-13.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-13.cypher.
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
  (person:Person)-[:own]->(pAcc:Account)
  -[edge2:transfer]->(compAcc:Account)
  <-[:own]-(company:Company)
WHERE start_time < edge2.timestamp AND edge2.timestamp < end_time
RETURN
  person.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  count(*) AS _matchingRows,
  sum(edge2.amount) AS _sumTransfer
ORDER BY _sumTransfer DESC, _matchingRows DESC, id ASC
LIMIT 15
