package infrastructure.persistence.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import domain.model.LeaveType;
import domain.repository.ILeaveTypeRepository;
import infrastructure.persistence.DatabaseConnectionManager;

public class JdbcLeaveTypeRepository implements ILeaveTypeRepository {
	private static final Logger LOGGER = Logger.getLogger(JdbcLeaveTypeRepository.class.getName());
	private final DatabaseConnectionManager connectionManager;

	public JdbcLeaveTypeRepository(DatabaseConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	@Override
	public Optional<LeaveType> findById(int id) {

		String sql = "SELECT leave_type_id, leave_type_name, leave_default  FROM leave_types WHERE leave_type_id = ?";

		try (Connection conn = connectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new LeaveType(rs.getInt("leave_type_id"), rs.getString("leave_type_name"),
							rs.getInt("default_balance")));
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Error finding leave type by id: " + e.getMessage());
		}

		return Optional.empty();
	}

	@Override
	public Optional<LeaveType> findByName(String name) {

		String sql = "SELECT leave_type_id, leave_type_name, leave_default FROM leave_types WHERE leave_type_name = ?";

		try (Connection conn = connectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return Optional.of(new LeaveType(rs.getInt("leave_type_id"), rs.getString("leave_type_name"),
							rs.getInt("default_balance")));
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Error finding leave type by name: " + e.getMessage());
		}

		return Optional.empty();
	}

	@Override
	public List<LeaveType> findAll() {
		List<LeaveType> leaveTypes = new ArrayList<>();
		String sql = "SELECT leave_type_id, leave_type_name, leave_default FROM leave_types";

		try (Connection conn = connectionManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {

				leaveTypes.add(new LeaveType(rs.getInt("leave_type_id"), rs.getString("leave_type_name"),
						rs.getInt("default_balance")));
			}
		} catch (SQLException e) {
			LOGGER.severe("Error finding all leave types: " + e.getMessage());
		}

		return leaveTypes;
	}

	@Override
	public LeaveType save(LeaveType leaveType) {
		
		if (leaveType.getLeaveTypeId() == 0) {
			return insert(leaveType);
		} else {
			return update(leaveType);
		}
	}

	private LeaveType insert(LeaveType leaveType) {
		
		String sql = "INSERT INTO leave_types (leave_type_name) VALUES (?)";
		try (Connection conn = connectionManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setString(1, leaveType.getLeaveTypeName());
			stmt.executeUpdate();

			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int id = generatedKeys.getInt(1);
					return new LeaveType(id, leaveType.getLeaveTypeName(), leaveType.getDefaultBalance());
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Error inserting leave type: " + e.getMessage());
		}

		return leaveType;
	}

	private LeaveType update(LeaveType leaveType) {
		
		String sql = "UPDATE leave_types SET leave_type_name = ? WHERE leave_type_id = ?";
		try (Connection conn = connectionManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, leaveType.getLeaveTypeName());
			stmt.setInt(2, leaveType.getLeaveTypeId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.severe("Error updating leave type: " + e.getMessage());
		}

		return leaveType;
	}
}