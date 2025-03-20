package infrastructure.persistence.jdbc;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import domain.model.LeaveRequest;
import domain.model.LeaveStatus;
import domain.repository.ILeaveRequestRepository;
import infrastructure.persistence.DatabaseConnectionManager;

public class JdbcLeaveRequestRepository implements ILeaveRequestRepository {
    
	private static final Logger LOGGER = Logger.getLogger(JdbcLeaveRequestRepository.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public JdbcLeaveRequestRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<LeaveRequest> findById(int requestId) {
    	
        String sql = "SELECT lr.request_id, lr.employee_id, lt.leave_type_name, " +
                     "lr.start_date, lr.end_date, lr.leave_days, lr.status, lr.remarks " +
                     "FROM leave_requests lr " +
                     "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
                     "WHERE lr.request_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
               
            	if (rs.next()) {
                    return Optional.of(new LeaveRequest(
                        rs.getInt("request_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getInt("leave_days"),
                        LeaveStatus.valueOf("status"),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave request by id: " + e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public List<LeaveRequest> findByEmployeeId(String employeeId) {
       
    	List<LeaveRequest> requests = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.employee_id, lt.leave_type_name, " +
                     "lr.start_date, lr.end_date, lr.leave_days, lr.status, lr.remarks " +
                     "FROM leave_requests lr " +
                     "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
                     "WHERE lr.employee_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new LeaveRequest(
                        rs.getInt("request_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getInt("leave_days"),
                        LeaveStatus.fromString(rs.getString("status")),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave requests by employee id: " + e.getMessage());
        }
        
        return requests;
    }

    @Override
    public List<LeaveRequest> findByEmployeeIdAndLeaveType(String employeeId, String leaveType) {
        
    	List<LeaveRequest> requests = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.employee_id, lt.leave_type_name, " +
                     "lr.start_date, lr.end_date, lr.leave_days, lr.status, lr.remarks " +
                     "FROM leave_requests lr " +
                     "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
                     "WHERE lr.employee_id = ? AND lt.leave_type_name = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            stmt.setString(2, leaveType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new LeaveRequest(
                        rs.getInt("request_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getInt("leave_days"),
                        LeaveStatus.fromString(rs.getString("status")),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave requests by employee id and leave type: " + e.getMessage());
        }
        
        return requests;
    }

    @Override
    public List<LeaveRequest> findByStatus(LeaveStatus status) {
    	
        List<LeaveRequest> requests = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.employee_id, lt.leave_type_name, " +
                     "lr.start_date, lr.end_date, lr.leave_days, lr.status, lr.remarks " +
                     "FROM leave_requests lr " +
                     "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
                     "WHERE lr.status = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.getStatus());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new LeaveRequest(
                        rs.getInt("request_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getFloat("leave_days"),
                        LeaveStatus.fromString(rs.getString("status")),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding leave requests by status: " + e.getMessage());
        }
        
        return requests;
    }

    @Override
    public LeaveRequest save(LeaveRequest leaveRequest) {
        
    	if (leaveRequest.getRequestId() == 0) {
    		
            return insert(leaveRequest);
        } else {
            return update(leaveRequest);
        }
    }
    
    private LeaveRequest insert(LeaveRequest leaveRequest) {
        String sql = "INSERT INTO leave_requests (employee_id, leave_type_id, start_date, end_date, " +
                     "leave_days, status, remarks) " +
                     "VALUES (?, (SELECT leave_type_id FROM leave_types WHERE leave_type_name = ?), " +
                     "?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, leaveRequest.getEmpId());
            stmt.setString(2, leaveRequest.getLeaveTypeId());
            stmt.setDate(3, Date.valueOf(leaveRequest.getStartDate()));
            stmt.setDate(4, Date.valueOf(leaveRequest.getEndDate()));
            stmt.setFloat(5, leaveRequest.getDays());
            stmt.setString(6, leaveRequest.getStatus().getStatus());
            stmt.setString(7, leaveRequest.getRemarks());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int requestId = generatedKeys.getInt(1);
                    LeaveRequest newRequest = new LeaveRequest(
                        requestId,
                        leaveRequest.getEmpId(),
                        leaveRequest.getLeaveTypeId(),
                        leaveRequest.getStartDate(),
                        leaveRequest.getEndDate(),
                        leaveRequest.getDays(),
                        leaveRequest.getStatus(),
                        leaveRequest.getRemarks()
                    );
                    return newRequest;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error inserting leave request: " + e.getMessage());
        }
        
        return leaveRequest;
    }
    
    private LeaveRequest update(LeaveRequest leaveRequest) {
        String sql = "UPDATE leave_requests SET leave_type_id = (SELECT leave_type_id FROM leave_types WHERE leave_type_name = ?), " +
                     "start_date = ?, end_date = ?, leave_days = ?, status = ?, remarks = ?, " +
                     "updated_at = CURRENT_TIMESTAMP " +
                     "WHERE request_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, leaveRequest.getLeaveTypeId());
            stmt.setDate(2, Date.valueOf(leaveRequest.getStartDate()));
            stmt.setDate(3, Date.valueOf(leaveRequest.getEndDate()));
            stmt.setFloat(4, leaveRequest.getDays());
            stmt.setString(5, leaveRequest.getStatus().getStatus());
            stmt.setString(6, leaveRequest.getRemarks());
            stmt.setInt(7, leaveRequest.getRequestId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error updating leave request: " + e.getMessage());
        }
        
        return leaveRequest;
    }

    @Override
    public void updateStatus(int requestId, LeaveStatus status, String remarks) {
        String sql = "UPDATE leave_requests SET status = ?, remarks = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE request_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.getStatus());
            stmt.setString(2, remarks);
            stmt.setInt(3, requestId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.severe("Error updating leave request status: " + e.getMessage());
        }
    }

    @Override
    public List<LeaveRequest> findOverlappingRequests(String employeeId, LocalDate startDate, LocalDate endDate) {
       
    	List<LeaveRequest> requests = new ArrayList<>();
        String sql = "SELECT lr.request_id, lr.employee_id, lt.leave_type_name, " +
                     "lr.start_date, lr.end_date, lr.leave_days, lr.status, lr.remarks " +
                     "FROM leave_requests lr " +
                     "JOIN leave_types lt ON lr.leave_type_id = lt.leave_type_id " +
                     "WHERE lr.employee_id = ? AND lr.status != 'Rejected' " +
                     "AND (lr.start_date <= ? AND lr.end_date >= ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employeeId);
            stmt.setDate(2, Date.valueOf(endDate));
            stmt.setDate(3, Date.valueOf(startDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new LeaveRequest(
                        rs.getInt("request_id"),
                        rs.getString("employee_id"),
                        rs.getString("leave_type_name"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getFloat("leave_days"),
                        LeaveStatus.fromString(rs.getString("status")),
                        rs.getString("remarks")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error finding overlapping leave requests: " + e.getMessage());
        }
        
        return requests;
    }
}