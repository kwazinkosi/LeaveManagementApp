package domain.validation;

import java.io.File;

import common.exception.ValidationException;

public class FileValidator implements IValidator<File> {
	@Override
	public void validate(File file) throws ValidationException {
		if (!file.exists()) {
			throw new ValidationException("File not found: " + file.getPath());
		}
		if (!file.canRead()) {
			throw new ValidationException("File not readable: " + file.getPath());
		}
		if (file.length() == 0) {
			throw new ValidationException("Empty file: " + file.getPath());
		}
	}
}