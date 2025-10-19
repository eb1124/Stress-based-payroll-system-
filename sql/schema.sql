-- schema.sql
CREATE DATABASE IF NOT EXISTS stress_payroll;
USE stress_payroll;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  employee_id BIGINT
);

CREATE TABLE employees (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(200) NOT NULL,
  email VARCHAR(150) UNIQUE NOT NULL,
  department VARCHAR(100),
  base_salary DECIMAL(10,2) NOT NULL,
  paid_leaves INT DEFAULT 12,
  total_prints_saved INT DEFAULT 0
);

CREATE TABLE payslip (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  month_year VARCHAR(7) NOT NULL,
  attendance_percent DECIMAL(5,2) NOT NULL,
  overtime_hours INT DEFAULT 0,
  leaves_taken INT DEFAULT 0,
  gross_salary DECIMAL(10,2),
  deductions DECIMAL(10,2),
  net_salary DECIMAL(10,2),
  downloaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stress_entry (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  month_year VARCHAR(7) NOT NULL,
  stress_score DECIMAL(5,2) NOT NULL,
  reason VARCHAR(500),
  suggestion VARCHAR(500)
);
