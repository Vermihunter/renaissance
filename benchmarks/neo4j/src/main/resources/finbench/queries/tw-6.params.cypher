// Parameter generator for tw-6.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tw-6.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

// The upstream Neo4j reference query for tw-6 currently creates the apply
// relationship from variable `p`, although it matches variable `c`.
// If your tw-6.cypher still contains `(p)`, fix it to `(c)` before execution.
WITH 8800000000300000000 AS base, 1672531200000 AS currentTime
MATCH (c:Company)
WITH c, base, currentTime
ORDER BY c.id ASC
LIMIT 15
WITH collect(c) AS companies, base, currentTime
UNWIND range(0, size(companies)-1) AS i
RETURN
  companies[i].id AS companyId,
  base + i AS loanId,
  25000.0 + i AS amount,
  currentTime + i AS currentTime
