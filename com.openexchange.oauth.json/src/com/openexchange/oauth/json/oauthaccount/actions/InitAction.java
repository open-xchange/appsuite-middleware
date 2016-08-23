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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.oauth.json.oauthaccount.actions;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.Client;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResult.ResultType;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.Parameterizable;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.Tools;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.oauth.scope.Module;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InitAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "init", description = "Initialize creation of an OAuth account", parameters = { 
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."), 
    @Parameter(name = "serviceId", description = "The service meta data identifier; e.g. \"com.openexchange.oauth.twitter\""), 
    @Parameter(name = "scope", description = "A comma separated list with scopes"),
}, responseDescription = "An JSON representation of the resulting interaction providing needed information to complete account creation. See OAuth interaction data.")
public final class InitAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link InitAction}.
     */
    public InitAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        try {
            String accountId = request.getParameter("id");
            if (null == accountId) {
                /*
                 * Call-back with action=create
                 */
                return createCallbackAction(request, session);
            }
            /*
             * Call-back with action=reauthorize
             */
            return reauthorizeCallbackAction(accountId, request, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    //FIXME: Refactor this. These methods are pretty similar. DRY

    private AJAXRequestResult createCallbackAction(AJAXRequestData request, ServerSession session) throws OXException {
        Locale locale = session.getUser().getLocale();
        try {
            final OAuthService oAuthService = getOAuthService();
            /*
             * Parse parameters
             */
            final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
            if (serviceId == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.SERVICE_ID.getName());
            }
            final String name = AccountField.DISPLAY_NAME.getName();
            final String displayName = request.getParameter(name);
            if (isEmpty(displayName)) {
                throw OAuthExceptionCodes.MISSING_DISPLAY_NAME.create();
            }
            final String scope = request.getParameter("scopes");
            if (isEmpty(scope)) {
                throw OAuthExceptionCodes.MISSING_SCOPE.create();
            }
            
            /*
             * Generate UUID
             */
            final String uuid = UUID.randomUUID().toString();
            /*
             * OAuth token for session
             */
            final String oauthSessionToken = UUID.randomUUID().toString();
            /*
             * Compose call-back URL
             */
            final String callbackUrl;
            {
                final StringBuilder callbackUrlBuilder = request.constructURL(new StringBuilder(PREFIX.get().getPrefix()).append("oauth/accounts").toString(), true);
                // Append query string
                callbackUrlBuilder.append("?action=create");
                callbackUrlBuilder.append("&respondWithHTML=true&session=").append(session.getSessionID());
                callbackUrlBuilder.append('&').append(name).append('=').append(urlEncode(displayName));
                callbackUrlBuilder.append('&').append(AccountField.SERVICE_ID.getName()).append('=').append(urlEncode(serviceId));
                callbackUrlBuilder.append('&').append(OAuthConstants.SESSION_PARAM_UUID).append('=').append(uuid);
                callbackUrlBuilder.append('&').append(Session.PARAM_TOKEN).append('=').append(oauthSessionToken);
                callbackUrlBuilder.append('&').append("scopes").append('=').append(scope);
                final String cb = request.getParameter("cb");
                if (!isEmpty(cb)) {
                    callbackUrlBuilder.append("&callback=").append(cb);
                }
                callbackUrl = callbackUrlBuilder.toString();
            }
            // Get the scopes
            OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
            Set<OAuthScope> scopes = scopeRegistry.getAvailableScopes(API.resolveFromServiceId(serviceId), Module.valuesOf(scope));
            /*
             * Invoke
             */
            final String currentHost = determineHost(request, session);
            final OAuthInteraction interaction = oAuthService.initOAuth(serviceId, callbackUrl, currentHost, session, scopes);
            final OAuthToken requestToken = interaction.getRequestToken();
            /*
             * Create a container to set some state information: Request token's secret, call-back URL, whatever
             */
            final Map<String, Object> oauthState = new HashMap<>();
            if (interaction instanceof Parameterizable) {
                final Parameterizable params = (Parameterizable) interaction;
                for (final String key : params.getParamterNames()) {
                    final Object value = params.getParameter(key);
                    if (null != value) {
                        oauthState.put(key, value);
                    }
                }
            }
            oauthState.put(OAuthConstants.ARGUMENT_SECRET, requestToken.getSecret());
            oauthState.put(OAuthConstants.ARGUMENT_CALLBACK, callbackUrl);
            oauthState.put(OAuthConstants.ARGUMENT_CURRENT_HOST, currentHost);
            oauthState.put(OAuthConstants.ARGUMENT_AUTH_URL, interaction.getAuthorizationURL());
            session.setParameter(uuid, oauthState);
            session.setParameter(Session.PARAM_TOKEN, oauthSessionToken);
            /*
             * Check redirect parameter
             */
            if (AJAXRequestDataTools.parseBoolParameter(request.getParameter("redirect"))) {
                // Request for redirect
                HttpServletResponse response = request.optHttpServletResponse();
                if (null != response) {
                    try {
                        response.sendRedirect(interaction.getAuthorizationURL());
                        //response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                        return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(ResultType.DIRECT);
                    } catch (IOException e) {
                        throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
                    }
                }
            }
            /*
             * Write as JSON
             */
            final JSONObject jsonInteraction = AccountWriter.write(interaction, uuid);
            /*
             * Return appropriate result
             */
            return new AJAXRequestResult(jsonInteraction);
        } catch (OXException e) {
            if (Client.OX6_UI.getClientId().equals(session.getClient())) {
                throw e;
            }
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_BAD_REQUEST), e.getDisplayMessage(locale));
        } catch (JSONException e) {
            if (Client.OX6_UI.getClientId().equals(session.getClient())) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_OK), StringHelper.valueOf(locale).getString(OXExceptionStrings.MESSAGE));
        } catch (RuntimeException e) {
            if (Client.OX6_UI.getClientId().equals(session.getClient())) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            throw AjaxExceptionCodes.HTTP_ERROR.create(e, Integer.valueOf(HttpServletResponse.SC_OK), StringHelper.valueOf(locale).getString(OXExceptionStrings.MESSAGE));
        }
    }

    private AJAXRequestResult reauthorizeCallbackAction(final String accountId, final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
        final String scope = request.getParameter("scopes");
        if (isEmpty(scope)) {
            throw OAuthExceptionCodes.MISSING_SCOPE.create();
        }
        
        final OAuthService oAuthService = getOAuthService();
        /*
         * Get account by identifier
         */
        final OAuthAccount account = oAuthService.getAccount(Tools.getUnsignedInteger(accountId), session, session.getUserId(), session.getContextId());
        final String serviceId = account.getMetaData().getId();
        /*
         * Generate UUID
         */
        final String uuid = UUID.randomUUID().toString();
        /*
         * OAuth token for session
         */
        final String oauthSessionToken = UUID.randomUUID().toString();
        /*
         * Compose call-back URL
         */
        final StringBuilder callbackUrlBuilder = request.constructURL(new StringBuilder(PREFIX.get().getPrefix()).append("oauth/accounts").toString(), true);
        callbackUrlBuilder.append("?action=reauthorize");
        callbackUrlBuilder.append("&id=").append(account.getId());
        callbackUrlBuilder.append("&respondWithHTML=true&session=").append(session.getSessionID());
        {
            final String name = AccountField.DISPLAY_NAME.getName();
            final String displayName = request.getParameter(name);
            if (displayName != null) {
                callbackUrlBuilder.append('&').append(name).append('=').append(urlEncode(displayName));
            }
        }
        callbackUrlBuilder.append('&').append(AccountField.SERVICE_ID.getName()).append('=').append(urlEncode(serviceId));
        callbackUrlBuilder.append('&').append(OAuthConstants.SESSION_PARAM_UUID).append('=').append(uuid);
        callbackUrlBuilder.append('&').append(Session.PARAM_TOKEN).append('=').append(oauthSessionToken);
        callbackUrlBuilder.append('&').append("scopes").append('=').append(scope);
        final String cb = request.getParameter("cb");
        if (!isEmpty(cb)) {
            callbackUrlBuilder.append("&callback=").append(cb);
        }
        // Get the scopes
        OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
        Set<OAuthScope> scopes = scopeRegistry.getAvailableScopes(API.resolveFromServiceId(serviceId), Module.valuesOf(scope));
        /*
         * Invoke
         */
        final OAuthInteraction interaction = oAuthService.initOAuth(serviceId, callbackUrlBuilder.toString(), determineHost(request, session), session, scopes);
        final OAuthToken requestToken = interaction.getRequestToken();
        /*
         * Create a container to set some state information: Request token's secret, call-back URL, whatever
         */
        final Map<String, Object> oauthState = new HashMap<>();
        if (interaction instanceof Parameterizable) {
            final Parameterizable params = (Parameterizable) interaction;
            for (final String key : params.getParamterNames()) {
                final Object value = params.getParameter(key);
                if (null != value) {
                    oauthState.put(key, value);
                }
            }
        }
        oauthState.put(OAuthConstants.ARGUMENT_SECRET, requestToken.getSecret());
        oauthState.put(OAuthConstants.ARGUMENT_CALLBACK, callbackUrlBuilder.toString());
        oauthState.put(OAuthConstants.ARGUMENT_AUTH_URL, interaction.getAuthorizationURL());
        session.setParameter(uuid, oauthState);
        session.setParameter(Session.PARAM_TOKEN, oauthSessionToken);
        /*
         * Check redirect parameter
         */
        if (AJAXRequestDataTools.parseBoolParameter(request.getParameter("redirect"))) {
            // Request for redirect
            HttpServletResponse response = request.optHttpServletResponse();
            if (null != response) {
                try {
                    response.sendRedirect(interaction.getAuthorizationURL());
                    //response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(ResultType.DIRECT);
                } catch (IOException e) {
                    throw OAuthExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
        }
        /*
         * Write as JSON
         */
        final JSONObject jsonInteraction = AccountWriter.write(interaction, uuid);
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(jsonInteraction);
    }

    private static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

    private static String determineHost(AJAXRequestData requestData, ServerSession session) {
        String hostName = null;
        /*
         * Ask hostname service if available
         */
        {
            final HostnameService hostnameService = Services.getService(HostnameService.class);
            if (null != hostnameService) {
                if (session.getUser().isGuest()) {
                    hostName = hostnameService.getGuestHostname(session.getUserId(), session.getContextId());
                } else {
                    hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
                }
            }
        }
        /*
         * Get hostname from request
         */
        if (isEmpty(hostName)) {
            hostName = requestData.getHostname();
        }
        /*
         * Get hostname from java
         */
        if (isEmpty(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                // ignore
            }
        }
        /*
         * Fall back to localhost as last resort
         */
        if (isEmpty(hostName)) {
            hostName = "localhost";
        }
        return requestData.getHostname();
    }

}
