package com.openexchange.ajax.redirect;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectServlet extends HttpServlet {
	
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String location = req.getParameter("location");
		
		if (location == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		if (!isRelative(location)) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		
		if ( isServerRelative(location)) {
			resp.sendRedirect(location);
			return;
		}
		
		
		String referer = purgeHost(req.getHeader("referer"));
		
		if (referer == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		location = assumeRelative(referer, location);
		
		resp.sendRedirect(location);
		
		
	}
	
	private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^(\\w*:)?//");
	
	private boolean isRelative(String location) {
		Matcher matcher = PROTOCOL_PATTERN.matcher(location);
		return !matcher.find();
	}

	private String purgeHost(String location) {
		if (location == null) {
			return null;
		}
		return location.replaceAll("^(\\w*:)?//\\w*/","");
	}

	private boolean isServerRelative(String location) {
		return location.startsWith("/");
	}

	private String assumeRelative(String referer, String location) {
		if (referer.endsWith("/")) {
			return "/"+referer + location;
		}
		int index = referer.lastIndexOf('/');

		return "/"+referer.substring(0, index) + "/" + location;
	}
	
}
