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

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.server.impl.Version;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.ServletResponseWrapper;

/**
 * HttpServletResponseWrapper
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HttpServletResponseWrapper extends ServletResponseWrapper implements HttpServletResponse {

	private static final String ERROR_PAGE_TEMPL = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\r\n"
			+ "<html><head>\r\n" + "<title>#STATUS_CODE# #STATUS_MSG#</title>\r\n" + "</head><body>\r\n"
			+ "<h1>#STATUS_CODE# #STATUS_MSG#</h1>\r\n" + "<p>#STATUS_DESC#</p>\r\n" + "<hr>\r\n"
			+ "<address>#DATE#,&nbsp;Open-Xchange v#VERSION#</address>\r\n" + "</body></html>";

	// private static final String ERROR_PAGE_TEMPLATE = "<!DOCTYPE HTML PUBLIC
	// \"-//W3C//DTD HTML 4.01//EN\""
	// + "\"http://www.w3.org/TR/html4/strict.dtd\">\n"
	// + "<html xmlns=\"http://www.w3.org/1999/xhtml\" language=\"#LANGUAGE#\"
	// xml:lang=\"#LANGUAGE#\">\n"
	// + "<head>\n" + "\t<title>#STATUS_MSG#</title>\n"
	// + "\t<style type=\"text/css\"><!--/*--><![CDATA[/*><!--*/ " + "\n"
	// + "\t\tbody { color: #000000; background-color: #FFFFFF; }\n" + "\t\tp,
	// address {margin-left: 3em;}"
	// + "\t\tspan {font-size: smaller;}" + "\t/*]]>*/--></style>\n" +
	// "</head>\n\n" + "<body>\n"
	// + "<h1>#STATUS_MSG#</h1>\n" + "<p>\n#STATUS_DESC#\n</p>\n\n" + "<h2>Error
	// #STATUS_CODE#</h2>\n"
	// + "<address>\n" + "<span>#DATE#<br />\n" + "\tOpen-Xchange</span>\n" +
	// "</address>\n" + "</body>\n"
	// + "</html>";

	private static final Map<Integer, String> STATUS_MSGS;

	private static final Map<Integer, String> STATUS_DESC;

	private static final SimpleDateFormat HEADER_DATE_FORMAT;

	static {
		STATUS_MSGS = new HashMap<Integer, String>();
		STATUS_MSGS.put(Integer.valueOf(100), "Continue");
		STATUS_MSGS.put(Integer.valueOf(101), "Switching Protocols");
		STATUS_MSGS.put(Integer.valueOf(200), "OK");
		STATUS_MSGS.put(Integer.valueOf(201), "Created");
		STATUS_MSGS.put(Integer.valueOf(202), "Accepted");
		STATUS_MSGS.put(Integer.valueOf(203), "Non-Authoritative Information");
		STATUS_MSGS.put(Integer.valueOf(204), "No Content");
		STATUS_MSGS.put(Integer.valueOf(205), "Reset Content");
		STATUS_MSGS.put(Integer.valueOf(206), "Partial Content");
		STATUS_MSGS.put(Integer.valueOf(207), "Multistatus");
		STATUS_MSGS.put(Integer.valueOf(300), "Multiple Choices");
		STATUS_MSGS.put(Integer.valueOf(301), "Moved Permanently");
		STATUS_MSGS.put(Integer.valueOf(302), "Found");
		STATUS_MSGS.put(Integer.valueOf(303), "See Other");
		STATUS_MSGS.put(Integer.valueOf(304), "Not Modified");
		STATUS_MSGS.put(Integer.valueOf(305), "Use Proxy");
		STATUS_MSGS.put(Integer.valueOf(306), "");
		STATUS_MSGS.put(Integer.valueOf(307), "Temporary Redirect");
		STATUS_MSGS.put(Integer.valueOf(400), "Bad Request");
		STATUS_MSGS.put(Integer.valueOf(401), "Unauthorized");
		STATUS_MSGS.put(Integer.valueOf(402), "Payment Required");
		STATUS_MSGS.put(Integer.valueOf(403), "Forbidden");
		STATUS_MSGS.put(Integer.valueOf(404), "Not Found");
		STATUS_MSGS.put(Integer.valueOf(405), "Method Not Allowed");
		STATUS_MSGS.put(Integer.valueOf(406), "Not Acceptable");
		STATUS_MSGS.put(Integer.valueOf(407), "Proxy Authentication Required");
		STATUS_MSGS.put(Integer.valueOf(408), "Request Timeout");
		STATUS_MSGS.put(Integer.valueOf(409), "Conflict");
		STATUS_MSGS.put(Integer.valueOf(410), "Gone");
		STATUS_MSGS.put(Integer.valueOf(411), "Length Required");
		STATUS_MSGS.put(Integer.valueOf(412), "Precondition Failed");
		STATUS_MSGS.put(Integer.valueOf(413), "Request Entity Too Large");
		STATUS_MSGS.put(Integer.valueOf(414), "Request-URI Too Long");
		STATUS_MSGS.put(Integer.valueOf(415), "Unsupported Media Type");
		STATUS_MSGS.put(Integer.valueOf(416), "Requested Range Not Satisfiable");
		STATUS_MSGS.put(Integer.valueOf(417), "Expectation Failed");
		STATUS_MSGS.put(Integer.valueOf(500), "Internal Server Error");
		STATUS_MSGS.put(Integer.valueOf(501), "Not Implemented");
		STATUS_MSGS.put(Integer.valueOf(502), "Bad Gateway");
		STATUS_MSGS.put(Integer.valueOf(503), "Service Unavailable");
		STATUS_MSGS.put(Integer.valueOf(504), "Gateway Timeout");
		STATUS_MSGS.put(Integer.valueOf(505), "HTTP Version Not Supported");
		/*
		 * Status descriptions
		 */
		STATUS_DESC = new HashMap<Integer, String>();
		STATUS_DESC.put(Integer.valueOf(404), "The requested URL %s was not found on this server.");
		STATUS_DESC
				.put(
						Integer.valueOf(503),
						"The server is temporarily unable to service your request due to maintenance downtime or capacity problems. Please try again later.");
		/*
		 * Date Format
		 */
		HEADER_DATE_FORMAT = new SimpleDateFormat("EEE',' dd MMMM yyyy hh:mm:ss z");
		final DateFormatSymbols dfs = HEADER_DATE_FORMAT.getDateFormatSymbols();
		final String[] shortWeekdays = new String[8];
		shortWeekdays[Calendar.SUNDAY] = "Sun";
		shortWeekdays[Calendar.MONDAY] = "Mon";
		shortWeekdays[Calendar.TUESDAY] = "Tue";
		shortWeekdays[Calendar.WEDNESDAY] = "Wed";
		shortWeekdays[Calendar.THURSDAY] = "Thu";
		shortWeekdays[Calendar.FRIDAY] = "Fri";
		shortWeekdays[Calendar.SATURDAY] = "Sat";
		dfs.setShortWeekdays(shortWeekdays);
		final String[] shortMonths = new String[12];
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
		HEADER_DATE_FORMAT.setDateFormatSymbols(dfs);
		HEADER_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	private final Set<Cookie> cookies = new HashSet<Cookie>();

	private String statusMsg;

	private final HttpServletRequestWrapper request;

	byte errormessage[];

	public HttpServletResponseWrapper(final HttpServletRequestWrapper request) {
		super();
		status = HttpServletResponse.SC_OK;
		this.request = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(final String url) {
		return encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(final String name) {
		return headers.containsKey(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.tools.servlet.ServletResponseWrapper#reset()
	 */
	@Override
	public void reset() {
		super.reset();
		this.cookies.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(final String url) {
		/*
		 * Retrieve groupware session, if user is logged in
		 */
		final Session groupwareSession = (Session) request.getAttribute(SessionServlet.SESSION_KEY);
		/*
		 * Check for HTTP session: First look for JSESSIONID cookie, if none
		 * found check if HTTP session was created.
		 */
		boolean foundInCookie = false;
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length && !foundInCookie; i++) {
				if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(cookies[i].getName())) {
					foundInCookie = true;
				}
			}
		}
		final HttpSession httpSession;
		if (foundInCookie) {
			/*
			 * Set to null, cause obviously cookies are used
			 */
			httpSession = null;
		} else {
			httpSession = request.getSession(false);
		}
		return appendSessionID(url, groupwareSession == null ? null : groupwareSession.getSecret(),
				httpSession == null ? null : httpSession.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(final String url) {
		return encodeURL(url);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(final String url) {
		return encodeURL(url);
	}

	private static final String appendSessionID(final String url, final String groupwareSessionId,
			final String httpSessionId) {
		if (url == null) {
			return null;
		} else if (groupwareSessionId == null && httpSessionId == null) {
			return url.indexOf('?') == -1 ? new StringBuilder(url).append("?jvm=").append(AJPv13Config.getJvmRoute())
					.toString() : url;
		}
		String path = url;
		String query = "";
		String anchor = "";
		final int question = url.indexOf('?');
		if (question >= 0) {
			path = url.substring(0, question);
			query = url.substring(question + 1);
		}
		final int pound = path.indexOf('#');
		if (pound >= 0) {
			anchor = path.substring(pound);
			path = path.substring(0, pound);
		}
		final StringBuilder sb = new StringBuilder(path);
		if (httpSessionId != null && sb.length() > 0) {
			sb.append(AJPv13RequestHandler.JSESSIONID_URI);
			sb.append(httpSessionId);
		}
		sb.append(anchor);
		boolean first = true;
		if (groupwareSessionId != null) {
			sb.append('?').append(AJAXServlet.PARAMETER_SESSION).append('=');
			sb.append(groupwareSessionId);
			first = false;
		}
		if (query.length() > 0) {
			sb.append(first ? '?' : '&').append(query);
			first = false;
		}
		if (first) {
			sb.append("?jvm=").append(AJPv13Config.getJvmRoute());
		}
		return (sb.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String,
	 *      long)
	 */
	public void addDateHeader(final String name, final long l) {
		synchronized (HEADER_DATE_FORMAT) {
			addHeader(name, HEADER_DATE_FORMAT.format(new Date(l)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String,
	 *      int)
	 */
	public void addIntHeader(final String name, final int i) {
		addHeader(name, String.valueOf(i));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(final Cookie cookie) {
		cookies.add(cookie);
	}

	/**
	 * Removes specified cookie from cookie set
	 * 
	 * @param cookie
	 *            The cookie to remove
	 */
	public void removeCookie(final Cookie cookie) {
		cookies.remove(cookie);
	}

	/**
	 * Generates a two dimensional array of {@link String} containing the
	 * <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers of this HTTP
	 * response's cookies.
	 * <p>
	 * For each cookie its HTTP header format is generated and added to
	 * corresponding array of {@link String}
	 * 
	 * @return A two dimensional array of {@link String} containing the
	 *         <tt>Set-Cookie</tt>/<tt>Set-Cookie2</tt> headers
	 */
	public String[][] getFormatedCookies() {
		final String[][] retval = new String[1][];
		final int cookiesSize = cookies.size();
		final String[] list = new String[cookiesSize];
		if (cookiesSize > 0) {
			final Iterator<Cookie> iter = cookies.iterator();
			final StringBuilder composer = new StringBuilder(32);
			list[0] = getFormattedCookie(iter.next(), composer);
			for (int i = 1; i < cookiesSize; i++) {
				composer.setLength(0);
				list[i] = getFormattedCookie(iter.next(), composer);
			}
		}
		retval[0] = list;
		return retval;
	}

	private static final String[] COOKIE_PARAMS = { "; expires=", "; version=", "; path=", "; domain=", "; secure" };

	/**
	 * Gets the HTTP header format for specified instance of {@link Cookie}
	 * 
	 * @param cookie
	 *            The cookie whose HTTP header format shall be returned
	 * @param composer
	 *            A string builder used for composing
	 * @return A string representing the HTTP header format
	 */
	private static final String getFormattedCookie(final Cookie cookie, final StringBuilder composer) {
		composer.append(cookie.getName()).append('=');
		composer.append(cookie.getValue());
		if (cookie.getMaxAge() >= 0) {
			final Date d;
			if (cookie.getMaxAge() == 0) {
				d = new Date(10000L); // 10sec after 01/01/1970
			} else {
				d = new Date(System.currentTimeMillis() + (cookie.getMaxAge() * 1000L));
			}
			synchronized (HEADER_DATE_FORMAT) {
				/*
				 * expires=Sat, 01-Jan-2000 00:00:00 GMT
				 */
				composer.append(COOKIE_PARAMS[0]).append(HEADER_DATE_FORMAT.format(d));
			}
			// composer.append("; max-age=").append(cookie.getMaxAge());
		}
		if (cookie.getVersion() > 0) {
			composer.append(COOKIE_PARAMS[1]).append(cookie.getVersion());
		}
		if (cookie.getPath() != null) {
			composer.append(COOKIE_PARAMS[2]).append(cookie.getPath());
		}
		if (cookie.getDomain() != null) {
			composer.append(COOKIE_PARAMS[3]).append(cookie.getDomain());
		}
		if (cookie.getSecure()) {
			composer.append(COOKIE_PARAMS[4]);
		}
		return composer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String,
	 *      java.lang.String)
	 */
	public void addHeader(final String name, final String value) {
		if (!headers.containsKey(name)) {
			headers.put(name, new String[] { value });
			return;
		}
		final String[] tmp = headers.get(name);
		final String[] val = new String[tmp.length + 1];
		System.arraycopy(tmp, 0, val, 0, tmp.length);
		val[val.length - 1] = value;
		headers.put(name, val);
	}

	public int getStatus() {
		return status;
	}

	public String getStatusMsg() {
		// System.out.println("STATUS: " + status + " - " + (statusMsg != null ?
		// statusMsg : statusMsgs.get(new Integer(status))));
		return statusMsg != null ? statusMsg : STATUS_MSGS.get(Integer.valueOf(status));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(final int status) {
		this.status = status;
		this.statusMsg = STATUS_MSGS.get(Integer.valueOf(status));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int,
	 *      java.lang.String)
	 */
	public void setStatus(final int status, final String statusMsg) {
		this.status = status;
		this.statusMsg = statusMsg != null ? statusMsg : STATUS_MSGS.get(Integer.valueOf(status));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String,
	 *      long)
	 */
	public void setDateHeader(final String name, final long l) {
		synchronized (HEADER_DATE_FORMAT) {
			setHeader(name, HEADER_DATE_FORMAT.format(new Date(l)));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String,
	 *      int)
	 */
	public void setIntHeader(final String name, final int i) {
		setHeader(name, String.valueOf(i));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String,
	 *      java.lang.String)
	 */
	public final void setHeader(final String name, final String value) {
		if (value == null) {
			/*
			 * Treat as a remove
			 */
			headers.remove(name);
			return;
		}
		headers.put(name, new String[] { value });
	}

	public final int getHeadersSize() {
		return headers.size();
	}

	public final Iterator<String> getHeaderNames() {
		return headers.keySet().iterator();
	}

	public final Set<Map.Entry<String, String[]>> getHeaderEntrySet() {
		return headers.entrySet();
	}

	public Enumeration<?> getHeaders(final String name) {
		return makeEnumeration(headers.get(name));
	}

	public final String getHeader(final String name) {
		if (!containsHeader(name)) {
			return null;
		}
		final StringBuilder retval = new StringBuilder(150);
		final String[] sa = headers.get(name);
		retval.append(sa[0]);
		for (int i = 1; i < sa.length; i++) {
			retval.append(',');
			retval.append(sa[i]);
		}
		return retval.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public final void sendRedirect(final String location) {
		status = HttpServletResponse.SC_MOVED_TEMPORARILY;
		statusMsg = STATUS_MSGS.get(Integer.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
		addHeader("Location", location);
	}

	private static final String ERR_DESC_NOT_AVAILABLE = "[no description available]";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int,
	 *      java.lang.String)
	 */
	public final void sendError(final int status, final String statusMsg) throws IOException {
		this.status = status;
		this.statusMsg = statusMsg != null ? statusMsg : STATUS_MSGS.get(Integer.valueOf(status));
		if (errormessage == null) {
			String desc = STATUS_DESC.containsKey(Integer.valueOf(this.status)) ? STATUS_DESC.get(Integer
					.valueOf(this.status)) : ERR_DESC_NOT_AVAILABLE;
			if (HttpServletResponse.SC_NOT_FOUND == status) {
				desc = String.format(desc, request.getServletPath());
			}
			String errorMsgStr = ERROR_PAGE_TEMPL;
			errorMsgStr = errorMsgStr.replaceAll("#STATUS_CODE#", String.valueOf(this.status)).replaceAll(
					"#STATUS_MSG#", this.statusMsg).replaceFirst("#STATUS_DESC#", desc).replaceFirst("#DATE#",
					HEADER_DATE_FORMAT.format(new Date(System.currentTimeMillis()))).replaceFirst("#VERSION#",
					Version.VERSION_STRING);
			setContentType(new StringBuilder("text/html; charset=").append(getCharacterEncoding()).toString());
			errormessage = errorMsgStr.getBytes(getCharacterEncoding());
			setContentLength(errormessage.length);
			oxOutputStream.write(errormessage);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(final int status) throws IOException {
		sendError(status, STATUS_MSGS.get(Integer.valueOf(status)));
	}

	private Enumeration<?> makeEnumeration(final Object obj) {
		final Class<?> type = obj.getClass();
		if (!type.isArray()) {
			throw new IllegalArgumentException(obj.getClass().toString());
		}
		return (new Enumeration<Object>() {
			int size = Array.getLength(obj);

			int cursor;

			public boolean hasMoreElements() {
				return (cursor < size);
			}

			public Object nextElement() {
				return Array.get(obj, cursor++);
			}
		});
	}

}
