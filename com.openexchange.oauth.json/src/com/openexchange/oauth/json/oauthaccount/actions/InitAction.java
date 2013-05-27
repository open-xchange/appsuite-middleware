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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthToken;
import com.openexchange.oauth.Parameterizable;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.Tools;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InitAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "init", description = "Initialize creation of an OAuth account", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "serviceId", description = "The service meta data identifier; e.g. \"com.openexchange.oauth.twitter\"")
}, responseDescription = "An JSON representation of the resulting interaction providing needed information to complete account creation. See OAuth interaction data.")
public final class InitAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link InitAction}.
     */
    public InitAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            final String accountId = request.getParameter("id");
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
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    //FIXME: Refactor this. These methods are pretty similar. DRY

    private AJAXRequestResult createCallbackAction(final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
        final OAuthService oAuthService = getOAuthService();
        /*
         * Parse parameters
         */
        final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
        if (serviceId == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( AccountField.SERVICE_ID.getName());
        }
        final String name = AccountField.DISPLAY_NAME.getName();
        final String displayName = request.getParameter(name);
        if (isEmpty(displayName)) {
            throw OAuthExceptionCodes.MISSING_DISPLAY_NAME.create();
        }
        /*
         * Generate UUID
         */
        final String uuid = UUID.randomUUID().toString();
        /*
         * Compose call-back URL
         */
        final String callbackUrl;
        {
            final StringBuilder callbackUrlBuilder = new StringBuilder();
            callbackUrlBuilder.append(request.isSecure() ? "https://" : "http://");
            callbackUrlBuilder.append(request.getHostname());
            callbackUrlBuilder.append(PREFIX.get().getPrefix()).append("oauth/accounts");
            callbackUrlBuilder.append("?action=create");
            callbackUrlBuilder.append("&respondWithHTML=true&session=").append(session.getSessionID());
            callbackUrlBuilder.append('&').append(name).append('=').append(urlEncode(displayName));
            callbackUrlBuilder.append('&').append(AccountField.SERVICE_ID.getName()).append('=').append(urlEncode(serviceId));
            callbackUrlBuilder.append('&').append(OAuthConstants.SESSION_PARAM_UUID).append('=').append(uuid);
            final String cb = request.getParameter("cb");
            if (!isEmpty(cb)) {
            	callbackUrlBuilder.append("&callback=").append(cb);
            }
            callbackUrl = callbackUrlBuilder.toString();
        }
        /*
         * Invoke
         */
        final OAuthInteraction interaction = oAuthService.initOAuth(serviceId, callbackUrl, session);
        final OAuthToken requestToken = interaction.getRequestToken();
        /*
         * Create a container to set some state information: Request token's secret, call-back URL, whatever
         */
        final Map<String, Object> oauthState = new HashMap<String, Object>();
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
        session.setParameter(uuid, oauthState);
        /*
         * Write as JSON
         */
        final JSONObject jsonInteraction = AccountWriter.write(interaction, uuid);
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(jsonInteraction);
    }

    private AJAXRequestResult reauthorizeCallbackAction(final String accountId, final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
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
         * Compose call-back URL
         */
        final StringBuilder callbackUrlBuilder = new StringBuilder(256);
        callbackUrlBuilder.append(request.isSecure() ? "https://" : "http://");
        callbackUrlBuilder.append(request.getHostname());
        callbackUrlBuilder.append(PREFIX.get().getPrefix()).append("oauth/accounts");
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
        if (request.getParameter("cb") != null) {
        	callbackUrlBuilder.append("&").append("callback=").append(request.getParameter("cb"));
        }
        /*
         * Invoke
         */
        final OAuthInteraction interaction = oAuthService.initOAuth(serviceId, callbackUrlBuilder.toString(), session);
        final OAuthToken requestToken = interaction.getRequestToken();
        /*
         * Create a container to set some state information: Request token's secret, call-back URL, whatever
         */
        final Map<String, Object> oauthState = new HashMap<String, Object>();
        oauthState.put(OAuthConstants.ARGUMENT_SECRET, requestToken.getSecret());
        oauthState.put(OAuthConstants.ARGUMENT_CALLBACK, callbackUrlBuilder.toString());
        session.setParameter(uuid, oauthState);        /*
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

    /** Checks for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
