package domain.repository;

import java.util.List;
import java.util.Optional;

import domain.model.LeaveBalance;

public interface ILeaveBalanceRepository {

	Optional<LeaveBalance> findByEmployeeIdAndLeaveType(String employeeId, String leaveType);

	List<LeaveBalance> findByEmployeeId(String employeeId);

	void save(LeaveBalance leaveBalance);

	void updateBalance(String employeeId, String leaveType, float newBalance);

}
