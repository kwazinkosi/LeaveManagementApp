package infrastructure.file.parsers;

import common.exception.DataParseException;
import domain.model.LeaveType;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaveTypeXlsxParser extends AbstractDataParser<LeaveType> {

	private enum LEAVETYPE {
		SICK(10), CASUAL(12), PAID(15);

		private final int value;

		LEAVETYPE(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	};

	private static final String[] EXPECTED_HEADERS = { "EMP_ID", "EMP_NAME", "DEPARTMENT", "LEAVE_TYPE",
			"LEAVE_START_DATE", "LEAVE_END_DATE", "LEAVE_DAYS", "LEAVE_STATUS", "REMARKS", "BALANCE_LEAVE" };

	@Override
	public List<LeaveType> parse(File file) throws DataParseException {
		List<LeaveType> leaveTypes = new ArrayList<>();

		try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheetAt(0);
			validateHeaders(getHeaderRow(sheet, EXPECTED_HEADERS.length), EXPECTED_HEADERS);

			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row == null)
					continue; // Skip empty rows

				try {
					leaveTypes.add(parseRow(row));
				} catch (DataParseException e) {
					throw new DataParseException(String.format("Error in row %d: %s", rowIndex + 1, e.getMessage()),
							e.getCause());
				}
			}

		} catch (IOException e) {
			throw new DataParseException("Failed to read XLSX file: " + file.getName(), e);
		}

		return leaveTypes;
	}

	@Override
	protected LeaveType parseRow(Row row) throws DataParseException {

		try {
			int leaveTypeId = 1;
			
			int leaveTypeValue = LEAVETYPE.valueOf(getStringValue(row, 3).toUpperCase()).getValue();
			if (leaveTypeValue == 12) {
				leaveTypeId =2;
			}else if (leaveTypeValue == 15) {
				leaveTypeId = 3;
			}
			return new LeaveType(

					leaveTypeId, // LEAVE_TYPE ID
					getStringValue(row, 3), // LEAVE_TYPE NAME
					leaveTypeValue // DEFAULT BALANCE
			);
		} catch (IndexOutOfBoundsException e) {
			throw new DataParseException("Missing required fields", e);
		}
	}

}