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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.sessiond.InvalidCredentialsException;
import com.openexchange.sessiond.LoginException;
import com.openexchange.sessiond.MaxSessionLimitException;
import com.openexchange.sessiond.PasswordExpiredException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessiondConnector;
import com.openexchange.sessiond.SessiondException;
import com.openexchange.sessiond.UserNotActivatedException;
import com.openexchange.sessiond.UserNotFoundException;
import com.openexchange.sessiond.LoginException.Code;
import com.openexchange.sessiond.exception.SessionException;
import com.openexchange.tools.ajp13.AJPv13RequestHandler;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;

public class Login extends AJAXServlet {
	
	/**
     * For serialization.
     */
    private static final long serialVersionUID = 7680745138705836499L;

    private static final String ERROR_INVALID_CREDENTIALS = "Invalid credentials";
	
	private static final String ERROR_USER_NOT_FOUND = "User not found";
	
	private static final String ERROR_USER_NOT_ACTIVE = "User not active";
	
	private static final String ERROR_PASSWORD_EXPIRED = "Password expired";
	
	private static final String ERROR_CONTEXT_NOT_FOUND = "Context not found";
	
	//private static final String ERROR_NO_VALID_SESSION = "No valid session";
	
	private static final String ERROR_INVALID_ACTION = "Invalid session";
	
	private static final String _error = "error";
	
	public static final String _random = "random";
	
	private static final String _name = "name";
	
	private static final String _password = "password";
	
	//private static final String _setCookie = "Set-Cookie";
	
	private static final String _redirectUrl = "/index.html#id=";
	
	public static final String cookiePrefix = "open-xchange-session-";
	
	private static transient final Log LOG = LogFactory.getLog(Login.class);
	
	/* (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException,
        IOException {
		final String action = req.getParameter(PARAMETER_ACTION);
		if (action == null) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if (action.equals(ACTION_LOGIN)) {
			final String name = req.getParameter(_name);
			final String password = req.getParameter(_password);
			if (name == null || password == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			final SessiondConnector sessiondConnector = SessiondConnector.getInstance();
			SessionObject sessionObj = null;
            final Response response = new Response();
			try {
				sessionObj = sessiondConnector.addSession(name, password, req.getRemoteAddr());
            } catch (LoginException e) {
                if (LoginException.Source.USER == e.getSource()) {
                    LOG.debug(e.getMessage(), e);
                } else {
                    LOG.error(e.getMessage(), e);
                }
                response.setException(e);
			} catch (InvalidCredentialsException e) {
				LOG.debug(ERROR_INVALID_CREDENTIALS, e);
                response.setException(new LoginException(
                    Code.INVALID_CREDENTIALS, e));
			} catch (UserNotFoundException e) {
				LOG.debug(ERROR_USER_NOT_FOUND, e);
                response.setException(new LoginException(
                    Code.INVALID_CREDENTIALS, e));
			} catch (UserNotActivatedException e) {
				LOG.debug(ERROR_USER_NOT_ACTIVE, e);
                response.setException(new LoginException(
                    Code.INVALID_CREDENTIALS, e));
			} catch (PasswordExpiredException e) {
				LOG.debug(ERROR_PASSWORD_EXPIRED, e);
                response.setException(new LoginException(
                    Code.INVALID_CREDENTIALS, e));
			} catch (MaxSessionLimitException e) {
                LOG.error("Session Limit exceeded.", e);
                response.setException(new LoginException(LoginException.Code
                    .DATABASE_DOWN, e));
            } catch (SessiondException e) {
                LOG.error("Error creating session.", e);
                response.setException(new LoginException(LoginException.Code
                    .DATABASE_DOWN, e));
            } catch (ContextException e) {
            	 LOG.error("Error looking up context.", e);
            	 response.setException(e);
			}
            SessionServlet.rememberSession(req, sessionObj);
            /*
             * Write response
             */
            JSONObject login = null;
            if (!response.hasError()) {
                try {
                    login = writeLogin(sessionObj);
                } catch (JSONException e) {
                    final OXJSONException oje = new OXJSONException(OXJSONException.Code
                        .JSON_WRITE_ERROR, e);
                    LOG.error(oje.getMessage(), oje);
                    response.setException(oje);
                }
            }
            /*
             * The magic spell to disable caching
             */
            Tools.disableCaching(resp);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            if (!response.hasError()) { 
                writeCookie(resp, sessionObj);
            }
            try {
                if (null == login) {
                    Response.write(response, resp.getWriter());
                } else {
                    login.write(resp.getWriter());
                }
            } catch (JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
		} else if (action.equals(ACTION_LOGOUT)) {
			/*
             * The magic spell to disable caching
             */
            Tools.disableCaching(resp);
			final String cookieId = req.getParameter(PARAMETER_SESSION);
			if (cookieId == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			String session = null;
			final Cookie[] cookie = req.getCookies();
			if (cookie != null) {
				for (int a = 0; a < cookie.length; a++) {
					if (cookie[a].getName().equals(Login.cookiePrefix + cookieId)) {
						session = cookie[a].getValue();
                        final Cookie respCookie = new Cookie(cookiePrefix + cookieId, session);
                        respCookie.setPath("/");
                        respCookie.setMaxAge(0);
                        resp.addCookie(respCookie);
						break;
					} else if (AJPv13RequestHandler.JSESSIONID_COOKIE.equals(cookie[a].getName())) {
						final Cookie jsessionIdCookie = new Cookie(AJPv13RequestHandler.JSESSIONID_COOKIE, cookie[a].getValue());
						jsessionIdCookie.setPath("/");
						jsessionIdCookie.setMaxAge(0); // delete
						resp.addCookie(jsessionIdCookie);
					}
				}
			}
			if (session != null) {			
				final SessiondConnector sd = SessiondConnector.getInstance();
				sd.removeSession(session);
			} else if (LOG.isDebugEnabled()) {
				LOG.debug("no session cookie found in request!");
			}
		} else if (action.equals(ACTION_REDIRECT)) {
			/*
             * The magic spell to disable caching
             */
			Tools.disableCaching(resp);
			final String randomToken = req.getParameter(_random);
			if (randomToken == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			final SessiondConnector sessiondConnector = SessiondConnector.getInstance();
			final SessionObject sessionObj = sessiondConnector.getSessionByRandomToken(randomToken);
            if (null == sessionObj) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
				writeCookie(resp, sessionObj);
				resp.sendRedirect(_redirectUrl + sessionObj.getSecret());
            }
		} else if (action.equals(ACTION_AUTOLOGIN)) {
			final Cookie[] cookies = req.getCookies();
            final Response response = new Response();
            try {
    			if (cookies == null) {
                    throw new OXJSONException(OXJSONException.Code
                        .INVALID_COOKIE);
                }
				final SessiondConnector sessiondConnector = SessiondConnector
				    .getInstance();
				for (Cookie cookie : cookies) {
					final String cookieName = cookie.getName();
					if (cookieName.startsWith(cookiePrefix)) {
						final String session = cookie.getValue();
						if (sessiondConnector.refreshSession(session)) {
							final SessionObject sessionObj = sessiondConnector
                                .getSession(session);
                            SessionServlet.checkIP(sessionObj.getLocalIp(), req
                                .getRemoteAddr());
                            response.setData(writeLogin(sessionObj));
							break;
						}
                        final Cookie respCookie = new Cookie(cookie.getName(),
                            cookie.getValue());
                        respCookie.setPath("/");
                        respCookie.setMaxAge(0); // delete
                        resp.addCookie(respCookie);
					}
				}
                if (null == response.getData()) {
                    throw new OXJSONException(OXJSONException.Code
                        .INVALID_COOKIE);
                }
            } catch (SessionException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (OXJSONException e) {
                LOG.debug(e.getMessage(), e);
                response.setException(e);
            } catch (JSONException e) {
                final OXJSONException oje = new OXJSONException(OXJSONException
                    .Code.JSON_WRITE_ERROR, e);
                LOG.error(oje.getMessage(), oje);
                response.setException(oje);
            }
			/*
             * The magic spell to disable caching
             */
			Tools.disableCaching(resp);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            try {
                if (response.hasError()) {
                    Response.write(response, resp.getWriter());
                } else {
                    ((JSONObject) response.getData()).write(resp.getWriter());
                }
            } catch (JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
        } else {
			writeError(resp, ERROR_INVALID_ACTION);
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(final HttpServletRequest req,
        final HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    protected void writeCookie(final HttpServletResponse resp,
        final SessionObject sessionObj) {
        final Cookie cookie = new Cookie(cookiePrefix + sessionObj.getSecret(),
            sessionObj.getSessionID());
        cookie.setPath("/");
        resp.addCookie(cookie);
	}
	
	private void writeError(final HttpServletResponse resp,
        final String message) throws IOException {
		try {
            final JSONObject retval = new JSONObject();
            retval.put(_error, message);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            retval.write(resp.getWriter());
		} catch (JSONException exc) {
			log(exc.getMessage(), exc);
		}
	}

    private JSONObject writeLogin(final SessionObject sessionObj)
        throws JSONException {
        final JSONObject retval = new JSONObject();
        retval.put(PARAMETER_SESSION, sessionObj.getSecret());
        retval.put(_random, sessionObj.getRandomToken());
        return retval;
	}
}
