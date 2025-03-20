package infrastructure.persistence.jdbc;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import domain.model.LeaveBalance;
import domain.repository.ILeaveBalanceRepository;
import infrastructure.persistence.DatabaseConnectionManager;

public class JdbcLeaveBalanceRepository implements ILeaveBalanceRepository {
    
	private static final Logger LOGGER = Logger.getLogger(JdbcLeaveBalanceRepository.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public JdbcLeaveBalanceRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<LeaveBalance> findByEmployeeIdAndLeaveType(String employeeId, String leaveType) {
        
    	String sql = "SELECT lb.balance_id, lb.employee_id, lt.leave_type_name, " +
                     "lb.balance_days, lb.last_updated " +
                     "FROM leave_balances lb " +
                     "JOIN leave_types lt ON lb.leave_type_id = lt.leave_type_id " +
                     "WHERE lb.employee_id = ? AND lt.leave_type_name = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            stmt.setString(2, leaveType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LeaveBalance(
                        rs.getInt("balance_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getFloat("balance_days"),
                        rs.getTimestamp("last_updated").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave balance: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<LeaveBalance> findByEmployeeId(String employeeId) {
        List<LeaveBalance> balances = new ArrayList<>();
        String sql = "SELECT lb.balance_id, lb.employee_id, lt.leave_type_name, " +
                     "lb.balance_days, lb.last_updated " +
                     "FROM leave_balances lb " +
                     "JOIN leave_types lt ON lb.leave_type_id = lt.leave_type_id " +
                     "WHERE lb.employee_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    balances.add(new LeaveBalance(
                        rs.getInt("balance_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getFloat("balance_days"),
                        rs.getTimestamp("last_updated").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave balances by employee id: " + e.getMessage());
        }
        
        return balances;
    }

    @Override
    public void save(LeaveBalance leaveBalance) {
    	
        String sql = "INSERT INTO leave_balances (employee_id, leave_type_id, balance_days) " +
                     "VALUES (?, (SELECT leave_type_id FROM leave_types WHERE leave_type_name = ?), ?) " +
                     "ON DUPLICATE KEY UPDATE balance_days = ?, last_updated = CURRENT_TIMESTAMP";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, leaveBalance.getEmpId());
            stmt.setString(2, leaveBalance.getLeaveType());
            stmt.setFloat(3, leaveBalance.getBalanceDays());
            stmt.setFloat(4, leaveBalance.getBalanceDays());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error saving leave balance: " + e.getMessage());
        }
    }

    @Override
    public void updateBalance(String employeeId, String leaveType, float newBalance) {
        
    	String sql = "UPDATE leave_balances SET balance_days = ?, last_updated = CURRENT_TIMESTAMP " +
                     "WHERE employee_id = ? AND leave_type_id = (SELECT leave_type_id FROM leave_types WHERE leave_type_name = ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setFloat(1, newBalance);
            stmt.setString(2, employeeId);
            stmt.setString(3, leaveType);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error updating leave balance: " + e.getMessage());
        }
    }
}