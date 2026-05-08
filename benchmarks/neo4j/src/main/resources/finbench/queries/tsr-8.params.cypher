// Parameter generator for tsr-8.cypher
// Generated for the Renaissance Neo4j LDBC/FinBench benchmark.
//
// Contract:
//   - Return one row per parameter set.
//   - Column names must match the parameters used by tsr-8.cypher.
//   - Diagnostic columns must start with "_" and are not passed to the benchmark query.
//   - Timestamps are epoch milliseconds.
//   - Default time window: [2022-01-01, 2023-01-01).
//
// Note: these generators target the canonicalized schema used by the supplied
// Neo4JLDBCAnalytics implementation:
//   nodes: Account/Person/Company/Loan/Medium with property id
//   rels: own/apply/repay/deposit/transfer/withdraw/invest/guarantee/signIn
//   rel timestamp: timestamp

MATCH (c:Company)
OPTIONAL MATCH (c)-[:own]->(acc:Account)
WITH c, count(DISTINCT acc) AS _accCount
OPTIONAL MATCH (:Person)-[:invest]->(c)
WITH c, _accCount, count(*) AS _personInvestorCount
OPTIONAL MATCH (:Company)-[:invest]->(c)
WITH c, _accCount, _personInvestorCount, count(*) AS _companyInvestorCount
OPTIONAL MATCH (c)-[:apply]->(loan:Loan)
WITH c, _accCount, _personInvestorCount, _companyInvestorCount, count(DISTINCT loan) AS _loanCount
WITH c, _accCount, _personInvestorCount, _companyInvestorCount, _loanCount,
     _accCount + _personInvestorCount + _companyInvestorCount + _loanCount AS _score
RETURN
  c.id AS id,
  _score,
  _accCount,
  _personInvestorCount,
  _companyInvestorCount,
  _loanCount
ORDER BY _score DESC, id ASC
LIMIT 15
