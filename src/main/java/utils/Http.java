package utils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.ws.rs.core.NoContentException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Http {

	private static final Logger LOG = LogManager.getLogger(Http.class);

	private static final HttpRequestInterceptor requestInterceptor = (request, context) -> {
	};

	private static final HttpResponseInterceptor responseInterceptor = (response, context) -> {
	};

	private static final int TIMEOUT = 3;
	private static final RequestConfig CONFIG = RequestConfig.custom().setConnectTimeout(TIMEOUT * 1000).setConnectionRequestTimeout(TIMEOUT * 1000).setSocketTimeout(TIMEOUT * 1000).build();
	private static final HttpClient CLIENT = HttpClientBuilder.create().setDefaultRequestConfig(CONFIG).addInterceptorFirst(requestInterceptor).addInterceptorFirst(responseInterceptor).build();

	public static final String CONTENT_TYPE = "content-type";

	public static Object post(String uri, JSONObject body, JSONObject headers, AcceptType type) {
		LOG.info("httpPost :: {} {} {}", uri, headers, body);

		try {
			HttpPost post = new HttpPost(uri);

			// setting headers to the post
			Iterator<String> headKeys = headers.keys();
			while (headKeys.hasNext()) {
				String headKey = headKeys.next();
				String headValue = (String) headers.get(headKey);
				post.addHeader(headKey, headValue);
			}

			if (!headers.has(CONTENT_TYPE))
				post.addHeader(CONTENT_TYPE, "application/json");

			StringEntity entity = new StringEntity(body.toString(), StandardCharsets.UTF_8);
			post.setEntity(entity);

			HttpResponse httpResponse = CLIENT.execute(post);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				String content = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
				if (content.isEmpty())
					throw new NoContentException("no content available");
				LOG.info("httpPost Succesfull :: {}", content);
				return type.equals(AcceptType.JSON_OBJECT) ? new JSONObject(content) : new JSONArray(content);
			} else {
				String error = "Error via Http with status code " + httpResponse.getStatusLine().getStatusCode();
				LOG.error("httpPost Failed :: {}", error);
			}

		} catch (JSONException e) {
			LOG.error("httpPost Failed :: {} {}", "response type is not " + type, e);
		} catch (Exception e) {
			LOG.error("httpPost Failed :: {} {} {}", uri, headers, body, e);
		}

		return type.equals(AcceptType.JSON_OBJECT) ? new JSONObject() : new JSONArray();
	}

	public static Object get(String uri, JSONObject headers, AcceptType type) {
		LOG.info("httpGet :: {} {}", uri, headers);

		try {
			HttpGet get = new HttpGet(uri);

			// setting headers to the get
			Iterator<String> headKeys = headers.keys();
			while (headKeys.hasNext()) {
				String headKey = headKeys.next();
				String headValue = (String) headers.get(headKey);
				get.addHeader(headKey, headValue);
			}

			if (!headers.has(CONTENT_TYPE))
				get.addHeader(CONTENT_TYPE, "application/json");

			HttpResponse httpResponse = CLIENT.execute(get);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				HttpEntity httpEntity = httpResponse.getEntity();
				String content = EntityUtils.toString(httpEntity, StandardCharsets.UTF_8);
				if (content.isEmpty())
					throw new NoContentException("no content available");
				LOG.info("httpGet Succesfull :: {} ", content);
				return type.equals(AcceptType.JSON_OBJECT) ? new JSONObject(content) : new JSONArray(content);
			} else {
				String error = "Error via Http with status code " + httpResponse.getStatusLine().getStatusCode();
				LOG.error("httpGet Failed :: {}", error);
			}
		} catch (JSONException e) {
			LOG.error("httpGet Failed :: {}", "response type is not " + type, e);
		} catch (Exception e) {
			LOG.error("httpGet Failed :: {} {}", uri, headers, e);
		}

		return type.equals(AcceptType.JSON_OBJECT) ? new JSONObject() : new JSONArray();
	}

	private Http() {}
}
