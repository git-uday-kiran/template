package controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.management.AttributeNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javassist.NotFoundException;
import utils.Cdr;
import utils.Queries;
import utils.Util;

import static utils.Constants.*;

public class Service {

	private static final Logger LOG = LogManager.getLogger(Service.class);

	public static void getData(JSONObject req, JSONObject resp) throws AttributeNotFoundException {
		int logId = req.optInt(LOG_ID);
		String sipId = req.optString(SIP_ID, R_USER_MESSAGE);
		String mobile = req.optString(MOBILE, R_MOBILE);
		String callType = logId == 0 ? "Incoming" : "Outgoing";

		if (logId == 0)
			LOG.error("log_id not found");
		if (sipId.equals(R_USER_MESSAGE) || !sipId.contains("sipheader"))
			LOG.error("sip_id not found, sipId: {}", sipId);
		if (mobile.equals(R_MOBILE))
			LOG.error("mobile number not found, mobile: {}", mobile);

		if (logId == 0 && mobile.equals(R_MOBILE))
			throw new AttributeNotFoundException("log_id and mobile not found, couldn't proceed with getData");

		Queries.loadData(resp, callType, mobile, logId);
	}

	public static void checkYesNo(JSONObject req, JSONObject resp) throws AttributeNotFoundException {
		String userMessage = Util.getUserMessage(req);

		if (Util.hasRepeatGeneric(userMessage, resp))
			LOG.info("checkYesNo :: found generic: {}", resp.getString(GENERIC));
		else if (Util.hasYesOrNo(userMessage, resp))
			LOG.info("checkYesNo :: found: {}", resp.get(FOUND));
		else {
			resp.put(MESSAGE, req.optString(FAIL_MESSAGE));
			LOG.info("checkYesNo :: 'yes' or 'no' not found");
		}
	}

	public static void checkDate(JSONObject req, JSONObject resp) throws AttributeNotFoundException {
		String userMessage = Util.getUserMessage(req);
		resp.put("time", "");

		if (Util.hasRepeatGeneric(userMessage, resp)) {
			LOG.info("checkDate :: found generic: {}", resp.getString(GENERIC));

		} else if (Util.hasDate(userMessage, resp)) {
			String date = resp.getString("date");
			String time = "";

			if (date.contains("Approximate"))
				resp.put("time", "don't ask time");
			else {
				if (Util.hasTime(userMessage, resp)) {
					time = resp.getString("time");
					time = LocalTime.parse(time, Util.TIME_FORMATTER).format(DateTimeFormatter.ofPattern("h"));
				}
				date = LocalDateTime.parse(date, Util.DATE_TIME_FORMATTER).format(Util.DATE_FORMATTER);
			}

			resp.put(FOUND, date);
			resp.put("call_me_later_response", "we will call you back on " + date + (time.isEmpty() ? "" : " at " + time + " o'clock") + ". Thank you ");
			LOG.info("checkDate :: found date: {} time: {}", resp.getString("date"), resp.opt("time"));

		} else {
			resp.put(MESSAGE, req.optString(FAIL_MESSAGE));
			LOG.info("checkDate :: no date found");
		}
	}

	public static void checkTime(JSONObject req, JSONObject resp) throws AttributeNotFoundException {
		String userMessage = Util.getUserMessage(req);

		if (Util.hasRepeatGeneric(userMessage, resp)) {
			LOG.info("checkTime :: found generic: {}", resp.getString(GENERIC));

		} else if (Util.hasTime(userMessage, resp)) {
			resp.put(FOUND, resp.getString("time"));
			//			resp.put("call_me_later_response", "we will call you back on " + resp.optString("date") + " at " + resp.getString(FOUND) + ". Thank you ");
			LOG.info("checkTime :: found time: {}", resp.getString("time"));

		} else {
			resp.put(MESSAGE, req.optString(FAIL_MESSAGE));
			LOG.info("checkTime :: no time found");
		}
	}

	public static void updateCDR(JSONObject req) throws SQLException, NotFoundException {
		int logId = req.optInt(LOG_ID);
		String mobile = req.optString(MOBILE, R_MOBILE);
		String sipId = req.optString(SIP_ID, R_USER_MESSAGE);
		String userMessage = req.optString(USER_MESSAGE, R_USER_MESSAGE);

		if (logId == 0)
			LOG.error("log_id not found");
		if (sipId.equals(R_USER_MESSAGE) || !sipId.contains("sipheader"))
			LOG.error("sip_id not found, sipId: {}", sipId);
		if (mobile.equals(R_MOBILE))
			LOG.error("mobile number not found, mobile: {}", mobile);

		if (logId == 0) {
			if (mobile.isEmpty())
				throw new NotFoundException("lod_id and mobile not available in payload, couldn't able to retrive logId for CDR");
			logId = Queries.getLogIdByMobile(mobile);
			req.put(LOG_ID, logId);
		}

		Cdr.processCDR(req, userMessage);
		Cdr.updateEntity(req, logId);
	}

	public static void setVoc(JSONObject req, JSONObject resp) {
		Queries.setVOC(req.optInt(LOG_ID), req.optString("entity", null), req.optString(USER_MESSAGE), resp.has(FOUND), resp.optString(FOUND, null));
	}

	private Service() {}
}
