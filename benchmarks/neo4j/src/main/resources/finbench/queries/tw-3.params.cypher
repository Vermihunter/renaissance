// Parameter generator for tw-3.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-3.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 1672531200000 AS currentTime
MATCH (src:Account), (dst:Account)
WHERE src.id < dst.id
WITH src, dst, currentTime
ORDER BY src.id ASC, dst.id ASC
LIMIT 15
RETURN
  src.id AS srcId,
  dst.id AS dstId,
  currentTime AS currentTime,
  1000.0 AS amt
