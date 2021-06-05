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
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.PositionAwareRecurrenceId;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceIdIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceIdIterator extends AbstractRecurrenceIterator<RecurrenceId> {

    /**
     * Initializes a new {@link RecurrenceIdIterator}.
     *
     * @param config The recurrence configuration to use
     * @param recurrenceData the recurrence data
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param startPosition The 1-based position of the occurrence in the recurrence set to start with, or <code>null</code> to start with the first occurrence
     * @param limit The maximum number of calculated instances. Optional, can be null.
     */
    public RecurrenceIdIterator(RecurrenceConfig config, RecurrenceData recurrenceData, boolean forwardToOccurrence, Calendar start, Calendar end, Integer startPosition, Integer limit) throws OXException {
        super(config, recurrenceData, 0L, forwardToOccurrence, start, end, startPosition, limit);
    }

    @Override
    protected RecurrenceId nextInstance() {
        return new PositionAwareRecurrenceId(recurrenceData, next, position, CalendarUtils.truncateTime(new Date(next.getTimestamp()), TimeZones.UTC));
    }

}
