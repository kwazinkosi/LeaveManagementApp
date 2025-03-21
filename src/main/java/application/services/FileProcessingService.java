package application.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import common.exception.DataPersistenceException;
import common.exception.DataReaderException;
import common.exception.ValidationException;
import domain.repository.IRepository;
import domain.validation.IValidator;
import infrastructure.file.FileDataReader;
import infrastructure.file.parsers.AbstractDataParser;
import infrastructure.monitor.FileMonitor;

public class FileProcessingService<T> {

	private final FileDataReader<T> fileDataReader;
	private final FileMonitor fileMonitor;
	private final IRepository<T> repository;
	public FileProcessingService(AbstractDataParser<T> parser, IValidator<File> fileValidator, Path directory, IRepository<T> repository) throws IOException {

		
        this.repository = repository;
		this.fileDataReader = new FileDataReader<>(parser, fileValidator);
		this.fileMonitor = new FileMonitor(directory, t -> {
			try {
				processFiles(t);
			} catch (DataPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void startMonitoring() {
		fileMonitor.start();
	}

	public void stopMonitoring() {
		fileMonitor.stop();
	}

	public void processFiles(List<Path> filePaths) throws DataPersistenceException, ValidationException {
		try {
           
            List<T> results = fileDataReader.readData(filePaths);
            
            // Save to database
            repository.saveAll(results);
            
            System.out.println("Successfully processed " + results.size() + " all "+ results.get(0).getClass().getSimpleName() + " records");
            
        } catch (DataReaderException e) {
            System.err.println("Error processing files: " + e.getMessage());
        }
	}
}