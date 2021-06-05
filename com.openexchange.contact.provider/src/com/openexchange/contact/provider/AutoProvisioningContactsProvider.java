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

package com.openexchange.contact.provider;

import org.json.JSONObject;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AutoProvisioningContactsProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface AutoProvisioningContactsProvider extends ContactsProvider {

    /**
     * Initialises the configuration prior creating a new contacts account. This method is invoked in case no existing contacts account
     * of the provider is found for the current session's user, allowing to auto-provision a corresponding account that is persisted
     * afterwards for later usage.
     * <p/>
     * <b>Note:</b> Auto-provisioning is only triggered for users that have the provider-specific capability assigned.
     * <p/>
     * Upon success, any <i>internal</i> configuration data is returned for persisting along with the newly created contacts account.
     *
     * @param session The user's session
     * @param userConfig The (empty) <i>user</i> configuration to populate with defaults
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A JSON object holding the <i>internal</i> configuration to store along with the new account, or <code>null</code>
     */
    JSONObject autoConfigureAccount(Session session, JSONObject userConfig, ContactsParameters parameters) throws OXException;
}
