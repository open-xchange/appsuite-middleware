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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link ChangeExceptionAwareRecurrenceIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ChangeExceptionAwareRecurrenceIterator implements Iterator<Event> {

    private RecurrenceIterator inner;
    private Map<Date, Event> changeExceptions;
    private Iterator<Date> changeIterator;
    private Date changeLookahead;
    private Event regularLookahead;
    private Event next;
    private Calendar start;
    private Calendar end;
    private Integer limit;
    private int count = 0;

    public ChangeExceptionAwareRecurrenceIterator(RecurrenceConfig config, Event master, boolean forwardToOccurrence, Calendar start, Calendar end, Integer limit, List<Event> changeExceptions) throws OXException {
        if (limit != null && limit.intValue() == 0) {
            //Nothing to do.
            return;
        }
        this.start = start;
        this.end = end;
        this.limit = limit;

        inner = new RecurrenceIterator(config, master, forwardToOccurrence, start, end, null, false);

        init(changeExceptions);
    }

    private void init(List<Event> changeExceptions) {
        // Sort change exception dates
        this.changeExceptions = new TreeMap<Date, Event>();
        if (changeExceptions != null) {
            for (Event e : changeExceptions) {
                this.changeExceptions.put(new Date(e.getStartDate().getTimestamp()), e);
            }
        }

        changeIterator = this.changeExceptions.keySet().iterator();

        // Fast forward change exceptions.
        if (start != null) {
            while (changeIterator.hasNext()) {
                Date tmp = changeIterator.next();
                if (this.changeExceptions.get(tmp).getEndDate().getTimestamp() > start.getTimeInMillis()) {
                    changeLookahead = tmp;
                    break;
                }
            }
        }

        innerNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Event next() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        Event retval = next;
        innerNext();
        return retval;
    }

    private void innerNext() {
        if (limit != null && count >= limit.intValue()) {
            // Reached limit
            next = null;
            return;
        }

        if (inner.hasNext() && regularLookahead == null) {
            // Get lookahead for regular occurrences
            regularLookahead = inner.next();
        }

        if (changeIterator.hasNext() && changeLookahead == null) {
            // Get lookahead for change exceptions
            changeLookahead = changeIterator.next();
        }

        if (regularLookahead == null && changeLookahead == null) {
            // Nothing left
            next = null;
            return;
        }

        if (changeLookahead == null) {
            // No more change exceptions, continue with regular occurrences
            next = regularLookahead;
            regularLookahead = null;
            count++;
            return;
        }

        if (regularLookahead == null) {
            // No more regular occurrences, continue with change exceptions
            Event candidate = changeExceptions.get(changeLookahead);
            if (end != null && candidate.getStartDate().getTimestamp() >= end.getTimeInMillis()) {
                // Right boundary reached
                next = null;
                return;
            } else {
                next = candidate;
                changeLookahead = null;
                count++;
                return;
            }
        }

        // Determine correct order. Change exception wins on equal.
        if (regularLookahead.getStartDate().getTimestamp() < changeLookahead.getTime()) {
            next = regularLookahead;
            regularLookahead = null;
        } else {
            Event candidate = changeExceptions.get(changeLookahead);
            if (end != null && candidate.getStartDate().getTimestamp() >= end.getTimeInMillis()) {
                next = regularLookahead;
                regularLookahead = null;
            } else {
                next = candidate;
                changeLookahead = null;
            }
        }
        count++;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
