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

package com.openexchange.ajax;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.UploadListener;
import com.openexchange.groupware.upload.UploadRegistry;
import com.openexchange.groupware.upload.UploadException.UploadCode;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.UploadServletException;

public abstract class AJAXServlet extends HttpServlet implements UploadRegistry {

	protected static final transient List<UploadListener> registeredUploadListeners = new ArrayList<UploadListener>();

	private static final transient Log LOG = LogFactory.getLog(AJAXServlet.class);

	// Modules
	public static final String MODULE_TASK = "tasks";

	public static final String MODULE_CALENDAR = "calendar";

	public static final String MODULE_CONTACT = "contacts";
	
	public static final String MODULE_UNBOUND = "unbound";

	public static final String MODULE_MAIL = "mail";

	public static final String MODULE_PROJECT = "projects";

	public static final String MODULE_INFOSTORE = "infostore";

	public static final String MODULE_SYSTEM = "system";

	// Action Values
	public static final String ACTION_APPEND = "append";

	public static final String ACTION_NEW = "new";
	
	public static final String ACTION_UPLOAD = "upload";

	public static final String ACTION_UPDATE = "update";

	public static final String ACTION_ERROR = "error";

	public static final String ACTION_UPDATES = "updates";

	public static final String ACTION_DELETE = "delete";

	public static final String ACTION_CONFIRM = "confirm";

	public static final String ACTION_LIST = "list";

	public static final String ACTION_RANGE = "range";

	public static final String ACTION_VIEW = "view";

	public static final String ACTION_SEARCH = "search";
	
	public static final String ACTION_NEW_APPOINTMENTS = "newappointments";

	public static final String ACTION_SEND = "send";

	public static final String ACTION_GET = "get";

	public static final String ACTION_IMAGE = "image";

	public static final String ACTION_REPLY = "reply";

	public static final String ACTION_REPLYALL = "replyall";

	public static final String ACTION_FORWARD = "forward";

	public static final String ACTION_MATTACH = "attachment";

	public static final String ACTION_MAIL_RECEIPT_ACK = "receipt_ack";

	public static final String ACTION_NEW_MSGS = "newmsgs";

	public static final String ACTION_COUNT = "count";

	public static final String ACTION_ROOT = "root";

	public static final String ACTION_ALL = "all";

	public static final String ACTION_HAS = "has";

	public static final String ACTION_FREEBUSY = "freebusy";

	protected static final String ACTION_GROUPS = "groups";

	public static final String ACTION_VERSIONS = "versions";

	public static final String ACTION_PATH = "path";

	public static final String ACTION_DOCUMENT = "document";

	public static final String ACTION_DETACH = "detach";

	protected static final String ACTION_ATTACH = "attach";

	public static final String ACTION_REVERT = "revert";

	public static final String ACTION_COPY = "copy";

	public static final String ACTION_LOCK = "lock";

	public static final String ACTION_UNLOCK = "unlock";

	public static final String ACTION_SAVE_AS = "saveAs";

	public static final String ACTION_LOGIN = "login";

	public static final String ACTION_LOGOUT = "logout";

	public static final String ACTION_REDIRECT = "redirect";

	public static final String ACTION_AUTOLOGIN = "autologin";

	public static final String ACTION_SAVE_VERSIT = "saveVersit";
	
	public static final String ACTION_CLEAR = "clear";
	
	public static final String ACTION_KEEPALIVE = "keepalive";

	/**
	 * The parameter 'from' specifies index of starting entry in list of objects
	 * dependent on given order criteria and folder id
	 */
	public static final String PARAMETER_FROM = "from";

	/**
	 * The parameter 'to' specifies the index of excluding ending entry in list
	 * of objects dependent on given order criteria and folder id
	 */
	public static final String PARAMETER_TO = "to";

	public static final String PARAMETER_START = "start";

	public static final String PARAMETER_END = "end";

	/**
	 * The parameter 'id' indicates the id of a certain objects from which
	 * certain informations must be returned to client
	 */
	public static final String PARAMETER_ID = "id";

	public static final String PARAMETER_ATTACHEDID = "attached";

	/**
	 * The parameter 'session' represents the id of current active user session
	 */
	public static final String PARAMETER_SESSION = "session";

	public static final String PARAMETER_DATA = "data";

	/**
	 * The parameter 'folderid' indicates the current active folder of user
	 */
	public static final String PARAMETER_FOLDERID = "folder";

	public static final String PARAMETER_INFOLDER = "folder";

	public static final String PARAMETER_MODULE = "module";

	/**
	 * The parameter 'sort' specifies the field which is used as order source
	 * and can be compared to SQL'S 'Order By' statement
	 */
	public static final String PARAMETER_SORT = "sort";

	/**
	 * The parameter 'dir' specifies the order direction: ASC (ascending) vs.
	 * DESC (descending)
	 */
	public static final String PARAMETER_ORDER = "order";
	
	public static final String PARAMETER_HARDDELETE = "harddelete";

	/**
	 * The parameter 'delete' indicates to perform a delete operation of an
	 * object whose id matches parameter's value: delete=12345
	 */
	public static final String PARAMETER_DELETE = "delete";

	public static final String PARAMETER_CONFIRM = "confirm";

	public static final String PARAMETER_CONFIRM_MESSAGE = "confirm_message";

	public static final String PARAMETER_ACTION = "action";

	/**
	 * The parameter 'columns' delivers a comma-sparated list of numbers which
	 * encode the fields of a certain object (Mail, Task, Appointment, etc.)
	 * that should be transfered to client
	 */
	public static final String PARAMETER_COLUMNS = "columns";

	/**
	 * The parameter 'timestamp' delivers the client-stored, last-changed
	 * timestamp of the considered object (folder, task, contact, appointment,
	 * ...). This parameter works as an anchor which lets the server determine
	 * an arising conflict when perfoming a client-initiated modification or
	 * lets the server restrict its object query for a client request.
	 */
	public static final String PARAMETER_TIMESTAMP_SINCE = "since";

	public static final String PARAMETER_SEARCHPATTERN = "pattern";

	public static final String PARAMETER_GROUPID = "group";

	public static final String PARAMETER_TIMESTAMP = "timestamp";

	public static final String PARAMETER_VERSION = "version";

	public static final String UPLOAD_FORMFIELD_MAIL = "json_0";

	public static final String PARAMETER_IGNORE = "ignore";

	public static final String PARAMETER_ALL = "all";
	
	public static final String PARAMETER_REPLY2ALL = "reply2all";

	public static final String PARAMETER_ATTACHMENT = "attachment";

	public static final String PARAMETER_JSON = "json";

	public static final String PARAMETER_FILE = "file";

	public static final String PARAMETER_CONTENT_TYPE = "content_type";
	
	public static final String PARAMETER_LIMIT = "limit";
	
	public static final String PARAMETER_TYPE = "type";

	/**
	 * The content type if the response body contains javascript data. Set it
	 * with <code>resp.setContentType(AJAXServlet.CONTENTTYPE_JAVASCRIPT)</code>.
	 */
	public static final String CONTENTTYPE_JAVASCRIPT = "text/javascript; charset=UTF-8";

    /**
     * The content type if the reponse body contains the html page include the
     * response for uploads.
     */
    public static final String CONTENTTYPE_HTML = "text/html; charset=UTF-8";
    
	/**
	 * This is the error message if the session identifier isn't sent by the
	 * client or the session timed out or no session can be found for the given
	 * session identifier. The HTTP error code must be SC_UNAUTHORIZED.
	 */
	protected static final String ERROR_SESSION_NOT_FOUND = "No valid session found.";

	private static final String STR_EMPTY = "";
	
	private static final String STR_ERROR = "error";

	private static final String STR_ERROR_PARAMS = "error_params";

	private static final transient Lock LOCK = new ReentrantLock();

	// Javascript

	public static final String JS_FRAGMENT = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
			+ "\"http://www.w3.org/TR/html4/strict.dtd\"> " + "<html> " + "<head>"
			+ "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> "
			+ "<script type=\"text/javascript\"> " + "callback (**json**);" + "function callback(arg) " + "{ "
			+ "  parent.callback_**action**(arg); " + '}' + "</script> " + "</head>" + "</html> ";

	protected static final String JS_FRAGMENT_POPUP = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\""
		+ "\"http://www.w3.org/TR/html4/strict.dtd\"> " + "<html> " + "<head>"
		+ "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"> "
		+ "<script type=\"text/javascript\"> " + "callback (**json**);" + "function callback(arg) " + "{ "
		+ "  window.opener.callback_**action**(arg); " + '}' + "</script> " + "</head>" + "</html> ";

	protected static final String SAVE_AS_TYPE = "application/octet-stream";

	public static final String JS_FRAGMENT_JSON = "\\*\\*json\\*\\*";

	public static final String JS_FRAGMENT_ACTION = "\\*\\*action\\*\\*";

	protected static final String _doGet = "doGet";

	protected static final String _doPut = "doPut";

	/**
	 * Error message if writing the response fails.
	 */
	protected static final String RESPONSE_ERROR = "Error while writing response object.";

	protected static void writeString(final Writer w, final String s) throws IOException {
		if (s == null) {
			w.write("null");
			return;
		}
		w.write('"');
		//w.write("\"");
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			final char c = s.charAt(i);
			if (c < ' ' || c > 126 || c == '"' || c == '<' || c == '>' || c == '&') {
				w.write("&#x");
				final String hex = Integer.toHexString(/*(int) */c & 0xffff);
				w.write(hex.toUpperCase(Locale.ENGLISH));
				w.write(';');
			} else {
				w.write(c);
			}
		}
		w.write('"');
		//w.write("\"");
	}

	public static boolean containsParameter(final HttpServletRequest req, final String name) {
		return (req.getParameter(name) != null);
	}

	/**
	 * Parses a nonnegative integer URI parameter.
	 * 
	 * @param req
	 *            The HttpServletRequest object which contains the parameter.
	 * @param name
	 *            The name of the parameter to parse.
	 * @return A negative value if the parameter is missing or invalid, the
	 *         parameter value otherwise.
	 */
	public static int getIntParameter(final HttpServletRequest req, final String name) {
		final String value_str = req.getParameter(name);
		if (value_str == null) {
			return -1;
		}
		try {
			return Integer.parseInt(value_str);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public static long getLongParameter(final HttpServletRequest req, final String name) throws OXConflictException {
		try {
			return Long.parseLong(req.getParameter(name));
		} catch (NumberFormatException ex) {
			throw new OXConflictException(new AjaxException(AjaxException.Code.InvalidParameter, ex, name));
		}
	}

	public static float getFloatParameter(final HttpServletRequest req, final String name) throws OXConflictException {
		try {
			return Float.parseFloat(req.getParameter(name));
		} catch (NumberFormatException ex) {
			throw new OXConflictException(new AjaxException(AjaxException.Code.InvalidParameter, ex, name));
		}
	}

	public static boolean getBooleanParameter(final HttpServletRequest req, final String name) {
		return Boolean.getBoolean(req.getParameter(name));
	}

	public static Date getDateParameter(final HttpServletRequest req, final String name) throws OXConflictException {
		try {
			return new Date(Long.parseLong(req.getParameter(name)));
		} catch (NumberFormatException ex) {
			throw new OXConflictException(new AjaxException(AjaxException.Code.InvalidParameter, ex, name));
		}
	}

	protected long getDateValue(final Date d, final User userObj) {
		long retval = d.getTime();
		final Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(d);
		if (cal.get(Calendar.HOUR_OF_DAY) != 0 || cal.get(Calendar.MINUTE) != 0) {
			retval += (TimeZone.getTimeZone(userObj.getTimeZone()).getOffset(retval));
		}
		return retval;
	}

	protected boolean getListOfObjectsFromToInFolder(final HttpServletRequest req) {
		return (req.getParameter(PARAMETER_FOLDERID) != null && req.getParameter(PARAMETER_FROM) != null && req
				.getParameter(PARAMETER_TO) != null);
	}

	protected boolean getObjectInfo(final HttpServletRequest req) {
		return (req.getParameter(PARAMETER_ID) != null && req.getParameter(PARAMETER_FROM) == null && req
				.getParameter(PARAMETER_TO) == null);
	}

	protected boolean deleteObject(final HttpServletRequest req) {
		return req.getParameter(PARAMETER_ACTION) != null
				&& req.getParameter(PARAMETER_ACTION).equalsIgnoreCase(ACTION_DELETE);
	}

	protected boolean confirmObject(final HttpServletRequest req) {
		return (req.getParameter(PARAMETER_CONFIRM) != null);
	}

	public boolean saveObject(final HttpServletRequest req) throws Exception {
		if (req.getParameter(AJAXServlet.PARAMETER_ID) != null
				&& req.getParameter(AJAXServlet.PARAMETER_FOLDERID) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the complete body as a string. Be carefull when getting big
	 * request bodies.
	 * 
	 * @param req
	 *            http servlet request.
	 * @return a string with the complete body.
	 * @throws IOException
	 *             if an error occurs while reading the body.
	 */
	public static String getBody(final HttpServletRequest req) throws IOException {
		InputStreamReader isr = null;
		try {
			int count = 0;
			final char[] c = new char[8192];
			final String charset = null == req.getCharacterEncoding() ? ServerConfig
					.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
			isr = new InputStreamReader(req.getInputStream(), charset);
			if ((count = isr.read(c)) > 0) {
				final StringBuilder sb = new StringBuilder(16384);
				do {
					sb.append(c, 0, count);
				} while ((count = isr.read(c)) > 0);
				return sb.toString();
			}
			return STR_EMPTY;
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Should never occur
			 */
			LOG.error("Unsupported encoding in request", e);
			return STR_EMPTY;
		} finally {
			if (null != isr) {
				try {
					isr.close();
				} catch (final IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
		/*final InputStream input = req.getInputStream();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(input.available());
		final byte[] buf = new byte[8192];
		int length = -1;
		while ((length = input.read(buf)) != -1) {
			baos.write(buf, 0, length);
		}
		String characterEncoding = req.getCharacterEncoding();
		if (null == characterEncoding) {
			characterEncoding = ServerConfig.getProperty(Property.DefaultEncoding);//"UTF-8";
		}
		return new String(baos.toByteArray(), characterEncoding);*/
	}

	/**
	 * Returns user's timezone that ought to be used to determine offset to a
	 * <code>java.util.Date</code> instance which has its timezone set to UTC.
	 * 
	 * @param session
	 * @return user's timezone
	 */
	protected TimeZone getUserTimeZone(final SessionObject session) {
		return TimeZone.getTimeZone(session.getUserObject().getTimeZone());
	}

	/**
	 * Returns a <code>long</code> value with added time zone offset
	 * 
	 * @param d -
	 *            the <code>java.util.Date</code> in GMT
	 * @param session
	 * @return a <code>long</code> value with added time zone offset
	 */
	protected long sendTime(final Date d, final SessionObject session) {
		return d.getTime() + getUserTimeZone(session).getOffset(d.getTime());
	}

	/**
	 * Returns a <code>java.util.Date</code> instance with subtracted time
	 * zone offset
	 * 
	 * @param time -
	 *            number of milliseconds in local timezone
	 * @param session
	 * @return a <code>java.util.Date</code> instance with subtracted time
	 *         zone offset
	 */
	protected Date parseTime(final long time, final SessionObject session) {
		return new Date(time - getUserTimeZone(session).getOffset(time));
	}

	/**
	 * Returns the URI part after path to the servlet.
	 * 
	 * @param req
	 *            the request that url should be parsed
	 * @return the URI part after the path to your servlet.
	 */
	protected static String getServletSpecificURI(final HttpServletRequest req) {
		String uri;
		try {
			uri = URLDecoder.decode(req.getRequestURI(), req.getCharacterEncoding() == null ? ServerConfig
					.getProperty(ServerConfig.Property.DefaultEncoding) : req.getCharacterEncoding());
		} catch (final UnsupportedEncodingException e) {
			LOG.error("Unsupported encoding", e);
			uri = req.getRequestURI();
		}
		final String path = req.getContextPath() + req.getServletPath();
		final int pos = uri.indexOf(path);
		if (pos != -1) {
			uri = uri.substring(pos + path.length());
		}
		return uri;
	}

	protected JSONObject getResponseObject(final Object data, final Timestamp serverTimestamp, final String errorMsg,
			final String[] errorParams) throws JSONException {
		final JSONObject retval = new JSONObject();
		retval.put("data", data);
		if (serverTimestamp != null) {
			retval.put("timestamp", serverTimestamp.getTime());
		}
		retval.put(STR_ERROR, errorMsg == null || errorMsg.length() == 0 ? JSONObject.NULL : errorMsg);
		if (errorParams == null || errorParams.length == 0) {
			retval.put(STR_ERROR_PARAMS, JSONObject.NULL);
		} else {
			final JSONArray arr = new JSONArray();
			for (int i = 0; i < errorParams.length; i++) {
				arr.put(errorParams[i]);
			}
			retval.put(STR_ERROR_PARAMS, arr);
		}
		return retval;
	}

	protected static String getAction(final HttpServletRequest req) throws OXConflictException {
		final String action = req.getParameter(PARAMETER_ACTION);

		if (action != null) {
			return action;
		}
		throw new OXConflictException(new AjaxException(AjaxException.Code.MissingParameter, PARAMETER_ACTION));
	}

	/**
	 * This method sends the given error message as a java script error object
	 * to the client.
	 * 
	 * @param resp
	 *            This response will be used to send the java script error
	 *            object.
	 * @param errorMessage
	 *            The error message to send to the client.
	 * @throws IOException
	 *             if writing to the response fails.
	 * @throws ServletException
	 *             if the creation of the java script error object fails.
	 */
	protected static void sendErrorAsJS(final HttpServletResponse resp, final String errorMessage) throws IOException,
			ServletException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		try {
			final PrintWriter w = resp.getWriter();
			final JSONWriter jw = new JSONWriter(w);
			jw.object();
			jw.key(STR_ERROR);
			jw.value(errorMessage);
			jw.endObject();
			w.flush();
		} catch (JSONException e1) {
			throw new ServletException("Cannot create JSON object.", e1);
		}
	}

	protected static void sendError(final HttpServletResponse resp) throws IOException {
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	protected static void sendErrorAsJS(final PrintWriter w, final String errorMessage) throws ServletException {
		try {
			final JSONWriter jw = new JSONWriter(w);
			jw.object();
			jw.key(STR_ERROR);
			jw.value(errorMessage);
			jw.endObject();
			w.flush();
		} catch (JSONException e1) {
			throw new ServletException("Cannot create JSON object.", e1);
		}
	}

	/**
	 * @deprecated
	 */
	protected static void sendErrorAsJSHTML(final HttpServletResponse res, final String error, final String action)
			throws IOException {
		res.setContentType("text/html");
		PrintWriter w = null;
		try {
			w = res.getWriter();
			final JSONObject obj = new JSONObject();
			obj.put(STR_ERROR, error);
			obj.put(STR_ERROR_PARAMS, Collections.emptyList());
			w.write(substitute(JS_FRAGMENT, "json", obj.toString(), "action", action));
		} catch (JSONException e) {
			LOG.error(e);
		} finally {
			close(w);
		}
	}

	protected void unknownColumn(final HttpServletResponse res, final String parameter, final String columnId,
			final boolean html, final String action) throws IOException, ServletException {
		final String msg = "Unknown column in " + parameter + " :" + columnId;
		if (html) {
			sendErrorAsJSHTML(res, msg, action);
			return;
		}
		sendErrorAsJS(res, msg);
	}

	protected void invalidParameter(final HttpServletResponse res, final String parameter, final String value,
			final boolean html, final String action) throws IOException, ServletException {
		final String msg = "Invalid Parameter " + parameter + " :" + value;
		if (html) {
			sendErrorAsJSHTML(res, msg, action);
			return;
		}
		sendErrorAsJS(res, msg);
	}

	protected void missingParameter(final String parameter, final HttpServletResponse res, final boolean html,
			final String action) throws IOException, ServletException {
		final String msg = "Missing Parameter: " + parameter;
		if (html) {
			sendErrorAsJSHTML(res, msg, action);
			return;
		}
		sendErrorAsJS(res, msg);
	}

	protected void unknownAction(final String method, final String action, final HttpServletResponse res,
			final boolean html) throws IOException, ServletException {
		final String msg = "The action " + action + " isn't even specified yet. At least not for the method: " + method;
		if (html) {
			sendErrorAsJSHTML(res, msg, action);
			return;
		}
		sendErrorAsJS(res, msg);
	}

	public static String substitute(final String text, final String... substitutions) {
		String s = text;
		assert (substitutions.length % 2 == 0);
		for (int i = 0; i < substitutions.length; i++) {
			final String key = substitutions[i];
			String value = substitutions[++i];
			value = value.replaceAll("\\\\", "\\\\\\\\"); //fix for 7583: replaces \ with \\, because \\ is later replaced with \ in String.replaceAll()
			value = value.replaceAll("\\$", "\\\\\\$"); // Escape-O-Rama. Turn
														// all $ in \$
			s = s.replaceAll("\\*\\*" + key + "\\*\\*", value);
		}
		return s;
	}

	public static int[] intArrayCopy(final String s[]) {
		int i[] = new int[s.length];

		for (int a = 0; a < i.length; a++) {
			i[a] = Integer.parseInt(s[a]);
		}

		return i;
	}

	/* --------------------- STUFF FOR UPLOAD --------------------- */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadRegistry#processUpload(javax.servlet.http.HttpServletRequest)
	 */
	public UploadEvent processUpload(final HttpServletRequest req) throws UploadException {
		return processUploadStatic(req);
	}

	@SuppressWarnings("unchecked")
	public static final UploadEvent processUploadStatic(final HttpServletRequest req) throws UploadException {
		final boolean isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(req));
		if (isMultipart) {
			/*
			 * Create the upload event
			 */
			final UploadEvent uploadEvent = new UploadEvent();
			final DiskFileItemFactory factory = new DiskFileItemFactory();
			/*
			 * Set factory constraints
			 */
			factory.setSizeThreshold(0);
			factory.setRepository(new File(ServerConfig.getProperty(Property.UploadDirectory)));
			/*
			 * Create a new file upload handler
			 */
			final ServletFileUpload upload = new ServletFileUpload(factory);
			/*
			 * Set overall request size constraint
			 */
			upload.setSizeMax(-1);
			List<FileItem> items;
			String action;
			try {
				action = getAction(req);
			} catch (final OXConflictException e) {
				throw new UploadException(UploadCode.UPLOAD_FAILED, null, e);
			}
			try {
				items = upload.parseRequest(req);
			} catch (final FileUploadException e) {
				throw new UploadException(UploadCode.UPLOAD_FAILED, action, e);
			}
			if (action != null
					&& (action.equals(ACTION_NEW) || action.equals(ACTION_UPLOAD) || action.equals(ACTION_APPEND)
							|| action.equals(ACTION_UPDATE) || action.equals(ACTION_ATTACH)
							|| action.equals(ACTION_COPY) || com.openexchange.groupware.importexport.Format
							.containsConstantName(action))) {
				uploadEvent.setAction(action);
				/*
				 * Set affiliation to mail upload
				 */
				uploadEvent.setAffiliationId(UploadEvent.MAIL_UPLOAD);
				/*
				 * Fill upload event instance
				 */
				final int size = items.size();
				final String charEnc = req.getCharacterEncoding() == null ? ServerConfig
						.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
				NextFileItem: for (int i = 0; i < size; i++) {
					final FileItem fileItem = items.get(i);
					if (fileItem.isFormField()) {
						final String fieldName = fileItem.getFieldName();
						try {
							uploadEvent.addFormField(fieldName, fileItem.getString(charEnc));
						} catch (final UnsupportedEncodingException e) {
							throw new UploadException(UploadCode.UPLOAD_FAILED, action, e);
						}
					} else {
						if (fileItem.getSize() == 0 && (fileItem.getName() == null || fileItem.getName().length() == 0)) {
							continue NextFileItem;
						}
						try {
							uploadEvent.addUploadFile(processUploadedFile(fileItem, ServerConfig
									.getProperty(Property.UploadDirectory)));
						} catch (final Exception e) {
							throw new UploadException(UploadCode.UPLOAD_FAILED, action, e);
						}
					}
				}
				if (uploadEvent.getAffiliationId() < 0) {
					throw new UploadException(UploadCode.MISSING_AFFILIATION_ID, action);
				}
				return uploadEvent;
			}
			throw new UploadException(UploadCode.UNKNOWN_ACTION_VALUE, null, action);
		}
		throw new UploadException(UploadCode.NO_MULTIPART_CONTENT, null);
	}

	private static final UploadFile processUploadedFile(final FileItem item, final String uploadDir) throws Exception {
		try {
			final UploadFile retval = new UploadFile();
			retval.setFieldName(item.getFieldName());
			retval.setFileName(item.getName());
			retval.setContentType(item.getContentType());
			retval.setSize(item.getSize());
			final File tmpFile = File.createTempFile("openexchange", null, new File(uploadDir));
			tmpFile.deleteOnExit();
			item.write(tmpFile);
			retval.setTmpFile(tmpFile);
			return retval;
		} finally {
			item.delete();
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadRegistry#addUploadListener(com.openexchange.groupware.upload.UploadListener)
	 */
	public void addUploadListener(final UploadListener uploadListener) {
		if (!containsUploadListener(uploadListener)) {
			registeredUploadListeners.add(uploadListener);
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadRegistry#containsUploadListener(com.openexchange.groupware.upload.UploadListener)
	 */
	public boolean containsUploadListener(final UploadListener uploadListener) {
		final int size = registeredUploadListeners.size();
		final Iterator<UploadListener> iter = registeredUploadListeners.iterator();
		for (int i = 0; i < size; i++) {
			final UploadListener ul = iter.next();
			if (ul.getClass().equals(uploadListener.getClass())) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * 
	 * @see com.openexchange.groupware.upload.UploadRegistry#fireUploadEvent(com.openexchange.groupware.upload.UploadEvent)
	 */
	public void fireUploadEvent(final UploadEvent uploadEvent) throws UploadServletException {
		try {
			final int size = registeredUploadListeners.size();
			final Iterator<UploadListener> iter = registeredUploadListeners.iterator();
			final StringBuilder errBuilder = new StringBuilder(100);
			for (int i = 0; i < size; i++) {
				final UploadListener uploadListener = iter.next();
				try {
					uploadListener.action(uploadEvent);
				} catch (OXException e) {
					LOG.error(errBuilder.append("ERROR: Upload method \"action()\" failed for UploadListener ")
							.append(uploadListener.getClass()), e);
					errBuilder.setLength(0);
				}
			}
		} finally {
			uploadEvent.cleanUp();
		}
	}

	public static void startResponse(final JSONWriter jsonwriter) throws JSONException {
		jsonwriter.object();
		jsonwriter.key("data");
	}

	public static void endResponse(final JSONWriter jsonwriter, final Date timestamp, final String error)
			throws JSONException {
		if (timestamp != null) {
			jsonwriter.key("timestamp");
			jsonwriter.value(timestamp.getTime());
		}

		if (error != null) {
			jsonwriter.key(STR_ERROR);
			jsonwriter.value(error);
			jsonwriter.key(STR_ERROR_PARAMS);
			jsonwriter.value(new JSONArray());
		}

		jsonwriter.endObject();
	}

	public static Date getCurrentTimestamp() throws Exception {
		return new Date();
	}

	public static void registerUploadListener(final UploadListener ul) {
		LOCK.lock();
		try {
			registeredUploadListeners.add(ul);
		} finally {
			LOCK.unlock();
		}
	}

	protected boolean checkRequired(final HttpServletRequest req, final HttpServletResponse res, final boolean html,
			final String action, final String... parameters) throws IOException, ServletException {
		if (html) {
			res.setContentType("text/html; charset=UTF-8");
		}
		for (String param : parameters) {
			if (req.getParameter(param) == null) {
				missingParameter(param, res, html, action);
				return false;
			}
		}
		return true;
	}

	protected static void close(final Writer w) {
		if (LOG.isTraceEnabled()) {
			LOG.trace(new StringBuilder("Called close() with writer").append(w.toString()));
		}
		// return;
		/*
		 * if (w == null) { return; } try { w.flush(); //
		 * System.out.println("INFOSTORE: Flushed!"); } catch (IOException e) {
		 * LOG.error(e); } try { w.close(); // System.out.println("INFOSTORE:
		 * Closed!"); } catch (IOException e) { LOG.error(e); }
		 */
	}

	protected void writeResponse(final Response response,
        final HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(CONTENTTYPE_JAVASCRIPT);
		try {
			Response.write(response, httpServletResponse.getWriter());
		} catch (JSONException e) {
			log(RESPONSE_ERROR, e);
			sendError(httpServletResponse);
		}
	}
	
	protected final boolean isIE(final HttpServletRequest req){
		return req.getHeader("User-Agent").contains("MSIE");
	}
	
	protected final boolean isIE7(final HttpServletRequest req){
		return req.getHeader("User-Agent").contains("MSIE 7");
	}

	public static final String getModuleString(final int module, final int objectId) {
		String moduleStr = null;
		switch (module) {
		case FolderObject.TASK:
			moduleStr = MODULE_TASK;
			break;
		case FolderObject.CONTACT:
			moduleStr = MODULE_CONTACT;
			break;
		case FolderObject.CALENDAR:
			moduleStr = MODULE_CALENDAR;
			break;
		case FolderObject.UNBOUND:
			moduleStr = MODULE_UNBOUND;
			break;
		case FolderObject.MAIL:
			moduleStr = MODULE_MAIL;
			break;
		case FolderObject.PROJECT:
			moduleStr = MODULE_PROJECT;
			break;
		case FolderObject.INFOSTORE:
			moduleStr = MODULE_INFOSTORE;
			break;
		case FolderObject.SYSTEM_MODULE:
			if (objectId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
				moduleStr = MODULE_PROJECT;
			} else if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
				moduleStr = MODULE_INFOSTORE;
			} else {
				moduleStr = MODULE_SYSTEM;
			}
			break;
		default:
			moduleStr = "";
			break;
		}
		return moduleStr;
	}
	
	public static final int getModuleInteger(final String moduleStr) {
		final int module;
		if (MODULE_TASK.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.TASK;
		} else if (MODULE_CONTACT.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.CONTACT;
		} else if (MODULE_CALENDAR.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.CALENDAR;
		} else if (MODULE_UNBOUND.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.UNBOUND;
		} else if (MODULE_MAIL.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.MAIL;
		} else if (MODULE_PROJECT.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.PROJECT;
		} else if (MODULE_INFOSTORE.equalsIgnoreCase(moduleStr)) {
			module = FolderObject.INFOSTORE;
		} else {
			module = -1;
		}
		return module;
	}

}