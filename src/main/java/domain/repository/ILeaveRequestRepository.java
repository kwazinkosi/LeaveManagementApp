package domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import domain.model.LeaveRequest;
import domain.model.LeaveStatus;

public interface ILeaveRequestRepository {
    
	Optional<LeaveRequest> findById(int requestId);
    List<LeaveRequest> findByEmployeeId(String employeeId);
    List<LeaveRequest> findByEmployeeIdAndLeaveType(String employeeId, String leaveType);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    LeaveRequest save(LeaveRequest leaveRequest);
    void updateStatus(int requestId, LeaveStatus status, String remarks);
    List<LeaveRequest> findOverlappingRequests(String employeeId, LocalDate startDate, LocalDate endDate);
}