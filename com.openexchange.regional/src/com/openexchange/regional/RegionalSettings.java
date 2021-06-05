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

import java.io.Serializable;

/**
 * {@link RegionalSettings}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public interface RegionalSettings extends Serializable {

    /**
     * Gets the time format
     *
     * @return The time format
     */
    String getTimeFormat();

    /**
     * Gets the long time format
     *
     * @return The long time format
     */
    String getTimeFormatLong();

    /**
     * Gets the date format
     *
     * @return The date format
     */
    String getDateFormat();

    /**
     * Gets the short date format
     *
     * @return The short date format
     */
    String getDateFormatShort();

    /**
     * Gets the medium date format
     *
     * @return The medium date format
     */
    String getDateFormatMedium();

    /**
     * Gets the long date format
     *
     * @return The long date format
     */
    String getDateFormatLong();

    /**
     * Gets the full date format
     *
     * @return The full date format
     */
    String getDateFormatFull();

    /**
     * Gets the number format
     *
     * @return The number format
     */
    String getNumberFormat();

    /**
     * Gets the first day of the week
     *
     * @return The number of the first day of the week starting with Sunday
     */
    Integer getFirstDayOfWeek();

    /**
     * Gets the first day of the year
     *
     * @return the first day of the year
     */
    Integer getFirstDayOfYear();

}
