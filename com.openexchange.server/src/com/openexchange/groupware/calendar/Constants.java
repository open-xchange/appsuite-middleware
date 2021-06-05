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

package com.openexchange.groupware.calendar;

/**
 * Some calendar constants.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Constants {

    public static final long MILLI_SECOND = 1000L;
    public static final long MILLI_MINUTE = 60 * MILLI_SECOND;
    public static final long MILLI_HOUR = 60 * MILLI_MINUTE;
    public static final long MILLI_DAY = 24 * MILLI_HOUR;
    public static final long MILLI_WEEK = 7 * MILLI_DAY;
    public static final long MILLI_MONTH = 31 * MILLI_DAY;
    public static final long MILLI_YEAR = 365 * MILLI_DAY;

    /**
     * Prevent instantiation.
     */
    private Constants() {
        super();
    }

}
