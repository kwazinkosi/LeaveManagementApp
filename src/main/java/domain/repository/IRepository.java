package domain.repository;

import java.util.List;

import common.exception.DataPersistenceException;

public interface IRepository<T> {
    void saveAll(List<T> entities) throws DataPersistenceException;
}
