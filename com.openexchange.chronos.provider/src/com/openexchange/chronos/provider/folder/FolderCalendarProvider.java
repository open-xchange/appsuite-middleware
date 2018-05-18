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

package com.openexchange.chronos.provider.folder;

import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FolderCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface FolderCalendarProvider extends CalendarProvider {

    /**
     * Initializes the configuration prior creating a new calendar account.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any <i>internal</i> configuration data is returned for
     * persisting along with the newly created calendar account.
     *
     * @param session The user's session
     * @param userConfig The <i>user</i> configuration as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the <i>internal</i> configuration to store along with the new account
     */
    JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException;

    /**
     * Re-initializes the configuration prior updating an existing calendar account.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any updated <i>internal</i> configuration data is
     * returned for persisting along with the updated calendar account.
     *
     * @param session The user's session
     * @param calendarAccount The currently stored calendar account holding the obsolete user and current <i>internal</i> configuration
     * @param userConfig The updated <i>user</i> configuration as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the updated <i>internal</i> configuration to store along with update, or <code>null</code> if unchanged
     */
    JSONObject reconfigureAccount(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException;

    /**
     * Initializes the connection to a specific calendar account.
     *
     * @param session The user's session
     * @param account The calendar account to connect to
     * @param parameters Additional calendar parameters
     * @return The connected calendar access
     */
    @Override
    FolderCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Override if applicable.
     */
    @Override
    default int getDefaultMaxAccounts() {
        return 1;
    }

}
