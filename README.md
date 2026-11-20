# ShopMetrics

ShopMetrics is a personal Java project for analysing sales in a digital goods store. It imports order data from CSV files and helps the store owner understand which products and suppliers are profitable, how often orders run into problems and how refunds affect profit.

**Project status:** Planned / in development. The features and milestones below describe intended functionality, not verified implementation.

## Motivation

Digital Core automates order processing, but the store owner needs a better overview of financial performance. ShopMetrics is intended to answer questions such as:

- Which products generate the most profit after supplier costs and marketplace fees?
- Which products have low or negative margins?
- Which supplier is more profitable for the same product?
- How many orders are pending, in progress, completed, problematic, cancelled or refunded?
- How does profitability change over time?

The project also provides practical experience with Java, object-oriented programming, testing, SQL, HTTP and backend development.

## Planned features

- Import and validate CSV files; report invalid rows without stopping the entire import.
- Recognise six order statuses and show order counts by status.
- Calculate revenue, supplier costs, marketplace fees, profit, margin and refund rate.
- Compare products, suppliers and suppliers of the same product.
- Show the five most profitable products and flag products with low margins or losses.
- Store and retrieve orders in Supabase, without a local database server or JDBC.
- Provide reports through a Java REST API.
- Compare reporting periods and warn about falling margins.

## Intended user

ShopMetrics is intended for the owner of a digital goods store. It is a personal analysis tool, not a public commercial service or an order automation system.

## Example input

```csv
order_id,date,product,supplier,sale_price,purchase_cost,marketplace_fee,status
1001,2026-11-03,Steam Gift Card 20 EUR,SupplierA,21.50,17.80,1.29,DELIVERED
1002,2026-11-03,Xbox Game Pass 1M,SupplierB,9.90,7.40,0.59,REFUNDED
1003,2026-11-04,Steam Gift Card 20 EUR,SupplierA,21.50,17.80,1.29,IN_PROGRESS
1004,2026-11-04,Xbox Game Pass 1M,SupplierB,9.90,0.00,0.00,PROBLEM
```

All example data is fictional. Open orders are included in status statistics but are not treated as completed sales. See [Project Specification](docs/SPEC.md) for the input format, calculation rules and assumptions about cancelled and refunded orders.

## Technologies

| Technology | Purpose |
| --- | --- |
| Java 21 | Application logic, collections, file handling and calculations |
| Maven | Building the project and managing dependencies |
| JUnit 5 | Automated tests |
| CSV | Order input and offline analysis |
| Supabase (hosted PostgreSQL) | Cloud storage for test orders from M2 |
| SQL | Creating tables and working with stored data |
| Java HTTP Client and JSON | Accessing the Supabase Data API from M2 |
| REST and JSON | Providing a ShopMetrics API from M3 |
| Docker | Running the application in a container in M4 |

Supabase manages the database server and provides a REST API. Java remains responsible for business logic and report calculations. No JDBC or local Oracle/PostgreSQL installation is required. The Java REST server framework and CSV/JSON libraries are not fixed yet.

## Roadmap

| Milestone | Planned result | Target |
| --- | --- | --- |
| M1 — Console Application | CSV input, validation, status and financial reports, JUnit tests | December 2026 |
| M2 — Database Integration | Store and retrieve orders using Supabase, SQL, HTTP and JSON | February 2027 |
| M3 — REST API | Own Java API for retrieving reports and product statistics | April 2027 |
| M4 — Advanced Analytics | Period comparison, margin warnings and Docker | June 2027 |

These dates are planning estimates based on the original concept, not fixed deadlines. Each milestone builds on the previous one. The console application remains usable offline even after cloud storage is added.

## Getting started

M1 requires JDK 21 and Maven. After the Maven project and tests have been implemented, the standard test command will be:

```bash
mvn test
```

Application startup and CSV usage instructions will be added when the console application is implemented. M2 will also require a Supabase project and an internet connection for cloud operations. M3 and M4 setup instructions will be added at those stages.

## Development approach

This is an independent learning project. Application code and tests are written manually. AI may explain concepts and review existing code, but must not generate the implementation. Suggested code changes are implemented manually.

Only fictional test data may be committed or uploaded to Supabase under the current project scope. Real store exports and their reports remain local. Credentials must not be committed to Git.
