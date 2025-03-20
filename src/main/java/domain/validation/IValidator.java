package domain.validation;

import common.exception.ValidationException;

public interface IValidator<T> {
    void validate(T item) throws ValidationException;
}