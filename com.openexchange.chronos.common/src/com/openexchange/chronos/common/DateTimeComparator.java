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

package com.openexchange.chronos.common;

import java.io.Serializable;
import java.util.Comparator;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.java.util.TimeZones;

/**
 * {@link DateTimeComparator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DateTimeComparator implements Comparator<DateTime>, Serializable {

    private static final long serialVersionUID = -291107087424182767L;

    private final TimeZone timeZone;

    /**
     * Initializes a new {@link DateTimeComparator}.
     *
     * @param timeZone The timezone to consider for <i>floating</i> dates, i.e. the actual 'perspective' of the comparison, or
     *            <code>null</code> to fall back to UTC
     */
    public DateTimeComparator(TimeZone timeZone) {
        super();
        this.timeZone = timeZone;
    }

    /**
     * Initializes a new {@link DateTimeComparator}.
     * <p>/
     * <i>Floating</i> dates are compared from an UTC timezone perspective.
     */
    public DateTimeComparator() {
        this(TimeZones.UTC);
    }

    @Override
    public int compare(DateTime dateTime1, DateTime dateTime2) {
        return CalendarUtils.compare(dateTime1, dateTime2, timeZone);
    }

}
