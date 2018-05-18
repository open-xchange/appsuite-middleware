/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultRecurrenceId implements RecurrenceId {

    protected final DateTime value;
    protected final RecurrenceRange range;

    /**
     * Initializes a new {@link DefaultRecurrenceId} for a specific date-time. If the date-time is neither <i>floating</i> nor in
     * <code>UTC</code> format, the value's timezone is shifted to <code>UTC</code> implicitly.
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
        if (null != value.getTimeZone() && false == DateTime.UTC.equals(value.getTimeZone())) {
            this.value = value.shiftTimeZone(DateTime.UTC);
        } else {
            this.value = value;
        }
        this.range = range;
    }

    /**
     * Initializes a new {@link DefaultRecurrenceId} for a specific date-time. If the date-time is neither <i>floating</i> nor in
     * <code>UTC</code> format, the value's timezone is shifted to <code>UTC</code> implicitly.
     *
     * @param value The recurrence-id value
     * @throws NullPointerException if the passed argument is <code>null</code>
     */
    public DefaultRecurrenceId(DateTime value) {
        this(value, null);
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
        this(DateTime.parse(value), Enums.parse(RecurrenceRange.class, range, null));
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
        this(DateTime.parse(value));
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
        return value.toString();
    }

}
