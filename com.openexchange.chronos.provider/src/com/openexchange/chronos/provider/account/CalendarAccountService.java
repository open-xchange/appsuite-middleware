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
import java.util.Map;
import com.openexchange.chronos.provider.CalendarAccount;
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
     * Creates a new calendar account and checks for permissions.
     * 
     * @param session The current user session
     * @param providerId The identifier of the corresponding calendar provider
     * @param userId The identifier of the user the account is added for
     * @param configuration The provider-specific configuration data for the calendar account
     * @return The loaded calendar account, or <code>null</code> if not found
     * @throws OXException if a database error occurs
     */
    CalendarAccount createAccount(Session session, String providerId, Map<String, Object> configuration) throws OXException;

    /**
     * Updates an existing calendar account and checks for permissions.
     * 
     * @param session The current user session 
     * @param id The identifier of the account to update
     * @param configuration The provider-specific configuration data for the calendar account
     * @param timestamp The timestamp that specifies the last modification of the account
     * @return CalendarAccount The updated calendar account 
     * @throws OXException if permission check fails
     */
    CalendarAccount updateAccount(Session session, int id, Map<String, Object> configuration, long timestamp) throws OXException;

    /**
     * Deletes an existing calendar account and checks for permissions.
     * 
     * @param session The current user session 
     * @param id The identifier of the account to delete
     * @param timestamp The timestamp that specifies the last modification of the account
     * @throws OXException if permission check fails
     */
    void deleteAccount(Session session, int id, long timestamp) throws OXException;

    /**
     * Gets an existing calendar account and checks for permissions.
     * 
     * @param session The current user session
     * @param id The identifier of the account to load
     * @return The loaded calendar account, or <code>null</code> if not found
     * @throws OXException if permission check fails
     */
    CalendarAccount getAccount(Session session, int id) throws OXException;

    /**
     * Gets a list of all calendar accounts stored for a specific user and checks for permissions.
     * 
     * @param session The current user session
     * @return The accounts, or an empty list if none were found
     * @throws OXException if permission check fails
     */
    List<CalendarAccount> getAccounts(Session session) throws OXException;

    /**
     * Gets a list of all calendar accounts stored for s specific user and provider, also checks for permissions
     * 
     * @param session The current user session
     * @param providerId The providerId to search with
     * @return The accounts, or an empty list if none were found
     * @throws OXException if permission check fails
     */
    List<CalendarAccount> getAccounts(Session session, String providerId) throws OXException;

}
