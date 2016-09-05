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
import static com.openexchange.oauth.OAuthConstants.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.Services;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.scope.Module;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractOAuthTokenAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractOAuthTokenAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link AbstractOAuthTokenAction}.
     */
    public AbstractOAuthTokenAction() {
        super();
    }

    protected Map<String, Object> processOAuthArguments(final AJAXRequestData request, final ServerSession session, final OAuthServiceMetaData service) throws OXException {
        /*
         * Parse OAuth parameters
         */
        // http://wiki.oauth.net/w/page/12238555/Signed-Callback-URLs
        // http://developer.linkedin.com/message/4568

        /*
         * Check for reported oauth problems
         */
        {
            String oauth_problem = request.getParameter(OAuthConstants.URLPARAM_OAUTH_PROBLEM);
            if(!Strings.isEmpty(oauth_problem)) {
                throw fromOauthProblem(oauth_problem, request, service);
            }
            oauth_problem = request.getParameter(OAuthConstants.URLPARAM_ERROR);
            if(!Strings.isEmpty(oauth_problem)) {
                throw fromOauthProblem(oauth_problem, request, service);
            }
        }

        String oauthToken = request.getParameter(OAuthConstants.URLPARAM_OAUTH_TOKEN);
        if (oauthToken == null) {
            oauthToken = request.getParameter("access_token");
        }
        if (oauthToken != null) {
        	oauthToken = stripExpireParam(oauthToken);
        }
        final String uuid = request.getParameter(OAuthConstants.SESSION_PARAM_UUID);
        if (uuid == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(OAuthConstants.SESSION_PARAM_UUID);
        }

        /*
         * Get request token secret from session parameters
         */
        @SuppressWarnings("unchecked")
        final Map<String, Object> state = (Map<String, Object>) session.getParameter(uuid); //request.getParameter("oauth_token_secret");
        if (null == state) {
            throw OAuthExceptionCodes.CANCELED_BY_USER.create();
        }
        String oauthTokenSecret = (String) state.get(OAuthConstants.ARGUMENT_SECRET);
        if (oauthTokenSecret != null) {
            oauthTokenSecret = stripExpireParam(oauthTokenSecret);
        }
        session.setParameter(uuid, null);
        /*
         * The OAuth verifier (PIN)
         */
        final String oauthVerfifier = request.getParameter(OAuthConstants.URLPARAM_OAUTH_VERIFIER);
        /*
         * Invoke
         */
        final Map<String, Object> arguments = new HashMap<String, Object>(3);
        {
            final String displayName = request.getParameter(AccountField.DISPLAY_NAME.getName());
            if (Strings.isEmpty(displayName)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.DISPLAY_NAME.getName());
            }
            arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, displayName);
        }
        arguments.put(OAuthConstants.ARGUMENT_PIN, oauthVerfifier);
        arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
        final DefaultOAuthToken token = new DefaultOAuthToken();
        token.setSecret(oauthTokenSecret);
        token.setToken(oauthToken);
        arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, token);
        /*
         * Process arguments
         */
        service.processArguments(arguments, request.getParameters(), state);
        return arguments;
    }

    private static final Pattern P_EXPIRES = Pattern.compile("&expires(=[0-9]+)?$");

    /*
     * Fixes bug 24332
     */
    private String stripExpireParam(final String token) {
        if (token.indexOf("&expires") < 0) {
            return token;
        }
        final Matcher m = P_EXPIRES.matcher(token);
        final StringBuffer sb = new StringBuffer(token.length());
        if (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
	}

    /**
     * Create the correct {@link OAuthExceptionCode} by mapping the incoming problem against the known problems in {@link OAuthConstants}
     *
     * @param oauth_problem the incoming problem
     * @param request the associated {@link AJAXRequestData}
     * @param service
     * @return the correct {@link OAuthExceptionCode} based on the known problems in {@link OAuthConstants} or
     */
    public static OXException fromOauthProblem(String oauth_problem, AJAXRequestData request, OAuthServiceMetaData service) {
        final String displayName = service.getDisplayName();
        if (OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_ADDITIONAL_AUTHORIZATION_REQUIRED.create(displayName);
        }
        if (OAUTH_PROBLEM_CONSUMER_KEY_REFUSED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_CONSUMER_KEY_REFUSED.create(displayName);
        }
        if (OAUTH_PROBLEM_CONSUMER_KEY_REJECTED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_CONSUMER_KEY_REJECTED.create(displayName);
        }
        if (OAUTH_PROBLEM_CONSUMER_KEY_UNKNOWN.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_CONSUMER_KEY_UNKNOWN.create(displayName);
        }
        if (OAUTH_PROBLEM_NONCE_USED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_NONCE_USED.create();
        }
        if (OAUTH_PROBLEM_PARAMETER_ABSENT.equals(oauth_problem)) {
            String absent_parameters = request.getParameter(URLPARAM_OAUTH_PARAMETERS_ABSENT);
            absent_parameters = Strings.isEmpty(absent_parameters) ? "unknown" : absent_parameters;
            return OAuthExceptionCodes.OAUTH_PROBLEM_PARAMETER_ABSENT.create(absent_parameters);
        }
        if (OAUTH_PROBLEM_PARAMETER_REJECTED.equals(oauth_problem)) {
            String rejected_parameters = request.getParameter(URLPARAM_OAUTH_PARAMETERS_REJECTED);
            rejected_parameters = Strings.isEmpty(rejected_parameters) ? "unknown" : rejected_parameters;
            return OAuthExceptionCodes.OAUTH_PROBLEM_PARAMETER_REJECTED.create(rejected_parameters);
        }
        if (OAUTH_PROBLEM_PERMISSION_DENIED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_PERMISSION_DENIED.create();
        }
        if (OAUTH_PROBLEM_ACCESS_DENIED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_ACCESS_DENIED.create();
        }
        if (OAUTH_PROBLEM_PERMISSION_UNKNOWN.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_PERMISSION_UNKNOWN.create();
        }
        if (OAUTH_PROBLEM_SIGNATURE_INVALID.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_SIGNATURE_INVALID.create();
        }
        if (OAUTH_PROBLEM_SIGNATURE_METHOD_REJECTED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_SIGNATURE_METHOD_REJECTED.create();
        }
        if (OAUTH_PROBLEM_TIMESTAMP_REFUSED.equals(oauth_problem)) {
            String acceptable_timestamps = request.getParameter(URLPARAM_OAUTH_ACCEPTABLE_TIMESTAMPS);
            acceptable_timestamps = Strings.isEmpty(acceptable_timestamps) ? "unknown" : acceptable_timestamps;
            return OAuthExceptionCodes.OAUTH_PROBLEM_TIMESTAMP_REFUSED.create(acceptable_timestamps);
        }
        if (OAUTH_PROBLEM_TOKEN_EXPIRED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_TOKEN_EXPIRED.create(displayName);
        }
        if (OAUTH_PROBLEM_TOKEN_REJECTED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_TOKEN_REJECTED.create(displayName);
        }
        if (OAUTH_PROBLEM_TOKEN_REVOKED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_TOKEN_REVOKED.create(displayName);
        }
        if (OAUTH_PROBLEM_TOKEN_USED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_TOKEN_USED.create(displayName);
        }
        if (OAUTH_PROBLEM_USER_REFUSED.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_USER_REFUSED.create(displayName);
        }
        if (OAUTH_PROBLEM_VERIFIER_INVALID.equals(oauth_problem)) {
            return OAuthExceptionCodes.OAUTH_PROBLEM_VERIFIER_INVALID.create();
        }
        if (OAUTH_PROBLEM_VERSION_REJECTED.equals(oauth_problem)) {
            String acceptable_versions = request.getParameter(URLPARAM_OAUTH_PARAMETERS_ABSENT);
            acceptable_versions = Strings.isEmpty(acceptable_versions) ? "unknown" : acceptable_versions;
        }
        return OAuthExceptionCodes.OAUTH_PROBLEM_UNEXPECTED.create(oauth_problem);
    }
    

    /**
     * Gets the scopes from the request and converts them to {@link OAuthScope}s using the {@link OAuthScopeRegistry}
     * 
     * @param request The {@link AJAXRequestData}
     * @param serviceId The OAuth service provider's identifier
     * @return A {@link Set} with all {@link OAuthScope}s to enable
     * @throws OXException if the {@link OAuthScope}s can not be retrieved or if the <code>scopes</code> URL parameter is missing form the request
     */
    protected Set<OAuthScope> getScopes(AJAXRequestData request, String serviceId) throws OXException {
        OAuthScopeRegistry scopeRegistry = Services.getService(OAuthScopeRegistry.class);
        // Get the scope parameter
        String scope = request.getParameter("scopes");
        if (isEmpty(scope)) {
            return scopeRegistry.getLegacyScopes(API.resolveFromServiceId(serviceId));
        }
        // Get the scopes
        return scopeRegistry.getAvailableScopes(API.resolveFromServiceId(serviceId), Module.valuesOf(scope));
    }
}
