// Parameter generator for tcr-8.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-8.cypher.
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
MATCH
  (loan:Loan)-[edge1:deposit]->(src:Account),
  p=(src)-[edge234:transfer|withdraw*1..3]->(dst:Account)
WITH loan, start_time, end_time, threshold, edge1, edge234, p,
     [e IN relationships(p) | e.amount] AS amts
WHERE start_time < edge1.timestamp AND edge1.timestamp < end_time
  AND all(e IN edge234 WHERE start_time < e.timestamp AND e.timestamp < end_time)
  AND reduce(curr = head(amts), x IN tail(amts) |
        CASE WHEN (curr <> -1) AND (x > curr * threshold) THEN x ELSE -1 END
      ) <> -1
RETURN
  loan.id AS id,
  start_time AS start_time,
  end_time AS end_time,
  threshold AS threshold,
  count(*) AS _matchingRows
ORDER BY _matchingRows DESC, id ASC
LIMIT 15
