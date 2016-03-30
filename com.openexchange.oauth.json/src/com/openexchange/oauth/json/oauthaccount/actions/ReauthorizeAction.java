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

import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.annotations.Module;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.json.Tools;
import com.openexchange.oauth.json.oauthaccount.AccountField;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReauthorizeAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Module(name = "oauth", description = "The OAuth module is used to manage multiple OAuth accounts for certian online services for a user. The OAuth mechanism allows the Open-Xchange application to act as behalf of this user using previously obtained access tokens granted by user.")
public class ReauthorizeAction extends AbstractOAuthTokenAction {

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final String accountId = request.getParameter("id");
        if (null == accountId) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( "id");
        }
        final int id = Tools.getUnsignedInteger(accountId);
        if (id < 0) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("id", Integer.valueOf(id));
        }

        final String serviceId = request.getParameter(AccountField.SERVICE_ID.getName());
        if (serviceId == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( AccountField.SERVICE_ID.getName());
        }
        final OAuthService oAuthService = getOAuthService();

        OAuthServiceMetaData service = oAuthService.getMetaDataRegistry().getService(serviceId, session.getUserId(), session.getContextId());

        Map<String, Object> arguments = processOAuthArguments(request, session, service);

        /*
         * By now it doesn't matter which interaction type is passed
         */
        oAuthService.updateAccount(id, serviceId, OAuthInteractionType.CALLBACK, arguments, session.getUserId(), session.getContextId());
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(Boolean.TRUE);

    }

}
