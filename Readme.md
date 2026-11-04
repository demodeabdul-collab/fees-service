Fees & Receipts Service

A backend API for a university bursary or finance system. It issues invoices, records student payments safely, computes balances, generates PDF receipts, produces reconciliation reports, accepts signed payment webhooks, and flags suspicious records with plain-English explanations.

The focus of this project is money correctness: retries must not double-charge, concurrent payments must not overpay, and arithmetic must be exact.

Tech stack
Layer	Tool
Framework	Spring Boot 4.1.1, Spring Web, Spring Data JPA
Language	Java (compiled for 21, developed on JDK 25)
Database	PostgreSQL 17
Migrations	Flyway
PDF receipts	Thymeleaf template + flying-saucer
API docs	springdoc-openapi (Swagger UI)
Tests	JUnit 5, Mockito
Build	Maven
Key features
Idempotent payments. Each payment carries an idempotency key. Reusing a key is rejected with 409 Conflict, so a network retry cannot double-charge.
Overpayment guard with row locking. The invoice row is locked (PESSIMISTIC_WRITE) inside one transaction, so two simultaneous payments cannot both pass the balance check.
Duplicate reference rejection. Enforced by both application logic and a database UNIQUE constraint.
Exact money handling. BigDecimal in Java and NUMERIC(14,2) in PostgreSQL. Balances are derived from payments, never stored, so they cannot drift.
Invoice status tracking. UNPAID, PARTIALLY_PAID, PAID, updated inside the payment transaction.
PDF receipts. Rendered from an HTML template.
Reconciliation report. Confirmed payments in a date range, with a total.
HMAC-signed webhooks. The raw request body is verified with HMAC-SHA256 using a constant-time comparison.
Anomaly detection. Flags status mismatches, negative balances, and suspected duplicate payments.
Explanations. Rule-based plain-English text for every anomaly, plus an optional AI-written explanation that falls back to the rule-based text if the AI service fails.
Consistent errors. One JSON error shape, with no stack traces exposed.
Project structure
src/main/java/com/intermidateproject/fees/fees_service/
anomaly/       Anomaly model and AnomalyType enum
controller/    REST controllers
dto/           Request and response objects
entity/        JPA entities (Student, Invoice, Payment, IdempotencyKey)
exception/     Custom exceptions and GlobalExceptionHandler
repository/    Spring Data JPA repositories
service/       Business logic
util/          HmacUtil
src/main/resources/
db/migration/  Flyway migrations (V1__init_schema.sql)
templates/     receipt.html
Database schema

Four tables, created by Flyway: students, invoices, payments, idempotency_keys. Hibernate runs with ddl-auto=validate, so it checks the entities against the schema and never changes it.

Getting started
Prerequisites
JDK 21 or newer
Maven
PostgreSQL
1. Create the database

Connect as the postgres superuser and run:

sql
CREATE DATABASE fees_service;
CREATE USER fees_app WITH ENCRYPTED PASSWORD 'choose_a_password';
GRANT ALL PRIVILEGES ON DATABASE fees_service TO fees_app;

Then connect to the new database (psql -U postgres -d fees_service) and grant schema rights. PostgreSQL 15 and later require this for Flyway to create tables:

sql
GRANT ALL ON SCHEMA public TO fees_app;

This must be run as postgres, not as fees_app.

2. Configure the application

Edit src/main/resources/application.properties:

properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fees_service
spring.datasource.username=fees_app
spring.datasource.password=your_password_here
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

spring.profiles.active=dev,local
webhook.secret=change-me-in-production
gemini.api-key=${GEMINI_API_KEY:}

Secrets should not be committed. Put local secrets in src/main/resources/application-local.properties (loaded by the local profile) and add that file to .gitignore:

properties
gemini.api-key=your-key-here

Optional property: gemini.model selects the AI model.

3. Run
   bash
   mvn spring-boot:run

Flyway applies the migration on first start. Swagger UI is then available at:

http://localhost:8080/swagger-ui/index.html
API overview
Method	Path	Description
POST	/api/students	Create a student
GET	/api/students, /api/students/{id}	List or fetch students
POST	/api/invoices	Create an invoice for a student
GET	/api/invoices, /api/invoices/{id}	List or fetch invoices
GET	/api/invoices/{id}/balance	Remaining balance
POST	/api/payments	Record a payment (idempotent)
GET	/api/payments/{id}/receipt	Download a PDF receipt
GET	/api/payments/reconciliation?start=...&end=...	Reconciliation report for a date range
POST	/api/webhooks/payment	Signed payment webhook (X-Signature header)
POST	/api/simulator/sign	Dev only: signs a payload, simulating a gateway
GET	/api/anomalies	Detected anomalies
GET	/api/anomalies/explained	Anomalies with rule-based explanations
GET	/api/anomalies/explained-ai	Anomalies with AI explanations, falling back to rule-based text

Date parameters use ISO format, for example 2026-01-01T00:00:00.

Error responses

All errors return { "status", "message", "timestamp" }.

Status	When
400	Webhook payload is malformed or incomplete
401	Webhook signature is missing or invalid
404	Resource not found
409	Idempotency key already used, or duplicate payment reference
422	Payment exceeds the remaining balance
Example: record a payment
json
POST /api/payments
{
"invoiceId": 1,
"amount": 200.00,
"reference": "REF001",
"idempotencyKey": "key-abc-123",
"requestHash": "hash1"
}

Sending the same request again returns 409 Conflict.

Example: try the webhook
Call POST /api/simulator/sign with a compact one-line JSON body and copy the returned signature:
json
{"invoiceId":1,"amount":100.00,"reference":"WH-001","idempotencyKey":"wh-key-001"}
Call POST /api/webhooks/payment with the identical body and the signature in the X-Signature header. The body must match character for character, because the signature covers the exact text.
A tampered body or a missing header returns 401.

The simulator exists only under the dev profile. It must never run in production, because it would let anyone forge valid signatures.

Tests
bash
mvn test
PaymentServiceTest: overpayment guard, idempotency conflict, duplicate reference (repositories and InvoiceService mocked).
HmacUtilTest: a published HMAC-SHA256 test vector, determinism, output format, valid signature, tampered payload, wrong secret, null and garbage signatures.
Design notes
Why compareTo for BigDecimal: equals compares scale as well as value, so 0.0 and 0.00 differ. Balances come from NUMERIC(14,2), so equals against BigDecimal.ZERO would never match.
Why the lock comes first in createPayment: the lock makes the balance read trustworthy for the rest of the transaction. It also means a second request with the same idempotency key waits, then finds the key already stored.
Why raw bodies for webhooks: re-serializing parsed JSON can change field order or whitespace and break valid signatures, so verification happens on the raw text before parsing.
Why a fallback for AI explanations: a finance tool should not lose its explanations when a third-party service is slow or unavailable.
Known limitations
No authentication or authorization.
requestHash is supplied by the client on POST /api/payments. The webhook path computes it from the signature instead.
A reused idempotency key returns 409 rather than replaying the original response.
Entities are returned directly instead of response DTOs.
Request validation (@Valid) and pagination are not implemented.
getBalance loads every payment and the anomaly checks use nested loops, which is acceptable at small scale but would be replaced with grouped SQL queries.
AnomalyDetectionService has no unit tests yet.
AI explanations depend on the external service's availability and make one call per anomaly.
License

Add a license of your choice.