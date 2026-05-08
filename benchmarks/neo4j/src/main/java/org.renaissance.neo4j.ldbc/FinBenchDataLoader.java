package org.renaissance.neo4j.ldbc;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.Schema;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * The data loader for the finbench benchmark
 * - Use the csv files from the "snapshot" directory downloaded from LDBC
 *
 */
public final class FinBenchDataLoader {
    private static final int BATCH_SIZE = 10_000;

    private static final RelationshipType OWN = RelationshipType.withName("own");
    private static final RelationshipType APPLY = RelationshipType.withName("apply");
    private static final RelationshipType REPAY = RelationshipType.withName("repay");
    private static final RelationshipType DEPOSIT = RelationshipType.withName("deposit");
    private static final RelationshipType TRANSFER = RelationshipType.withName("transfer");
    private static final RelationshipType WITHDRAW = RelationshipType.withName("withdraw");
    private static final RelationshipType INVEST = RelationshipType.withName("invest");
    private static final RelationshipType GUARANTEE = RelationshipType.withName("guarantee");
    private static final RelationshipType SIGN_IN = RelationshipType.withName("signIn");

    private final GraphDatabaseService db;

    public FinBenchDataLoader(GraphDatabaseService db) {
        this.db = db;
    }

    public void loadDataset(String base) {
        loadNodes(base);
        createIndexes();
        loadRelationships(base);
        printGraphSummary();
    }

    private void loadNodes(String base) {
        loadAccountNodes(base + "/Account.csv");
        loadCompanyNodes(base + "/Company.csv");
        loadLoanNodes(base + "/Loan.csv");
        loadMediumNodes(base + "/Medium.csv");
        loadPersonNodes(base + "/Person.csv");
    }

    private void loadRelationships(String base) {
        loadPersonOwnAccount(base + "/PersonOwnAccount.csv");
        loadCompanyOwnAccount(base + "/CompanyOwnAccount.csv");
        loadPersonApplyLoan(base + "/PersonApplyLoan.csv");
        loadCompanyApplyLoan(base + "/CompanyApplyLoan.csv");
        loadAccountRepayLoan(base + "/AccountRepayLoan.csv");
        loadLoanDepositAccount(base + "/LoanDepositAccount.csv");
        loadAccountTransferAccount(base + "/AccountTransferAccount.csv");
        loadAccountWithdrawAccount(base + "/AccountWithdrawAccount.csv");
        loadPersonInvestCompany(base + "/PersonInvestCompany.csv");
        loadCompanyInvestCompany(base + "/CompanyInvestCompany.csv");
        loadPersonGuaranteePerson(base + "/PersonGuaranteePerson.csv");
        loadCompanyGuaranteeCompany(base + "/CompanyGuaranteeCompany.csv");
        loadMediumSignInAccount(base + "/MediumSignInAccount.csv");
    }

    private void createIndexes() {
        try (Transaction tx = db.beginTx()) {
            Schema schema = tx.schema();

            schema.indexFor(Label.label("Account")).on("id").create();
            schema.indexFor(Label.label("Company")).on("id").create();
            schema.indexFor(Label.label("Loan")).on("id").create();
            schema.indexFor(Label.label("Medium")).on("id").create();
            schema.indexFor(Label.label("Person")).on("id").create();

            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create indexes", e);
        }

        try (Transaction tx = db.beginTx()) {
            tx.schema().awaitIndexesOnline(10, TimeUnit.MINUTES);
            tx.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed waiting for indexes", e);
        }
    }

    private void loadAccountNodes(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node n = row.tx.createNode(Label.label("Account"));

            setLong(n, "id", row.at(0));
            setTimestamp(n, "createTime", row.at(1));
            setBoolean(n, "isBlocked", row.at(2));
            setString(n, "type", row.at(3));
            setString(n, "accountType", row.at(3));
            setString(n, "nickname", row.at(4));
            setString(n, "phoneNum", row.at(5));
            setString(n, "phonenum", row.at(5));
            setString(n, "email", row.at(6));
            setString(n, "freqLoginType", row.at(7));
            setLong(n, "lastLoginTime", row.at(8));
            setString(n, "level", row.at(9));
            setString(n, "accountLevel", row.at(9));
        });
    }

    private void loadCompanyNodes(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node n = row.tx.createNode(Label.label("Company"));

            setLong(n, "id", row.at(0));
            setString(n, "name", row.at(1));
            setString(n, "companyName", row.at(1));
            setBoolean(n, "isBlocked", row.at(2));
            setTimestamp(n, "createTime", row.at(3));
            setString(n, "country", row.at(4));
            setString(n, "city", row.at(5));
            setString(n, "business", row.at(6));
            setString(n, "description", row.at(7));
            setString(n, "url", row.at(8));
        });
    }

    private void loadLoanNodes(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node n = row.tx.createNode(Label.label("Loan"));

            setLong(n, "id", row.at(0));
            setDouble(n, "amount", row.at(1));
            setDouble(n, "loanAmount", row.at(1));
            setDouble(n, "balance", row.at(2));
            setTimestamp(n, "createTime", row.at(3));
            setString(n, "usage", row.at(4));
            setString(n, "loanUsage", row.at(4));
            setDouble(n, "interestRate", row.at(5));
        });
    }

    private void loadMediumNodes(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node n = row.tx.createNode(Label.label("Medium"));

            setLong(n, "id", row.at(0));
            setString(n, "type", row.at(1));
            setString(n, "mediumType", row.at(1));
            setBoolean(n, "isBlocked", row.at(2));
            setTimestamp(n, "createTime", row.at(3));
            setLong(n, "lastLoginTime", row.at(4));
            setString(n, "riskLevel", row.at(5));
        });
    }

    private void loadPersonNodes(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node n = row.tx.createNode(Label.label("Person"));

            setLong(n, "id", row.at(0));
            setString(n, "name", row.at(1));
            setString(n, "personName", row.at(1));
            setBoolean(n, "isBlocked", row.at(2));
            setTimestamp(n, "createTime", row.at(3));
            setString(n, "gender", row.at(4));
            setString(n, "birthday", row.at(5));
            setString(n, "country", row.at(6));
            setString(n, "city", row.at(7));
        });
    }

    private void loadPersonOwnAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node person = findRequired(row.tx, "Person", parseLong(row.at(0)));
            Node account = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = person.createRelationshipTo(account, OWN);
            setTimestamp(rel, "timestamp", row.at(2));
        });
    }

    private void loadCompanyOwnAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node company = findRequired(row.tx, "Company", parseLong(row.at(0)));
            Node account = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = company.createRelationshipTo(account, OWN);
            setTimestamp(rel, "timestamp", row.at(2));
        });
    }

    private void loadPersonApplyLoan(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node person = findRequired(row.tx, "Person", parseLong(row.at(0)));
            Node loan = findRequired(row.tx, "Loan", parseLong(row.at(1)));

            Relationship rel = person.createRelationshipTo(loan, APPLY);
            setTimestamp(rel, "timestamp", row.at(2));
            setString(rel, "org", row.at(3));
        });
    }

    private void loadCompanyApplyLoan(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node company = findRequired(row.tx, "Company", parseLong(row.at(0)));
            Node loan = findRequired(row.tx, "Loan", parseLong(row.at(1)));

            Relationship rel = company.createRelationshipTo(loan, APPLY);
            setTimestamp(rel, "timestamp", row.at(2));
            setString(rel, "org", row.at(3));
        });
    }

    private void loadAccountRepayLoan(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node account = findRequired(row.tx, "Account", parseLong(row.at(0)));
            Node loan = findRequired(row.tx, "Loan", parseLong(row.at(1)));

            Relationship rel = account.createRelationshipTo(loan, REPAY);
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
        });
    }

    private void loadLoanDepositAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node loan = findRequired(row.tx, "Loan", parseLong(row.at(0)));
            Node account = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = loan.createRelationshipTo(account, DEPOSIT);
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
        });
    }

    private void loadAccountTransferAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node from = findRequired(row.tx, "Account", parseLong(row.at(0)));
            Node to = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = from.createRelationshipTo(to, TRANSFER);
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
            setString(rel, "orderNum", row.at(4));
            setString(rel, "comment", row.at(5));
            setString(rel, "payType", row.at(6));
            setString(rel, "goodsType", row.at(7));
        });
    }

    private void loadAccountWithdrawAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node from = findRequired(row.tx, "Account", parseLong(row.at(0)));
            Node to = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = from.createRelationshipTo(to, WITHDRAW);
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
        });
    }

    private void loadPersonInvestCompany(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node person = findRequired(row.tx, "Person", parseLong(row.at(0)));
            Node company = findRequired(row.tx, "Company", parseLong(row.at(1)));

            Relationship rel = person.createRelationshipTo(company, INVEST);
            setDouble(rel, "ratio", row.at(2));
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
        });
    }

    private void loadCompanyInvestCompany(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node investor = findRequired(row.tx, "Company", parseLong(row.at(0)));
            Node company = findRequired(row.tx, "Company", parseLong(row.at(1)));

            Relationship rel = investor.createRelationshipTo(company, INVEST);
            setDouble(rel, "ratio", row.at(2));
            setDouble(rel, "amount", row.at(2));
            setTimestamp(rel, "timestamp", row.at(3));
        });
    }

    private void loadPersonGuaranteePerson(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node from = findRequired(row.tx, "Person", parseLong(row.at(0)));
            Node to = findRequired(row.tx, "Person", parseLong(row.at(1)));

            Relationship rel = from.createRelationshipTo(to, GUARANTEE);
            setTimestamp(rel, "timestamp", row.at(2));
            setString(rel, "relation", row.at(3));
        });
    }

    private void loadCompanyGuaranteeCompany(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node from = findRequired(row.tx, "Company", parseLong(row.at(0)));
            Node to = findRequired(row.tx, "Company", parseLong(row.at(1)));

            Relationship rel = from.createRelationshipTo(to, GUARANTEE);
            setTimestamp(rel, "timestamp", row.at(2));
            setString(rel, "relation", row.at(3));
        });
    }

    private void loadMediumSignInAccount(String resourcePath) {
        loadCsv(resourcePath, row -> {
            Node medium = findRequired(row.tx, "Medium", parseLong(row.at(0)));
            Node account = findRequired(row.tx, "Account", parseLong(row.at(1)));

            Relationship rel = medium.createRelationshipTo(account, SIGN_IN);
            setTimestamp(rel, "timestamp", row.at(2));
            setString(rel, "location", row.at(3));
        });
    }

    private interface RowConsumer {
        void accept(CsvRow row) throws Exception;
    }

    private static final class CsvRow {
        final Transaction tx;
        final String[] cols;

        CsvRow(Transaction tx, String[] cols) {
            this.tx = tx;
            this.cols = cols;
        }

        String at(int i) {
            return i < cols.length ? cols[i] : "";
        }
    }

    private void loadCsv(String resourcePath, RowConsumer consumer) {
        System.out.println("Loading: " + resourcePath);

        try (
                InputStream is = FinBenchDataLoader.class
                        .getClassLoader()
                        .getResourceAsStream(resourcePath)
        ) {
            if (is == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String header = reader.readLine();
                if (header == null) {
                    System.out.println("Skipping empty file: " + resourcePath);
                    return;
                }

                int count = 0;
                Transaction tx = db.beginTx();

                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) {
                            continue;
                        }

                        String[] cols = line.split("\\|", -1);
                        consumer.accept(new CsvRow(tx, cols));
                        count++;

                        if (count % BATCH_SIZE == 0) {
                            tx.commit();
                            tx.close();
                            System.out.printf("Loaded %,d rows from %s%n", count, resourcePath);
                            tx = db.beginTx();
                        }
                    }

                    tx.commit();
                    System.out.printf("Finished %,d rows from %s%n", count, resourcePath);
                } finally {
                    tx.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed loading CSV: " + resourcePath, e);
        }
    }

    private Node findRequired(Transaction tx, String label, Object value) {
        Node node = tx.findNode(Label.label(label), "id", value);
        if (node == null) {
            throw new IllegalStateException(
                    "Could not find node: label=" + label + ", key=id, value=" + value
            );
        }
        return node;
    }

    private void printGraphSummary() {
        try (Transaction tx = db.beginTx()) {
            System.out.printf(
                    "Graph summary: Account=%d Person=%d Company=%d Loan=%d Medium=%d%n",
                    countNodes(tx, "Account"),
                    countNodes(tx, "Person"),
                    countNodes(tx, "Company"),
                    countNodes(tx, "Loan"),
                    countNodes(tx, "Medium")
            );

            System.out.printf(
                    "Relationship summary: own=%d transfer=%d withdraw=%d repay=%d deposit=%d invest=%d guarantee=%d signIn=%d apply=%d%n",
                    countRelationships(tx, "own"),
                    countRelationships(tx, "transfer"),
                    countRelationships(tx, "withdraw"),
                    countRelationships(tx, "repay"),
                    countRelationships(tx, "deposit"),
                    countRelationships(tx, "invest"),
                    countRelationships(tx, "guarantee"),
                    countRelationships(tx, "signIn"),
                    countRelationships(tx, "apply")
            );

            tx.commit();
        }
    }

    private long countNodes(Transaction tx, String label) {
        Result result = tx.execute("MATCH (n:" + label + ") RETURN count(n) AS c");
        return ((Number) result.next().get("c")).longValue();
    }

    private long countRelationships(Transaction tx, String type) {
        Result result = tx.execute("MATCH ()-[r:" + type + "]->() RETURN count(r) AS c");
        return ((Number) result.next().get("c")).longValue();
    }

    private static long parseLong(String s) {
        return Long.parseLong(s.trim());
    }

    private static double parseDouble(String s) {
        return Double.parseDouble(s.trim());
    }

    private static boolean parseBoolean(String s) {
        return Boolean.parseBoolean(s.trim());
    }

    private static void setString(Entity entity, String key, String value) {
        if (value != null && !value.isBlank()) {
            entity.setProperty(key, value);
        }
    }

    private static void setLong(Entity entity, String key, String value) {
        if (value != null && !value.isBlank()) {
            entity.setProperty(key, parseLong(value));
        }
    }

    private static void setDouble(Entity entity, String key, String value) {
        if (value != null && !value.isBlank()) {
            entity.setProperty(key, parseDouble(value));
        }
    }

    private static void setBoolean(Entity entity, String key, String value) {
        if (value != null && !value.isBlank()) {
            entity.setProperty(key, parseBoolean(value));
        }
    }

    private static void setTimestamp(Entity entity, String key, String value) {
        if (value != null && !value.isBlank()) {
            entity.setProperty(key, ParameterCsv.parseTimestampMillis(value));
        }
    }
}
