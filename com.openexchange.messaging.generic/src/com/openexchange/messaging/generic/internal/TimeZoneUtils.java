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

package com.openexchange.messaging.generic.internal;

import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link TimeZoneUtils} - Utility class for time zone.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class TimeZoneUtils {

    /**
     * Initializes a new {@link TimeZoneUtils}.
     */
    private TimeZoneUtils() {
        super();
    }

    private static final ConcurrentMap<String, TimeZone> ZONE_CACHE = new ConcurrentHashMap<String, TimeZone>();

    /**
     * Gets the {@link TimeZone} instance for the given ID.
     *
     * @param ID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a
     *            custom ID such as "GMT-8:00".
     * @return The specified {@link TimeZone} instance, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String ID) {
        TimeZone tz = ZONE_CACHE.get(ID);
        if (tz == null) {
            final TimeZone tmp = TimeZone.getTimeZone(ID);
            tz = ZONE_CACHE.putIfAbsent(ID, tmp);
            if (null == tz) {
                tz = tmp;
            }
        }
        return tz;
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone identifier
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final String timeZone) {
        return (date + getTimeZone(timeZone).getOffset(date));
    }

    /**
     * Adds the time zone offset to given date millis.
     *
     * @param date The date millis
     * @param timeZone The time zone
     * @return The date millis with time zone offset added
     */
    public static long addTimeZoneOffset(final long date, final TimeZone timeZone) {
        return (date + timeZone.getOffset(date));
    }

}
