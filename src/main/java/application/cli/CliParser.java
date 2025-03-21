package application.cli;

import application.services.LeaveService;
import common.exception.InvalidLeaveRequestException;
import domain.model.LeaveBalance;
import domain.model.LeaveRequest;
import presentation.UserInterface;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CliParser implements UserInterface {

    private final LeaveService leaveService;

    public CliParser(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Override
    public void launch(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }

        try {
            CliCommand command = parseCommand(args[0]);
            switch (command) {
                case REQUEST_LEAVE:
                    handleLeaveRequest(args);
                    break;
                case APPROVE_LEAVE:
                    handleApproveRequest(args);
                    break;
                case VIEW_BALANCE:
                    handleViewBalance(args);
                    break;
                case HELP:
                default:
                    showHelp();
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            showHelp();
        } catch (InvalidLeaveRequestException e) {
            System.err.println("Failed to process request: " + e.getMessage());
        }
    }

    private CliCommand parseCommand(String command) {
        try {
            return CliCommand.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid command: " + command);
        }
    }

    private void handleLeaveRequest(String[] args) throws InvalidLeaveRequestException {
        
    	if (args.length < 5) {
            throw new IllegalArgumentException("Missing arguments for leave request.");
        }

        String employeeId = args[1];
        String leaveType = args[2];
        LocalDate startDate = parseDate(args[3]);
        LocalDate endDate = parseDate(args[4]);

        LeaveRequest request = leaveService.requestLeave(employeeId, leaveType, startDate, endDate);
        System.out.println("Leave request created: " + request);
    }

    private void handleApproveRequest(String[] args) throws InvalidLeaveRequestException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing leave request ID.");
        }

        int requestId = Integer.parseInt(args[1]);
        leaveService.approveLeave(requestId);
        System.out.println("Leave request approved successfully.");
    }

    private void handleViewBalance(String[] args) throws InvalidLeaveRequestException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Missing employee ID.");
        }

        String employeeId = args[1];
        List<LeaveBalance> balances = leaveService.getLeaveBalances(employeeId);
        System.out.println("Leave Balances for Employee " + employeeId + ":");
        balances.forEach(System.out::println);
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private void showHelp() {
        System.out.println("Usage: LeaveManagementSystem <command> [arguments]");
        System.out.println("Commands:");
        System.out.println("  REQUEST_LEAVE <employeeId> <leaveType> <startDate> <endDate>");
        System.out.println("  APPROVE_LEAVE <requestId>");
        System.out.println("  VIEW_BALANCE <employeeId>");
        System.out.println("  HELP");
    }

    @Override
    public void shutdown() {
        System.out.println("Shutting down CLI...");
    }
}