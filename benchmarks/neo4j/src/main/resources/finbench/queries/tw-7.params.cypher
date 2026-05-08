// Parameter generator for tw-7.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-7.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

WITH 8800000000400000000 AS base, 1672531200000 AS currentTime
MATCH (acc:Account)
WITH acc, base, currentTime
ORDER BY acc.id ASC
LIMIT 15
WITH collect(acc) AS accounts, base, currentTime
UNWIND range(0, size(accounts)-1) AS i
RETURN
  accounts[i].id AS accountId,
  base + i AS mediumId,
  false AS mediumBlocked,
  currentTime + i AS currentTime
