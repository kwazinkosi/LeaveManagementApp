package common.exception;

public class DataParseException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public DataParseException(String message) {
		super(message);
	}

	public DataParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataParseException(Throwable cause) {
		super(cause);
	}

}

