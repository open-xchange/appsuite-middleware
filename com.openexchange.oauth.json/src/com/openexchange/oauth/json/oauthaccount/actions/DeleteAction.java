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

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.json.AbstractOAuthAJAXActionService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "delete", description = "Delete an OAuth account", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "The account identifier.") }, requestBody = "A JSON object providing the OAuth account fields to update. See OAuth account data. Currently the only values which make sende being updated are \"displayName\" and the \"token\"-\"secret\"-pair.", responseDescription = "The boolean value \"true\" if successful.")
public final class DeleteAction extends AbstractOAuthAJAXActionService {

    /**
     * Initializes a new {@link DeleteAction}.
     */
    public DeleteAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        final Object data = request.getData();
        if (data instanceof JSONObject) {
            /*
             * Parse parameters
             */
            final String accountId = request.getParameter("id");
            if (null == accountId) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
            }
            /*
             * Delete account
             */
            final OAuthService oAuthService = getOAuthService();
            oAuthService.deleteAccount(getUnsignedInteger(accountId), session.getUserId(), session.getContextId());
        } else if(data instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) data;
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                JSONObject json = jsonArray.getJSONObject(i);
                final String accountId = json.getString("id");
                if (null == accountId) {
                    throw AjaxExceptionCodes.MISSING_PARAMETER.create("id");
                }
                /*
                 * Delete account
                 */
                final OAuthService oAuthService = getOAuthService();
                oAuthService.deleteAccount(getUnsignedInteger(accountId), session.getUserId(), session.getContextId());
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.JSON_ERROR.create(e);
                }
            }
        } else if (data == null) {
        	request.require("id");
        	int id = request.getParameter("id", int.class).intValue();
        	final OAuthService oAuthService = getOAuthService();
            oAuthService.deleteAccount(id, session.getUserId(), session.getContextId());

        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Boolean.TRUE);
    }

}
