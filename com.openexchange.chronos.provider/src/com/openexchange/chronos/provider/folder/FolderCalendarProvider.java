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
