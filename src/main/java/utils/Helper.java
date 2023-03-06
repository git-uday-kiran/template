package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Helper {

	private static final String LANGUAGE = "language";
	private static final String LANGUAGE_VALUE = "hindi";

	private static final String PASSWORD = "password";
	private static final String MODEL_NAME = "model_name";

	private static final String CONTENT_TYPE = "content-type";
	private static final String CONTENT_TYPE_VALUE = "application/json";

	private static final Logger LOG = LogManager.getLogger(Helper.class);

	public static JSONObject getDataFromString(String text) {
		LOG.info("getDataFromString text: {}", text);

		JSONObject headers = new JSONObject();
		JSONObject payload = new JSONObject();

		headers.put(CONTENT_TYPE, CONTENT_TYPE_VALUE);

		payload.put("text", text);
		payload.put("region", "IN");
		payload.put(MODEL_NAME, MessageProperty.BOT_NAME);
		payload.put(PASSWORD, MessageProperty.HELPER_PASSWORD);

		String url = MessageProperty.HELPER_URL + "getDataFromString";
		JSONArray arr = (JSONArray) Http.post(url, payload, headers, AcceptType.JSON_ARRAY);
		JSONObject response = arr.isEmpty() ? new JSONObject() : arr.getJSONObject(0);

		LOG.info("getDataFromString Response :: {}", response);
		return response;
	}

	public static JSONObject searchRegex(String text, String id) {
		LOG.info("searchRegex id: {} text: {}", id, text);

		JSONObject headers = new JSONObject();
		JSONObject payload = new JSONObject();

		headers.put(CONTENT_TYPE, CONTENT_TYPE_VALUE);

		payload.put("id", id);
		payload.put("text", text);
		payload.put(MODEL_NAME, MessageProperty.BOT_NAME);
		payload.put(PASSWORD, MessageProperty.HELPER_PASSWORD);

		String url = MessageProperty.HELPER_URL + "search_regex";
		JSONObject response = (JSONObject) Http.post(url, payload, headers, AcceptType.JSON_OBJECT);

		LOG.info("searchRegex Response :: {}", response);
		return response;
	}

	public static JSONObject nameSearchV2(String text) {
		LOG.info("nameSearchV2 text: {}", text);

		JSONObject headers = new JSONObject();
		JSONObject payload = new JSONObject();

		headers.put(CONTENT_TYPE, CONTENT_TYPE_VALUE);

		payload.put("text", text);
		payload.put(LANGUAGE, LANGUAGE_VALUE);
		payload.put(MODEL_NAME, MessageProperty.BOT_NAME);
		payload.put(PASSWORD, MessageProperty.HELPER_PASSWORD);

		String url = MessageProperty.HELPER_URL + "search_regex";
		JSONObject response = (JSONObject) Http.post(url, payload, headers, AcceptType.JSON_OBJECT);

		LOG.info("nameSearchV2 Response :: {}", response);
		return response;
	}

	private Helper() {}
}
