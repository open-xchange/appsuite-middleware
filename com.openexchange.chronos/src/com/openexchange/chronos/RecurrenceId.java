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

package com.openexchange.chronos;

import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;

/**
 * {@link RecurrenceId}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.4.4">RFC 5545, section 3.8.4.4</a>
 */
public interface RecurrenceId extends Comparable<RecurrenceId> {

    /**
     * Gets the value, i.e. the (original) start-date of the targeted recurrence in the event series.
     * <p/>
     * The returned date-time is either in <code>UTC</code> format or a <i>floating</i> date or date-time.
     *
     * @return The recurrence-id value
     */
    DateTime getValue();

    /**
     * Gets a value indicating the effective range of targeted recurrence instances.
     *
     * @return The range, or <code>null</code> if only this instance is targeted.
     */
    RecurrenceRange getRange();

    /**
     * Compares this recurrence id to another one, taking a concrete timezone into consideration for <i>floating</i> values.
     *
     * @param other The recurrence id to compare with
     * @param timeZone The timezone to consider for <i>floating</i> dates, i.e. the actual 'perspective' of the comparison, or
     *            <code>null</code> to fall back to UTC
     * @return A negative integer, zero, or a positive integer as this recurrence id is less than, equal to, or greater than the other
     *         recurrence id
     */
    int compareTo(RecurrenceId other, TimeZone timeZone);

    /**
     * Gets a value indicating whether this recurrence id <i>matches</i> another one, i.e. both are pointing to the same absolute timestamp
     * (although possibly in different timezones for non-<i>floating</i> values, or decorated with a different recurrence range).
     *
     * @param other The recurrent identifier to match
     * @return <code>true</code> if both recurrence ids match, <code>false</code> otherwise
     */
    boolean matches(RecurrenceId other);

}
