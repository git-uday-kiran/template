package template;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import controllers.Controller;
import providers.JsonIO;

class ControllerTest extends JerseyTest {

	@Override
	protected Application configure() {
		ResourceConfig config = new ResourceConfig();
		config.registerClasses(Controller.class);
		config.registerClasses(JsonIO.class);
		return config;
	}

	@Test
	@Disabled
	void getIt() {
		Response response = target("api").request().get();
		String content = response.readEntity(String.class);
		Assertions.assertEquals("Hey, It's Working!", content);
	}

	@Test
	@Disabled
	void getData() {
		JSONObject payload = new JSONObject();
		payload.put("mobile", "6301815817");
		payload.put("log_id", "101");

		Response response = target("api/getData").request(MediaType.APPLICATION_JSON).post(Entity.json(payload.toString()));
		String content = response.readEntity(String.class);

		JSONObject resp = new JSONObject();
		resp.put("user_status", "valid_user");

		Assertions.assertEquals(200, response.getStatus());
		Assertions.assertEquals(resp.toString(), content);
	}

	@Test
	void checkYesNo() {
		JSONObject payload = new JSONObject();
		payload.put("mobile", "6301815817");
		payload.put("log_id", "101");

		payload.put("entity", "test yes no");
		payload.put("user_message", "नो यस ");

		Response response = target("api/checkYesNo").request(MediaType.APPLICATION_JSON).post(Entity.json(payload.toString()));
		String content = response.readEntity(String.class);

		JSONObject resp = new JSONObject();
		resp.put("found", "no");

		Assertions.assertEquals(200, response.getStatus());
	}

	@Test
	@Disabled
	void checkDate() {
		JSONObject payload = new JSONObject("{\"user_message\":\"september 2nd\",\"repeat_message\":\"let us let us know date and time\",\"fail_message\":\"please let us know date and time\"}[m  \r\n" + "");
		payload.put("user_message", "प्लीज़ कॉल मी ओन मर्च।");

		Response response = target("api/checkDate").request(MediaType.APPLICATION_JSON).post(Entity.json(payload.toString()));
		String content = response.readEntity(String.class);

		JSONObject resp = new JSONObject();
		resp.put("time", "");
		resp.put("date", "2023-02-09 00:00:00");

		Assertions.assertEquals(200, response.getStatus());
	}
}
