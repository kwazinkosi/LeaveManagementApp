package infrastructure.monitor;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class FileMonitor {

    private final WatchService watchService;
    private final Path directory;
    private final ExecutorService executor;
    private final Consumer<List<Path>> fileChangeHandler;

    public FileMonitor(Path directory, Consumer<List<Path>> fileChangeHandler) throws IOException {
        
    	this.directory = directory;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.executor = Executors.newSingleThreadExecutor();
        this.fileChangeHandler = fileChangeHandler;

        // Register the directory for create and modify events
        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
    }

    public void start() {
        executor.submit(this::monitorDirectory);
    }

    public void stop() {
        
    	try {
            watchService.close();
        } catch (IOException e) {
            System.err.println("Error closing watch service: " + e.getMessage());
        }
        executor.shutdown();
    }

    private void monitorDirectory() {
        
    	try {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    handleWatchEvent(event);
                }
                key.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("File monitoring interrupted");
        } catch (ClosedWatchServiceException e) {
            System.out.println("File monitoring stopped");
        }
    }

    private void handleWatchEvent(WatchEvent<?> event) {
        WatchEvent.Kind<?> kind = event.kind();

        if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
            Path filePath = directory.resolve((Path) event.context());
            fileChangeHandler.accept(List.of(filePath));
        }
    }
}