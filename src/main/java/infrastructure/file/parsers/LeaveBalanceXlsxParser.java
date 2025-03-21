package infrastructure.file.parsers;

import common.exception.DataParseException;
import domain.model.LeaveBalance;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaveBalanceXlsxParser extends AbstractDataParser<LeaveBalance> {

    private static final String[] EXPECTED_HEADERS = {
        "EMP_ID", "EMP_NAME", "DEPARTMENT", "LEAVE_TYPE",
        "LEAVE_START_DATE", "LEAVE_END_DATE", "LEAVE_DAYS",
        "LEAVE_STATUS", "REMARKS", "BALANCE_LEAVE"
    };

    @Override
    public List<LeaveBalance> parse(File file) throws DataParseException {
        List<LeaveBalance> balances = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(getHeaderRow(sheet, EXPECTED_HEADERS.length), EXPECTED_HEADERS);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue; // Skip empty rows

                try {
                    balances.add(parseRow(row));
                } catch (DataParseException e) {
                    throw new DataParseException(
                        String.format("Error in row %d: %s", rowIndex + 1, e.getMessage()),
                        e.getCause()
                    );
                }
            }

        } catch (IOException e) {
            throw new DataParseException("Failed to read XLSX file: " + file.getName(), e);
        }

        return balances;
    }

    @Override
    protected LeaveBalance parseRow(Row row) throws DataParseException {
        
    	try {
            return new LeaveBalance(
                getStringValue(row, 0), // EMP_ID
                getStringValue(row, 3), // LEAVE_TYPE
                getFloatValue(row, 9)  // BALANCE_LEAVE
            );
        } catch (IndexOutOfBoundsException e) {
            throw new DataParseException("Missing required fields", e);
        }
    }


}