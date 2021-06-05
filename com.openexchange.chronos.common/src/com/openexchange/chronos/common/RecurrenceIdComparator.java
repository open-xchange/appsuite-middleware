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

import java.util.Comparator;
import java.util.TimeZone;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceIdComparator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RecurrenceIdComparator implements Comparator<RecurrenceId> {

    /** A default comparator using the <code>UTC</code> timezone when comparing <i>floating</i> recurrence identifiers */
    public static RecurrenceIdComparator DEFAULT_COMPARATOR = new RecurrenceIdComparator();

    private final TimeZone timeZone;

    /**
     * Initializes a new {@link RecurrenceIdComparator}.
     *
     * @param timeZone The timezone to consider for <i>floating</i> recurrence ids, i.e. the actual 'perspective' of the comparison, or
     *            <code>null</code> to fall back to UTC
     */
    public RecurrenceIdComparator(TimeZone timeZone) {
        super();
        this.timeZone = timeZone;
    }

    /**
     * Initializes a new {@link RecurrenceIdComparator}.
     * <p>/
     * <i>Floating</i> recurrence ids are compared from an UTC timezone perspective.
     */
    public RecurrenceIdComparator() {
        this(TimeZones.UTC);
    }

    @Override
    public int compare(RecurrenceId recurrenceId1, RecurrenceId recurrenceId2) {
        if (null == recurrenceId1) {
            return null == recurrenceId2 ? 0 : -1;
        }
        if (null == recurrenceId2) {
            return 1;
        }
        return CalendarUtils.compare(recurrenceId1.getValue(), recurrenceId2.getValue(), timeZone);
    }

}
