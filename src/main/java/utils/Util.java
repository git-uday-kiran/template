package utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import javax.management.AttributeNotFoundException;

import org.json.JSONObject;

import static utils.Constants.FOUND;
import static utils.Constants.USER_MESSAGE;
import static utils.Constants.R_USER_MESSAGE;

public class Util {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM");
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:m a");
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static String getLast10Digits(String mobile) {
		if (mobile.length() > 10)
			mobile = mobile.substring(mobile.length() - 10, mobile.length());
		return mobile;
	}

	public static String getUserMessage(JSONObject json) throws AttributeNotFoundException {
		String userMessage = json.optString(USER_MESSAGE, R_USER_MESSAGE);
		if (userMessage.equals(R_USER_MESSAGE))
			throw new AttributeNotFoundException("user_message not found");
		return userMessage;
	}

	public static boolean hasRepeatGeneric(String text, JSONObject json) {
		JSONObject rsp = Helper.searchRegex(text, MessageProperty.REPEAT_GENERIC_ID);
		if (rsp.has(FOUND)) {
			json.put("generic", rsp.getString(FOUND));
			json.put("message", json.optString("repeat_message"));
			return true;
		}
		return false;
	}

	public static boolean hasYesOrNo(String text, JSONObject json) {
		JSONObject rsp = Helper.searchRegex(text, "140");
		preferNoIfYesAndNoExists(rsp);
		if (rsp.has(FOUND)) {
			json.put(FOUND, rsp.getString(FOUND).toLowerCase());
			return true;
		}
		return false;
	}

	public static void preferNoIfYesAndNoExists(JSONObject json) {
		if (!json.has(FOUND) || json.getString(FOUND).equalsIgnoreCase("no"))
			return;
		String key;
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			key = keys.next();
			if (json.getString(key).equalsIgnoreCase("no")) {
				json.put(FOUND, "no");
				return;
			}
		}
	}

	public static boolean hasDate(String text, JSONObject json) {
		if (hasApproximateDate(text, json))
			return true;
		if (hasDateUsingHelper(text, json)) {
			checkIfItsPastDate(json);
			return true;
		}
		return false;
	}

	public static boolean hasTime(String text, JSONObject json) {
		text = replaceWordsWithDigits(text);

		if (text.startsWith("12")) {
			json.put("time", LocalTime.MIN.withHour(12).format(TIME_FORMATTER));
			return true;
		}

		if (hasDateUsingHelper(text, json) && hasTimeUsingDate(json))
			return true;

		if (hasTimeUsingHelper("14", text, json)) {
			LocalTime localTime = LocalTime.now();
			if (json.getString("time").equals("Night01001"))
				localTime = localTime.withHour(19);
			else
				localTime = LocalTime.parse(json.getString("time").toLowerCase(), TIME_FORMATTER);
			json.put("time", localTime.format(TIME_FORMATTER));
			return true;
		}

		if (hasTimeUsingHelper("17", text, json)) {
			String time = anyMatchWithNoons(json.getString("time"));
			LocalTime localTime = LocalTime.parse(time.toLowerCase(), TIME_FORMATTER);
			json.put("time", localTime.format(TIME_FORMATTER));
			return true;
		}

		return false;
	}

	public static boolean hasTimeUsingHelper(String id, String text, JSONObject json) {
		JSONObject resp = Helper.searchRegex(text, id);
		if (resp.has(FOUND)) {
			json.put("time", resp.getString(FOUND));
			return true;
		}
		return false;
	}

	public static boolean hasTimeUsingDate(JSONObject json) {
		if (!json.has("date"))
			return false;
		String date = json.getString("date");
		LocalTime time = LocalTime.parse(date, DATE_TIME_FORMATTER);
		if (time.getHour() != 0) {
			time = getOfficeHoursTime(time);
			json.put("time", time.format(TIME_FORMATTER));
			return true;
		}
		return false;
	}

	public static boolean hasDateUsingHelper(String text, JSONObject json) {
		if (json.has("date"))
			return true;
		JSONObject resp = Helper.getDataFromString(text);
		if (resp.has("date")) {
			json.put("date", resp.get("date"));
			return true;
		}
		return false;
	}

	public static String replaceWordsWithDigits(String text) {
		JSONObject resp = Helper.searchRegex(text, "7");
		if (resp.has(FOUND)) {
			Iterator<String> keys = resp.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				text = text.replace(key, resp.getString(key));
			}
		}
		return text;
	}

	public static LocalTime getOfficeHoursTime(LocalTime time) {
		int hour = time.getHour();
		if (hour >= 1 && hour <= 5)
			hour = 12 + hour;
		else if (hour < 9)
			hour = 11;
		return time.withHour(hour);
	}

	public static String anyMatchWithNoons(String time) {
		if (time.equals("morning"))
			return LocalTime.MIN.withHour(11).format(TIME_FORMATTER);
		if (time.equals("afternoon"))
			return LocalTime.NOON.format(TIME_FORMATTER);
		if (time.equals("evening"))
			return LocalTime.MIN.withHour(16).format(TIME_FORMATTER);
		return time;
	}

	public static void checkIfItsPastDate(JSONObject json) {
		LocalDateTime curDateTime = LocalDateTime.now();
		LocalDateTime dateTime = LocalDateTime.parse(json.getString("date"), DATE_TIME_FORMATTER);
		if (dateTime.isBefore(curDateTime)) {
			dateTime = dateTime.withDayOfMonth(curDateTime.getDayOfMonth());
			dateTime = dateTime.withMonth(curDateTime.getMonthValue());
			dateTime = dateTime.withYear(curDateTime.getYear());
		}
		json.put("date", dateTime.format(DATE_TIME_FORMATTER));
	}

	public static boolean hasApproximateDate(String text, JSONObject json) {
		JSONObject resp = Helper.searchRegex(text, "68");
		if (resp.has(FOUND)) {
			json.put("date", resp.getString(FOUND));
			return true;
		}
		return false;
	}

	private Util() {}
}
