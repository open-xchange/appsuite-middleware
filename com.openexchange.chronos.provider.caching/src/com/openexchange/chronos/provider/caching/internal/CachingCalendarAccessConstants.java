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

package com.openexchange.chronos.provider.caching.internal;

/**
 *
 * {@link CachingCalendarAccessConstants}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public final class CachingCalendarAccessConstants {

    /**
     * The general key for persisting the caching information
     */
    public static final String CACHING = "caching";

    /**
     * The key for persisting if the account is currently blocked by another request
     */
    public static final String BLOCKED = "blocked";

    /**
     * The key for persisting accounts last update information
     */
    public static final String LAST_UPDATE = "lastUpdate";

    /**
     * The key for persisting the information about a current lock
     */
    public static final String LOCKED_FOR_UPDATE_UNTIL = "lockedForUpdateUntil";

    /**
     * The key for persisting the information about who owns the current lock
     */
    public static final String LOCKED_FOR_UPDATE_BY = "lockedForUpdateBy";

}
