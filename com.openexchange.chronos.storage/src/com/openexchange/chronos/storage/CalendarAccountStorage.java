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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarAccountStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarAccountStorage {

    /**
     * Creates a new calendar account.
     *
     * @param providerId The identifier of the corresponding calendar provider
     * @param userId The identifier of the user the account is added for
     * @param configuration The provider-specific configuration data for the calendar account
     * @return The identifier of the newly inserted calendar account
     */
    int createAccount(String providerId, int userId, JSONObject internalConfig, JSONObject userConfig) throws OXException;

    /**
     * Updates the <i>internal</i> configuration data for an existing calendar account.
     *
     * @param userId The identifier of the user to update the account for
     * @param id The identifier of the account to update
     * @param internalConfig The provider-specific <i>internal</i> configuration data for the calendar account, or <code>null</code> to skip
     * @param userConfig The provider-specific <i>user</i> configuration data for the calendar account, or <code>null</code> to skip
     * @param timestamp The last-known timestamp of the account to catch concurrent modifications, or {@link CalendarUtils#DISTANT_FUTURE} to circumvent the check
     */
    void updateAccount(int userId, int id, JSONObject internalConfig, JSONObject userConfig, long timestamp) throws OXException;

    /**
     * Deletes an existing calendar account.
     *
     * @param userId The identifier of the user to delete the account for
     * @param id The identifier of the account to delete
     */
    void deleteAccount(int userId, int id) throws OXException;

    /**
     * Gets an existing calendar account.
     *
     * @param userId The identifier of the user to get the account for
     * @param id The identifier of the account to load
     * @return The loaded calendar account, or <code>null</code> if not found
     */
    CalendarAccount getAccount(int userId, int id) throws OXException;

    /**
     * Gets a list of all calendar accounts stored for a specific user.
     *
     * @param userId The identifier of the user to get the accounts for
     * @return The accounts, or an empty list if none were found
     */
    List<CalendarAccount> getAccounts(int userId) throws OXException;

    /**
     * Gets a list of all stored accounts of certain users for a specific calendar provider.
     *
     * @param providerId The identifier of the provider to get the accounts from
     * @param userIds The identifiers of the users to get the accounts from
     * @return The accounts, or an empty list if there are none
     */
    List<CalendarAccount> getAccounts(String providerId, int[] userIds) throws OXException;

}
