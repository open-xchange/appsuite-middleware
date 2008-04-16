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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.exception.Classes;
import com.openexchange.sessiond.exception.SessionExceptionFactory;
import com.openexchange.sessiond.exception.SessiondException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Overriden service method that checks if a valid session can be found for the
 * request.
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.SESSION_SERVLET, component = EnumComponent.SESSION)
public abstract class SessionServlet extends AJAXServlet {

	/**
	 * Logger.
	 */
	private static transient final Log LOG = LogFactory
			.getLog(SessionServlet.class);

	/**
	 * Factory for creating exceptions.
	 */
	private static transient final SessionExceptionFactory EXCEPTION = new SessionExceptionFactory(
			SessionServlet.class);

	/**
	 * Name of the key to remember the session for the request.
	 */
	public static final String SESSION_KEY = "sessionObject";

	/**
	 * Default constructor.
	 */
	protected SessionServlet() {
		super();
	}

	/**
	 * Checks the session ID supplied as a query parameter in the request URI.
	 * {@inheritDoc}
	 */
	@Override
	@OXThrows(category = Category.TRY_AGAIN, desc = "", exceptionId = 4, msg = "Context is locked.")
	protected void service(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {
		Tools.disableCaching(resp);
		try {
			final String cookieId = getCookieId(req);
			final String sessionId = getSessionId(req, cookieId);
			final Session session = getSession(sessionId);
			checkIP(session.getLocalIp(), req.getRemoteAddr());
			rememberSession(req, session);
			final Context ctx = ContextStorage.getStorageContext(session.getContextId());
			if (!ctx.isEnabled()) {
				final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(
						SessiondService.class);
				sessiondCon.removeSession(sessionId);
				throw EXCEPTION.create(4);
			}
			super.service(req, resp);
		} catch (SessiondException e) {
			LOG.debug(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			resp.setContentType(CONTENTTYPE_JAVASCRIPT);
			Tools.deleteCookies(req, resp);
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
				writer.flush();
			} catch (JSONException e1) {
				log(RESPONSE_ERROR, e1);
				sendError(resp);
			}
		} catch (ContextException e) {
			LOG.debug(e.getMessage(), e);
			final Response response = new Response();
			response.setException(e);
			resp.setContentType(CONTENTTYPE_JAVASCRIPT);
			Tools.deleteCookies(req, resp);
			final PrintWriter writer = resp.getWriter();
			try {
				Response.write(response, writer);
				writer.flush();
			} catch (JSONException e1) {
				log(RESPONSE_ERROR, e1);
				sendError(resp);
			}
		}
	}

	/**
	 * Checks if the client IP address of the current request matches the one
	 * through that the session has been created.
	 * 
	 * @param remembered
	 *            IP address stored in the session object.
	 * @param actual
	 *            IP address of the current request.
	 * @throws SessionException
	 *             if the IP addresses don't match.
	 */
	@OXThrows(category = Category.PERMISSION, desc = "If a session exists every request is checked for its client IP "
			+ "address to match the one while creating the session.", exceptionId = 5, msg = "Wrong client IP address.")
	public static void checkIP(final String remembered, final String actual)
			throws SessiondException {
		if (null == actual || !actual.equals(remembered)) {
			throw EXCEPTION.create(5);
		}
	}

	/**
	 * Gets the cookie identifier from the request.
	 * 
	 * @param req
	 *            servlet request.
	 * @return the cookie identifier.
	 * @throws SessionException
	 *             if the cookie identifier can not be found.
	 */
	@OXThrows(category = Category.CODE_ERROR, desc = "Every AJAX request must contain a parameter named session "
			+ "that value contains the identifier of the session cookie.", exceptionId = 1, msg = "The session parameter is missing.")
	private static String getCookieId(final ServletRequest req)
			throws SessiondException {
		final String retval = req.getParameter(PARAMETER_SESSION);
		if (null == retval) {
			if (LOG.isDebugEnabled()) {
				final StringBuilder debug = new StringBuilder();
				debug.append("Parameter session not found: ");
				final Enumeration<?> enm = req.getParameterNames();
				while (enm.hasMoreElements()) {
					debug.append(enm.nextElement());
					debug.append(',');
				}
				if (debug.length() > 0) {
					debug.setCharAt(debug.length() - 1, '.');
				}
				LOG.debug(debug.toString());
			}
			throw EXCEPTION.create(1);
		}
		return retval;
	}

	/**
	 * Gets the session identifier from the request.
	 * 
	 * @param req
	 *            HTTP servlet request.
	 * @param cookieId
	 *            Identifier of the cookie.
	 * @return the session identifier.
	 * @throws SessionException
	 *             if the session identifier can not be found in a cookie.
	 */
	@OXThrows(category = Category.CODE_ERROR, desc = "Your browser does not send the cookie for identifying your "
			+ "session.", exceptionId = 2, msg = "The cookie with the session identifier is missing.")
	private static String getSessionId(final HttpServletRequest req,
			final String cookieId) throws SessiondException {
		final Cookie[] cookies = req.getCookies();
		String retval = null;
		if (cookies != null) {
			final String cookieName = Login.cookiePrefix + cookieId;
			for (int a = 0; a < cookies.length && retval == null; a++) {
				if (cookieName.equals(cookies[a].getName())) {
					retval = cookies[a].getValue();
				}
			}
		}
		if (null == retval) {
			if (LOG.isDebugEnabled() && cookies != null) {
				final StringBuilder debug = new StringBuilder();
				debug.append("No cookie for ID: ");
				debug.append(cookieId);
				debug.append(". Cookie names: ");
				for (Cookie cookie : cookies) {
					debug.append(cookie.getName());
					debug.append(',');
				}
				debug.setCharAt(debug.length() - 1, '.');
				LOG.debug(debug.toString());
			}
			throw EXCEPTION.create(2);
		}
		return retval;
	}

	/**
	 * Finds the session.
	 * 
	 * @param sessionId
	 *            identifier of the session.
	 * @return the session.
	 * @throws SessionException
	 *             if the session can not be found.
	 */
	@OXThrows(category = Category.TRY_AGAIN, desc = "A session with the given identifier can not be found.", exceptionId = 3, msg = "Your session %s expired. Please start a new browser session.")
	private static Session getSession(final String sessionId)
			throws SessiondException {
		final SessiondService sessiondCon = ServerServiceRegistry.getInstance().getService(
				SessiondService.class);
		if (sessiondCon == null) {
			throw new SessiondException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName()));
		}
		final Session retval = sessiondCon.getSession(sessionId);
		if (null == retval) {
			throw EXCEPTION.create(3, sessionId);
		}
        try {
        	final Context context = ContextStorage.getStorageContext(retval.getContextId());
            final User user = UserStorage.getStorageUser(retval.getUserId(), context);
            if (!context.isEnabled() ||!user.isMailEnabled()) {
                throw EXCEPTION.create(3, sessionId);
            }
        } catch (UndeclaredThrowableException e) {
            throw EXCEPTION.create(3, sessionId);
        } catch (AbstractOXException e) {
            throw EXCEPTION.create(3, sessionId);
        }
		return retval;
	}

	/**
	 * Convenience method to remember the session for a request in the servlet
	 * attributes.
	 * 
	 * @param req
	 *            servlet request.
	 * @param session
	 *            session to remember.
	 */
	public static void rememberSession(final ServletRequest req,
			final Session session) {
		req.setAttribute(SESSION_KEY, session);
	}

	/**
	 * Returns the remembered session.
	 * 
	 * @param req
	 *            servlet request.
	 * @return the remembered session.
	 */
	protected static Session getSessionObject(final ServletRequest req) {
		return (Session) req.getAttribute(SESSION_KEY);
	}
}
