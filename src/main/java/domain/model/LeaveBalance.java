package domain.model;

import java.time.LocalDateTime;

public class LeaveBalance {

	private int balanceId;
	private String empId;
	private String leaveType;
	private float balanceDays;
	private int year;
	private LocalDateTime lastUpdated;

	

	public LeaveBalance(String employeeId, String leaveType, float balanceDays) {
        this.empId = employeeId;
        this.leaveType = leaveType;
        this.balanceDays = balanceDays;
        this.lastUpdated = LocalDateTime.now();
    }
	public LeaveBalance(Integer balanceId, String employeeId, String leaveType, float balanceDays,
			LocalDateTime lastUpdated) {
		this.balanceId = balanceId;
		this.empId = employeeId;
		this.leaveType = leaveType;
		this.balanceDays = balanceDays;
		this.lastUpdated = lastUpdated;
	}

	// Getters and Setters
	public int getBalanceId() {
		return balanceId;
	}

	public String getEmpId() {
		return empId;
	}

	public String getLeaveType() {
		return leaveType; //leave type name
	}

	public float getBalanceDays() {
		return balanceDays;
	}

	public int getYear() {
		return year;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void deductDays(int days) {
		if (days > balanceDays) {
			throw new IllegalArgumentException("Cannot deduct more days than available balance.");
		}
		this.balanceDays -= days;
	}
}
