package com.sjsu.wascengine;

import java.io.IOException;
import javax.servlet.http.*;

import com.google.gson.Gson;

/**
 * This is going to be the "main" class for the App Engine. It
 * is going to listen on a certain port for an Http Request and
 * should receive a pdf file. It is then going to be doing the
 * work in order to figure out the appropriate rubric scores and
 * return the final result as a JSON object.
 * 
 * @author Tim Stullich
 *
 */
@SuppressWarnings("serial")
public class WASC_EngineServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	   resp.setContentType("text/json");
		resp.getWriter().println("Hello, world");
		resp.getWriter().println("Testing commit - keats");
		//Just a quick test to see how we can convert an object to JSON
		Gson gson = new Gson();
		String[] number = {"Tim", "Eddy", "Michael"};
		String json = gson.toJson(number);
		resp.getWriter().println(json);
	}
}
