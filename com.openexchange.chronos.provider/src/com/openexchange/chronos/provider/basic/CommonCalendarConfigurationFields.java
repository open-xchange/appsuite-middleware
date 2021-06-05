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

import com.openexchange.chronos.provider.CalendarFolderProperty;

/**
 * {@link CommonCalendarConfigurationFields} - Specifies the common fields of calendar configurations
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class CommonCalendarConfigurationFields {

    /**
     * The folder's description
     */
    public static final String DESCRIPTION = CalendarFolderProperty.DESCRIPTION_LITERAL;

    /**
     * The folder's color
     */
    public static final String COLOR = CalendarFolderProperty.COLOR_LITERAL;

    /**
     * The user configuration's key for the folder's name
     */
    public static final String NAME = "name";

    /**
     * The URI of an external calendar subscription source.
     */
    public static final String URI = "uri";

    /**
     * The interval at which the data for an external subscription is refreshed (in minutes).
     */
    public static final String REFRESH_INTERVAL = "refreshInterval";

    /**
     * Flag indicating whether the folder is used for sync
     */
    public static final String USED_FOR_SYNC = CalendarFolderProperty.USED_FOR_SYNC_LITERAL;

}
