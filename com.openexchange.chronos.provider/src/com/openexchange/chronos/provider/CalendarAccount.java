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

package com.openexchange.chronos.provider;

import java.io.Serializable;
import java.util.Date;
import org.json.JSONObject;

/**
 * {@link CalendarAccount}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public interface CalendarAccount extends Serializable {

    /** The default <i>internal</i> calendar provider account */
    static final CalendarAccount DEFAULT_ACCOUNT = new DefaultCalendarAccount("chronos", 0, 0, null, null, null);

    /**
     * Gets the account's identifier.
     *
     * @return The identifier of the account
     */
    int getAccountId();

    /**
     * Gets the identifier of the calendar provider this account is associated with.
     *
     * @return The identifier of the calendar provider
     */
    String getProviderId();

    /**
     * Gets the identifier of the user this account is registered for.
     *
     * @return The identifier of the user
     */
    int getUserId();

    /**
     * Gets the last modification timestamp of the calendar account.
     *
     * @return The last modification timestamp
     */
    Date getLastModified();

    /**
     * Gets the account's internal / protected configuration data.
     *
     * @return The internal configuration, or <code>null</code> if not set
     */
    JSONObject getInternalConfiguration();

    /**
     * Gets the account's external / user configuration data.
     *
     * @return The user configuration, or <code>null</code> if not set
     */
    JSONObject getUserConfiguration();

}
