/*
  # Stress-Based Payroll System Database Schema

  ## Overview
  This migration creates the complete database schema for a stress-based employee payroll system
  that tracks overtime, calculates stress levels, and manages payroll processing.

  ## New Tables

  ### 1. `users` - Authentication and role management
    - `id` (uuid, primary key) - Unique identifier
    - `email` (text, unique, not null) - User email for login
    - `password` (text, not null) - Hashed password
    - `role` (text, not null) - Either 'EMPLOYEE' or 'HR'
    - `created_at` (timestamptz) - Account creation timestamp
    - `updated_at` (timestamptz) - Last update timestamp

  ### 2. `employees` - Employee profile and payroll information
    - `id` (uuid, primary key) - Unique identifier
    - `user_id` (uuid, foreign key) - Links to users table
    - `full_name` (text, not null) - Employee full name
    - `employee_code` (text, unique, not null) - Unique employee identifier
    - `department` (text, not null) - Department name
    - `designation` (text, not null) - Job title
    - `base_salary` (decimal, not null) - Monthly base salary
    - `paid_leaves` (integer, default 24) - Annual paid leave quota
    - `join_date` (date, not null) - Date of joining
    - `is_active` (boolean, default true) - Employment status
    - `created_at` (timestamptz) - Record creation timestamp
    - `updated_at` (timestamptz) - Last update timestamp

  ### 3. `stress_entries` - Monthly stress and overtime tracking
    - `id` (uuid, primary key) - Unique identifier
    - `employee_id` (uuid, foreign key) - Links to employees table
    - `month` (integer, not null) - Month (1-12)
    - `year` (integer, not null) - Year
    - `overtime_hours` (decimal, not null) - Hours worked overtime
    - `working_days` (integer, not null) - Total working days in month
    - `days_present` (integer, not null) - Days employee was present
    - `paid_leaves_taken` (integer, default 0) - Paid leaves used
    - `unpaid_leaves_taken` (integer, default 0) - Unpaid leaves taken
    - `stress_level` (decimal, not null) - Calculated stress score (0-100)
    - `stress_category` (text, not null) - LOW, MODERATE, HIGH, CRITICAL
    - `stress_factors` (text) - Analysis of stress contributors
    - `recommendations` (text) - Suggestions for stress management
    - `requires_counseling` (boolean, default false) - Flag for mandatory counseling
    - `created_at` (timestamptz) - Record creation timestamp
    - Unique constraint on (employee_id, month, year)

  ### 4. `payslips` - Generated payslips with environmental impact
    - `id` (uuid, primary key) - Unique identifier
    - `employee_id` (uuid, foreign key) - Links to employees table
    - `stress_entry_id` (uuid, foreign key) - Links to stress_entries table
    - `month` (integer, not null) - Month (1-12)
    - `year` (integer, not null) - Year
    - `base_salary` (decimal, not null) - Base monthly salary
    - `overtime_pay` (decimal, default 0) - Additional overtime compensation
    - `deductions` (decimal, default 0) - Leave and other deductions
    - `net_salary` (decimal, not null) - Final take-home salary
    - `paper_saved_grams` (decimal, not null) - Environmental impact metric
    - `trees_saved` (decimal, not null) - Environmental impact metric
    - `generated_at` (timestamptz, default now()) - Payslip generation time
    - `downloaded_at` (timestamptz) - When employee downloaded payslip

  ## Security
  - Row Level Security (RLS) enabled on all tables
  - Employees can only view/edit their own data
  - HR users can view all employee data
  - Authentication required for all operations

  ## Indexes
  - Created on foreign keys for performance
  - Created on email for fast login lookups
  - Created on employee_code for quick employee searches
  - Created on (employee_id, month, year) for payroll queries
*/

-- Create users table
CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email text UNIQUE NOT NULL,
  password text NOT NULL,
  role text NOT NULL CHECK (role IN ('EMPLOYEE', 'HR')),
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create employees table
CREATE TABLE IF NOT EXISTS employees (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  full_name text NOT NULL,
  employee_code text UNIQUE NOT NULL,
  department text NOT NULL,
  designation text NOT NULL,
  base_salary decimal(10, 2) NOT NULL CHECK (base_salary > 0),
  paid_leaves integer DEFAULT 24 CHECK (paid_leaves >= 0),
  join_date date NOT NULL,
  is_active boolean DEFAULT true,
  created_at timestamptz DEFAULT now(),
  updated_at timestamptz DEFAULT now()
);

-- Create stress_entries table
CREATE TABLE IF NOT EXISTS stress_entries (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_id uuid NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  month integer NOT NULL CHECK (month >= 1 AND month <= 12),
  year integer NOT NULL CHECK (year >= 2020 AND year <= 2100),
  overtime_hours decimal(5, 2) NOT NULL CHECK (overtime_hours >= 0),
  working_days integer NOT NULL CHECK (working_days > 0),
  days_present integer NOT NULL CHECK (days_present >= 0),
  paid_leaves_taken integer DEFAULT 0 CHECK (paid_leaves_taken >= 0),
  unpaid_leaves_taken integer DEFAULT 0 CHECK (unpaid_leaves_taken >= 0),
  stress_level decimal(5, 2) NOT NULL CHECK (stress_level >= 0 AND stress_level <= 100),
  stress_category text NOT NULL CHECK (stress_category IN ('LOW', 'MODERATE', 'HIGH', 'CRITICAL')),
  stress_factors text,
  recommendations text,
  requires_counseling boolean DEFAULT false,
  created_at timestamptz DEFAULT now(),
  UNIQUE(employee_id, month, year)
);

-- Create payslips table
CREATE TABLE IF NOT EXISTS payslips (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  employee_id uuid NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
  stress_entry_id uuid NOT NULL REFERENCES stress_entries(id) ON DELETE CASCADE,
  month integer NOT NULL CHECK (month >= 1 AND month <= 12),
  year integer NOT NULL CHECK (year >= 2020 AND year <= 2100),
  base_salary decimal(10, 2) NOT NULL,
  overtime_pay decimal(10, 2) DEFAULT 0,
  deductions decimal(10, 2) DEFAULT 0,
  net_salary decimal(10, 2) NOT NULL,
  paper_saved_grams decimal(8, 2) NOT NULL,
  trees_saved decimal(10, 6) NOT NULL,
  generated_at timestamptz DEFAULT now(),
  downloaded_at timestamptz
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_employees_user_id ON employees(user_id);
CREATE INDEX IF NOT EXISTS idx_employees_code ON employees(employee_code);
CREATE INDEX IF NOT EXISTS idx_stress_entries_employee ON stress_entries(employee_id);
CREATE INDEX IF NOT EXISTS idx_stress_entries_month_year ON stress_entries(employee_id, month, year);
CREATE INDEX IF NOT EXISTS idx_payslips_employee ON payslips(employee_id);
CREATE INDEX IF NOT EXISTS idx_payslips_month_year ON payslips(employee_id, month, year);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE stress_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE payslips ENABLE ROW LEVEL SECURITY;

-- RLS Policies for users table
CREATE POLICY "Users can view own profile"
  ON users FOR SELECT
  TO authenticated
  USING (auth.uid() = id);

CREATE POLICY "Users can update own profile"
  ON users FOR UPDATE
  TO authenticated
  USING (auth.uid() = id)
  WITH CHECK (auth.uid() = id);

-- RLS Policies for employees table
CREATE POLICY "Employees can view own data"
  ON employees FOR SELECT
  TO authenticated
  USING (
    user_id = auth.uid() OR
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );

CREATE POLICY "Employees can update own data"
  ON employees FOR UPDATE
  TO authenticated
  USING (user_id = auth.uid())
  WITH CHECK (user_id = auth.uid());

CREATE POLICY "HR can view all employees"
  ON employees FOR SELECT
  TO authenticated
  USING (
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );

-- RLS Policies for stress_entries table
CREATE POLICY "Employees can view own stress entries"
  ON stress_entries FOR SELECT
  TO authenticated
  USING (
    employee_id IN (SELECT id FROM employees WHERE user_id = auth.uid()) OR
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );

CREATE POLICY "Employees can create own stress entries"
  ON stress_entries FOR INSERT
  TO authenticated
  WITH CHECK (
    employee_id IN (SELECT id FROM employees WHERE user_id = auth.uid())
  );

CREATE POLICY "HR can view all stress entries"
  ON stress_entries FOR SELECT
  TO authenticated
  USING (
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );

-- RLS Policies for payslips table
CREATE POLICY "Employees can view own payslips"
  ON payslips FOR SELECT
  TO authenticated
  USING (
    employee_id IN (SELECT id FROM employees WHERE user_id = auth.uid()) OR
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );

CREATE POLICY "Employees can update own payslip download time"
  ON payslips FOR UPDATE
  TO authenticated
  USING (employee_id IN (SELECT id FROM employees WHERE user_id = auth.uid()))
  WITH CHECK (employee_id IN (SELECT id FROM employees WHERE user_id = auth.uid()));

CREATE POLICY "HR can view all payslips"
  ON payslips FOR SELECT
  TO authenticated
  USING (
    EXISTS (SELECT 1 FROM users WHERE users.id = auth.uid() AND users.role = 'HR')
  );