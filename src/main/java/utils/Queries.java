package utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javassist.NotFoundException;

public class Queries {

	private static final String USER_STATUS = "user_status";
	private static final Logger LOG = LogManager.getLogger(Queries.class);

	private static final String BY_LOG_ID = "SELECT cl.id as call_logs_id, cl.master_data_id as call_logs_master_data_id,  md.* FROM call_logs as cl JOIN master_data as md on cl.master_data_id = md.id where cl.id = '*log_id*'";
	private static final String BY_MOBILE = "SELECT cl.id as call_logs_id, cl.master_data_id as call_logs_master_data_id,  md.* FROM call_logs as cl JOIN master_data as md on cl.master_data_id = md.id where cl.mobile = '*mobile*'";

	public static void loadData(JSONObject json, String callType, String mobile, int logId) {
		String query = "";

		if (callType.equals("Incoming"))
			query = BY_MOBILE;
		if (callType.equals("Outgoing"))
			query = BY_LOG_ID;

		mobile = Util.getLast10Digits(mobile);
		query = query.replace("*log_id*", "" + logId).replace("*mobile*", mobile);

		LOG.info("loadData Query :: {}", query);
		try (Connection con = JdbcPool.getConnection(); Statement stmt = con.createStatement();) {
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				if (isWelcomeFilled(rs)) {
					json.put(USER_STATUS, "input_received");
				} else {
					json.put(USER_STATUS, "valid_user");
					fetchRecordDetails(rs, json);
				}
			} else {
				LOG.error("User details not found, Invalid User, mobile: {} logId: {}", mobile, logId);
				json.put(USER_STATUS, "invalid_user");
			}
		} catch (Exception e) {
			LOG.error("loadData failed {}, Query: {}", e, query);
		}
	}

	public static int getLogIdByMobile(String mobile) throws SQLException, NotFoundException {
		mobile = Util.getLast10Digits(mobile);
		String query = "select * from call_logs where mobile = '" + mobile + "' AND DATE(CURDATE()) <=  DATE_ADD(DATE(call_triggered_at), INTERVAL 1 DAY)  order by call_triggered_at DESC limit 1";
		LOG.info("getLogIdByMobile Query: {}", query);
		try (Connection con = JdbcPool.getConnection(); Statement stmt = con.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			if (!rs.next())
				throw new NotFoundException("getLogIdByMobile No records found, Query: " + query);
			return rs.getInt("id");
		}
	}

	public static void setVOC(int logId, String entity, String voc, boolean status, String found) {
		LOG.info("Setting VOC :: log_id : {} entity : {} voc : {} status : {} found : {}", logId, entity, voc, status, found);
		String insertVOC = "INSERT INTO" + " voc " + "(log_id, entity, voc, status, found) " + "VALUES" + "(?, ?, ?, ?, ?)";
		LOG.info("INSERT VOC: {}", insertVOC);

		try (Connection con = JdbcPool.getConnection(); PreparedStatement psmt = con.prepareStatement(insertVOC)) {
			psmt.setInt(1, logId);
			psmt.setString(2, entity);
			psmt.setString(3, voc);
			psmt.setInt(4, status ? 1 : 0);
			psmt.setString(5, found);
			int rowsEffected = psmt.executeUpdate();
			LOG.info("setVOC successfull, {} rows effected", rowsEffected);
		} catch (Exception e) {
			LOG.error("setVOC Failed Query: {}", insertVOC, e);
		}
	}

	public static boolean isWelcomeFilled(ResultSet rs) throws SQLException {
		return rs.getString("welcome") != null && !rs.getString("welcome").isEmpty();
	}

	public static void fetchRecordDetails(ResultSet rs, JSONObject json) throws SQLException, IOException {
		Map<String, String> map = new HashMap<>();

		// add placeholder's to map

		Properties msgs = MessageProperty.getMessagesByReplacingTextWithMapKeyValues(map);
		MessageProperty.wrapWithSSML(msgs);
		passToJson(msgs, json);
	}

	public static void passToJson(Properties source, JSONObject target) {
		for (String key : source.stringPropertyNames())
			target.put(key, source.getProperty(key));
	}

	private Queries() {}
}
