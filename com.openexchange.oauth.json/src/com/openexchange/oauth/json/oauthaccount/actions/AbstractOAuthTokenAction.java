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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.oauthaccount.AccountField;
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
        oauthTokenSecret = stripExpireParam(oauthTokenSecret);
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
            if (isEmpty(displayName)) {
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

    /*
     * Fixes bug 24332
     */
    private String stripExpireParam(final String token) {
    	Pattern P_EXPIRES = Pattern.compile("&expires(=[0-9]+)?$");

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
