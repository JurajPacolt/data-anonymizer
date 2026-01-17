# DATA-ANONYMIZER
Data anonymizer via JDBC - a tool for anonymizing sensitive data in databases through JDBC connection.

## How it works

Data Anonymizer works in several steps:

1. **Configuration loading** - Loads the configuration file (or uses default settings)
2. **Database connection** - Connects to the database via JDBC
3. **Metadata loading** - Reads the database structure (tables, columns, primary and foreign keys)
4. **Anonymization plan creation** - Based on the configuration, determines which columns in which tables should be anonymized
5. **Batch processing** - Splits tables into batches and processes them in parallel using a thread pool
6. **Data anonymization** - For each row, generates fake data using DataFaker and updates them in the database

## Default configuration

The default configuration automatically recognizes common column names in English, Slovak and Czech:
- **Email**: `email`, `e_mail`, `mail`, `elektronicka_posta`, `elektronicky_mail`
- **Name**: `name`, `first_name`, `firstname`, `meno`, `krstne_meno`, `krestni_meno`, `jmeno` (only in user tables)
- **Surname**: `surname`, `last_name`, `lastname`, `priezvisko`, `prijmeni`
- **Full name**: `full_name`, `fullname`, `cele_meno`, `plne_meno`
- **Username**: `username`, `user_name`, `login`, `uzivatelske_meno`, `uzivatelske_jmeno`
- **Phone**: `phone`, `telephone`, `mobile`, `telefon`, `telefonne_cislo`, `mobil`
- **Birth date**: `birth_date`, `birthdate`, `datum_narodenia`, `datum_narozeni`, `dat_nar`
- **Personal identifiers**:
  - SSN/National ID number: `ssn`, `social_security_number`, `rodne_cislo`, `rc`, `rodni_cislo`
  - Passport number: `passport`, `passport_number`, `pas`, `cislo_pasu`
  - Driver's license: `driver_license`, `ridicsky_preukaz`, `rp`, `ridicak`, `ridicsky_prukaz`
  - Tax ID/VAT: `tax_id`, `vat_id`, `ico`, `ic`, `dic`, `danove_cislo`
  - National ID card: `national_id`, `obciansky_preukaz`, `op`, `obcansky_prukaz`
- **Financial data**:
  - Credit card: `credit_card`, `card_number`, `kreditna_karta`, `cislo_karty`, `kreditni_karta`
  - IBAN: `iban`, `international_bank`, `medzinarodne_cislo_uctu`
  - Bank account: `bank_account`, `account_number`, `bankovy_ucet`, `cislo_uctu`, `bankovni_ucet`
- **Company data**:
  - Company name: `company_name`, `company`, `firma`, `nazov_firmy`, `spolecnost`, `organizacia`
  - Position: `job_title`, `position`, `pozicia`, `pracovna_pozicia`, `funkce`, `funkcia`
  - Department: `department`, `dept`, `oddelenie`, `oddeleni`, `divize`, `sekcia`
- **Internet**:
  - IP address: `ip_address`, `ip`, `ip_adresa`
  - MAC address: `mac_address`, `mac`, `mac_adresa`
  - URL: `url`, `website`, `webova_stranka`, `webova_adresa`
  - Domain: `domain`, `domain_name`, `domena`, `domenove_meno`
- **Additional personal data**:
  - Gender: `gender`, `sex`, `pohlavie`, `pohlavi`
  - Blood type: `blood_type`, `blood_group`, `krevna_skupina`, `krevni_skupina`
- **Address**: 
  - City: `city`, `town`, `mesto`, `obec`
  - County: `county`, `okres`
  - Region: `region`, `state`, `kraj`, `zupa`
  - Country: `country`, `stat`, `krajina`
  - Postal code: `postal_code`, `zip_code`, `psc`, `postal_index`
  - Street: `street`, `address`, `ulica`, `adresa`, `ulice`

Excludes by default:
- Tables: `log`, `audit`, `history`, `migration`, `historia`, `migrace`
- Columns: `id`, `created`, `updated`, `deleted`, `version`, `vytvorene`, `aktualizovane`, `zmazane`, `verze`, `verzia`

## Configuration file

You can create your own configuration file in JSON format. Example:

```json
{
  "searchColumnTerms": {
    "email": ["email", "user_email"],
    "name": {
      "tableNameSearchTerms": ["users", "customers"],
      "excludedTableNameSearchTerms": ["logs"],
      "filter": ["first_name", "name"]
    },
    "surname": ["last_name", "surname"],
    "username": ["username", "login"],
    "phone": ["phone", "mobile"],
    "birthDate": ["birth_date", "dob"],
    "address": {
      "city": ["city"],
      "country": ["country"],
      "postalCode": ["zip", "postal_code"],
      "street": ["street", "address"]
    },
    "customs": ["nickname=#{Name.firstName}"]
  },
  "excludeTableTerms": ["audit_log", "history"],
  "excludeColumnTerms": ["id", "created_at", "updated_at"]
}
```

### Configuration explanation:

- **searchColumnTerms**: Search terms for column names (case-insensitive, substring match)
- **name**: Special configuration for names with the ability to filter by table name
  - `tableNameSearchTerms`: Only tables with these terms in the name
  - `excludedTableNameSearchTerms`: Exclude tables with these terms
  - `filter`: Search terms for column names
- **customs**: Custom mappings in the form `columnPattern=expression` (e.g., `nickname=#{Name.firstName}`)
- **excludeTableTerms**: Tables with these terms in the name will not be processed
- **excludeColumnTerms**: Columns with these terms in the name will not be anonymized

## Usage examples

### Basic usage with default configuration:
```bash
java -jar data-anonymizer-0.0.1.jar \
  -d org.postgresql.Driver \
  -l jdbc:postgresql://localhost:5432/mydb \
  -u postgres \
  -p password \
  -s public
```

### Usage with custom configuration file:
```bash
java -jar data-anonymizer-0.0.1.jar \
  -d org.postgresql.Driver \
  -l jdbc:postgresql://localhost:5432/mydb \
  -u postgres \
  -p password \
  -s public \
  -c config.json \
  -t 10
```

### Parameters:

- `-d, --driver`: JDBC driver class (default: `org.postgresql.Driver`)
- `-l, --url`: JDBC connection URL (required)
- `-u, --username`: Database username
- `-p, --password`: Password
- `-s, --schema`: Database schema (optional)
- `-c, --config`: Path to configuration file (optional, otherwise default configuration is used)
- `-t, --threads`: Number of threads for parallel processing (default: 5)
- `-b, --batch-size`: Table batch size for processing (default: 5)
- `-h, --help`: Display help
- `-V, --version`: Display version

### Example for MySQL:
```bash
java -jar data-anonymizer-0.0.1.jar \
  -d com.mysql.cj.jdbc.Driver \
  -l jdbc:mysql://localhost:3306/mydb \
  -u root \
  -p password \
  -c custom-config.json
```

**Note:** We recommend creating a database backup before running on production data!

## Supported Languages

The tool automatically recognizes column names in:
- 🇬🇧 **English** - Standard international naming conventions
- 🇸🇰 **Slovak** - `meno`, `priezvisko`, `rodne_cislo`, `mesto`, `ulica`, etc.
- 🇨🇿 **Czech** - `jmeno`, `prijmeni`, `rodni_cislo`, `mesto`, `ulice`, etc.

## Real-World Usage Examples

### Example 1: Anonymizing a User Database

**Scenario:** You have a PostgreSQL database with user data that needs to be anonymized for testing purposes.

**Database structure:**
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50),
    email VARCHAR(100),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    birth_date DATE,
    created_at TIMESTAMP
);
```

**Command:**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://localhost:5432/myapp \
  -u postgres \
  -p secret123 \
  -s public
```

**Result:**
- `username` → anonymized (detected automatically)
- `email` → anonymized (detected automatically)
- `first_name` → anonymized (detected automatically as name in "users" table)
- `last_name` → anonymized (detected automatically)
- `phone` → anonymized (detected automatically)
- `birth_date` → anonymized (detected automatically)
- `id`, `created_at` → **NOT** anonymized (excluded by default)

**Before:**
```
| id | username  | email              | first_name | last_name | phone          | birth_date |
|----|-----------|-----------------------|-----------|-----------|----------------|------------|
| 1  | john_doe  | john@example.com      | John      | Doe       | +1-555-0100    | 1985-03-15 |
| 2  | jane_smith| jane.smith@email.com  | Jane      | Smith     | +1-555-0200    | 1990-07-22 |
```

**After:**
```
| id | username     | email                  | first_name | last_name | phone          | birth_date |
|----|--------------|------------------------|------------|-----------|----------------|------------|
| 1  | maria.harris | william.moore@gmail.com| William    | Moore     | +1-234-567-890 | 1967-11-08 |
| 2  | james_wilson | sophia.taylor@yahoo.com| Sophia     | Taylor    | +1-345-678-901 | 1978-02-19 |
```

---

### Example 2: Slovak Database with Custom Configuration

**Scenario:** Slovak e-commerce database with customer and order information.

**Database structure:**
```sql
CREATE TABLE zakaznici (
    id SERIAL PRIMARY KEY,
    meno VARCHAR(50),
    priezvisko VARCHAR(50),
    email VARCHAR(100),
    telefon VARCHAR(20),
    mesto VARCHAR(50),
    ulica VARCHAR(100),
    psc VARCHAR(10),
    rodne_cislo VARCHAR(11),
    datum_narodenia DATE
);

CREATE TABLE objednavky (
    id SERIAL PRIMARY KEY,
    zakaznik_id INTEGER REFERENCES zakaznici(id),
    cislo_karty VARCHAR(20),
    suma DECIMAL(10,2),
    vytvorene TIMESTAMP
);
```

**Custom config file (sk-config.json):**
```json
{
  "searchColumnTerms": {
    "email": ["email"],
    "name": {
      "tableNameSearchTerms": ["zakaznici"],
      "filter": ["meno"]
    },
    "surname": ["priezvisko"],
    "phone": ["telefon"],
    "birthDate": ["datum_narodenia"],
    "ssn": ["rodne_cislo"],
    "creditCard": ["cislo_karty"],
    "address": {
      "city": ["mesto"],
      "street": ["ulica"],
      "postalCode": ["psc"]
    }
  },
  "excludeTableTerms": [],
  "excludeColumnTerms": ["id", "suma", "vytvorene", "zakaznik_id"]
}
```

**Command:**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://localhost:5432/eshop \
  -u admin \
  -p password \
  -s public \
  -c sk-config.json \
  -t 5
```

**Result:**
All sensitive Slovak customer data anonymized while preserving referential integrity and business data (amounts, dates).

---

### Example 3: MySQL HR Database

**Scenario:** Human Resources database with employee records.

**Database structure:**
```sql
CREATE TABLE employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100),
    email VARCHAR(100),
    ssn VARCHAR(11),
    job_title VARCHAR(50),
    department VARCHAR(50),
    company_name VARCHAR(100),
    salary DECIMAL(10,2),
    hire_date DATE
);
```

**Command:**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -d com.mysql.cj.jdbc.Driver \
  -l "jdbc:mysql://localhost:3306/hr_system?useSSL=false" \
  -u hr_admin \
  -p hr_pass_2024 \
  -t 8
```

**Result:**
- `full_name` → John Smith → **Emma Johnson**
- `email` → john.smith@company.com → **emma.johnson@example.com**
- `ssn` → 123-45-6789 → **987-65-4321**
- `job_title` → Senior Developer → **Product Manager**
- `department` → Engineering → **Marketing**
- `company_name` → TechCorp Inc. → **InnovateSoft LLC**
- `salary` → **NOT** anonymized (not in default config)
- `hire_date` → **NOT** anonymized (not a birth date)

---

### Example 4: Selective Anonymization with Exclusions

**Scenario:** You want to anonymize a database but keep certain tables and columns intact.

**Custom config (selective-config.json):**
```json
{
  "searchColumnTerms": {
    "email": ["email", "contact_email"],
    "name": {
      "tableNameSearchTerms": ["users", "customers"],
      "excludedTableNameSearchTerms": ["admin"],
      "filter": ["first_name", "name"]
    },
    "surname": ["last_name"],
    "phone": ["phone", "mobile"]
  },
  "excludeTableTerms": ["logs", "audit", "system_config", "admin_users"],
  "excludeColumnTerms": ["id", "created_at", "updated_at", "status", "is_active"]
}
```

**Command:**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://localhost:5432/production_clone \
  -u postgres \
  -p secure_pass \
  -c selective-config.json \
  -t 10
```

**Result:**
- Tables `admin_users`, `logs`, `audit`, `system_config` → **Skipped entirely**
- Admin table users → **NOT** anonymized (excluded by name config)
- Status, timestamps, flags → **Preserved**
- Only customer/user PII → **Anonymized**

---

### Example 5: Czech Financial Database

**Scenario:** Czech banking database with client and transaction data.

**Database structure:**
```sql
CREATE TABLE klienti (
    id SERIAL PRIMARY KEY,
    jmeno VARCHAR(50),
    prijmeni VARCHAR(50),
    rodni_cislo VARCHAR(11),
    email VARCHAR(100),
    telefon VARCHAR(20),
    ico VARCHAR(8),
    dic VARCHAR(12),
    bankovni_ucet VARCHAR(34)
);
```

**Command (using default config - Czech terms auto-detected):**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://banka.local:5432/banking \
  -u db_user \
  -p db_pass \
  -s public \
  -t 15
```

**Before:**
```
| id | jmeno | prijmeni | rodni_cislo | email           | ico      | dic        | bankovni_ucet              |
|----|-------|----------|-------------|-----------------|----------|------------|----------------------------|
| 1  | Pavel | Novák    | 850315/1234 | pavel@email.cz  | 12345678 | CZ12345678 | CZ6508000000192000145399   |
```

**After:**
```
| id | jmeno  | prijmeni | rodni_cislo | email              | ico      | dic        | bankovni_ucet              |
|----|--------|----------|-------------|--------------------|----------|------------|----------------------------|
| 1  | Martin | Svoboda  | 780522/5678 | martin@example.com | 87654321 | CZ87654321 | CZ9401000000123456789012   |
```

---

### Example 6: Large Database with Progress Monitoring

**Scenario:** Anonymizing a large production database clone with millions of records.

**Command:**
```bash
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://dbserver:5432/production_copy \
  -u anonymizer \
  -p anon_pass_2024 \
  -s public \
  -t 20 \
  2>&1 | tee anonymization.log
```

**Console output:**
```
Processing table: users (5 columns to anonymize)
  Processed 1000 rows in users
  Processed 2000 rows in users
  Processed 3000 rows in users
  Total rows anonymized: 3450
Completed table: users

Processing table: customers (8 columns to anonymize)
  Processed 1000 rows in customers
  Processed 2000 rows in customers
  Total rows anonymized: 2180
Completed table: customers

Skipping table (no columns to anonymize): products
Skipping table (no columns to anonymize): orders
```

---

### Example 7: Docker Database Anonymization

**Scenario:** Anonymize a database running in Docker container.

**Command:**
```bash
# Get container IP or use localhost with port mapping
docker exec -it postgres-container bash -c "psql -U postgres -d mydb -c 'SELECT 1'"

# Run anonymizer
java -jar data-anonymizer-0.0.1.jar \
  -l jdbc:postgresql://localhost:5432/mydb \
  -u postgres \
  -p postgres \
  -s public \
  -t 10
```

---

### Example 8: Custom DataFaker Expressions

**Scenario:** Using custom expressions for specific data generation.

**Custom config (custom-faker.json):**
```json
{
  "searchColumnTerms": {
    "email": ["email"],
    "customs": ["alt_email=#{Internet.emailAddress}", "full_name=#{Name.fullName}", "cell=#{PhoneNumber.cellPhone}"]
  },
  "excludeTableTerms": [],
  "excludeColumnTerms": ["id"]
}
```

Entries in `customs` use `columnPattern=expression` and match column names by substring.

## Anonymization Types

The tool supports 30+ types of data anonymization:

**Personal Data:**
- Email, Name, Surname, Full Name, Username
- Phone number, Birth date, Gender, Blood type

**Personal Identifiers:**
- SSN/National ID number, Passport number
- Driver's license, Tax ID, National ID card

**Financial Data:**
- Credit card, IBAN, Bank account

**Company Data:**
- Company name, Job title, Department

**Internet:**
- IP address, MAC address, URL, Domain

**Address:**
- City, County, Region, Country, Postal code, Street

**Custom:**
- DataFaker expressions for custom generation

