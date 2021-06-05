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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * A class implementing the "all" action for listing messaging accounts. Optionally only accounts of a certain service
 * are returned. Parameters are:
 * <dl>
 *  <dt>messagingService</dt><dd>(optional) The ID of the messaging service. If present lists only accounts of this service.</dd>
 * </dl>
 * Returns a JSONArray of JSONObjects representing the messaging accounts.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllAction extends AbstractMessagingAccountAction {

    public AllAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {

        final String messagingServiceId = request.getParameter("messagingService");

        final List<MessagingService> services = new ArrayList<MessagingService>();
        if (messagingServiceId != null) {
            services.add(registry.getMessagingService(messagingServiceId, session.getUserId(), session.getContextId()));
        } else {
            services.addAll(registry.getAllServices(session.getUserId(), session.getContextId()));
        }

        final JSONArray result = new JSONArray();

        for (final MessagingService messagingService : services) {
            final boolean isMail = ("com.openexchange.messaging.mail".equals(messagingService.getId()));
            NextAccount: for (final MessagingAccount account : messagingService.getAccountManager().getAccounts(session)) {
                if (isMail && account.getId() == 0) {
                    /*
                     * The primary mail account
                     */
                    continue NextAccount;
                }
                result.put(writer.write(account));
            }
        }

        return new AJAXRequestResult(result);
    }

}
