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

import static com.openexchange.messaging.json.MessagingAccountConstants.CONFIGURATION;
import static com.openexchange.messaging.json.MessagingAccountConstants.DISPLAY_NAME;
import static com.openexchange.messaging.json.MessagingAccountConstants.ID;
import static com.openexchange.messaging.json.MessagingAccountConstants.MESSAGING_SERVICE;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormContentWriter;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;

/**
 * Renders a MessagingAccount in its JSON representation also using the dynamic form description of the parent messaging service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingAccountWriter {

    /**
     * Initializes a new {@link MessagingAccountWriter}.
     */
    public MessagingAccountWriter() {
        super();
    }

    public JSONObject write(final MessagingAccount account) throws JSONException {
        final JSONObject accountJSON = new JSONObject();
        accountJSON.put(ID, account.getId());
        accountJSON.put(DISPLAY_NAME, account.getDisplayName());
        final MessagingService messagingService = account.getMessagingService();
        accountJSON.put(MESSAGING_SERVICE, messagingService.getId());
        final DynamicFormDescription formDescription = messagingService.getFormDescription();
        if (null != formDescription && null != account.getConfiguration()) {
            final JSONObject configJSON = FormContentWriter.write(formDescription, account.getConfiguration(), null);
            accountJSON.put(CONFIGURATION, configJSON);
        }
        return accountJSON;
    }

}
