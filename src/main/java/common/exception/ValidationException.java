package common.exception;

public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String message, Throwable cause, ErrorType errorType) {
		super(message, cause);
	}

	public enum ErrorType {
		FILE_NOT_FOUND, FILE_NOT_READABLE, EMPTY_FILE
	}
}	
