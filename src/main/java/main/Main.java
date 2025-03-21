package main;

import infrastructure.file.parsers.*;
import infrastructure.persistence.DatabaseConnectionManager;
import infrastructure.persistence.SchemaManager;
import infrastructure.persistence.jdbc.*;
import presentation.ApplicationRunner;
import application.services.FileProcessingService;
import application.services.LeaveService;
import domain.model.Employee;
import domain.model.LeaveBalance;
import domain.model.LeaveRequest;
import domain.model.LeaveType;
import domain.validation.FileValidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		try {
			
			// Initialize database connection
			DatabaseConnectionManager dbManager = DatabaseConnectionManager.getInstance();

			// Initialize database schema
			SchemaManager.initializeSchema(getSchemaDirectory());
			// Initialize repositories
			JdbcEmployeeRepository employeeRepository = new JdbcEmployeeRepository(dbManager);
			JdbcLeaveBalanceRepository leaveBalanceRepository = new JdbcLeaveBalanceRepository(dbManager);
			JdbcLeaveRequestRepository leaveRequestRepository = new JdbcLeaveRequestRepository(dbManager);
			JdbcLeaveTypeRepository leaveTypeRepository = new JdbcLeaveTypeRepository(dbManager);

			// Initialize leave service
			LeaveService leaveService = new LeaveService(leaveRequestRepository, leaveBalanceRepository, employeeRepository,
                    leaveTypeRepository );
			// Start the application
			
			// Set up data directory
			Path dataDirectory = getDataDirectory();
			List<Path> files = Arrays.asList(dataDirectory.resolve("CasualLeave.xlsm"),dataDirectory.resolve("PaidLeave.xlsm"),
					dataDirectory.resolve("SickLeave.xlsm"));

			// Initialize data processing services
			FileProcessingService<LeaveType> leaveTypeProcess = new FileProcessingService<>(new LeaveTypeXlsxParser(),
					new FileValidator(), dataDirectory, leaveTypeRepository);
			FileProcessingService<Employee> employeeProcess = new FileProcessingService<>(new EmployeeXlsxParser(),
					new FileValidator(), dataDirectory, employeeRepository);
			
			FileProcessingService<LeaveRequest> leaveRequestProcess = new FileProcessingService<>(
					new LeaveRequestXlsxParser(), new FileValidator(), dataDirectory, leaveRequestRepository);
			FileProcessingService<LeaveBalance> leaveBalanceProcess = new FileProcessingService<>(
					new LeaveBalanceXlsxParser(), new FileValidator(), dataDirectory, leaveBalanceRepository);

			// Process initial data files
			leaveTypeProcess.processFiles(files);
			employeeProcess.processFiles(files);
			leaveRequestProcess.processFiles(files);
			leaveBalanceProcess.processFiles(files);

			// Configure and run application
			ApplicationRunner runner = new ApplicationRunner(leaveService);
			runner.run(args);

		} catch (IOException e) {
			System.err.println("Error initializing file processing service: " + e.getMessage());
		} catch (IllegalStateException e) {
			System.err.println("Error: " + e.getMessage());
		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static Path getDataDirectory() {
		String sep = File.separator;
		String pathToData = System.getProperty("user.dir") + sep + "src" + sep + "main" + sep + "resources" + sep
				+ "data";
		Path directory = Paths.get(pathToData);

		// Ensure the directory exists
		if (!directory.toFile().exists()) {
			throw new IllegalStateException("Data directory not found: " + directory);
		}

		return directory;
	}

	private static String getSchemaDirectory() {
		String sep = File.separator;
		String pathToSchema= System.getProperty("user.dir") + sep + "src" + sep + "main" + sep + "resources" + sep
				+ "schema"+sep+"leave_management_schema.sql";

		return pathToSchema;
	}
}