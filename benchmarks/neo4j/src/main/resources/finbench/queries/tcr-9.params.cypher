// Parameter generator for tcr-9.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-9.cypher.
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
     0.0 AS threshold, 0.0 AS lowerbound, 1000000000.0 AS upperbound
MATCH
  (loan:Loan)-[edge1:deposit]->(mid:Account)-[edge2:repay]->(loan),
  (up:Account)-[edge3:transfer]->(mid)-[edge4:transfer]->(down:Account)
WHERE edge1.amount > threshold
  AND start_time < edge1.timestamp AND edge1.timestamp < end_time
  AND edge2.amount > threshold
  AND start_time < edge2.timestamp AND edge2.timestamp < end_time
  AND lowerbound < edge1.amount / edge2.amount AND edge1.amount / edge2.amount < upperbound
  AND edge3.amount > threshold
  AND start_time < edge3.timestamp AND edge3.timestamp < end_time
  AND edge4.amount > threshold
  AND start_time < edge4.timestamp AND edge4.timestamp < end_time
RETURN
  mid.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  threshold AS threshold,
  lowerbound AS lowerbound,
  upperbound AS upperbound,
  count(*) AS _matchingRows
ORDER BY _matchingRows DESC, id ASC
LIMIT 15
