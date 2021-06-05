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

package com.openexchange.chronos.provider.basic;

import org.json.JSONObject;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link BasicCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface BasicCalendarProvider extends CalendarProvider {

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
     * @param settings Calendar settings to be probed for the new account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return The proposed calendar settings, enhanced by additional default values
     */
    CalendarSettings probe(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    /**
     * Initializes the configuration prior creating a new calendar account.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any <i>internal</i> configuration data is returned for
     * persisting along with the newly created calendar account.
     * <p/>
     * <b>Note: </b>Since this method is usually invoked within a running database transaction, refrain from performing possibly
     * long-running checks, especially not over the network. Those checks have usually been executed before (during the {@link #probe}
     * -phase of the settings).
     *
     * @param session The user's session
     * @param settings Calendar settings for the new account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the <i>internal</i> configuration to store along with the new account
     */
    JSONObject configureAccount(Session session, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    /**
     * Re-initializes the configuration prior updating an existing calendar account.
     * <p/>
     * Any permission- or data validation checks are performed during this initialization phase. In case of erroneous or incomplete
     * configuration data, an appropriate exception will be thrown. Upon success, any updated <i>internal</i> configuration data is
     * returned for persisting along with the updated calendar account.
     *
     * @param session The user's session
     * @param account The currently stored calendar account holding the obsolete user and current <i>internal</i> configuration
     * @param settings The updated settings for the updated account as supplied by the client
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     * @return A JSON object holding the updated <i>internal</i> configuration to store along with update, or <code>null</code> if unchanged
     */
    JSONObject reconfigureAccount(Session session, CalendarAccount account, CalendarSettings settings, CalendarParameters parameters) throws OXException;

    /**
     * Initializes the connection to a specific calendar account.
     *
     * @param session The user's session
     * @param account The calendar account to connect to
     * @param parameters Additional calendar parameters
     * @return The connected calendar access
     */
    @Override
    BasicCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException;

    /**
     * Override if applicable.
     */
    @Override
    default int getDefaultMaxAccounts() {
        return 25;
    }

}
