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

    // Constructor for new leave request
    public LeaveRequest(String employeeId, String leaveType, LocalDate startDate, LocalDate endDate) {
        this.empId = employeeId.toUpperCase();
//        capitalize first letter of leave type
        this.leaveTypeName = leaveType.substring(0, 1).toUpperCase() + leaveType.substring(1);
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = calculateLeaveDays(); // Calculate leave days
        this.status = LeaveStatus.PENDING;
        this.remarks = "None";
    }
    
    // Constructor for ex
    public LeaveRequest(int requestId, String empId, String leaveTypeName, LocalDate startDate, LocalDate endDate, float days, LeaveStatus status, String remarks) {
        
    	this.requestId = requestId;
        this.empId = empId;
        // capitalize first letter of leave type
        this.leaveTypeName = leaveTypeName.substring(0, 1).toUpperCase() + leaveTypeName.substring(1);
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
    
	public void printRequestDetails() {
		System.out.println("\n========================================\n"
				          + "|          Leave Request Details       |\n" 
						  + "|======================================|");
		
		System.out.println("|	 Leave Request ID: " + requestId + "\n"
				+ "|______________________________________|");
		System.out.println("|	 Employee ID: 	   " + empId +"\n"
				+ "|______________________________________|");
		System.out.println("|	 Leave Type:       " + leaveTypeName + "\n"
                + "|______________________________________|");
		System.out.println("|	 Start Date:       " + startDate + "\n"
                + "|______________________________________|");
		System.out.println("|	 End Date:         " + endDate + "\n"
                + "|______________________________________|");
		System.out.println("|	 Days: 	           " + days + "\n"
                + "|______________________________________|");
		System.out.println("|	 Status: 	   " + status + "\n"
                + "|______________________________________|");
		if (remarks.equalsIgnoreCase("None") && remarks != null) {
		    System.out.println("|     Remarks:        " + remarks+ " |");
		} else {
			System.out.println("| Remarks: " + remarks+ " |");
		}
		
		System.out.println("|======================================|\n");
	}
	
    private float calculateLeaveDays() {
        return startDate.datesUntil(endDate.plusDays(1))
                .count();
    }

	public String getLeaveTypeName() {
		return leaveTypeName;
	}

	public void setRemarks(String message) {
		this.remarks = message;
	}
}