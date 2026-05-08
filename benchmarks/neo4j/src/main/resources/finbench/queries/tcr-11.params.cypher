// Parameter generator for tcr-11.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tcr-11.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

MATCH path=(comp:Company)<-[:invest*1..3]-(investor)
WHERE (investor:Company) OR (investor:Person)
RETURN
  comp.id AS id,
  count(*) AS _matchingRows
ORDER BY _matchingRows DESC, id ASC
LIMIT 15
