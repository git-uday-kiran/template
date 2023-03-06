package providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider
public class JsonIO implements MessageBodyReader<JSONObject>, MessageBodyWriter<JSONObject> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == JSONObject.class && mediaType.getClass() == MediaType.APPLICATION_JSON_TYPE.getClass();
	}

	@Override
	public JSONObject readFrom(Class<JSONObject> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		String content = new String(entityStream.readAllBytes(), StandardCharsets.UTF_8);
		if (content.isEmpty())
			return new JSONObject();
		return new JSONObject(content);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == JSONObject.class && mediaType.getClass() == MediaType.APPLICATION_JSON_TYPE.getClass();
	}

	@Override
	public long getSize(JSONObject t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(JSONObject t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		entityStream.write(t.toString().getBytes());
	}
}
