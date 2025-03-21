package application.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import common.exception.InvalidLeaveRequestException;
import domain.model.LeaveBalance;
import domain.model.LeaveRequest;
import domain.model.LeaveType;
import domain.model.LeaveStatus;
import domain.repository.IEmployeeRepository;
import domain.repository.ILeaveBalanceRepository;
import domain.repository.ILeaveRequestRepository;
import domain.repository.ILeaveTypeRepository;

public class LeaveService {

	private final ILeaveRequestRepository leaveRequestRepository;
	private final ILeaveBalanceRepository leaveBalanceRepository;
	private final IEmployeeRepository employeeRepository;
	private final ILeaveTypeRepository leaveTypeRepository;

	public LeaveService(ILeaveRequestRepository leaveRequestRepository, ILeaveBalanceRepository leaveBalanceRepository,
			IEmployeeRepository employeeRepository, ILeaveTypeRepository leaveTypeRepository) {
		
		this.leaveRequestRepository = leaveRequestRepository;
		this.leaveBalanceRepository = leaveBalanceRepository;
		this.employeeRepository = employeeRepository;
		this.leaveTypeRepository = leaveTypeRepository;
	}

	public LeaveRequest requestLeave(String employeeId, String leaveType, LocalDate startDate, LocalDate endDate) throws InvalidLeaveRequestException{
		
		validateEmployee(employeeId);
		validateLeaveType(leaveType);
		validateLeaveDates(startDate, endDate);
		validateNoOverlappingRequests(employeeId, startDate, endDate);

		LeaveRequest leaveRequest = new LeaveRequest(employeeId, leaveType, startDate, endDate);
		try {
			validateLeaveBalance(employeeId, leaveType, leaveRequest.getDays());
		}
		catch (InvalidLeaveRequestException e) {
			leaveRequest.setStatus(LeaveStatus.REJECTED);
			leaveRequest.setRemarks(e.getMessage());
		}
		return leaveRequestRepository.save(leaveRequest);
	}

	public void approveLeave(int requestId) throws InvalidLeaveRequestException {

		LeaveRequest request = getPendingLeaveRequest(requestId);
		LeaveBalance balance = getLeaveBalance(request.getEmpId(), request.getLeaveTypeId());

		balance.deductLeave(request.getDays());
		leaveBalanceRepository.save(balance);

		request.approve();
		leaveRequestRepository.save(request);
		
	}

	public void rejectLeave(int requestId, String reason) throws InvalidLeaveRequestException {

		LeaveRequest request = getPendingLeaveRequest(requestId);
		request.reject(reason);
		leaveRequestRepository.save(request);
	}

	public List<LeaveBalance> getLeaveBalances(String employeeId) throws InvalidLeaveRequestException {

		validateEmployee(employeeId);
		return leaveBalanceRepository.findByEmployeeId(employeeId);
	}

	public List<LeaveRequest> getLeaveHistory(String employeeId) throws InvalidLeaveRequestException {
		validateEmployee(employeeId);
		return leaveRequestRepository.findByEmployeeId(employeeId);
	}

	public List<LeaveType> getAllLeaveTypes() {
		return leaveTypeRepository.findAll();
	}

	// Helper methods for validation and business logic

	private void validateEmployee(String employeeId) throws InvalidLeaveRequestException {
		
		if (!employeeRepository.existsById(employeeId)) {
			throw new InvalidLeaveRequestException("Employee does not exist: " + employeeId);
		}
	}

	private void validateLeaveType(String leaveType) throws InvalidLeaveRequestException {

		if (!leaveTypeRepository.findByName(leaveType).isPresent()) {
			throw new InvalidLeaveRequestException("Invalid leave type: " + leaveType);
		}
	}

	private void validateLeaveDates(LocalDate startDate, LocalDate endDate) throws InvalidLeaveRequestException {
		
		if (startDate.isAfter(endDate)) {
			throw new InvalidLeaveRequestException("Start date must be before or equal to end date");
		}

		if (startDate.isBefore(LocalDate.now())) {
			throw new InvalidLeaveRequestException("Leave cannot be requested for past dates");
		}
		
		if (startDate.equals(endDate)) {
			throw new InvalidLeaveRequestException("Start and end dates cannot be the same");
		}
		
		if (startDate.getDayOfWeek().getValue() > 5 || endDate.getDayOfWeek().getValue() > 5) {
			throw new InvalidLeaveRequestException("Leave cannot be requested on weekends");
		}
		
		if (startDate.isAfter(LocalDate.now().plusMonths(1))) {
			throw new InvalidLeaveRequestException("Leave cannot be requested more than a month in advance");
		}
		
		if (startDate.isAfter(LocalDate.now().plusWeeks(2))) {
			throw new InvalidLeaveRequestException("Leave cannot be requested more than two weeks in advance");
		}
		
		if (endDate.isAfter(startDate.plusDays(15))) {
			throw new InvalidLeaveRequestException("Leave cannot be requested for more than 14 days");
		}
		
	}

	private void validateNoOverlappingRequests(String employeeId, LocalDate startDate, LocalDate endDate)
			throws InvalidLeaveRequestException {
		
		List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingRequests(employeeId, startDate,
				endDate);
		if (!overlappingRequests.isEmpty()) {
			throw new InvalidLeaveRequestException("Employee already has leave requests for the selected dates");
		}
	}

	private void validateLeaveBalance(String employeeId, String leaveType, float requestedDays)
			throws InvalidLeaveRequestException {
		
		Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, leaveType);
		if (balanceOpt.isEmpty()) {
			throw new InvalidLeaveRequestException(
					"No leave balance found for employee: " + employeeId + " and leave type: " + leaveType);
		}

		LeaveBalance balance = balanceOpt.get();
		if (!balance.hasEnoughBalance(requestedDays)) {
			throw new InvalidLeaveRequestException("Insufficient leave balance. Available: " + balance.getBalanceDays()
					+ ", Requested: " + requestedDays);
		}
	}

	private LeaveRequest getPendingLeaveRequest(int requestId) throws InvalidLeaveRequestException {
		
		Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
		if (requestOpt.isEmpty()) {
			throw new InvalidLeaveRequestException("Leave request not found: " + requestId);
		}

		LeaveRequest request = requestOpt.get();
		if (request.getStatus() != LeaveStatus.PENDING) {
			throw new InvalidLeaveRequestException("Only pending requests can be modified");
		}

		return request;
	}

	private LeaveBalance getLeaveBalance(String employeeId, String leaveType) throws InvalidLeaveRequestException {
		
		Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveType(employeeId, leaveType);
		if (balanceOpt.isEmpty()) {
			throw new InvalidLeaveRequestException(
					"No leave balance found for employee: " + employeeId + " and leave type: " + leaveType);
		}
		return balanceOpt.get();
	}
}