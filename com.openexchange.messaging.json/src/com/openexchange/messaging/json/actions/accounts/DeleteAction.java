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
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.DefaultMessagingAccount;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * This action deletes a messaging account. Parameters are:
 * <dl>
 *  <dt>messagingService</dt> <dd>The ID of the messaging service. </dd>
 *  <dt>id</dt><dd>The id of the messaging service that is to be deleted</dd>
 * </dl>
 * Throws an exception upon an error or returns "1" on success.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteAction extends AbstractMessagingAccountAction {

    public DeleteAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        final List<String> missingParameters = request.getMissingParameters("messagingService", "id");
        if (!missingParameters.isEmpty()) {
            throw MessagingExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }
        final String messagingServiceId = request.getParameter("messagingService");

        int id = 0;
        final String idS = request.getParameter("id");
        try {
            id = Integer.parseInt(idS);
        } catch (NumberFormatException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("id", idS);
        }

        final MessagingService messagingService = registry.getMessagingService(messagingServiceId, session.getUserId(), session.getContextId());

        final DefaultMessagingAccount messagingAccount = new DefaultMessagingAccount();
        messagingAccount.setMessagingService(messagingService);
        messagingAccount.setId(id);
        messagingService.getAccountManager().deleteAccount(messagingAccount, session);

        return new AJAXRequestResult(Integer.valueOf(1));
    }

}
