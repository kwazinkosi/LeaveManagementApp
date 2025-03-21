package domain.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Set;

import common.exception.ValidationException;

public class ConsoleInputValidator implements IValidator<String> {

	private static final Set<String> VALID_LEAVE_TYPES = Set.of("SICK", "CASUAL", "PAID");

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
			.withResolverStyle(ResolverStyle.SMART);

	public static LocalDate parseDate(String input) throws ValidationException {
		try {
			return LocalDate.parse(input, DATE_FORMATTER);
		} catch (DateTimeParseException e) {
			throw new ValidationException("Invalid date format. Use exact format: YYYY-MM-DD");
		}
	}

	public static void validateStartDate(LocalDate date) throws ValidationException {
		if (date.isBefore(LocalDate.now())) {
			throw new ValidationException("Start date cannot be in the past");
		}
	}

	public static void validateEndDate(LocalDate startDate, LocalDate endDate) throws ValidationException {
		if (endDate.isBefore(startDate)) {
			throw new ValidationException("End date must be after start date");
		}
		if (endDate.isBefore(LocalDate.now())) {
			throw new ValidationException("End date cannot be in the past");
		}
	}

	public static void validateEmployeeId(String employeeId) throws ValidationException {
		// Check if employee ID is in the format E001 (E followed by 3 to 6 digits)
		if (!employeeId.toUpperCase().matches("^E\\d{3,6}$")) {
			throw new ValidationException(
					"Invalid employee ID format. Should be E followed by at least 3 digits (e.g., E001)");
		}
	}

	public static void validateLeaveType(String leaveType) throws ValidationException {
		if (!VALID_LEAVE_TYPES.contains(leaveType.toUpperCase())) {
			throw new ValidationException("Invalid leave type. Valid types: SICK, CASUAL, PAID");
		}
	}

	@Override
	public void validate(String item) throws ValidationException {
		// TODO Auto-generated method stub

	}
}