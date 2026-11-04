CREATE TABLE students (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE invoices (
                          id BIGSERIAL PRIMARY KEY,
                          student_id BIGINT NOT NULL REFERENCES students(id),
                          total_amount NUMERIC(14,2) NOT NULL CHECK (total_amount >= 0),
                          status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          invoice_id BIGINT NOT NULL REFERENCES invoices(id),
                          amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
                          reference VARCHAR(100) NOT NULL UNIQUE,
                          status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE idempotency_keys (
                                  idempotency_key VARCHAR(100) PRIMARY KEY,
                                  request_hash VARCHAR(64) NOT NULL,
                                  response_body TEXT,
                                  response_status INTEGER,
                                  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_invoices_student_id ON invoices(student_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);