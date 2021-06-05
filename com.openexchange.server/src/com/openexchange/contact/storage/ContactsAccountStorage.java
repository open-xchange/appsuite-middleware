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

package com.openexchange.contact.storage;

import java.util.List;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link ContactsAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface ContactsAccountStorage {

    /**
     * Generates the next unique identifier for inserting new account data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favour of an externally controlled transaction.
     *
     * @return The next unique contacts account identifier
     * @throws OXException if an error is occurred
     */
    int nextId() throws OXException;

    /**
     * Inserts a new contacts account.
     *
     * @param account The account data to insert
     * @throws OXException if an error is occurred
     */
    void insertAccount(ContactsAccount account) throws OXException;

    /**
     * Updates an existing contacts account.
     *
     * @param account The account data to update
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     * @throws OXException if an error is occurred
     */
    void updateAccount(ContactsAccount account, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing contacts account.
     *
     * @param userId The identifier of the user to delete the account for
     * @param accountId The identifier of the account to delete
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     * @throws OXException if an error is occurred
     */
    void deleteAccount(int userId, int accountId, long clientTimestamp) throws OXException;

    /**
     * Loads an existing contacts account.
     *
     * @param userId The identifier of the user to get the account for
     * @param accountId The identifier of the account to load
     * @return The loaded contacts account
     * @throws OXException if the account does not exist or any other error is occurred
     */
    ContactsAccount loadAccount(int userId, int accountId) throws OXException;

    /**
     * Loads multiple existing contacts accounts.
     *
     * @param userId The identifier of the user to get the account for
     * @param accountIds The identifiers of the accounts to load
     * @return The loaded contacts accounts
     * @throws OXException if an error is occurred
     */
    ContactsAccount[] loadAccounts(int userId, int[] accountIds) throws OXException;

    /**
     * Loads a list of all contacts accounts stored for a specific user.
     *
     * @param userId The identifier of the user to get the accounts for
     * @return The accounts, or an empty list if none were found
     * @throws OXException if an error is occurred
     */
    List<ContactsAccount> loadAccounts(int userId) throws OXException;

    /**
     * Loads a list of all contacts accounts of specific contact providers stored for a specific user.
     *
     * @param userId The identifier of the user to get the accounts for
     * @param providerIds The identifiers of the provider to get the accounts from, or <code>null</code> to get accounts from all providers
     * @return The accounts, or an empty list if none were found
     * @throws OXException if an error is occurred
     */
    List<ContactsAccount> loadAccounts(int userId, String... providerIds) throws OXException;

    /**
     * Invalidates any cached references of a specific contacts account.
     *
     * @param userId The identifier of the user to invalidate the account for
     * @param accountId The identifier of the account to invalidate, or <code>-1</code> to only invalidate the user's list of known accounts
     */
    void invalidateAccount(int userId, int accountId) throws OXException;

}
