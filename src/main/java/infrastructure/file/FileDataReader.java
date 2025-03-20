package infrastructure.file;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import common.exception.DataParseException;
import common.exception.DataReaderException;
import common.exception.DataReaderException.ErrorType;
import domain.validation.IValidator;
import infrastructure.file.parsers.AbstractDataParser;
import common.exception.ValidationException;

public class FileDataReader<T> implements IDataReader<T> {
    
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors(); //5
    
    private final AbstractDataParser<T> parser;
    private final IValidator<File> fileValidator;

    public FileDataReader(AbstractDataParser<T> parser, IValidator<File> fileValidator) {
        this.parser = parser;
        this.fileValidator = fileValidator;
    }

    @Override
    public List<T> readData(List<Path> filePaths) throws DataReaderException {
       
    	ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        List<Future<List<T>>> futures = new ArrayList<>();
        List<T> results = Collections.synchronizedList(new ArrayList<>());

        try {
            for (Path path : filePaths) {
                futures.add(executor.submit(() -> processFile(path)));
            }
            
            for (Future<List<T>> future : futures) {
                try {
                    results.addAll(future.get());
                } catch (ExecutionException e) {
                    handleExecutionException(e);
                }
            }
        } catch (InterruptedException e) {
            handleInterruption(e);
        } finally {
            gracefulShutdown(executor);
        }

        return results;
    }

    private List<T> processFile(Path path) throws DataReaderException {
        try {
            File file = path.toFile();
            fileValidator.validate(file);
            return parser.parse(file);
        } catch (DataParseException | ValidationException e) {
            throw new DataReaderException("Error processing file: " + path, e, ErrorType.FILE_NOT_FOUND);
        }
    }

    private void handleExecutionException(ExecutionException e) throws DataReaderException {
        
    	Throwable cause = e.getCause();
        if (cause instanceof DataReaderException) {
            throw (DataReaderException) cause;
        }
        throw new DataReaderException("Unexpected error during processing", cause, ErrorType.PARSE_ERROR);
    }

    private void handleInterruption(InterruptedException e) throws DataReaderException {
        Thread.currentThread().interrupt();
        throw new DataReaderException("Data reading process interrupted", e, ErrorType.PARSE_ERROR);
    }

    private void gracefulShutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}