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

package com.openexchange.messaging.json;

import static com.openexchange.messaging.json.MessagingAccountConstants.ID;
import static com.openexchange.messaging.json.MessagingAccountConstants.MESSAGING_SERVICE;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.DefaultMessagingAccount;
import com.openexchange.messaging.registry.MessagingServiceRegistry;

/**
 * Parses the JSON representation of a messaging account according to its messaging services dynamic form.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingAccountParser {

    private final MessagingServiceRegistry registry;

    public MessagingAccountParser(final MessagingServiceRegistry serviceRegistry) {
        registry = serviceRegistry;
    }

    public MessagingAccount parse(final JSONObject accountJSON, int userId, int contextId) throws OXException, JSONException {
        final DefaultMessagingAccount account = new DefaultMessagingAccount();

        account.setId(accountJSON.optInt(ID));
        if (accountJSON.has("displayName")) {
            account.setDisplayName(accountJSON.optString("displayName"));
        }
        final MessagingService messagingService = registry.getMessagingService(accountJSON.getString(MESSAGING_SERVICE), userId, contextId);
        account.setMessagingService(messagingService);
        if (accountJSON.has("configuration")) {
            account.setConfiguration(FormContentParser.parse(
                accountJSON.getJSONObject("configuration"),
                messagingService.getFormDescription()));
        }

        return account;
    }
}
