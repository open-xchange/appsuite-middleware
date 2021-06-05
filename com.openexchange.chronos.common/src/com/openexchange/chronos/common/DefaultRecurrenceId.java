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

import java.util.Objects;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.java.Enums;
import com.openexchange.java.util.TimeZones;

/**
 * {@link DefaultRecurrenceId}
 * <p/>
 * <b>Note:</b> This class has a natural ordering that is inconsistent with equals.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultRecurrenceId implements RecurrenceId {

    protected final DateTime value;
    protected final RecurrenceRange range;

    /**
     * Initializes a new {@link DefaultRecurrenceId} for a specific date-time.
     *
     * @param value The recurrence-id value
     * @throws NullPointerException if the passed argument is <code>null</code>
     */
    public DefaultRecurrenceId(DateTime value) {
        this(value, (RecurrenceRange) null);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} for a specific date-time.
     * 
     * @param value The recurrence-id value
     * @param range The targeted range, or <code>null</code> if only this recurrence is targeted
     * @throws NullPointerException if the passed value argument is <code>null</code>
     */
    public DefaultRecurrenceId(DateTime value, String range) {
        this(value, Enums.parse(RecurrenceRange.class, range, null));
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} for a specific date-time.
     * 
     * @param value The recurrence-id value
     * @param range The targeted range, or <code>null</code> if only this recurrence is targeted
     * @throws NullPointerException if the passed value argument is <code>null</code>
     */
    public DefaultRecurrenceId(DateTime value, RecurrenceRange range) {
        super();
        if (null == value) {
            throw new NullPointerException("value");
        }
        this.value = value;
        this.range = range;
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} from a RFC 5545 date-time string, either in <code>UTC</code> format (with trailing
     * <code>Z</code>), or as <i>floating</i> date or date-time.
     *
     * @param value The recurrence-id value as RFC 5545 date-time string
     * @throws NullPointerException if the passed argument is <code>null</code>
     * @throws IllegalArgumentException if the passed argument is not a valid date-time string
     */
    public DefaultRecurrenceId(String value) {
        this(value, null, null);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} from a RFC 5545 date-time string, either in <code>UTC</code> format (with trailing
     * <code>Z</code>), or as <i>floating</i> date or date-time.
     *
     * @param value The recurrence-id value as RFC 5545 date-time string
     * @param range The targeted range, or <code>null</code> if only this recurrence is targeted
     * @throws NullPointerException if the passed value argument is <code>null</code>
     * @throws IllegalArgumentException if the passed value argument is not a valid date-time string, or the passed range argument is invalid
     */
    public DefaultRecurrenceId(String value, String range) {
        this(value, null, range);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} from a RFC 5545 date-time string.
     *
     * @param value The recurrence-id value as RFC 5545 date-time string
     * @param timeZone The time zone to apply, or <code>null</code> if <i>floating</i> or in <code>UTC</code> format
     * @throws NullPointerException if the passed value argument is <code>null</code>
     * @throws IllegalArgumentException if the passed value argument is not a valid date-time string, or the passed range argument is invalid
     */
    public DefaultRecurrenceId(String value, TimeZone timeZone) {
        this(value, timeZone, null);
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} from a RFC 5545 date-time string.
     *
     * @param value The recurrence-id value as RFC 5545 date-time string
     * @param timeZone The time zone to apply, or <code>null</code> if <i>floating</i> or in <code>UTC</code> format
     * @param range The targeted range, or <code>null</code> if only this recurrence is targeted
     * @throws NullPointerException if the passed value argument is <code>null</code>
     * @throws IllegalArgumentException if the passed value argument is not a valid date-time string, or the passed range argument is invalid
     */
    public DefaultRecurrenceId(String value, TimeZone timeZone, String range) {
        this(DateTime.parse(timeZone, value), range);
    }

    @Override
    public DateTime getValue() {
        return value;
    }

    @Override
    public RecurrenceRange getRange() {
        return range;
    }

    @Override
    public int compareTo(RecurrenceId other, TimeZone timeZone) {
        return null == other ? 1 : CalendarUtils.compare(value, other.getValue(), timeZone);
    }

    @Override
    public int compareTo(RecurrenceId other) {
        return compareTo(other, TimeZones.UTC);
    }

    @Override
    public boolean matches(RecurrenceId other) {
        return null != other && getValue().getTimestamp() == other.getValue().getTimestamp() && 
            getValue().isFloating() == other.getValue().isFloating() &&
            getValue().isAllDay() == other.getValue().isAllDay();
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        if (null != range) {
            result = 31 * result + range.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (null != other && RecurrenceId.class.isInstance(other)) {
            RecurrenceId otherReurrenceId = (RecurrenceId) other;
            return Objects.equals(value, otherReurrenceId.getValue()) && Objects.equals(range, otherReurrenceId.getRange());
        }
        return false;
    }

    @Override
    public String toString() {
        return CalendarUtils.encode(getValue());
    }

}
