package domain.repository;

import java.util.List;
import java.util.Optional;

import domain.model.LeaveType;

public interface ILeaveTypeRepository {
    
	Optional<LeaveType> findById(int id); 
    Optional<LeaveType> findByName(String name);
    List<LeaveType> findAll();
    LeaveType save(LeaveType leaveType);
}