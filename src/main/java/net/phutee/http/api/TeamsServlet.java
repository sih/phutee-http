package net.phutee.http.api;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.gson.Gson;

import net.phutee.data.api.*;
import net.phutee.data.domain.Division;

public class TeamsServlet extends HttpServlet {

    private static final long serialVersionUID = 4393626160305232798L;

    private PopulatorAPI api;

    @Override
    public void init() throws ServletException {
	super.init();
	api = new PopulatorAPIImpl();
    }

    /**
     * Return either all teams that the populator supports or the teams in a
     * particular division.
     * <p>
     * <b>Usage:</b> <br/>
     * <ul>
     * <li>GET /teams/all</li>
     * <li>GET /teams/division/{divisionName}</li>
     * <p>
     * <b>Status codes:</b> <br/>
     * <ul>
     * <li>200 - Data found</li>
     * <li>204 - Good request but no data found</li>
     * <li>400 - For an improperly formed request</li>
     * </ul>
     * <p>
     * <b>Output (JSON):</b> <br/>
     * {"name":"West Ham","played":"12","goalDiff":"+4","points":"18","form":
     * "3-2-1"}
     */
    @Override
    protected void doGet(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {

	// get three coffins ready
	response.setContentType("application/json");
	String json = null;

	// parse the request
	String resourceStr = request.getPathInfo();
	if (resourceStr.startsWith("/"))
	    resourceStr = resourceStr.substring(1); // lose the preceding /
	String[] subResources = resourceStr.split("/");

	boolean all = false;
	String division = null;

	// validate the request
	if (subResources.length == 1 && subResources[0].equals("all")) {
	    all = true;
	}
	else if (subResources.length == 1 && subResources[0].equals("help")) {
	    response.setStatus(HttpServletResponse.SC_OK);
	    json = printHelp(); // be kind
	}
	else if (subResources.length == 2 && subResources[0].equals("division")) {
	    division = subResources[1];
	    if (!isValidDivision(division)) {
		json = printHelp();
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, json);
	    }
	}
	else {
	    json = printHelp();
	    response.sendError(HttpServletResponse.SC_BAD_REQUEST, json);
	}

	// process the request
	if (null == json && (all || division != null)) {
	    Map<String, List<Object>> results = new HashMap<String, List<Object>>();
	    if (all) {
		results = api.getAllTeamsByDivision();
	    }
	    else {
		List<Object> teams = api.getTeamsForDivision(Division
			.valueOf(division));
		results.put(division, teams);
	    }
	    if (results != null && !results.isEmpty()) {
		Gson g = new Gson();
		json = g.toJson(results);
		response.setStatus(HttpServletResponse.SC_OK);
	    }
	    else {
		json = "{}";
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	    }

	}

	// emit JSON
	PrintWriter writer = response.getWriter();
	writer.println(json);

    }

    private String printHelp() {
	StringBuilder builder = new StringBuilder();
	builder.append("{")
		.append("\r")
		.append("\"Usage\":")
		.append("\"GET \\\\teams\\\\all or GET \\\\teams\\\\division\\\\<division_name>\"")
		.append(",").append("\r").append("\t").append("\"divisions\":")
		.append("[");
	for (Division d : Division.values()) {
	    builder.append("\"").append(d).append("\",");
	}
	builder.deleteCharAt(builder.length() - 1); // kill the last comma
	builder.append("]").append("\r").append("}");

	return builder.toString();
    }
    
    private boolean isValidDivision(String division) {
	boolean test = false;
	for (Division d : Division.values()) {
	    if (division.equals(d.name())) {
		test = true;
		break;
	    }
	}
	return test;
    }

}
