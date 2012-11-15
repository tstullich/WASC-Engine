package com.sjsu.wascengine;

import java.io.IOException;
import javax.servlet.http.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
		//Just a quick test to see how we can convert an object to JSON	   
		String[] names = {"Tim", "Eddy", "Michael"};
		JsonObject obj = new JsonObject();
		JsonArray numArray = new JsonArray();
		for (String word : names)
		{
		   numArray.add(new JsonPrimitive(word));
		}
		obj.addProperty("numOfContributors", names.length);
		obj.add("contributors", numArray);
		String json = obj.toString();
		resp.getWriter().println(json);
	}
}