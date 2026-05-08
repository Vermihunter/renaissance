// MATCH
//   (p1:Person {id: $id1})-[edge1:invest]->(m1:Company),
//   (p2:Person {id: $id2})-[edge2:invest]->(m2:Company)
// WHERE $start_time < edge1.timestamp < $end_time
//   AND $start_time < edge2.timestamp < $end_time
// WITH gds.similarity.jaccard(collect(m1.id), collect(m2.id)) AS jaccardSimilarity
// RETURN round(1000 * jaccardSimilarity) / 1000 AS jaccardSimilarity
MATCH
  (p1:Person {id: $id1})-[edge1:invest]->(m1:Company)
  WHERE $start_time < edge1.timestamp AND edge1.timestamp < $end_time

WITH collect(DISTINCT m1.id) AS companies1

MATCH
  (p2:Person {id: $id2})-[edge2:invest]->(m2:Company)
  WHERE $start_time < edge2.timestamp AND edge2.timestamp < $end_time

WITH
  companies1,
  collect(DISTINCT m2.id) AS companies2

WITH
  [x IN companies1 WHERE x IN companies2] AS intersection,
  companies1 + [x IN companies2 WHERE NOT x IN companies1] AS unionSet

WITH
  CASE
    WHEN size(unionSet) = 0 THEN 0.0
    ELSE toFloat(size(intersection)) / toFloat(size(unionSet))
    END AS jaccardSimilarity

RETURN round(1000 * jaccardSimilarity) / 1000 AS jaccardSimilarity