/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        if (!missingParameters.isEmpty()) {
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
        } catch (NumberFormatException x) {
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
