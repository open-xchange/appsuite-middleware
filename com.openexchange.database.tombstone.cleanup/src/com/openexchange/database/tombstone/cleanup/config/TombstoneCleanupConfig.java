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

package com.openexchange.database.tombstone.cleanup.config;

import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.Property;

/**
 *
 * {@link TombstoneCleanupConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class TombstoneCleanupConfig {

    private static final String PREFIX = "com.openexchange.database.tombstone.cleanup.";

    /**
     * Enables or disables the cleanup of tombstone tables.
     */
    public static final Property ENABLED = DefaultProperty.valueOf(PREFIX + "enabled", Boolean.TRUE);

    /**
     * Defines the timespan an entry in any tombstone tables is kept before removing it.
     *
     * A time span specification consists of a number and a unit of measurement. Units are: 
     * - ms for milliseconds
     * - s for seconds
     * - m for minutes
     * - h for hours
     * - D for days
     * - W for weeks
     */
    public static final Property TIMESPAN = DefaultProperty.valueOf(PREFIX + "timespan", "12w"); // ca. 3 months
}
