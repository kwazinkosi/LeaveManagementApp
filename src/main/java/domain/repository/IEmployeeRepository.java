package domain.repository;

import java.util.List;
import java.util.Optional;

import domain.model.Employee;

public interface IEmployeeRepository {

	Optional<Employee> findById(String employeeId);

	List<Employee> findAll();

	void save(Employee employee);

	boolean existsById(String employeeId);

}
