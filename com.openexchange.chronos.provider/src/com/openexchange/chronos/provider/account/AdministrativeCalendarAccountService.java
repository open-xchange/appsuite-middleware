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

package com.openexchange.chronos.provider.account;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link AdministrativeCalendarAccountService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface AdministrativeCalendarAccountService {

    /**
     * Gets a list of all stored calendar accounts of a certain user.
     * <p/>
     * Not yet existing accounts from registered auto-provisioning providers won't be created implicitly.
     *
     * @param contextId The context identifier
     * @param userId The identifier of the users to get the accounts from
     * @return The accounts, or an empty list if there are none
     */
    List<CalendarAccount> getAccounts(int contextId, int userId) throws OXException;

    /**
     * Gets a specific calendar account.
     * <p/>
     * Not yet existing accounts from registered auto-provisioning providers won't be created implicitly.
     *
     * @param contextId The context identifier
     * @param userId The identifier of the users to get the account for
     * @param id The identifier of the account to get
     * @return The account, or <code>null</code> if there is none
     */
    CalendarAccount getAccount(int contextId, int userId, int id) throws OXException;

    /**
     * Gets a list of all accounts of certain users in a context for a specific calendar provider.
     * <p/>
     * Not yet existing accounts from registered auto-provisioning providers won't be created implicitly.
     *
     * @param contextId The context identifier
     * @param userIds The identifiers of the users to get the accounts from
     * @param providerId The identifier of the provider to get the accounts from
     * @return The accounts, or an empty list if there are none
     */
    List<CalendarAccount> getAccounts(int contextId, int[] userIds, String providerId) throws OXException;

    /**
     * Gets the (first) account of a certain user in a context for a specific calendar provider.
     * <p/>
     * Not yet existing accounts from registered auto-provisioning providers won't be created implicitly.
     *
     * @param contextId The context identifier
     * @param userId The identifier of the user to get the account from
     * @param providerId The identifier of the provider to get the account from
     * @return The account, or <code>null</code> if there is none
     */
    CalendarAccount getAccount(int contextId, int userId, String providerId) throws OXException;

    /**
     * Gets all accounts of a certain user in a context for a specific calendar provider.
     * <p/>
     * Not yet existing accounts from registered auto-provisioning providers won't be created implicitly.
     *
     * @param contextId The context identifier
     * @param userIds The identifier of the user to get the accounts from
     * @param providerId The identifier of the provider to get the accounts from
     * @return The accounts, or <code>null</code> if there are none
     */
    List<CalendarAccount> getAccounts(int contextId, int userId, String providerId) throws OXException;

    /**
     * Updates the configuration data of a specific calendar account.
     *
     * @param contextId The context identifier
     * @param userId The identifier of the user owning the account
     * @param id The identifier of the account to update
     * @param internalConfig The provider-specific <i>internal</i> configuration data for the calendar account, or <code>null</code> to skip
     * @param userConfig The provider-specific <i>user</i> configuration data for the calendar account, or <code>null</code> to skip
     * @param clientTimestamp The last-known timestamp of the account to catch concurrent modifications, or {@link CalendarUtils#DISTANT_FUTURE} to circumvent the check
     * @return The updated calendar account
     */
    CalendarAccount updateAccount(int contextId, int userId, int id, JSONObject internalConfig, JSONObject userConfig, long clientTimestamp) throws OXException;

    /**
     * Deletes the given accounts
     *
     * @param contextId The context identifier
     * @param userId The identifier of the user owning the accounts
     * @param accounts The accounts to delete
     */
    void deleteAccounts(int contextId, int userId, List<CalendarAccount> accounts) throws OXException;

}
