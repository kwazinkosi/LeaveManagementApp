package infrastructure.file.parsers;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import common.exception.DataParseException;


public abstract class AbstractDataParser<T> {
    
	public abstract List<T> parse(File file) throws DataParseException ;
	
	protected abstract T parseRow(Row row) throws DataParseException;
    
	protected String getStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        return cell.toString().trim();
    }

    protected LocalDate getDateValue(Row row, int cellIndex) throws DataParseException {
        Cell cell = row.getCell(cellIndex);
        try {
            if (cell == null) {
                throw new DataParseException("Missing date value");
            }
            
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            return LocalDate.parse(cell.toString().trim());
        } catch (Exception e) {
            throw new DataParseException(
                String.format("Invalid date format in cell %d: %s", 
                    cellIndex + 1, cell.toString()),
                e
            );
        }
    }

    protected int getNumericValue(Row row, int cellIndex) throws DataParseException {
    	Cell cell = row.getCell(cellIndex);
        try {
            if (cell == null) {
                throw new DataParseException("Missing numeric value");
            }
            
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            return Integer.parseInt(cell.toString().trim());
        } catch (Exception e) {
            throw new DataParseException(
                String.format("Invalid number format in cell %d: %s", cellIndex + 1, cell.toString()), e
                    );
        }
    }
    
    protected float getFloatValue(Row row, int cellIndex) throws DataParseException {
        
    	Cell cell = row.getCell(cellIndex);
        try {
            if (cell == null) {
                throw new DataParseException("Missing float value");
            }

            if (cell.getCellType() == CellType.NUMERIC) {
                return (float) cell.getNumericCellValue();
            }
            return Float.parseFloat(cell.toString().trim());
        } catch (Exception e) {
            throw new DataParseException(
                String.format("Invalid float format in cell %d: %s", cellIndex + 1, cell.toString()), e
            );
        }
    }
    
    protected String[] getHeaderRow(Sheet sheet, int headerLength) throws DataParseException {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new DataParseException("Missing header row");
        }

        String[] headers = new String[headerLength];
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            headers[i] = cell.toString().trim();
        }
        return headers;
    }
    
    protected void validateHeaders(String[] headers, String[] expectedHeaders) throws DataParseException {
		
    	if (!Arrays.equals(expectedHeaders, headers)) {
			throw new DataParseException(String.format("Invalid headers. Expected %s, got %s",
					Arrays.toString(expectedHeaders), Arrays.toString(headers)));
		}
	}
}