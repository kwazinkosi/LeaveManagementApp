package infrastructure.file;

import java.nio.file.Path;
import java.util.List;

import common.exception.DataReaderException;

public interface IDataReader<T> {

	List<T> readData(List<Path> filePaths) throws DataReaderException;
    
}