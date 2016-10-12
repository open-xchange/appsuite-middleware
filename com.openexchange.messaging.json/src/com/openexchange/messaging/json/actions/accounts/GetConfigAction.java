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

package com.openexchange.messaging.json.actions.accounts;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.ConfigProvidingMessagingService;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * Loads a messaging account. Parameters are:
 * <dl>
 *  <dt>messagingService</dt> <dd>The ID of the messaging service. </dd>
 *  <dt>id</dt><dd>The id of the messaging service that is to be loaded</dd>
 * </dl>
 * Throws an exception upon an error or returns the loaded MessagingAccount JSON representation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetConfigAction extends AbstractMessagingAccountAction {

    public GetConfigAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws OXException, JSONException {
        final List<String> missingParameters = request.getMissingParameters("messagingService", "id");
        if(!missingParameters.isEmpty()) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }
        /*
         * Get service identifier
         */
        final String messagingServiceId = request.getParameter("messagingService");
        /*
         * Get account identifier
         */
        final int id;
        final String idS = request.getParameter("id");
        try {
            id = Integer.parseInt(idS);
        } catch (final NumberFormatException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("id", idS);
        }
        /*
         * Get configuration
         */
        final Map<String, Object> configuration;
        {
            final MessagingService messagingService = registry.getMessagingService(messagingServiceId, session.getUserId(), session.getContextId());
            if (messagingService instanceof ConfigProvidingMessagingService) {
                configuration = ((ConfigProvidingMessagingService) messagingService).getConfiguration(id, session);
            } else {
                configuration = messagingService.getAccountManager().getAccount(id, session).getConfiguration();
            }
        }
        /*
         * Compose JSON object from configuration
         */
        final JSONObject jsonObject = new JSONObject();
        for (final Entry<String, Object> entry : configuration.entrySet()) {
            jsonObject.put(entry.getKey(), JSONCoercion.coerceToJSON(entry.getValue()));
        }
        return new AJAXRequestResult(jsonObject);
    }

}
