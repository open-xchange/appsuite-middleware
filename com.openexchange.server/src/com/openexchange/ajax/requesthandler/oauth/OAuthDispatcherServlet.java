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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.oauth;

import static com.openexchange.ajax.requesthandler.Dispatcher.PREFIX;
import static com.openexchange.tools.servlet.http.Tools.isMultipartContent;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.oauth.OAuthInvalidTokenException.Reason;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link OAuthDispatcherServlet}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = 2930109046898745937L;

    /*
     * From https://tools.ietf.org/html/rfc6750#section-2.1:
     *   The syntax for Bearer credentials is as follows:
     *   b64token = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
     *   credentials = "Bearer" 1*SP b64token
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\x41-\\x5a\\x61-\\x7a\\x30-\\x39-._~+/]+=*");

    @Override
    protected void initializeSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        if (SessionUtility.getSessionObject(httpRequest, false) != null) {
            return;
        }

        String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        } else {
            String authScheme = Authorization.extractAuthScheme(authHeader);
            if (authScheme != null && authScheme.equalsIgnoreCase(OAuthConstants.BEARER_SCHEME) && authHeader.length() > OAuthConstants.BEARER_SCHEME.length() + 1) {
                String accessToken = authHeader.substring(OAuthConstants.BEARER_SCHEME.length() + 1);
                if (!TOKEN_PATTERN.matcher(accessToken).matches()) {
                    throw new OAuthInvalidTokenException(Reason.TOKEN_MALFORMED);
                }


//              TODO validate token and init session
//              if (accessToken.equals("1234")) {
//                  ServerSession session = createOAuthSession(accessToken);
//                  session.setParameter("com.openexchange.authType", OAuthConstants.AUTH_TYPE);
//                  SessionUtility.rememberSession(httpRequest, session);
//                  return;
//              }
                throw new OAuthInvalidTokenException(Reason.TOKEN_EXPIRED);
            }
        }
    }

    private ServerSession createOAuthSession(String accessToken) throws OXException {
        SessionObject session = new SessionObject("oauthv2-access-token:" + accessToken);
        session.setContextId(424242669);
        session.setUsername("84");
        session.setParameter("com.openexchange.oauth.scopes", "rw_contacts");
        return ServerSessionAdapter.valueOf(session);
    }

    @Override
    protected AJAXRequestData initializeRequestData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean preferStream) throws OXException, IOException {
        Dispatcher dispatcher = DISPATCHER.get();
        AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
        String module = requestDataTools.getModule(PREFIX.get() + "oauth/modules/", httpRequest);
        String action = requestDataTools.getAction(httpRequest);
        ServerSession session = SessionUtility.getSessionObject(httpRequest);
        if (session == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }
        /*
         * Parse AJAXRequestData
         */
        AJAXRequestData requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), session, PREFIX.get(), httpResponse);
        requestData.setModule(module);
        requestData.setSession(session);

        AJAXActionServiceFactory factory = dispatcher.lookupFactory(module);
        if (factory == null || !factory.getClass().isAnnotationPresent(OAuthModule.class)) {
            // TODO: 404
            throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
        }

        AJAXActionService actionService = factory.createActionService(action);
        if (actionService == null || !actionService.getClass().isAnnotationPresent(OAuthAction.class)) {
            // TODO: 404
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, module);
        }

        return requestData;
    }

    @Override
    protected void handleOXException(OXException e, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (e instanceof OAuthInvalidTokenException) {
            OAuthInvalidTokenException ex = (OAuthInvalidTokenException) e;
            if (ex.getReason() == Reason.TOKEN_MISSING) {
                httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuthConstants.BEARER_SCHEME);
                httpResponse.setContentType(null);
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                StringBuilder sb = new StringBuilder(OAuthConstants.BEARER_SCHEME).append(",error=\"invalid_token\"");
                httpResponse.setHeader(HttpHeaders.WWW_AUTHENTICATE, sb.toString());
                JSONObject result = new JSONObject();
                try {
                    result.put("error", "invalid_token");
                    result.put("error_description", ex.getErrorDescription());
                } catch (JSONException je) {
                    result = null;
                    logException(je);
                }

                if (result == null) {
                    httpResponse.reset();
                    httpResponse.setContentType(null);
                    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    PrintWriter writer = httpResponse.getWriter();
                    writer.write(result.toString());
                    writer.flush();
                }
            }
        } else if (e instanceof OAuthInsufficientScopeException) {
            OAuthInsufficientScopeException ex = (OAuthInsufficientScopeException) e;
            JSONObject result = new JSONObject();
            try {
                result.put("error", "insufficient_scope");
                result.put("error_description", ex.getErrorDescription());
                result.put("scope", ex.getScope());
            } catch (JSONException je) {
                result = null;
                logException(je);
            }

            if (result == null) {
                httpResponse.reset();
                httpResponse.setContentType(null);
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                PrintWriter writer = httpResponse.getWriter();
                writer.write(result.toString());
                writer.flush();
            }
        } else if (e instanceof OAuthInvalidRequestException) {
            OAuthInvalidRequestException ex = (OAuthInvalidRequestException) e;
            JSONObject result = new JSONObject();
            try {
                result.put("error", "invalid_request");
                result.put("error_description", ex.getErrorDescription());
            } catch (JSONException je) {
                result = null;
                logException(je);
            }

            if (result == null) {
                httpResponse.reset();
                httpResponse.setContentType(null);
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                PrintWriter writer = httpResponse.getWriter();
                writer.write(result.toString());
                writer.flush();
            }
        } else {
            super.handleOXException(e, httpRequest, httpResponse);
        }

    }

}
