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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

public abstract class SessionServlet extends AJAXServlet {
	
	public static final String _sessionObject = "sessionObject";
	
	private static final String ERROR_MISSING_COOKIE_ID = "Missing cookie ID";
	
	private static final String ERROR_MISSING_SESSION_COOKIE = "Missing session cookie";
	
	private static final String ERROR_NO_SESSION_OBJECT_FOUND = "No SessionObject found for ID: ";

	/**
	 * Checks the session ID supplied as a query parameter in the request URI.
	 * Intended usage:
	 *
	 * <pre>
	 * if (isInvalidSession(req, w))
	 * 	return;
	 * </pre>
	 *
	 * at the beginning of {@link #doGet}, {@link #doPost} etc.
	 *
	 * @param req
	 *            The HttpServletRequest object containing the session ID as URI
	 *            parameter.
	 * @param w
	 *            The PrintWriter object returned by
	 *            {@link HttpServletResponse#getWriter} which is used to send a
	 *            JSON error object if the session ID is invalid.
	 * @return true if the session ID is invalid, false if the session ID is
	 *         valid.
	 * @throws IOException
	 */
	
	protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
		 * The magic spell to disable caching
		 */
		resp.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		resp.setHeader("Cache-Control", "post-check=0, pre-check=0");
		resp.setHeader("Pragma", "no-cache");
		
		final String cookieId = req.getParameter(PARAMETER_SESSION);
		String session = null;
		if (cookieId == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ERROR_MISSING_COOKIE_ID);
			return;
		}
		
		boolean isCookieSet = false;
		
		final Cookie[] cookie = req.getCookies();
		if (cookie != null) {
			for (int a = 0; a < cookie.length; a++) {
				if (cookie[a].getName().equals(Login.cookiePrefix + cookieId)) {
					session = cookie[a].getValue();
					isCookieSet = true;
					break;
				}
			}
		}
		
		if (!isCookieSet) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ERROR_MISSING_SESSION_COOKIE);
			return;
		}
		
		/*
		 * Retrieve session object and corresponding context
		 */
		final SessionObject sessionObj = SessiondConnector.getInstance().getSession(session, true);
		if (sessionObj == null) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ERROR_NO_SESSION_OBJECT_FOUND + session);
			return;
		}
		req.setAttribute(_sessionObject, sessionObj);
		
		super.service(req, resp);
	}
	
	protected SessionObject getSessionObject(final HttpServletRequest req) {
		return (SessionObject)req.getAttribute(_sessionObject);
	}
}
