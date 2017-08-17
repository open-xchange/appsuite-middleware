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

package com.openexchange.chronos.account.json.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.chronos.account.json.CalendarAccountFields;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class UpdateAction extends AbstractAccountAction implements CalendarAccountFields{

    /**
     * Initialises a new {@link UpdateAction}.
     * 
     * @param services
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        String providerId = requestData.getParameter(PARAMETER_PROVIDER_ID);
        if (Strings.isEmpty(providerId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_PROVIDER_ID);
        }
        String accountId = requestData.getParameter(PARAMETER_ACCOUNT_ID);
        if (Strings.isEmpty(accountId)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ACCOUNT_ID);
        }
        CalendarAccountService service = getService(CalendarAccountService.class);
        CalendarAccount account = service.loadAccount(session, Integer.parseInt(accountId));
        // Updates
        account = service.updateAccount(session, Integer.parseInt(accountId), account.getConfiguration(), account.getLastModified().getTime());

        JSONObject response = new JSONObject();
        try {
            response.put(ID, JSONCoercion.coerceToJSON(account.getAccountId()));
            response.put(PROVIDER, JSONCoercion.coerceToJSON(account.getProviderId()));
            response.put(LAST_MODIFIED, JSONCoercion.coerceToJSON(account.getLastModified().getTime()));
            response.put(CONFIGURATION, JSONCoercion.coerceToJSON(account.getConfiguration()));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
        return new AJAXRequestResult(response);
    }
}
