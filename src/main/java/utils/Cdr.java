package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javassist.NotFoundException;

public class Cdr {

	private static final String LOG_ID = "log_id";
	private static final Logger LOG = LogManager.getLogger(Cdr.class);

	private static final String CDR_QUERY = "UPDATE call_logs as cl INNER JOIN master_data as md on cl.master_data_id = md.id SET  `sip_id` = ?, `cdr_date_time` = ?, `cdr_seconds` = ?, cl.call_recording_url = ?, md.call_recording_url = ?, `cdr_status` = ?, `cdr_code` = ?, `cdr_cause` = ? "
			+ " WHERE (cl.id = '*logId*');";

	public static void updateEntity(JSONObject json, int logId) {
		String query = CDR_QUERY.replace("*logId*", "" + logId);
		try (Connection con = JdbcPool.getConnection(); PreparedStatement prsmt = con.prepareStatement(query)) {
			setCdrColumns(json, prsmt);
			setCustomColumns(json, prsmt);
			LOG.info("updateEntity Query: {}", prsmt);
			int rowsEffected = prsmt.executeUpdate();
			LOG.info("updateEntity {} rows effected", rowsEffected);
		} catch (Exception e) {
			LOG.error("updateEntity failed, {} Query: {}", e, query);
		}
	}

	public static void setCdrColumns(JSONObject json, PreparedStatement prsmt) throws SQLException, NotFoundException {
		String sipId = extractSipId(json.optString("sip_id"));
		String cdrDateTime = Timestamp.from(Instant.now()).toString();
		String cdrSeconds = json.optString("cdr_seconds", null);
		String callRecordingUrl = getRecordingUrl(sipId);
		String cdrStatus = json.optString("cdr_status", null);
		String cdrCode = json.optString("cdr_code", null);
		String cdrCause = json.optString("cdr_cause", null);

		prsmt.setString(1, sipId);
		prsmt.setString(2, cdrDateTime);
		prsmt.setString(3, cdrSeconds);
		prsmt.setString(4, callRecordingUrl);
		prsmt.setString(5, callRecordingUrl);
		prsmt.setString(6, cdrStatus);
		prsmt.setString(7, cdrCode);
		prsmt.setString(8, cdrCause);
	}

	// starts from 9
	public static void setCustomColumns(JSONObject json, PreparedStatement prsmt) {
		// optional
	}

	public static void processCDR(JSONObject json, String message) {
		String orgMsg = message.replace(LOG_ID, "logid").replace("calling_no", "callingno");
		String[] cdr = message.split("_");

		if (cdr.length > 2)
			json.put("cdr_reason", cdr[2]);

		if (cdr.length > 3) {
			json.put("cdr_code", cdr[3]);
			json.put("cdr_cause", cdr[4]);
		}

		if (cdr.length > 5) {
			LOG.info("Custom message in CDR {}", orgMsg);
			json.put("unique_id", orgMsg.split("_")[5]);
			setCallId(json, orgMsg);
		}
		json.put("cdr_seconds", convertMilliSecIntoMinutes(message));
	}

	public static void setCallId(JSONObject response, String cdrMsg) {
		if (cdrMsg != null && !cdrMsg.isEmpty()) {
			String[] allIds = cdrMsg.split("&");
			for (String id : allIds) {
				if (id.contains("callid=")) {
					id = id.split("=")[1];
					id = id.replace("%40", "@");
					response.put("callid", id);
				} else if (id.contains("log_id=") || id.contains("logid=")) {
					id = id.split("=")[1];
					response.put(LOG_ID, id);
				} else
					LOG.warn("Skipping URL param... id: {}", id);
			}
		}
	}

	public static Long convertMilliSecIntoMinutes(String milliseconds) {
		try {
			if (milliseconds.contains(":"))
				milliseconds = milliseconds.split(":")[0];
			return TimeUnit.MILLISECONDS.toSeconds(Long.valueOf(extractDigits(milliseconds)));
		} catch (Exception e) {
			LOG.error("convertMilliSecIntoMinutes {}, milliseconds: {}", e, milliseconds);
			return 0l;
		}
	}

	private static String extractDigits(String text) {
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(text);
		if (m.find())
			return m.group();
		return "0";
	}

	public static String extractSipId(String sipMessage) throws NotFoundException {
		if (!sipMessage.contains("sipheader"))
			throw new NotFoundException("sipheader not found in sipMessage " + sipMessage);
		String regex = "(\\w+)@([\\d.]+):(\\d+)";
		Matcher matcher = Pattern.compile(regex).matcher(sipMessage);
		return matcher.find() ? matcher.group(1) + "@" + matcher.group(2) + "_" + matcher.group(3) : "no sip_id";
	}

	public static String getRecordingUrl(String sipId) {
		return MessageProperty.RECORDING_URL + sipId + ".wav";
	}

	private Cdr() {}
}
