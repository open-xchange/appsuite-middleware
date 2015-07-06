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

import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.tools.servlet.http.Tools.isMultipartContent;
import static com.openexchange.tools.servlet.http.Tools.sendEmptyErrorResponse;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.OAuthResourceService;
import com.openexchange.oauth.provider.annotations.OAuthAction;
import com.openexchange.oauth.provider.annotations.OAuthModule;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidRequestException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.grant.OAuthGrant;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.SessionResult;
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

    private static final Logger LOG = LoggerFactory.getLogger(OAuthDispatcherServlet.class);

    private final ServiceLookup services;

    private final OAuthSessionProvider sessionProvider;

    public OAuthDispatcherServlet(ServiceLookup services, OAuthSessionProvider sessionProvider, String prefix) {
        super(prefix);
        this.services = services;
        this.sessionProvider = sessionProvider;
    }

    @Override
    protected SessionResult<ServerSession> initializeSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        Session session = SessionUtility.getSessionObject(httpRequest, false);
        if (session != null) {
            return new SessionResult<ServerSession>(Reply.CONTINUE, ServerSessionAdapter.valueOf(session));
        }

        String authHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }

        String authScheme = Authorization.extractAuthScheme(authHeader);
        if (authScheme == null || !authScheme.equalsIgnoreCase(OAuthConstants.BEARER_SCHEME) || authHeader.length() <= OAuthConstants.BEARER_SCHEME.length() + 1) {
            throw new OAuthInvalidTokenException(Reason.INVALID_AUTH_SCHEME);
        }

        OAuthResourceService oAuthResourceService = requireService(OAuthResourceService.class, services);
        OAuthGrant grant = oAuthResourceService.validate(authHeader.substring(OAuthConstants.BEARER_SCHEME.length() + 1));
        session = sessionProvider.getSession(grant, httpRequest);
        SessionUtility.rememberSession(httpRequest, ServerSessionAdapter.valueOf(session));
        httpRequest.setAttribute(OAuthConstants.PARAM_OAUTH_GRANT, grant);
        return new SessionResult<ServerSession>(Reply.CONTINUE, ServerSessionAdapter.valueOf(session));
    }

    @Override
    protected AJAXRequestData initializeRequestData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean preferStream) throws OXException, IOException {
        Dispatcher dispatcher = DISPATCHER.get();
        AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
        String module = requestDataTools.getModule(prefix + "oauth/modules/", httpRequest);
        String action = requestDataTools.getAction(httpRequest);
        ServerSession session = SessionUtility.getSessionObject(httpRequest, false);
        if (session == null) {
            LOG.warn("Session was not contained in servlet request attributes!", new Exception());
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }

        OAuthGrant grant = (OAuthGrant) httpRequest.getAttribute(OAuthConstants.PARAM_OAUTH_GRANT);
        if (grant == null) {
            LOG.warn("OAuthToken was not contained in servlet request attributes!", new Exception());
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }

        /*
         * Parse AJAXRequestData
         */
        AJAXRequestData requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), session, prefix, httpResponse);
        requestData.setModule(module);
        requestData.setSession(session);
        requestData.setProperty(OAuthConstants.PARAM_OAUTH_GRANT, grant);

        AJAXActionServiceFactory factory = dispatcher.lookupFactory(module);
        if (factory == null || !factory.getClass().isAnnotationPresent(OAuthModule.class)) {
            throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
        }

        AJAXActionService actionService = factory.createActionService(action);
        if (actionService == null || !actionService.getClass().isAnnotationPresent(OAuthAction.class)) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, module);
        }

        return requestData;
    }

    @Override
    protected void handleOXException(OXException e, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (e instanceof OAuthInvalidTokenException) {
            OAuthInvalidTokenException ex = (OAuthInvalidTokenException) e;
            if (ex.getReason() == Reason.TOKEN_MISSING) {
                sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, Collections.singletonMap(HttpHeaders.WWW_AUTHENTICATE, OAuthConstants.BEARER_SCHEME));
            } else {
                String errorDescription = ex.getErrorDescription();
                StringBuilder sb = new StringBuilder(OAuthConstants.BEARER_SCHEME);
                sb.append(",error=\"invalid_token\"");
                if (errorDescription != null) {
                    sb.append(",error_description=\"").append(errorDescription).append("\"");
                }

                JSONObject result = new JSONObject();
                try {
                    result.put("error", "invalid_token");
                    result.put("error_description", errorDescription);
                } catch (JSONException je) {
                    result = null;
                    logException(je);
                }

                if (result == null) {
                    sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED);
                } else {
                    sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, Collections.singletonMap(HttpHeaders.WWW_AUTHENTICATE, sb.toString()), result.toString());
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
                sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN);
            } else {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, result.toString());
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
                sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_BAD_REQUEST, result.toString());
            }
        } else {
            super.handleOXException(e, httpRequest, httpResponse);
        }
    }

}
