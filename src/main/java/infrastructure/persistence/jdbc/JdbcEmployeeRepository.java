package infrastructure.persistence.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import common.exception.DataPersistenceException;
import domain.model.Employee;
import domain.repository.IEmployeeRepository;
import domain.repository.IRepository;
import infrastructure.persistence.DatabaseConnectionManager;

public class JdbcEmployeeRepository implements IEmployeeRepository, IRepository<Employee> {
    
	private static final Logger LOGGER = Logger.getLogger(JdbcEmployeeRepository.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public JdbcEmployeeRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Employee> findById(String employeeId) {
        
    	String sql = "SELECT emp_id, emp_name, department FROM employees WHERE emp_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Employee(
                        rs.getString("emp_id"),
                        rs.getString("emp_name"),
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
        String sql = "SELECT emp_id, emp_name, department FROM employees";
        try (Connection conn = connectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employees.add(new Employee(
                    rs.getString("emp_id"),
                    rs.getString("emp_name"),
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
        
    	String sql = "INSERT INTO employees (emp_id, emp_name, department) VALUES (?, ?, ?) ON CONFLICT (emp_id) DO UPDATE SET emp_name = ?, department = ?";   
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getEmpId());
            stmt.setString(2, employee.getEmpName());
            stmt.setString(3, employee.getDepartment());
            stmt.setString(4, employee.getEmpName());
            stmt.setString(5, employee.getDepartment());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error saving employee: " + e.getMessage());
        }
    }

    @Override
    public boolean existsById(String employeeId) {
        
    	String sql = "SELECT 1 FROM employees WHERE emp_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.severe("Error checking if employee exists: " + e.getMessage());
        }
        
        return false;
    }

	@Override
	public void saveAll(List<Employee> entities) throws DataPersistenceException  {
		
		try (Connection conn = connectionManager.getConnection()) {
			conn.setAutoCommit(false);
			for (Employee employee : entities) {
				save(employee);
			}
			conn.commit();
			conn.setAutoCommit(true); 
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to save leave types", e);
        }
	}
}
