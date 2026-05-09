// Parameter generator for tcr-9.cypher
// Safe version for the original unmodified standard query.
//
// Important:
//   Excludes ids outside the exact integer range of JSON/Double-safe numbers.
//   This prevents Account.id from being corrupted between generation and execution.

WITH
  1640995200000 AS start_time,
  1672531200000 AS end_time,
  0.0 AS threshold,
  0.0 AS lowerbound,
  1000000000.0 AS upperbound,
  9007199254740991 AS max_safe_integer

MATCH
  (loan:Loan)-[edge1:deposit]->(mid:Account)-[edge2:repay]->(loan),
  (up:Account)-[edge3:transfer]->(mid)-[edge4:transfer]->(down:Account)

  WHERE mid.id IS NOT NULL
  AND mid.id >= -max_safe_integer
  AND mid.id <= max_safe_integer

  AND edge1.amount > threshold
  AND start_time < edge1.timestamp < end_time

  AND edge2.amount > threshold
  AND start_time < edge2.timestamp < end_time

  AND lowerbound < edge1.amount / edge2.amount < upperbound

  AND edge3.amount > threshold
  AND start_time < edge3.timestamp < end_time

  AND edge4.amount > threshold
  AND start_time < edge4.timestamp < end_time

WITH
  mid.id AS id,
  start_time,
  end_time,
  threshold,
  lowerbound,
  upperbound,

  count(*) AS _matchingRows,
sum(edge1.amount) AS _originalDepositSum,
sum(edge2.amount) AS _originalRepaySum,
sum(edge3.amount) AS _originalTransferInSum,
sum(edge4.amount) AS _originalTransferOutSum

WHERE _matchingRows > 0
AND _originalRepaySum IS NOT NULL
AND _originalTransferOutSum IS NOT NULL
AND _originalRepaySum <> 0
AND _originalTransferOutSum <> 0

RETURN
  id AS id,
  start_time AS start_time,
  end_time AS end_time,
  threshold AS threshold,
  lowerbound AS lowerbound,
  upperbound AS upperbound,

_matchingRows,
_originalDepositSum,
_originalRepaySum,
_originalTransferInSum,
_originalTransferOutSum

ORDER BY _matchingRows DESC, id ASC
LIMIT 15