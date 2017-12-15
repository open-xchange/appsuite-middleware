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
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link CalendarAccountService}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarAccountService {

    /**
     * Gets a list of registered calendar providers.
     *
     * @return The calendar providers
     */
    List<CalendarProvider> getProviders() throws OXException;

    /**
     * Probes specific client-supplied, possibly erroneous and/or incomplete calendar settings by checking if they are valid or further
     * configuration settings are required. This step is typically performed prior creating a new account.
     * <p/>
     * In case the settings are valid and can be used to create a new calendar account, the result will contain the proposed calendar
     * settings, which may be enhanced by additional default values for certain properties of the calendar. The client is encouraged to
     * create the account with these settings, then.
     * <p/>
     * In case the settings are invalid or incomplete, an appropriate exception is thrown providing further details about the root cause.
     *
     * @param session The user's session
     * @param providerId The identifier of the corresponding calendar provider
     * @param settings Calendar settings to be probed for the new account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return The proposed calendar settings, enhanced by additional default values
     */
    CalendarSettings probeAccountSettings(Session session, String providerId, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    /**
     * Creates a new calendar account for the current session' user.
     *
     * @param session The current session
     * @param providerId The identifier of the corresponding calendar provider
     * @param userConfig The account's external / user configuration data
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return The created calendar account
     */
    CalendarAccount createAccount(Session session, String providerId, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    /**
     * Updates an existing calendar account.
     *
     * @param session The current session
     * @param id The identifier of the account to update
     * @param userConfig The account's external / user configuration data
     * @param enabled {@link Boolean#TRUE} to enable the account, {@link Boolean#FALSE} to disable the account, or <code>null</code> to skip
     * @param clientTimestamp The last timestamp known by the client to catch concurrent updates
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return The updated calendar account
     */
    CalendarAccount updateAccount(Session session, int id, CalendarSettings settings, long clientTimestamp, CalendarParameters parameters) throws OXException;

    /**
     * Deletes an existing calendar account.
     *
     * @param session The current user session
     * @param id The identifier of the account to delete
     * @param timestamp The timestamp that specifies the last modification of the account
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @throws OXException if permission check fails
     */
    void deleteAccount(Session session, int id, long timestamp, CalendarParameters parameters) throws OXException;

    /**
     * Gets an existing calendar account.
     *
     * @param session The current user session
     * @param id The identifier of the account to load
     * @return The loaded calendar account, or <code>null</code> if not found
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @throws OXException if permission check fails
     */
    CalendarAccount getAccount(Session session, int id, CalendarParameters parameters) throws OXException;

    /**
     * Gets a list of all calendar accounts stored for a specific user.
     *
     * @param session The current user session
     * @return The accounts, or an empty list if none were found
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @throws OXException if permission check fails
     */
    List<CalendarAccount> getAccounts(Session session, CalendarParameters parameters) throws OXException;

    /**
     * Gets a list of all calendar accounts stored for s specific user and provider.
     *
     * @param session The current user session
     * @param providerId The providerId to search with
     * @return The accounts, or an empty list if none were found
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @throws OXException if permission check fails
     */
    List<CalendarAccount> getAccounts(Session session, String providerId, CalendarParameters parameters) throws OXException;

}
