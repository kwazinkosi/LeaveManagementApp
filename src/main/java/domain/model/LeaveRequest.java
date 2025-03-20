package domain.model;

import java.time.LocalDate;
import common.exception.InvalidLeaveRequestException;

public class LeaveRequest {
    private int requestId;
    private String empId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private float days;
    private LeaveStatus status;
    private String remarks;

    public LeaveRequest(int requestId, String empId, String leaveTypeName, LocalDate startDate, LocalDate endDate, float days, LeaveStatus status, String remarks) {
        
    	this.requestId = requestId;
        this.empId = empId;
        this.leaveTypeName = leaveTypeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.status = status;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getRequestId() { return requestId; }
    public String getEmpId() { return empId; }
    public String getLeaveTypeId() { return leaveTypeName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public float getDays() { return days; }
    public LeaveStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }

    public void setStatus(LeaveStatus status) { this.status = status; }
    
    // Methods to change status
    public void approve() throws InvalidLeaveRequestException {
        
    	if (status == LeaveStatus.PENDING) {
            status = LeaveStatus.APPROVED;
        } else {
            throw new InvalidLeaveRequestException("Can only approve PENDING leave requests");
        }
    }

    public void reject(String reason) throws InvalidLeaveRequestException {
       
    	if (status == LeaveStatus.PENDING) {
            
    		status = LeaveStatus.REJECTED;
            this.remarks = reason;
        } else {
            throw new InvalidLeaveRequestException("Can only reject PENDING leave requests");
        }
    }
}