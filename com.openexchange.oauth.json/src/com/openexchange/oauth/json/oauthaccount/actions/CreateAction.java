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

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.SecureContentWrapper;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.oauth.json.oauthaccount.AccountWriter;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(noSecretCallback = true)
public final class CreateAction extends AbstractOAuthTokenAction {

    /**
     * Initializes a new {@link CreateAction}.
     */
    public CreateAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            // The meta data identifier
            final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
            if (serviceId == null) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(AccountField.SERVICE_ID.getName());
            }

            // Get service meta data
            final OAuthService oAuthService = getOAuthService();
            final OAuthServiceMetaData service;
            {
                final OAuthServiceMetaDataRegistry registry = oAuthService.getMetaDataRegistry();
                service = registry.getService(serviceId, session.getUserId(), session.getContextId());
            }
            final Map<String, Object> arguments = processOAuthArguments(request, session, service);

            // Get the scopes
            Set<OAuthScope> scopes = getScopes(request, serviceId);

            // By now it doesn't matter which interaction type is passed
            OAuthAccount newAccount;
            try {
                newAccount = oAuthService.createAccount(serviceId, OAuthInteractionType.CALLBACK, arguments, session.getUserId(), session.getContextId(), scopes);
            } catch (OXException e) {
                // Create attempt failed
                HttpServletResponse response = request.optHttpServletResponse();
                if (null == response) {
                    throw e;
                }

                try {
                    Tools.sendErrorPage(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                } catch (IOException ioe) {
                    throw OAuthExceptionCodes.IO_ERROR.create(ioe, ioe.getMessage());
                }
            }

            // Write as JSON
            final JSONObject jsonAccount = AccountWriter.write(newAccount, session);
            // Return appropriate result
            return new AJAXRequestResult(new SecureContentWrapper(jsonAccount, "json"), SecureContentWrapper.CONTENT_TYPE);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }
}
