/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */



package com.openexchange.tools.servlet.http;

import java.security.Principal;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.openexchange.tools.ajp13.AJPv13Exception;
import com.openexchange.tools.ajp13.AJPv13RequestHandler;
import com.openexchange.tools.servlet.ServletRequestWrapper;

/**
 * HttpServletRequestWrapper
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletRequestWrapper extends ServletRequestWrapper implements HttpServletRequest {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(HttpServletRequestWrapper.class);

	private static final String HTTP_HEADER_DATE_FORMAT = "EEE',' dd MMMM yyyy hh:mm:ss z";

	private static final SimpleDateFormat HEADER_DATEFORMAT;

	static {
		HEADER_DATEFORMAT = new SimpleDateFormat(HTTP_HEADER_DATE_FORMAT);
		DateFormatSymbols dfs = HEADER_DATEFORMAT.getDateFormatSymbols();
		String[] shortWeekdays = new String[8];
		shortWeekdays[Calendar.SUNDAY] = "Sun";
		shortWeekdays[Calendar.MONDAY] = "Mon";
		shortWeekdays[Calendar.TUESDAY] = "Tue";
		shortWeekdays[Calendar.WEDNESDAY] = "Wed";
		shortWeekdays[Calendar.THURSDAY] = "Thu";
		shortWeekdays[Calendar.FRIDAY] = "Fri";
		shortWeekdays[Calendar.SATURDAY] = "Sat";
		dfs.setShortWeekdays(shortWeekdays);
		String[] shortMonths = new String[12];
		shortMonths[Calendar.JANUARY] = "Jan";
		shortMonths[Calendar.FEBRUARY] = "Feb";
		shortMonths[Calendar.MARCH] = "Mar";
		shortMonths[Calendar.APRIL] = "April";
		shortMonths[Calendar.MAY] = "May";
		shortMonths[Calendar.JUNE] = "June";
		shortMonths[Calendar.JULY] = "July";
		shortMonths[Calendar.AUGUST] = "Aug";
		shortMonths[Calendar.SEPTEMBER] = "Sep";
		shortMonths[Calendar.OCTOBER] = "Oct";
		shortMonths[Calendar.NOVEMBER] = "Nov";
		shortMonths[Calendar.DECEMBER] = "Dec";
		dfs.setShortMonths(shortMonths);
		HEADER_DATEFORMAT.setDateFormatSymbols(dfs);
	}

	private String authType;

	private Cookie[] cookies;

	private String method;

	private String pathInfo;

	private String requestURI;

	private String pathTranslated;

	private String servletPath;

	private String queryString;

	private String contextPath = "";

	private String remoteUser;

	private StringBuffer requestURL;

	private Principal userPrincipal;

	private HttpSessionWrapper session;

	private boolean requestedSessionIdFromCookie;

	private boolean requestedSessionIdFromURL;

	private final AJPv13RequestHandler ajpRequestHandler;

	public HttpServletRequestWrapper(AJPv13RequestHandler ajpRequestHandler) throws AJPv13Exception {
		super();
		this.ajpRequestHandler = ajpRequestHandler;
	}

	public String getAuthType() {
		return authType;
	}

	public void setCookies(final Cookie[] cookies) {
		this.cookies = new Cookie[cookies.length];
		System.arraycopy(cookies, 0, this.cookies, 0, cookies.length);
	}

	public Cookie[] getCookies() {
		if (cookies == null) {
			return null;
		}
		final Cookie[] retval = new Cookie[cookies.length];
		System.arraycopy(cookies, 0, retval, 0, cookies.length);
		return retval;
	}

	public long getDateHeader(final String name) {
		return containsHeader(name) ? getDateValueFromHeaderField(getHeader(name)) : -1;
	}

	public int getIntHeader(final String name) {
		return containsHeader(name) ? Integer.parseInt(getHeader(name)) : -1;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setPathInfo(final String path_info) {
		this.pathInfo = path_info;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathTranslated(final String path_translated) {
		this.pathTranslated = path_translated;
	}

	public String getPathTranslated() {
		return pathTranslated;
	}

	public void setContextPath(final String context_path) {
		this.contextPath = context_path;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setQueryString(final String query_string) {
		this.queryString = query_string;
	}

	public String getQueryString() {
		return queryString;
	}

	public void setRemoteUser(final String remote_user) {
		this.remoteUser = remote_user;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public boolean isUserInRole(final String role) {
		System.err.println("Method isUserInRole() is not implemented in HttpServletRequestWrapper, yet!");
		return false;
	}

	public java.security.Principal getUserPrincipal() {
		return userPrincipal;
	}

	public void setUserPrincipal(final Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}

	public String getRequestedSessionId() {
		return session.getId();
	}

	public void setRequestURI(final String request_uri) {
		this.requestURI = request_uri;
	}

	public String getRequestURI() {
		return requestURI;
	}

	public void setRequestURL(final StringBuffer request_url) {
		this.requestURL = request_url;
	}

	public StringBuffer getRequestURL() {
		return requestURL;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(final String servlet_path) {
		this.servletPath = servlet_path;
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(final boolean create) {
		if (create && session == null) {
			final String id = ajpRequestHandler.getHttpSessionId();
			final HttpSession httpSession = HttpSessionManagement.getHttpSession(id);
			if (httpSession != null) {
				if (!HttpSessionManagement.isHttpSessionExpired(httpSession)) {
					session = (HttpSessionWrapper) httpSession;
					session.setNew(false);
					return session;
				}
				/*
				 * Invalidate session
				 */
				httpSession.invalidate();
				HttpSessionManagement.removeHttpSession(id);
			}
			/*
			 * Create new session
			 */
			session = (HttpSessionWrapper) HttpSessionManagement.createHttpSession(id);
			session.setNew(true);
		}
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	public void setSession(final HttpSession session) {
		this.session = (HttpSessionWrapper) session;
	}

	public boolean isRequestedSessionIdValid() {
		return !HttpSessionManagement.isHttpSessionExpired(session);
	}

	public boolean isRequestedSessionIdFromCookie() {
		return requestedSessionIdFromCookie;
	}

	public void setRequestedSessionIdFromCookie(final boolean requestedSessionIdFromCookie) {
		this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
	}

	public boolean isRequestedSessionIdFromURL() {
		return requestedSessionIdFromURL;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return requestedSessionIdFromURL;
	}

	public void setRequestedSessionIdFromURL(final boolean requestedSessionIdFromURL) {
		this.requestedSessionIdFromURL = requestedSessionIdFromURL;
	}

	public void setAuthType(final String authType) {
		this.authType = authType;
	}

	private long getDateValueFromHeaderField(final String headerValue) {
		try {
			synchronized (HEADER_DATEFORMAT) {
				return HEADER_DATEFORMAT.parse(headerValue).getTime();
			}
		} catch (ParseException e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException(e.getMessage());
		}
	}

}
