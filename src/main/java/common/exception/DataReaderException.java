package common.exception;
public class DataReaderException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum ErrorType { FILE_NOT_FOUND, PARSE_ERROR, VALIDATION_ERROR }
    
    private final ErrorType errorType;
    
    public DataReaderException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }

	public DataReaderException(String string, Exception e) {

		super(string, e);
		this.errorType = ErrorType.PARSE_ERROR;
		
	}
    
    
}