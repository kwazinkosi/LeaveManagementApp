package domain.model;

public enum LeaveStatus {
    
	PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");

    private final String status;

    LeaveStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public static LeaveStatus fromString(String text) {
        for (LeaveStatus status : LeaveStatus.values()) {
            if (status.status.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}