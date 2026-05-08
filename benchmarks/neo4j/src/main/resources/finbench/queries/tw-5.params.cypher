// Parameter generator for tw-5.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-5.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 8800000000200000000 AS base, 1672531200000 AS currentTime
MATCH (p:Person)
WITH p, base, currentTime
ORDER BY p.id ASC
LIMIT 15
WITH collect(p) AS people, base, currentTime
UNWIND range(0, size(people)-1) AS i
RETURN
  people[i].id AS personId,
  base + i AS loanId,
  10000.0 + i AS amount,
  currentTime + i AS currentTime
