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

import java.util.List;
import org.json.JSONObject;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.basic.ContactsSettings;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ContactsAccountService} - Provides access and CRUD operations to the contacts accounts
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
@SingletonService
public interface ContactsAccountService {

    /**
     * Probes specific client-supplied, possibly erroneous and/or incomplete contacts settings by checking if they are valid or further
     * configuration settings are required. This step is typically performed prior creating a new account.
     * <p/>
     * In case the settings are valid and can be used to create a new contacts account, the result will contain the proposed contacts
     * settings, which may be enhanced by additional default values for certain properties of the contacts account. The client is encouraged to
     * create the account with these settings, then.
     * <p/>
     * In case the settings are invalid or incomplete, an appropriate exception is thrown providing further details about the root cause.
     *
     * @param session The user's session
     * @param providerId The identifier of the corresponding contacts provider
     * @param settings The contacts settings to be probed for the new account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The proposed contacts settings, enhanced by additional default values
     */
    ContactsSettings probeAccountSettings(Session session, String providerId, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Creates a new contacts account for the current user
     *
     * @param session The current session
     * @param providerId The contacts provider identifier
     * @param settings The contacts settings for the new account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The created contacts account
     * @throws OXException if an error is occurred
     */
    ContactsAccount createAccount(Session session, String providerId, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Updates an existing contacts account
     *
     * @param session The current session
     * @param id The account identifier
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     * @param settings The contacts settings for the new account as supplied by the client
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The updated contacts account
     * @throws OXException if an error is occurred
     */
    ContactsAccount updateAccount(Session session, int id, long clientTimestamp, ContactsSettings settings, ContactsParameters parameters) throws OXException;

    /**
     * Updates an existing contacts account.
     *
     * @param session The current session
     * @param id The identifier of the account to update
     * @param userConfig Updated provider-specific <i>user</i> configuration data for the contacts account
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The updated contacts account
     */
    ContactsAccount updateAccount(Session session, int id, JSONObject userConfig, long clientTimestamp, ContactsParameters parameters) throws OXException;

    /**
     * Deletes an existing contacts account, i.e. the one with the specified identifier
     *
     * @param session The current session
     * @param id The contacts account identifier
     * @param clientTimestamp The last timestamp known by the client (for considering concurrent updates)
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @throws OXException if an error is occurred
     */
    void deleteAccount(Session session, int id, long clientTimestamp, ContactsParameters parameters) throws OXException;

    /**
     * Gets an existing contact account.
     *
     * @param session The current user session
     * @param id The identifier of the account to load
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return The loaded contact account
     * @throws OXException if an error is occurred
     */
    ContactsAccount getAccount(Session session, int id, ContactsParameters parameters) throws OXException;

    /**
     * Retrieves a list with all contacts accounts for the current user
     *
     * @param session The current session
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A {@link List} with the contacts accounts
     * @throws OXException if an error is occurred
     */
    List<ContactsAccount> getAccounts(Session session, ContactsParameters parameters) throws OXException;

    /**
     * Retrieves a list with the contacts accounts that match the specified ids
     *
     * @param session The current session
     * @param ids The contacts accounts' identifiers
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A {@link List} with the contacts accounts
     * @throws OXException if an error is occurred
     */
    List<ContactsAccount> getAccounts(Session session, List<Integer> ids, ContactsParameters parameters) throws OXException;

    /**
     * Retrieves a list with all contacts accounts that match the specified provider and user
     *
     * @param session The current session
     * @param providerId The provider identifier
     * @param parameters Additional contacts parameters, or <code>null</code> if not set
     * @return A {@link List} with the contacts accounts
     * @throws OXException if an error is occurred
     */
    List<ContactsAccount> getAccounts(Session session, String providerId, ContactsParameters parameters) throws OXException;
}
