-- Database schema for Leave Management System

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS leave_balances;
DROP TABLE IF EXISTS leave_types;
DROP TABLE IF EXISTS employees;

-- Create employees table
CREATE TABLE employees (
    emp_id VARCHAR(10) PRIMARY KEY,
    emp_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL
);

-- Create leave_types table
CREATE TABLE leave_types (
    leave_type_id SERIAL PRIMARY KEY,
    leave_type_name VARCHAR(50) UNIQUE NOT NULL,
    default_balance INT NOT NULL
);

-- Create leave_balances table
CREATE TABLE leave_balances (
    balance_id SERIAL PRIMARY KEY,
    emp_id VARCHAR(10) NOT NULL,
    leave_type_id INT NOT NULL,
    balance_days INT NOT NULL,
    year INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id) ON DELETE CASCADE,
    UNIQUE (emp_id, leave_type_id, year)
);

-- Create leave_requests table
CREATE TABLE leave_requests (
    request_id SERIAL PRIMARY KEY,
    emp_id VARCHAR(10) NOT NULL,
    leave_type_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_days INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (emp_id) REFERENCES employees(emp_id) ON DELETE CASCADE,
    FOREIGN KEY (leave_type_id) REFERENCES leave_types(leave_type_id) ON DELETE CASCADE
);

-- Insert default leave types
INSERT INTO leave_types (leave_type_name, default_balance) VALUES
    ('Sick', 10),
    ('Casual', 12),
    ('Paid', 15);

-- Create indexes for better performance
CREATE INDEX idx_leave_requests_emp_id ON leave_requests(emp_id);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_balances_emp_id ON leave_balances(emp_id);
CREATE INDEX idx_leave_balances_leave_type_id ON leave_balances(leave_type_id);