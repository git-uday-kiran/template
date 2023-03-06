package controllers;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javassist.NotFoundException;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Controller {

	private static final Logger LOG = LogManager.getLogger(Controller.class);

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Hey, It's Working!";
	}

	@POST
	@Path("getData")
	public Response getData(JSONObject req) {
		LOG.info("getData :: {}", req);
		JSONObject resp = new JSONObject();

		try {
			Service.getData(req, resp);
		} catch (Exception e) {
			LOG.error("getData Failed :: {} {}", e, req);
			return send(resp, Status.OK);
		}

		LOG.info("getData Successful :: {}", resp);
		return send(resp, Status.OK);
	}

	@POST
	@Path("checkYesNo")
	public Response checkYesNo(JSONObject req) {
		LOG.info("checkYesNo :: {}", req);
		JSONObject resp = new JSONObject();

		try {
			Service.checkYesNo(req, resp);
		} catch (Exception e) {
			Service.setVoc(req, resp);
			LOG.error("checkYesNo Failed :: {} {}", e, req);
			return send(resp, Status.OK);
		}

		Service.setVoc(req, resp);
		LOG.info("checkYesNo Successful :: {}", resp);
		return send(resp, Status.OK);
	}

	@POST
	@Path("checkDate")
	public Response checkDate(JSONObject req) {
		LOG.info("checkDate :: {}", req);
		JSONObject resp = new JSONObject();

		try {
			Service.checkDate(req, resp);
		} catch (Exception e) {
			LOG.error("checkDate Failed :: {}", req, e);
			return send(resp, Status.OK);
		}

		LOG.info("checkDate Successfull :: {}", resp);
		return send(resp, Status.OK);
	}

	@POST
	@Path("checkTime")
	public Response checkTime(JSONObject req) {
		LOG.info("checkTime :: {}", req);
		JSONObject resp = new JSONObject();

		try {
			Service.checkTime(req, resp);
		} catch (Exception e) {
			LOG.error("checkTime Failed :: {}", req, e);
			return send(resp, Status.OK);
		}

		LOG.info("checkTime Successfull :: {}", resp);
		return send(resp, Status.OK);
	}

	@POST
	@Path("updateCDR")
	public Response updateCDR(JSONObject req) {
		LOG.info("updateCDR :: {}", req);

		try {
			Service.updateCDR(req);
		} catch (NotFoundException | SQLException e) {
			LOG.error("updateCDR Failed :: {} {}", e, req);
			return send(req, Status.OK);
		}

		LOG.info("updateCDR Successfull :: {}", req);
		return send(req, Status.OK);
	}

	public Response send(JSONObject json, Status status) {
		return Response.status(status).entity(json.toString()).build();
	}
}
