/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidRequestException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException.Reason;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
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
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class OAuthDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = 2930109046898745937L;

    private final ServiceLookup services;

    private final boolean useOAuthPrefix;

    /**
     * Initializes a new {@link OAuthDispatcherServlet}.
     *
     * @param services The service lookup
     * @param prefix The servlet prefix
     * @param useOAuthPrefix Whether to use the additional "oauth/modules" prefix or not
     */
    public OAuthDispatcherServlet(ServiceLookup services, String prefix, boolean useOAuthPrefix) {
        super(prefix);
        this.services = services;
        this.useOAuthPrefix = useOAuthPrefix;
    }

    @Override
    protected SessionResult<ServerSession> initializeSession(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws OXException {
        Session session = SessionUtility.getSessionObject(httpRequest, false);
        if (session != null) {
            return new SessionResult<ServerSession>(Reply.CONTINUE, ServerSessionAdapter.valueOf(session));
        }

        if (isSessionBasedRequest(httpRequest)) {
            return super.initializeSession(httpRequest, httpResponse);
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
        OAuthAccess oAuthAccess = oAuthResourceService.checkAccessToken(authHeader.substring(OAuthConstants.BEARER_SCHEME.length() + 1), httpRequest);
        session = oAuthAccess.getSession();
        SessionUtility.rememberSession(httpRequest, ServerSessionAdapter.valueOf(session));
        httpRequest.setAttribute(OAuthConstants.PARAM_OAUTH_ACCESS, oAuthAccess);
        return new SessionResult<ServerSession>(Reply.CONTINUE, ServerSessionAdapter.valueOf(session));
    }

    /**
     * Checks if the request is a normal session request
     *
     * @param httpRequest The {@link HttpServletRequest} to check
     * @return <code>true</code> if it is a session based request, <code>false</code> otherwise
     */
    private boolean isSessionBasedRequest(HttpServletRequest httpRequest) {
        String sessionId = httpRequest.getParameter(PARAMETER_SESSION);
        return useOAuthPrefix == false && ((sessionId != null && sessionId.length() > 0) || httpRequest.getHeader(HttpHeaders.AUTHORIZATION) == null);
    }

    @Override
    protected AJAXRequestData initializeRequestData(HttpServletRequest httpRequest, HttpServletResponse httpResponse, boolean preferStream) throws OXException, IOException {
        if (isSessionBasedRequest(httpRequest)) {
            return super.initializeRequestData(httpRequest, httpResponse, preferStream);
        }

        Dispatcher dispatcher = getDispatcher();
        AJAXRequestDataTools requestDataTools = getAjaxRequestDataTools();
        String module = requestDataTools.getModule(useOAuthPrefix ? prefix + "oauth/modules/" : prefix, httpRequest);
        String action = requestDataTools.getAction(httpRequest);
        ServerSession session = SessionUtility.getSessionObject(httpRequest, false);
        if (session == null) {
            LOG.warn("Session was not contained in servlet request attributes!", new Exception());
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }

        OAuthAccess oAuthAccess = (OAuthAccess) httpRequest.getAttribute(OAuthConstants.PARAM_OAUTH_ACCESS);
        if (oAuthAccess == null) {
            LOG.warn("OAuthToken was not contained in servlet request attributes!", new Exception());
            throw new OAuthInvalidTokenException(Reason.TOKEN_MISSING);
        }

        /*
         * Parse AJAXRequestData
         */
        AJAXRequestData requestData = requestDataTools.parseRequest(httpRequest, preferStream, isMultipartContent(httpRequest), session, prefix, httpResponse);
        requestData.setModule(module);
        requestData.setSession(session);
        requestData.setProperty(OAuthConstants.PARAM_OAUTH_ACCESS, oAuthAccess);

        AJAXActionServiceFactory factory = dispatcher.lookupFactory(module);
        if (factory == null || !factory.getClass().isAnnotationPresent(OAuthModule.class)) {
            throw AjaxExceptionCodes.UNKNOWN_MODULE.create(module);
        }

        AJAXActionService actionService = factory.createActionService(action);
        if (actionService == null || (!actionService.getClass().isAnnotationPresent(OAuthAction.class) && !actionService.getClass().isAnnotationPresent(RestrictedAction.class))) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, module);
        }

        return requestData;
    }

    @Override
    protected void handleOXException(OXException e, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        if (isSessionBasedRequest(httpRequest)) {
            super.handleOXException(e, httpRequest, httpResponse);
            return;
        }
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
                    sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, Collections.singletonMap(HttpHeaders.WWW_AUTHENTICATE, sb.toString()));
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
