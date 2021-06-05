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

package com.openexchange.chronos.recurrence.service;

import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.EventOccurrence;
import com.openexchange.chronos.compat.PositionAwareRecurrenceId;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceIterator extends AbstractRecurrenceIterator<Event> {

    private final Event master;

    /**
     * Initializes a new {@link RecurrenceIterator}.
     *
     * @param config The recurrence configuration to use
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the series master's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @param ignoreExceptions Determines if exceptions should be ignored. If true, all occurrences are calculated as if no exceptions exist. Note: This does not add change exceptions. See {@link ChangeExceptionAwareRecurrenceIterator}
     */
    public RecurrenceIterator(RecurrenceConfig config, Event master, boolean forwardToOccurrence, Calendar start, Calendar end, Integer limit, boolean ignoreExceptions) throws OXException {
        super(config, master, forwardToOccurrence, start, end, limit, ignoreExceptions);
        this.master = master;
    }

    @Override
    protected Event nextInstance() {
        PositionAwareRecurrenceId recurrenceId = new PositionAwareRecurrenceId(recurrenceData, next, position, CalendarUtils.truncateTime(new Date(next.getTimestamp()), TimeZones.UTC));
        /*
         * extend flags (if actually set and not null in master) by "first" / "last" occurrence flag dynamically
         */
        if (1 == position) {
            return new EventOccurrence(master, recurrenceId) {

                @Override
                public EnumSet<EventFlag> getFlags() {
                    EnumSet<EventFlag> flags = super.getFlags();
                    if (null != flags) {
                        flags = EnumSet.copyOf(flags);
                        flags.add(EventFlag.FIRST_OCCURRENCE);
                    }
                    return flags;
                }
            };
        }
        if (isLastOccurrence()) {
            return new EventOccurrence(master, recurrenceId) {

                @Override
                public EnumSet<EventFlag> getFlags() {
                    EnumSet<EventFlag> flags = super.getFlags();
                    if (null != flags) {
                        flags = EnumSet.copyOf(flags);
                        flags.add(EventFlag.LAST_OCCURRENCE);
                    }
                    return flags;
                }
            };
        }
        return new EventOccurrence(master, recurrenceId);
    }

}
