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

import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * Creates a new MessagingAccount. The body of the request must contain the JSON representation of the given account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NewAction extends AbstractMessagingAccountAction {

    /**
     * Initializes a new {@link NewAction}.
     *
     * @param registry The {@link MessagingServiceRegistry}
     */
    public NewAction(final MessagingServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        final MessagingAccount account = parser.parse((JSONObject) request.requireData(), session.getUserId(), session.getContextId());
        saneConfiguration(account);

        // Check integrity of messaging service and configuration
        checkAccountConfiguration(account, session);

        final int id = account.getMessagingService().getAccountManager().addAccount(account, session);
        return new AJAXRequestResult(Integer.valueOf(id));
    }

    /**
     * Checks whether the configuration contains any json null values and removes it
     *
     * @param account The {@link MessagingAccount} to check
     */
    private static void saneConfiguration(final MessagingAccount account) {
        if (null == account) {
            return;
        }
        for (final Iterator<Entry<String, Object>> it = account.getConfiguration().entrySet().iterator(); it.hasNext();) {
            if (JSONObject.NULL.equals(it.next().getValue())) {
                it.remove();
            }
        }
    }

}
