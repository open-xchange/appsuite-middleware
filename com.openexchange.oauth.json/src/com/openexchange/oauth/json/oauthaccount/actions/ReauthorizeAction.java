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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.oauth.json.Tools;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountParser;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReauthorizeAction}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ReauthorizeAction extends AbstractOAuthAJAXActionService {

    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws AbstractOXException {
        /*
         * Parse parameters
         */
        final String accountId = request.getParameter("id");
        final int id;
        if (null == accountId) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, "id");
        } else {
            id = Tools.getUnsignedInteger(accountId);
        }

        // http://wiki.oauth.net/w/page/12238555/Signed-Callback-URLs
        // http://developer.linkedin.com/message/4568
        final String oauthToken = request.getParameter(OAuthConstants.URLPARAM_OAUTH_TOKEN);
        if (oauthToken == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, OAuthConstants.URLPARAM_OAUTH_TOKEN);
        }
        final String uuid = request.getParameter(OAuthConstants.SESSION_PARAM_UUID);
        if (uuid == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, OAuthConstants.SESSION_PARAM_UUID);
        }
        /*
         * Get request token secret from session parameters
         */
        final String oauthTokenSecret = (String) session.getParameter(uuid); // request.getParameter("oauth_token_secret");
        session.setParameter(uuid, null);
        if (oauthTokenSecret == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, AccountField.SECRET.getName());
        }
        /*
         * The OAuth verifier (PIN)
         */
        final String oauthVerfifier = request.getParameter(OAuthConstants.URLPARAM_OAUTH_VERIFIER);
        if (oauthVerfifier == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, OAuthConstants.URLPARAM_OAUTH_VERIFIER);
        }
        /*
         * The meta data identifier
         */
        final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
        if (serviceId == null) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, AccountField.SERVICE_ID.getName());
        }
        /*
         * Invoke
         */
        final Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(OAuthConstants.ARGUMENT_DISPLAY_NAME, request.getParameter(AccountField.DISPLAY_NAME.getName()));
        arguments.put(OAuthConstants.ARGUMENT_PIN, oauthVerfifier);
        final DefaultOAuthToken token = new DefaultOAuthToken();
        token.setSecret(oauthTokenSecret);
        token.setToken(oauthToken);
        arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, token);
        final OAuthService oAuthService = getOAuthService();
        /*
         * By now it doesn't matter which interaction type is passed
         */
        oAuthService.updateAccount(id, arguments, session.getUserId(), session.getContextId());

        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Boolean.TRUE);

    }

}
