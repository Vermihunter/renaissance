// Parameter generator for tw-1.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-1.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 8800000000000000000 AS base, 1672531200000 AS currentTime
UNWIND range(1, 15) AS i
RETURN
  base + i AS personId,
  "Generated Person " + toString(i) AS personName,
  base + 100000 + i AS accountId,
  currentTime + i AS currentTime,
  false AS accountBlocked,
  "checking account" AS accountType
