package infrastructure.persistence.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import domain.model.Employee;
import domain.repository.IEmployeeRepository;
import infrastructure.persistence.DatabaseConnectionManager;

public class JdbcEmployeeRepository implements IEmployeeRepository {
    
	private static final Logger LOGGER = Logger.getLogger(JdbcEmployeeRepository.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public JdbcEmployeeRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Employee> findById(String employeeId) {
        
    	String sql = "SELECT employee_id, employee_name, department FROM employees WHERE employee_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Employee(
                        rs.getString("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("department")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding employee by ID: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<Employee> findAll() {
        
    	List<Employee> employees = new ArrayList<>();
        String sql = "SELECT employee_id, employee_name, department FROM employees";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employees.add(new Employee(
                    rs.getString("employee_id"),
                    rs.getString("employee_name"),
                    rs.getString("department")
                ));
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding all employees: " + e.getMessage());
        }
        
        return employees;
    }

    @Override
    public void save(Employee employee) {
        
    	String sql = "INSERT INTO employees (employee_id, employee_name, department) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE employee_name = ?, department = ?";   
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getEmpId());
            stmt.setString(2, employee.getEmpName());
            stmt.setString(3, employee.getDepartment());
            stmt.setString(4, employee.getEmpName());
            stmt.setString(5, employee.getDepartment());
            
            stmt.executeUpdate();
            LOGGER.info("Employee saved: " + employee.getEmpId());
        } catch (SQLException e) {
            LOGGER.severe("Error saving employee: " + e.getMessage());
        }
    }

    @Override
    public boolean existsById(String employeeId) {
        
    	String sql = "SELECT 1 FROM employees WHERE employee_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking if employee exists: " + e.getMessage());
        }
        
        return false;
    }
}
