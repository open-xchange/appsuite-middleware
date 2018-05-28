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

package com.openexchange.chronos.storage;

import java.util.List;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAccountStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarAccountStorage {

    /**
     * Generates the next unique identifier for inserting new account data.
     * <p/>
     * <b>Note:</b> This method should only be called within an active transaction, i.e. if the storage has been initialized using
     * {@link DBTransactionPolicy#NO_TRANSACTIONS} in favor of an externally controlled transaction.
     *
     * @return The next unique event identifier
     */
    int nextId() throws OXException;

    /**
     * Inserts a new calendar account.
     *
     * @param account The account data to insert
     */
    void insertAccount(CalendarAccount account) throws OXException;

    /**
     * Inserts a new calendar account, obeying the maximum number of allowed accounts for this provider and user. If the account was not
     * inserted, this is indicated via {@link CalendarExceptionCodes#ACCOUNT_NOT_WRITTEN}.
     *
     * @param account The account data to insert
     * @param maxAccounts The maximum number of accounts allowed for this provider and user
     */
    void insertAccount(CalendarAccount account, int maxAccounts) throws OXException;

    /**
     * Updates an existing calendar account.
     *
     * @param account The account data to update
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     */
    void updateAccount(CalendarAccount account, long clientTimestamp) throws OXException;

    /**
     * Deletes an existing calendar account.
     *
     * @param userId The identifier of the user to delete the account for
     * @param accountId The identifier of the account to delete
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     */
    void deleteAccount(int userId, int accountId, long clientTimestamp) throws OXException;

    /**
     * Loads an existing calendar account.
     *
     * @param userId The identifier of the user to get the account for
     * @param accountId The identifier of the account to load
     * @return The loaded calendar account, or <code>null</code> if not found
     */
    CalendarAccount loadAccount(int userId, int accountId) throws OXException;

    /**
     * Loads multiple existing calendar accounts.
     *
     * @param userId The identifier of the user to get the account for
     * @param accountIds The identifiers of the accounts to load
     * @return The loaded calendar accounts
     */
    CalendarAccount[] loadAccounts(int userId, int[] accountIds) throws OXException;

    /**
     * Loads a list of all calendar accounts stored for a specific user.
     *
     * @param userId The identifier of the user to get the accounts for
     * @return The accounts, or an empty list if none were found
     */
    List<CalendarAccount> loadAccounts(int userId) throws OXException;

    /**
     * Loads a list of all stored accounts of certain users for a specific calendar provider.
     *
     * @param userIds The identifiers of the users to get the accounts from
     * @param providerId The identifier of the provider to get the accounts from
     * @return The accounts, or an empty list if there are none
     */
    List<CalendarAccount> loadAccounts(int[] userIds, String providerId) throws OXException;

    /**
     * Loads the (first) account stored for a specific user of a specific calendar provider.
     *
     * @param userId The identifier of the user to get the account for
     * @param providerId The identifier of the provider to get the account from
     * @return The account, or <code>null</code> if there is none
     */
    CalendarAccount loadAccount(int userId, String providerId) throws OXException;

    /**
     * Invalidates any cached references of a specific calendar account.
     *
     * @param userId The identifier of the user to invalidate the account for
     * @param accountId The identifier of the account to invalidate, or <code>-1</code> to only invalidate the user's list of known accounts
     */
    void invalidateAccount(int userId, int accountId) throws OXException;

}
