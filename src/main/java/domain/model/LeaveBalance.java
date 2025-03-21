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
		this.year = lastUpdated.getYear();
	}

	// Constructor for balance retrieved from database
	public LeaveBalance(Integer balanceId, String employeeId, String leaveType, float balanceDays,
			int year, LocalDateTime lastUpdated) {
		this.balanceId = balanceId;
		this.empId = employeeId;
		this.leaveType = leaveType;
		this.balanceDays = balanceDays;
		this.lastUpdated = lastUpdated;
		this.year = year;
	}

	// Methods
	public boolean hasEnoughBalance(float requestedDays) {
		return balanceDays >= requestedDays;
	}

	public void deductLeave(float days) {
		if (!hasEnoughBalance(days)) {
			throw new IllegalStateException("Not enough leave balance");
		}
		balanceDays -= days;
		lastUpdated = LocalDateTime.now();
	}

	// Getters and Setters
	public int getBalanceId() {
		return balanceId;
	}

	public String getEmpId() {
		return empId;
	}

	public String getLeaveType() {
		return leaveType; // leave type name
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
	
	public void addDays(int days) {
		this.balanceDays += days;
	}
	
	public void printBalanceDetails() {
		
		System.out.println("|	 "+ leaveType + " Leave: " + balanceDays + " days\n"
				+ "|_________________________________________|");
	}
}
