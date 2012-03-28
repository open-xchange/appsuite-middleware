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
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthConstants;
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
        final String uuid = request.getParameter(OAuthConstants.SESSION_PARAM_UUID);
        if (uuid == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( OAuthConstants.SESSION_PARAM_UUID);
        }
        /*
         * Get request token secret from session parameters
         */
        @SuppressWarnings("unchecked")
        final Map<String, Object> state = (Map<String, Object>) session.getParameter(uuid); //request.getParameter("oauth_token_secret");
        final String oauthTokenSecret = (String) state.get(OAuthConstants.ARGUMENT_SECRET);
        session.setParameter(uuid, null);
        /*
         * The OAuth verifier (PIN)
         */
        final String oauthVerfifier = request.getParameter(OAuthConstants.URLPARAM_OAUTH_VERIFIER);
        /*
         * Invoke
         */
        final Map<String, Object> arguments = new HashMap<String, Object>(3);
        arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, request.getParameter(AccountField.DISPLAY_NAME.getName()));
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

}
