MATCH path=(comp:Company {id: $id})<-[:invest*1..3]-(investor)
  WHERE (investor:Company) OR (investor:Person)
WITH
  investor.id AS id,
  labels(investor)[0] AS type,
  reduce(
  ratio = 1.0,
  e IN relationships(path) |
  ratio * e.amount /
  reduce(
  total = 0.0,
  x IN [ (:Company {id: startNode(e).id})<-[inv:invest]-() | inv.amount ] |
  total + x
  )
  ) AS ratio
RETURN id, type, round(1000 * ratio) / 1000 AS ratio
ORDER BY ratio DESC