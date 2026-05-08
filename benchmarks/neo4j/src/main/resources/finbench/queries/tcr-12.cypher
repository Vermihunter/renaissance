// TODO: implement truncation strategy
MATCH path=(p1:Person {id: $id})-[:guarantee*]->(pX:Person)
WHERE all(e IN relationships(path) WHERE $start_time < e.timestamp AND e.timestamp < $end_time)
UNWIND nodes(path)[1..] AS person
MATCH (person)-[:apply]->(loan:Loan)
RETURN sum(loan.loanAmount) AS sumLoanAmount, count(loan) AS numLoans