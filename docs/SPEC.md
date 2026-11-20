# ShopMetrics — Project Specification

**Status:** Planned / in development

This document defines the intended behaviour of ShopMetrics. Rules marked **Proposed** are working assumptions to confirm before implementing the affected functionality; they are not verified facts about the store or its marketplaces.

## 1. Initial Situation (Ausgangslage)

The owner of a digital goods store can view individual orders but does not have a clear overview of the store's financial performance. Digital Core automates order processing, but the owner needs separate financial and order statistics.

The owner wants to identify profitable and loss-making products, compare suppliers of the same product and understand how refunds and other order problems affect the business.

ShopMetrics will start as a local Java application that analyses CSV exports and gradually develop into a backend service. It is also an independent learning project for practising Java, object-oriented programming, collections, exceptions, file handling, testing, SQL and HTTP APIs.

## 2. Project Objectives

- Calculate financial results for a selected period and identify profitable and loss-making products.
- Compare suppliers, including suppliers offering the same product.
- Show order status and refund statistics without treating unfinished orders as completed sales.
- Handle invalid CSV rows without losing valid input.
- Store and retrieve orders through Supabase and reuse the Java reporting logic.
- Provide reports through a Java REST API and compare financial performance across reporting periods.
- Develop and test the application independently.

## 3. Scope, Users and Milestones

The main user is the store owner, who imports data and reads reports. The student developer is responsible for implementation, testing and maintenance. Suppliers and marketplaces are represented in the input data; they do not directly interact with ShopMetrics.

| Milestone | Scope | Target |
| --- | --- | --- |
| M1 — Console Application | CSV reading, validation, status statistics, financial calculations, console reports, error file, fictional data generator and JUnit tests | December 2026 |
| M2 — Database Integration | Supabase storage, SQL table setup, loading and updating orders via REST, reports using stored orders | February 2027 |
| M3 — REST API | Java HTTP endpoints for financial reports and product statistics | April 2027 |
| M4 — Advanced Analytics | Comparison with the previous period, margin warnings and Docker | June 2027 |

The targets are estimates from the original concept. Each milestone must be usable and testable without requiring unfinished later features. M1 remains available without a network connection.

**Outside the scope:** order fulfilment, editing marketplace orders or prices, direct marketplace/supplier integrations, a graphical user interface, a public multi-user service, currency conversion, full accounting and tax reporting. Real store data must remain local within the current scope.

## 4. Functional Requirements

### M1 — Console Application

| ID | Requirement |
| --- | --- |
| FR-01 | Read a CSV file containing one order per data row. |
| FR-02 | Validate the header, required fields, dates, monetary amounts and supported order statuses. |
| FR-03 | Skip invalid data rows and record their line number and reason in `errors.txt`; continue processing valid rows. |
| FR-04 | Calculate order counts by status, sales count, refund count and refund rate for a selected date range. |
| FR-05 | Calculate revenue, supplier costs, marketplace fees, profit and profit margin for that date range according to Section 6. |
| FR-06 | Show order and financial statistics by product, supplier and product–supplier combination. |
| FR-07 | Show up to five products with financial results ranked by total profit, highest first. |
| FR-08 | Flag products with negative profit or a margin below a user-defined threshold. |
| FR-09 | Display a readable console report containing the reporting period, totals, status breakdown, grouped statistics, rankings, flagged products and skipped-row count. |
| FR-10 | Provide a `DataGenerator` that creates fictional CSV orders for testing and demonstration, including the supported statuses. |

### M2 — Database Integration

| ID | Requirement |
| --- | --- |
| FR-11 | Create an orders table in Supabase with appropriate data types and a unique order identifier. |
| FR-12 | Import validated fictional orders into Supabase through its Data API using Java HTTP Client and JSON. Re-imports must not create duplicate orders; newer snapshots may update existing orders as described in Section 7. |
| FR-13 | Retrieve all stored orders in a selected date range, including additional result pages when necessary, and generate the same statistics as M1 from those orders. |
| FR-14 | Detect failed cloud requests, report unsuccessful or partially successful imports and never present incomplete retrieval results as a complete report. |

### M3 — REST API

| ID | Requirement |
| --- | --- |
| FR-15 | Provide a Java REST API that returns a financial and order-status report for a selected period in JSON. |
| FR-16 | Provide a Java REST API endpoint that returns statistics for a selected product and period in JSON. |
| FR-17 | Validate request parameters and return understandable HTTP errors for invalid requests, unknown products and unavailable data sources. |

### M4 — Advanced Analytics

| ID | Requirement |
| --- | --- |
| FR-18 | Compare revenue, profit, margin and order statistics for a selected period and an equivalent previous period. |
| FR-19 | Show warnings for falling profit margins according to an agreed warning rule and clearly identify unavailable comparisons. |
| FR-20 | Provide a Docker setup and documented startup instructions for running the Java backend with its configuration supplied separately. |

## 5. Data Requirements

### 5.1 CSV input

Each row represents the current snapshot of one order.

| Column | Description | Example |
| --- | --- | --- |
| `order_id` | Unique order identifier, stored as text | `1001` |
| `date` | Date when the order was created | `2026-11-03` |
| `product` | Product name | `Steam Gift Card 20 EUR` |
| `supplier` | Supplier name | `SupplierA` |
| `sale_price` | Order's sale amount before any refund; not necessarily recognised revenue | `21.50` |
| `purchase_cost` | Supplier cost; for final orders, used as retained cost under the proposed financial rules | `17.80` |
| `marketplace_fee` | Marketplace fee; for final orders, used as retained fee under the proposed financial rules | `1.29` |
| `status` | Current order status | `DELIVERED` |

```csv
order_id,date,product,supplier,sale_price,purchase_cost,marketplace_fee,status
1001,2026-11-03,Steam Gift Card 20 EUR,SupplierA,21.50,17.80,1.29,DELIVERED
1002,2026-11-03,Xbox Game Pass 1M,SupplierB,9.90,7.40,0.59,REFUNDED
1003,2026-11-04,Steam Gift Card 20 EUR,SupplierA,21.50,17.80,1.29,IN_PROGRESS
1004,2026-11-04,Xbox Game Pass 1M,SupplierB,9.90,0.00,0.00,PROBLEM
```

**Proposed format:** UTF-8, comma-separated fields, the listed header, quoted fields when a value contains a comma, one physical line per order, dates in `YYYY-MM-DD` format, decimal points for money and one configured currency for each dataset. Examples use EUR. All fields are required and monetary inputs must be non-negative. Use zero only when the amount is known to be zero, not as a substitute for an unknown amount. The actual export format, including whether early orders have missing financial values, must be checked before M1. A missing or invalid header is a file-level error. Error line numbers start at 1 and include the header.

### 5.2 Order statuses

The following six internal statuses are the planned minimum. Their mapping to real marketplace statuses is an open question.

| Status | Meaning | Classification for reports |
| --- | --- | --- |
| `PENDING` | Waiting to be processed | Open |
| `IN_PROGRESS` | Being processed | Open |
| `DELIVERED` | Successfully delivered and treated as a completed sale | Final for this snapshot |
| `PROBLEM` | An issue has occurred and the outcome is unresolved | Open |
| `CANCELLED` | Cancelled without a completed sale | Final for this snapshot |
| `REFUNDED` | Customer received a full refund under the proposed rules | Final for this snapshot |

All six statuses are valid input and must be counted. Other values are invalid until their mapping is defined. ShopMetrics records the current status; it does not control order transitions or keep status history. A delivered order may later become refunded in a newer export. The real marketplace may distinguish delivery from confirmation; that distinction must be checked when mapping statuses.

### 5.3 Stored data (M2)

Start with a single `orders` table containing the same eight fields. Use a text primary key for `order_id`, a date type for `date`, text fields for names and status, and exact decimal (`numeric`) types for monetary values. Store one currency consistently in a database dataset. The table can be created through the Supabase dashboard or SQL editor. Separate product and supplier tables are not required for the initial version; names are used for grouping. More detailed database modelling may be added only if it becomes useful.

### 5.4 Outputs

- Console reports and `errors.txt` in M1.
- Equivalent reports from stored test orders in M2.
- JSON report responses in M3; period comparisons and warnings in M4.

## 6. Business Rules and Financial Calculations

### 6.1 Status and financial treatment

Only valid orders in the selected date range are considered. Orders are grouped by their recorded order date, not their delivery or refund date, because those dates are not in the input format.

**Proposed M1 accounting rule:** recognise financial results only for final orders. Open orders appear in operational statistics but are excluded from recognised revenue, costs and fees; their possible expenses and income are not included in final-order profit. Make the exclusion clear in reports. This is an order-cohort analysis, not a cash-flow or accounting report.

| Status | Recognised revenue | Recognised cost | Recognised fee | Counts as sale? |
| --- | --- | --- | --- | --- |
| `PENDING` | Excluded | Excluded | Excluded | No |
| `IN_PROGRESS` | Excluded | Excluded | Excluded | No |
| `PROBLEM` | Excluded | Excluded | Excluded | No |
| `DELIVERED` | Sale price | Purchase cost | Marketplace fee | Yes |
| `CANCELLED` | 0 | Retained purchase cost | Retained marketplace fee | No |
| `REFUNDED` | 0 | Retained purchase cost | Retained marketplace fee | No |

For cancelled and refunded orders, the proposed rule assumes no retained customer revenue and that the cost and fee columns represent amounts actually lost after any reimbursement. If the export contains original amounts instead, the rule must be changed or additional fields introduced. A cancelled order with no costs has zero profit; a refunded order with unrecovered costs produces a loss. Partial refunds and other unusual settlements are not defined yet. Do not use this proposed rule for real financial decisions until the actual export and settlement behaviour have been confirmed.

### 6.2 Metrics

| Metric | Calculation |
| --- | --- |
| Total orders | Count of all included valid orders |
| Status count | Count of included orders with the specified status |
| Sales count | Count of `DELIVERED` orders |
| Refund count | Count of `REFUNDED` orders |
| Refund rate (%) | Refund count / total orders × 100 |
| Revenue | Sum of recognised revenue |
| Purchase costs | Sum of recognised supplier costs |
| Marketplace fees | Sum of recognised marketplace fees |
| Profit | Revenue − purchase costs − marketplace fees |
| Profit margin (%) | Profit / revenue × 100 |

The proposed refund-rate denominator includes open orders; it is the share of all orders in the selected cohort currently refunded, not the share of completed sales later refunded. Reports must label it accordingly. A separate refund-rate definition or high-refund warning can be agreed later.

Use the same formulas for product, supplier and product–supplier statistics. Calculate percentages from group totals, not averages of individual percentages. Profit excludes taxes and expenses absent from the data.

### 6.3 Proposed reporting conventions

- Include both date boundaries; reject a start date after the end date.
- Use `BigDecimal` for monetary values and exact arithmetic. Round display values to two decimal places using `HALF_UP`; calculate percentages from unrounded totals with sufficient precision.
- Show `N/A` when a percentage denominator is zero. For groups with only open orders, show no recognised financial result rather than implying that their final profit is zero.
- Rank only products with at least one final order. Show up to five products, sorted by descending profit, breaking ties by product name.
- Flag negative profit even if the margin is undefined, and flag defined margins strictly below the selected threshold. Do not flag a group with no final results as loss-making.
- A report with no matching orders must clearly say so. An order group with only open orders still appears in status statistics.

## 7. Import and Storage Behaviour

**Proposed rules:**

- Within a CSV file, keep the first valid occurrence of each order ID and report later valid duplicates as errors. Reject invalid rows independently.
- Within Supabase, use the unique order ID to prevent duplicates. Re-importing the same valid order has no effect on counts; re-importing an updated order replaces its stored snapshot. This assumes that the owner imports the latest export and that order IDs refer to the same store. The source has no update timestamp, so older imports cannot be detected reliably. Warn the user about importing older snapshots. Do not automatically delete stored orders absent from a new CSV.
- Validate all rows locally before sending valid records to Supabase. An import may partially succeed if a cloud request fails; report the outcome accurately. Re-importing a complete current export should not inflate totals.
- Cloud retrieval must include every matching result, not merely the first API response page. If retrieval fails, display an error instead of reporting partial totals as complete.
- Use the same Java calculation logic for CSV and cloud data. Storing data in Supabase does not require rewriting the reporting rules in SQL.

## 8. Use Cases and API

### UC-01 — Analyse a CSV export (M1)

The owner supplies a CSV file, a date range and a margin threshold. The application validates rows, calculates status and financial statistics, and displays the report. The owner compares suppliers and identifies profitable or problematic products. If a file cannot be read, show a useful error and do not generate misleading results.

Example based on the four fictional CSV rows in Section 5, using the proposed rules:

```text
Report: 2026-11-01 to 2026-11-30
Orders: 4 | Sales: 1 | Refunds: 1 (25.00% of all orders)
Pending: 0 | In progress: 1 | Delivered: 1
Problem: 1 | Cancelled: 0 | Refunded: 1
Final orders: 2 | Open orders excluded from financial totals: 2
Revenue:         21.50 EUR
Purchase costs:  25.20 EUR
Fees:             1.88 EUR
Profit:          -5.58 EUR
Margin:         -25.95%
Skipped rows: 0
```

The complete report also contains grouped statistics, product rankings and margin flags.

### UC-02 — Review invalid input (M1)

The owner opens `errors.txt`, locates invalid rows, corrects the CSV and runs the report again. Example: `Line 7: Unknown status`.

### UC-03 — Import and analyse stored orders (M2)

The owner imports fictional orders into Supabase, then requests a report for a selected period from stored data. A later export updates orders whose status or financial details have changed. The owner checks that there are no duplicate orders. If the connection fails, the application explains the failure. Offline CSV analysis remains available.

### UC-04 — Retrieve reports through HTTP (M3)

The owner sends a request to the local Java backend and receives a JSON report. Proposed endpoints:

| Method and path | Purpose |
| --- | --- |
| `GET /reports?from=2026-11-01&to=2026-11-30&marginThreshold=10` | Period report, including grouped statistics and low-margin flags |
| `GET /products/stats?product=Steam%20Gift%20Card%2020%20EUR&from=2026-11-01&to=2026-11-30` | Statistics for one product |

The product name query parameter is proposed instead of the original `/products/{id}/stats` route because M2 does not require a separate product-ID table. The final route format and JSON field names can be agreed before M3. Dates are required in both examples; the report threshold must be provided for margin flags. Return HTTP 200 for valid reports (including empty results), 400 for missing/invalid parameters, 404 for a product that does not exist in the selected data source at all, and 503 if the required Supabase data source is unavailable. An existing product without orders in the selected period returns an empty result with 200. Do not expose credentials or internal error details in JSON responses.

### UC-05 — Compare periods (M4)

The owner compares the selected period with the immediately preceding period of the same number of calendar days (**proposed**). The application displays changes in revenue, profit, margin and order statistics. Changes in margin are measured in percentage points. Warn if the decrease exceeds an agreed threshold; do not generate a margin comparison or warning from undefined margins. Missing prior-period data must be labelled clearly.

## 9. Technical and Quality Requirements

- Use Java 21, Maven and JUnit 5. Use understandable classes and methods to separate CSV reading, validation, calculation, storage access and output where useful. No enterprise architecture is required.
- Use `BigDecimal` rather than `double` or `float` for money. Use exact decimal database types and preserve monetary precision when reading JSON.
- M1 must work offline. From M2, use hosted Supabase, SQL for table setup and the Supabase Data API via Java HTTP Client over HTTPS. No JDBC or locally hosted database is required. An internet connection is necessary only for cloud operations.
- Use a JSON library if useful. The specific JSON/CSV libraries and Java REST server framework remain open choices. A lightweight Java HTTP server or a suitable framework may be chosen before M3.
- The project is for one owner. For M2, a proposed simple access setup is a single Supabase Auth user, a publishable API key and owner-only database access using grants and Row Level Security (RLS). Disable public registration if it is not needed; do not grant anonymous order-table access. Keep credentials and user tokens outside source control and do not use a privileged secret key in the CLI. The exact sign-in method can be selected before M2.
- The M3 API is intended for local use only; bind to a local interface during normal use and publish Docker ports only to localhost in M4. Public hosting or remote access would require an explicit security decision and suitable authentication before exposure.
- Only fictional data may be uploaded to Supabase or committed to Git, including samples, logs and reports. Real exports and reports stay local. Do not include credentials in source code, logs, test data or container images.
- Show useful error messages for unreadable files and failed HTTP requests. An invalid data row must not crash the application. No fixed numerical performance target is specified.
- Test every agreed calculation and validation rule with small fictional datasets and known expected results. Add tests for cloud request handling, API behaviour and trends as those milestones are implemented.
- Write application code and tests manually. AI may explain concepts and review existing code; implement suggested changes independently.

Official documentation for M2: [Supabase Data API](https://supabase.com/docs/guides/api), [API keys](https://supabase.com/docs/guides/getting-started/api-keys), [Row Level Security](https://supabase.com/docs/guides/database/postgres/row-level-security). These describe the platform, not completed ShopMetrics functionality.

## 10. Acceptance Criteria

A milestone is complete when its requirements and acceptance criteria are met. Criteria relying on proposed rules apply once those rules have been confirmed. Automated tests must cover agreed calculation and validation rules, and integration checks must use fictional data.

| ID | Milestone | Expected result |
| --- | --- | --- |
| AC-01 | M1 | A valid CSV imports all six supported statuses; status counts sum to the total count. Unknown statuses, missing required fields, invalid dates and invalid monetary amounts in data rows are skipped and reported with correct reasons and line numbers. |
| AC-02 | M1 | The delivered example order produces profit 2.41 EUR and margin 11.21%. The refunded example produces a loss of 7.99 EUR under the proposed no-recovery inputs. |
| AC-03 | M1 | The four example rows produce the exact totals and status counts shown in UC-01. Open orders do not affect realised financial totals. A cancelled order with retained cost 2.00 and fee 0.50 produces a loss of 2.50; one with no costs produces zero profit. |
| AC-04 | M1 | A dataset with several products and suppliers produces correct totals for each product, supplier and product–supplier combination, including a group with only open orders. |
| AC-05 | M1 | With six eligible products the ranking shows the five highest profits; with fewer it shows all eligible products. Ties use name order and open-only products are excluded. |
| AC-06 | M1 | With a threshold of 10%, margins of 9%, 10% and 11% are classified correctly. Negative profit with zero revenue is flagged despite undefined margin; open-only groups are not flagged. |
| AC-07 | M1 | Inclusive date boundaries, out-of-period exclusion, duplicate handling, report counts and error line numbers are correct. Empty datasets, zero denominators, invalid date ranges, invalid headers and unreadable files are handled without misleading results. |
| AC-08 | M1 | The data generator creates valid fictional orders covering all six statuses. Agreed JUnit tests pass with `mvn test`, and offline use requires no Supabase connection. |
| AC-09 | M2 | A fictional dataset is stored and retrieved through Supabase; re-importing it creates no duplicates. Importing a newer snapshot with an updated status changes the corresponding report without increasing order count. |
| AC-10 | M2 | A report retrieved across multiple pages matches the CSV report for the same orders. Failed cloud reads or writes are reported; incomplete retrieval is not presented as a complete report, and any partial import is disclosed. |
| AC-11 | M2 | The configured owner can read and write test orders; unauthenticated or unauthorised requests cannot read or modify them. No real exports, credentials or privileged keys are stored in the repository or exposed in reports. |
| AC-12 | M3 | Valid requests to both endpoints return JSON with the expected statistics and HTTP 200, including empty periods. Invalid parameters return 400, unknown products return 404 and an unavailable required cloud source returns 503. |
| AC-13 | M3 | API values match the existing report calculations for the same dataset. The local API does not expose credentials or listen publicly by default. |
| AC-14 | M4 | Two known periods produce the expected comparison. A margin decrease above the agreed threshold triggers a warning; missing or undefined comparison data does not. |
| AC-15 | M4 | The backend builds and starts using the documented Docker setup, accepts external configuration, and returns an expected report from fictional data through a localhost-published port. |

## 11. Open Questions

| Question | Decide before |
| --- | --- |
| What statuses exist in the actual export? Does delivered mean financially completed, and can early orders have unknown supplier or fee amounts? | M1 validation |
| Are supplier costs and fees recovered on cancellation or refund? Does the export show retained or original amounts? Are there partial refunds or compensation? | M1 financial calculations; real-data use |
| Is the order-date cohort definition suitable, or are separate delivery and refund dates required? Is the refund rate based on all orders appropriate? | M1 report calculations |
| What currency and CSV format does the actual export use? Are the duplicate, rounding, threshold and zero-denominator conventions acceptable? | M1 validation and testing |
| How should the user enter reporting dates and thresholds? Where should `errors.txt` be saved and should it overwrite earlier errors? | M1 console interface |
| Is the latest-export assumption sound for updating stored orders? Are order IDs unique across all intended exports? | M2 integration |
| Which owner sign-in method, exact numeric column definitions and free plan limits are appropriate? | M2 integration |
| Which JSON library and Java REST server will be used? Are the proposed endpoint paths and JSON response fields appropriate? | M2/M3 as applicable |
| What margin decrease triggers a warning, and should high refund rates also trigger warnings? Is the preceding equal-length period suitable? | M4 analytics |

M1 can be implemented and tested on fictional data using confirmed rules while later technical choices remain open. The application must not present unverified financial assumptions as actual store accounting behaviour.
