# Data Anonymizer

Data Anonymizer is a Java 21 command-line tool that finds configured sensitive columns through JDBC, generates replacement data with DataFaker, and updates database tables in transactional JDBC batches.

PostgreSQL and MySQL JDBC drivers are included in the executable JAR. Other JDBC databases can be used when their driver is supplied on the classpath, but should be verified against a clone before use.

> Always create and verify a database backup. Run `--dry-run` first. Anonymization is destructive and cannot reconstruct the original values.

## Requirements

- Java 21 or newer
- Maven 3.9+ to build from source
- A database account allowed to read metadata, select rows, and update the selected tables
- Docker only when running the optional PostgreSQL/MySQL Testcontainers integration tests

## Safety model

- The plan can be inspected without writes using `--dry-run`.
- Each table is processed in its own transaction. Any generation, constraint, or SQL error rolls back that whole table.
- Updates use configurable JDBC batches and fetch sizes.
- Primary keys, foreign keys, and parent columns referenced by foreign keys are never anonymized.
- Single-column unique indexes are detected. Generated values are checked for duplicates within the current table run; the database constraint remains the final authority and triggers rollback on a collision.
- Tables without a primary key are reported and skipped because rows cannot be updated safely.
- Parent/child dependency layers run in order; independent batches in one layer may run concurrently.
- Cyclic dependencies are reported and processed in a final layer. This is safe because both sides of foreign-key relationships are protected.
- Reserved, mixed-case, and schema-qualified identifiers are quoted using JDBC metadata.
- `--continue-on-error` finishes independent work, prints a failure summary, and exits with code `2`. Failed tables can be retried with `--table`.

Protected key columns can still contain personal information. The tool deliberately leaves them unchanged rather than breaking referential integrity. If key pseudonymization is required, use a database-specific migration with deferred constraints and a deterministic cross-table mapping.

## Build and verification

```bash
mvn clean verify
```

The build creates one executable shaded JAR:

```text
target/data-anonymizer-26.3.1.jar
```

`verify` runs JUnit tests (including Mockito-based JDBC and transaction tests), embedded H2 integration tests, an executable-JAR test, and PostgreSQL/MySQL Testcontainers tests when Docker is available. Container tests are skipped when Docker is unavailable.

Publishing-related source/Javadoc generation, GPG signing, and Maven Central publishing are isolated in the `release` profile.

## Basic usage

First inspect the plan:

```bash
java -jar target/data-anonymizer-26.3.1.jar \
  --url jdbc:postgresql://localhost:5432/mydb \
  --username postgres \
  --password password \
  --schema public \
  --dry-run
```

Then run the anonymization without `--dry-run`:

```bash
java -jar target/data-anonymizer-26.3.1.jar \
  --url jdbc:postgresql://localhost:5432/mydb \
  --username postgres \
  --password password \
  --schema public
```

### MySQL

```bash
java -jar target/data-anonymizer-26.3.1.jar \
  --driver com.mysql.cj.jdbc.Driver \
  --url "jdbc:mysql://localhost:3306/mydb?useCursorFetch=true" \
  --username root \
  --password password
```

The MySQL driver is included in the shaded JAR.

## CLI options

| Option | Meaning | Default |
|---|---|---|
| `-d`, `--driver` | JDBC driver class | `org.postgresql.Driver` |
| `-l`, `--url` | JDBC URL | required |
| `-u`, `--username` | Database username | none |
| `-p`, `--password` | Database password | none |
| `-s`, `--schema` | JDBC schema pattern, or catalog/database for catalog-based drivers | current catalog or all visible schemas |
| `-c`, `--config` | Custom JSON configuration | built-in configuration |
| `-t`, `--threads` | Maximum parallel workers | `5` |
| `-b`, `--batch-size` | Tables grouped into one worker task | `5` |
| `--jdbc-batch-size` | Rows per `executeBatch()` | `500` |
| `--fetch-size` | JDBC result-set fetch size | `1000` |
| `--locale` | DataFaker BCP 47 language tag | `en` |
| `--dry-run` | Print the plan without writes | off |
| `--table` | Only selected table name/key; repeat, comma-separate, or use `*` | all |
| `--continue-on-error` | Continue and report failed tables | off |
| `-h`, `--help` | Display help | |
| `-V`, `--version` | Display the Maven/JAR version | |

Examples for resuming selected failures:

```bash
java -jar target/data-anonymizer-26.3.1.jar \
  -l jdbc:postgresql://localhost/mydb -u postgres -p password -s public \
  --table users,customers
```

## Configuration

Custom JSON is recursively merged over the built-in configuration:

- an object merges with the corresponding default object;
- a supplied array replaces the default array at that exact key;
- omitted categories keep their built-in defaults;
- `[]` explicitly disables one category or exclusion list.

Example:

```json
{
  "searchColumnTerms": {
    "email": ["email", "business_email"],
    "name": {
      "tableNameSearchTerms": ["users", "customers"],
      "excludedTableNameSearchTerms": ["admin_users"],
      "filter": ["first_name", "name"]
    },
    "surname": ["last_name", "surname"],
    "address": {
      "city": ["city"],
      "country": ["country"],
      "postalCode": ["zip", "postal_code"],
      "street": ["street", "address"]
    },
    "customs": [
      "nickname=#{Name.firstName}",
      "legacy_*=#{Internet.emailAddress}"
    ]
  },
  "excludeTableTerms": ["audit_log", "history"],
  "excludeColumnTerms": ["id", "created_at", "updated_at"]
}
```

Configuration is validated at startup. Blank patterns, malformed regular expressions, and malformed custom mappings are rejected before connecting to worker threads.

### Identifier matching

Identifiers are normalized case-insensitively. Camel case and separators become token boundaries:

- `valid_email` matches `email`;
- `userEmail` matches `email`;
- `shipping_address` does not match the short token `ip`;
- `source` does not match `rc`.

Normal search terms match complete identifier tokens or token sequences. Exclusion-column terms are exact by default, so excluding `id` does not exclude `national_id` or `valid_email`.

Advanced patterns:

- `legacy_*` — normalized glob;
- `regex:^email_[0-9]{4}$` — case-insensitive regular expression.

Custom mappings are evaluated before built-in mappings. Among built-in types, specific types such as full name, username, surname, company name, and domain take precedence over the broad `name` rule.

## Supported anonymization types

The implementation provides 31 strategies:

- Personal: email, first name, surname, full name, username, phone, birth date, gender, blood type
- Identifiers: SSN/national number, passport, driver's license, tax ID, national ID card
- Financial: credit card, IBAN, bank account
- Company: company name, job title, department
- Internet: IP address, MAC address, URL, domain
- Address: city, county, region, country, postal code, street
- Custom DataFaker expression

The default configuration recognizes common English, Slovak, and Czech column-name tokens. `--locale` controls the DataFaker locale where a provider supports it. Generic identifiers such as tax IDs, national IDs, and bank accounts do not guarantee country-specific legal formats; configure a custom DataFaker expression when an exact format is required.

## Processing large databases

`--fetch-size` controls how many primary-key rows the driver fetches at a time, while `--jdbc-batch-size` controls how many updates are sent in one JDBC batch. A table is committed only after the complete table succeeds, prioritizing atomic rollback over small commit checkpoints.

For very large tables:

1. run `--dry-run` and review every selected column;
2. test realistic volumes on a disposable clone;
3. tune fetch and JDBC batch sizes for the database driver;
4. use `--table` to process and resume manageable table groups;
5. monitor transaction-log/WAL capacity because one table is one transaction.

## Known intentional limits

- Tables without a primary key are skipped.
- Key columns involved in referential integrity are protected rather than pseudonymized.
- Multi-column unique constraints are left to the database to validate; a violation rolls back the table.
- Country-specific identifier validity is not inferred from Slovak/Czech column names.
- The tool updates values in place and does not create a reversible mapping.

## License

Apache License 2.0. See [LICENSE](LICENSE).
