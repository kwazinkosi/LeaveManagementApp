package application.console;

import application.services.LeaveService;
import common.exception.InvalidLeaveRequestException;
import common.exception.ValidationException;
import domain.model.LeaveBalance;
import domain.model.LeaveRequest;
import domain.validation.ConsoleInputValidator;
import presentation.UserInterface;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI implements UserInterface {

	private final LeaveService leaveService;
	private final Scanner scanner = new Scanner(System.in);
	private static final int OPTION_REQUEST_LEAVE = 1;
	private static final int OPTION_APPROVE_LEAVE = 2;
	private static final int OPTION_VIEW_BALANCES = 3;
	private static final int OPTION_EXIT = 4;

	public ConsoleUI(LeaveService leaveService) {
		this.leaveService = leaveService;
	}

	@Override
	public void launch(String[] args) {
		showMainMenu();
	}

	private void showMainMenu() {
		boolean running = true;
		while (running) {
			printMenuOptions();

			try {
				String input = scanner.nextLine().trim();
				int choice;

				try {
					choice = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					System.err.println("Please enter a valid number.");
					continue;
				}

				running = handleMenuChoice(choice);
			} catch (InvalidLeaveRequestException e) {
				System.err.println("Error: " + e.getMessage());
			} catch (DateTimeParseException e) {
				System.err.println("Invalid date format. Use YYYY-MM-DD.");
			} catch (Exception e) {
				System.err.println("An unexpected error occurred: " + e.getMessage());
				e.printStackTrace();
			}
		}

		shutdown();
	}

	private boolean handleMenuChoice(int choice) throws InvalidLeaveRequestException {
		switch (choice) {
		case OPTION_REQUEST_LEAVE:
			handleLeaveRequest();
			return true;
		case OPTION_APPROVE_LEAVE:
			handleApproveLeave();
			return true;
		case OPTION_VIEW_BALANCES:
			handleViewBalances();
			return true;
		case OPTION_EXIT:
			return false;
		default:
			System.out.println("Invalid choice. Please try again.");
			return true;
		}
	}

	private void printMenuOptions() {
		System.out.println("\nWelcome to the Employee Leave Management System!");
		System.out.println("1. Request Leave");
		System.out.println("2. Approve Leave");
		System.out.println("3. View Leave Balances");
		System.out.println("4. Exit");
		System.out.print("Enter your choice: ");
	}

	private void handleLeaveRequest() {
		String employeeId = "";
		String leaveType = "";

		// Get and validate Start Date
		LocalDate startDate = getValidDate("Enter Start Date (YYYY-MM-DD): ", true, // isStartDate = true
				null);

		// Get and validate End Date
		LocalDate endDate = getValidDate("Enter End Date (YYYY-MM-DD): ", false, // isStartDate = false
				startDate);

		// Employee ID Validation Loop
		while (employeeId.isEmpty()) {
			try {
				System.out.print("\nEnter Employee ID: ");
				employeeId = scanner.nextLine().trim();
				ConsoleInputValidator.validateEmployeeId(employeeId);

			} catch (ValidationException e) {
				System.err.println("Error: " + e.getMessage() + ". Please try again");
				employeeId = "";
			}
		}

		// Leave Type Validation Loop
		while (leaveType.isEmpty()) {
			try {
				System.out.print("Enter Leave Type (Sick/Casual/Paid): ");
				leaveType = scanner.nextLine().trim().toUpperCase();

				ConsoleInputValidator.validateLeaveType(leaveType);
			} catch (ValidationException e) {
				System.err.println("Error: " + e.getMessage());
				leaveType = "";
			}
		}

		try {
			LeaveRequest request = leaveService.requestLeave(employeeId, leaveType, startDate, endDate);
			System.out.println("Leave request created: ");
			request.printRequestDetails();
			viewBalancesAfterRequest(request.getEmpId());
		} catch (InvalidLeaveRequestException e) {
			System.err.println("Error creating request: " + e.getMessage());
		}
	}

	private LocalDate getValidDate(String prompt, boolean isStartDate, LocalDate referenceDate) {
		while (true) {
			try {
				System.out.print(prompt);
				String input = scanner.nextLine().trim();
				LocalDate date = ConsoleInputValidator.parseDate(input);

				if (isStartDate) {
					ConsoleInputValidator.validateStartDate(date);
				} else {
					ConsoleInputValidator.validateEndDate(referenceDate, date);
				}

				return date;
			} catch (ValidationException | DateTimeParseException e) {
	            // Print error with proper newlines
	            System.err.printf("%n%s%n", e.getMessage()+". Please try again");  // Newline before and after
	            
	            System.out.flush();  // Ensure output is written
	        }
		}
	}

	private void handleApproveLeave() {
		int requestId = -1;

		while (requestId == -1) {
			try {
				System.out.print("\nEnter Leave Request ID: ");
				if (scanner.hasNextInt()) {
					requestId = scanner.nextInt();
					scanner.nextLine(); // Consume newline
					leaveService.approveLeave(requestId);
					System.out.println("Leave request approved successfully.");
				} else {
					System.err.println("Invalid input. Please enter a numeric ID.");
					scanner.next(); // Clear invalid input
				}
			} catch (InvalidLeaveRequestException e) {
				System.err.println("Error: " + e.getMessage()+"\n");
				
				requestId = -1; // Reset to force retry
			}
		}
	}

	private void handleViewBalances() {
		String employeeId = "";

		while (employeeId.isEmpty()) {
			try {
				System.out.print("\nEnter Employee ID: ");
				employeeId = scanner.nextLine().trim();
				ConsoleInputValidator.validateEmployeeId(employeeId);

				List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
				if (balances.isEmpty()) {
					System.out.println("No leave balances found for this employee.");
					return;
				}

				System.out.println("\n===========================================");
				System.out.println("|     Leave Type Balance Details          |");
				System.out.println("|=========================================|");
				balances.forEach(LeaveBalance::printBalanceDetails);
				
			} catch (ValidationException e) {
				System.err.println("Error: " + e.getMessage());
				employeeId = "";
			} catch (InvalidLeaveRequestException e) {
				System.err.println("Error: " + e.getMessage());
				employeeId = "";
			}
		}
	}

	private void viewBalancesAfterRequest(String employeeId) throws InvalidLeaveRequestException {

		List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
		System.out.println("Leave Balances for Employee " + employeeId + ":");
		System.out.println("\n===========================================\n"
				+ "|       Leave Type Balance Details   	  |\n" 
				+ "|=========================================|");
		balances.stream().forEach(LeaveBalance::printBalanceDetails);

	}

	@Override
	public void shutdown() {
		System.out.println("Shutting down console UI...");
		scanner.close();
	}
}