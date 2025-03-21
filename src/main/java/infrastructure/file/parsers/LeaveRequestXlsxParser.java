package infrastructure.file.parsers;

import common.exception.DataParseException;
import domain.model.LeaveRequest;
import domain.model.LeaveStatus;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestXlsxParser extends AbstractDataParser<LeaveRequest> {

	private static final String[] EXPECTED_HEADERS = { "EMP_ID", "EMP_NAME", "DEPARTMENT", "LEAVE_TYPE",
			"LEAVE_START_DATE", "LEAVE_END_DATE", "LEAVE_DAYS", "LEAVE_STATUS", "REMARKS", "BALANCE_LEAVE" };

	@Override
	public List<LeaveRequest> parse(File file) throws DataParseException {
		List<LeaveRequest> leaveRequests = new ArrayList<>(); // to avoid duplicate rows across multiple files (if any), use a Set instead
		

		try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			validateHeaders(getHeaderRow(sheet, EXPECTED_HEADERS.length), EXPECTED_HEADERS);

			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null)
					continue; // Skip empty rows

				try {
					leaveRequests.add(parseRow(row));
				} catch (DataParseException e) {
					throw new DataParseException(String.format("Error in row %d: %s", rowIndex + 1, e.getMessage()),
							e.getCause());
				}
			}

		} catch (IOException e) {
			throw new DataParseException("Failed to read XLSX file: " + file.getName(), e);
		}

		return leaveRequests;
	}

	@Override
	protected LeaveRequest parseRow(Row row) throws DataParseException {
		try {
			return new LeaveRequest(0, getStringValue(row, 0), // EMP_ID
					getStringValue(row, 3), // LEAVE_TYPE
					getDateValue(row, 4), // LEAVE_START_DATE
					getDateValue(row, 5), // LEAVE_END_DATE
					getFloatValue(row, 6), // LEAVE_DAYS
					LeaveStatus.fromString(getStringValue(row, 7)), // LEAVE_STATUS
					getStringValue(row, 8) // REMARKS
			);
		} catch (IndexOutOfBoundsException e) {
			throw new DataParseException("Missing required fields", e);
		}
	}

}