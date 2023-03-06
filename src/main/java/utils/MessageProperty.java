package utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProperty {

	private static final Properties CONFIGS = new Properties();
	private static final Logger LOG = LogManager.getLogger(MessageProperty.class);

	public static final String BOT_NAME;
	public static final String HELPER_URL;
	public static final String HELPER_PASSWORD;
	public static final String RECORDING_URL;

	public static final String REPEAT_GENERIC_ID;

	private static String msgFileContent;

	private static Map<String, String> regionMap;

	static {
		regionMap = getRegionMap();
		InputStream messages = Thread.currentThread().getContextClassLoader().getResourceAsStream("message.properties");
		InputStream configuration = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

		try {
			LOG.info("Loading properties started...");
			if (messages == null)
				throw new FileNotFoundException("messages file not found on resource path");
			if (configuration == null)
				throw new FileNotFoundException("configuration file not found on resource path");

			msgFileContent = new String(messages.readAllBytes());
			CONFIGS.load(configuration);
			LOG.info("Loaded properties, msgFileContent: {} CONFIGS: {}", msgFileContent, CONFIGS);

		} catch (IOException e) {
			LOG.fatal("Failded loading properties, ", e);
		}

		BOT_NAME = CONFIGS.getProperty("bot_name", "bot_name");
		HELPER_URL = CONFIGS.getProperty("helper_url", "helper_url");
		HELPER_PASSWORD = CONFIGS.getProperty("helper_password", "helper_password");
		RECORDING_URL = CONFIGS.getProperty("rec_url", "rec_url");
		REPEAT_GENERIC_ID = CONFIGS.getProperty("repeat_generic_id", "140");
	}

	public static Properties getMessagesByReplacingTextWithMapKeyValues(Map<String, String> replaceMap) throws IOException {
		String content = replaceTextWithMapKeyValues(msgFileContent, replaceMap);

		Properties messages = new Properties();
		messages.load(new ByteArrayInputStream(content.getBytes()));

		return messages;
	}

	public static String replaceTextWithMapKeyValues(String text, Map<String, String> replaceMap) {
		StringBuilder wordBuilder = new StringBuilder();
		StringBuilder result = new StringBuilder();

		boolean wordAlive = false;
		for (char ch : text.toCharArray()) {
			if (ch == '{')
				wordAlive = true;
			if (wordAlive)
				wordBuilder.append(ch);
			else
				result.append(ch);
			if (ch == '}') {
				String word = replaceMap.getOrDefault(wordBuilder.toString(), wordBuilder.toString());
				if (word.contains("{"))
					LOG.warn("Skipping {} placeholder in message properties", wordBuilder);
				wordAlive = false;
				result.append(word);
				wordBuilder.setLength(0);
			}
		}
		return result.toString();
	}

	public static void wrapWithSSML(Properties msgs) {
		for (String key : msgs.stringPropertyNames())
			msgs.put(key, Azure.wrapWithSSML(msgs.getProperty(key)));
	}

	public static Properties getRegionMessages(Properties msgs, String lang) {
		Properties prop = new Properties();
		for (String key : msgs.stringPropertyNames()) {
			if (key.endsWith("_" + lang))
				prop.put(key.substring(0, key.length() - lang.length() - 1), msgs.get(key));
		}
		return prop;
	}

	public static String getLangByRegion(String region) {
		return regionMap.getOrDefault(region, "english").toLowerCase();
	}

	private static Map<String, String> getRegionMap() {
		Map<String, String> map = new HashMap<>();

		final String HINDI = "Hindi";

		map.put("C1", HINDI);
		map.put("C2", HINDI);
		map.put("C3", HINDI);
		map.put("C4", HINDI);

		map.put("E1", HINDI);
		map.put("E2", HINDI);
		map.put("E3", HINDI);

		map.put("N1", HINDI);
		map.put("N2", HINDI);
		map.put("N3", HINDI);
		map.put("N4", HINDI);

		map.put("S1", "Tamil");
		map.put("S2", "Kannada");
		map.put("S3", "Malayalam");

		map.put("T1", "Telugu");
		map.put("T2", HINDI);

		map.put("W1", HINDI);
		map.put("W2", "Marathi");
		map.put("W3", "Gujarati");

		return map;
	}

	public static Properties getConfigs() {
		return CONFIGS;
	}
	private MessageProperty() {}
}
