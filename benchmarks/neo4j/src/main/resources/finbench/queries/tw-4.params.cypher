// Parameter generator for tw-4.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-4.cypher.
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

MATCH (a:Account)
  WHERE a.type IS NOT NULL
WITH currentTime, a.type AS chosenType, count(*) AS c
  ORDER BY c DESC
  LIMIT 1

MATCH (dst:Account)
  WHERE dst.type = chosenType
WITH currentTime, collect(dst)[0..100] AS dstAccounts

MATCH (src:Account)
WITH currentTime, dstAccounts, collect(src)[0..100] AS srcAccounts

UNWIND range(0, 14) AS i
WITH
  currentTime,
  srcAccounts[i % size(srcAccounts)] AS src,
  dstAccounts[i % size(dstAccounts)] AS dst

  WHERE src IS NOT NULL
  AND dst IS NOT NULL
  AND src.id <> dst.id

RETURN
  src.id AS srcId,
  dst.id AS dstId,
  currentTime AS currentTime,
  500.0 AS amt
  LIMIT 15