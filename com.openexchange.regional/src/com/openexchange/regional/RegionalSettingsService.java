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

package com.openexchange.regional;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import com.openexchange.exception.OXException;

/**
 * {@link RegionalSettingsService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public interface RegionalSettingsService {

    /**
     * Gets the {@link RegionalSettingsImpl} for the given user
     *
     * @param contextId The context id
     * @param userId The user id
     * @return The {@link RegionalSettingsImpl} for the user or <code>null</code> if the user
     *         does not have any custom regional settings stored
     */
    RegionalSettings get(int contextId, int userId);

    /**
     * Creates or updates the regional settings of the user
     *
     * @param contextId The context id
     * @param userId The user id
     * @param settings The new settings
     * @param locale The locale of the user
     * @throws OXException if the operation fails
     */
    void save(int contextId, int userId, RegionalSettings settings, Locale locale) throws OXException;

    /**
     * Removes the custom regional settings for the given user
     * 
     * @param contextId The context id
     * @param id The user id
     * @throws OXException if the operation fails
     */
    void delete(int contextId, int id) throws OXException;

    /**
     * Gets a date format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param style The formatting style. @see {@link DateFormat#getDateInstance(int, Locale)}
     * @return The {@link DateFormat}
     */
    DateFormat getDateFormat(int contextId, int userId, Locale locale, int style);

    /**
     * Gets a time format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param style The formatting style. @see {@link DateFormat#getTimeInstance(int, Locale)}
     * @return The {@link DateFormat}
     */
    DateFormat getTimeFormat(int contextId, int userId, Locale locale, int type);

    /**
     * Gets a date/time format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param dateStyle The formatting style. @see {@link DateFormat#getDateInstance(int, Locale)}
     * @param timeStyle The formatting style. @see {@link DateFormat#getTimeInstance(int, Locale)}
     * @return The {@link DateFormat}
     */
    DateFormat getDateTimeFormat(int contextId, int userId, Locale locale, int dateStyle, int timeStyle);

    /**
     * Gets a number format for the specified locale. If the user has a custom format defined this format is used instead.
     * 
     * @param contextId The context id
     * @param userId The user id
     * @param locale The locale of the user
     * @param format The format to use. E.g. <code>###.###,##</code>
     * @return The {@link NumberFormat}
     */
    NumberFormat getNumberFormat(int contextId, int userId, Locale locale, String format);
}
