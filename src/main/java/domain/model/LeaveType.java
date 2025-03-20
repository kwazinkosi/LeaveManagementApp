package domain.model;

public class LeaveType {
   
	private int leaveTypeId;
    private String leaveTypeName;
    private int defaultBalance;
    
	
    public LeaveType(int leaveTypeId, String leaveTypeName, int defaultBalance) {
        
    	this.leaveTypeId = leaveTypeId;
        this.leaveTypeName = leaveTypeName;
        this.defaultBalance = defaultBalance;
    }

    // Getters and Setters
    public int getLeaveTypeId() { return leaveTypeId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public int getDefaultBalance() { return defaultBalance; }
}