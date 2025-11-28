# Product Catalog (AST Course Project)

A product catalog application developed using Test-Driven Development for the AST course.

## Quality Control

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=bugs)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=coverage)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=dariopipa_tdd-product-catalog&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=dariopipa_tdd-product-catalog)

---

# Setup & Run the Project

To be able to run the project the following requirements must be installed on the machine:

- Java 17
- Maven
- Docker & Docker Compose

Navigate to the public repository Github Repository  
Clone the project to the desired directory, with the following command

```
git clone https://github.com/dariopipa/tdd-product-catalog.git
```

Navigate to the project directory

```
cd tdd-product-catalog
```

Run Unit Test only

```
mvn test
```

Run Integration Test & E2E tests

```
mvn verify
```

Run Mutation Tests (PIT) by activating the profile

```
mvn verify -Pmutation-testing
```

Generate Junit & IT reports, should be run only after having run the tests.

```
mvn surefire-report:report-only surefire-report:failsafe-report-only
```

Create the Maven Site, it’s better to run it after having the reports.

```
mvn site
```

Run the Maven Site locally, by default the site will be launched at http://localhost:8080/

```
mvn site:run
```

Package the application

```
mvn package
```

---

# Running the application with the default settings.

In the terminal spin a PostgreSQL container with the below parameters that can be changed, but in this scenario, we will only pass the default values that are also found in the entry point of the application.

```
docker run -d \
--name tdd-postgres \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=postgres \
-e POSTGRES_DB=tdd_product_catalog \
-p 5432:5432 \
-v tdd-product-catalog-data:/var/lib/postgresql/data \
postgres:16
```

Simpler way is to use docker compose to spin up the container

```
docker compose up -d
```

After running the DB instance, we can run the application with the following command

```
java -jar target/tdd-product-catalog-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

---

# Run the application with custom parameters

We first start the database container, with the new custom parameters, we pass custom values using shell variable replacement, which take precedence over anything else.

```
POSTGRES_USER={any-desired-value} \
POSTGRES_PASSWORD={any-desired-value} \
POSTGRES_DB={any-desired-value} \
DB_PORT={any-desired-value} \
docker compose up -d
```

As discussed in section App.java our application entry point accepts command line arguments for configuring the database instance and hibernate.

```
java -jar target/tdd-product-catalog-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  --jdbc-url jdbc:postgresql://localhost:5432/{my_other_db} \
  --jdbc-user {any-desired-value} \
  --jdbc-password {any-desired-value} \
  --hibernate-ddl {update/create-drop/validate/none}
```

Be careful that the jdbc-url, user and password match with the PostgreSQL container that was spined at the step above, otherwise the application will not connect with the database.

---

## External links

[Github-Pages-Maven-Site-AST-Project](https://dariopipa.github.io/tdd-product-catalog/index.html)

[Coveralls](https://coveralls.io/github/dariopipa/tdd-product-catalog)

[SonarCloud Code Analysis](https://sonarcloud.io/project/overview?id=dariopipa_tdd-product-catalog)
