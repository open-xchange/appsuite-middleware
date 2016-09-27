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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.recurrence.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import com.openexchange.chronos.Event;

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
    private Date nextChange;
    private Event nextCalculated;
    private Event next;
    private Calendar start;
    private Calendar end;
    private Integer limit;
    private int count = 0;

    public ChangeExceptionAwareRecurrenceIterator(Event master, Calendar start, Calendar end, Integer limit, List<Event> changeExceptions) {
        if (limit != null && limit == 0) {
            //Nothing to do.
            return;
        }
        this.start = start;
        this.end = end;
        this.limit = limit;

        inner = new RecurrenceIterator(master, start, end, null, false);

        init(changeExceptions);
    }

    private void init(List<Event> changeExceptions) {
        // Sort change exception dates
        this.changeExceptions = new TreeMap<Date, Event>();
        if (changeExceptions != null) {
            for (Event e : changeExceptions) {
                this.changeExceptions.put(e.getStartDate(), e);
            }
        }

        changeIterator = this.changeExceptions.keySet().iterator();

        // Fast forward change exceptions.
        if (start != null) {
            while (changeIterator.hasNext()) {
                Date tmp = changeIterator.next();
                if (this.changeExceptions.get(tmp).getEndDate().after(start.getTime())) {
                    nextChange = tmp;
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
        if (limit != null && count >= limit) {
            // Reached limit
            next = null;
            return;
        }

        if (inner.hasNext() && nextCalculated == null) {
            // Get lookahead for regular occurrences
            nextCalculated = inner.next();
        }

        if (changeIterator.hasNext() && nextChange == null) {
            // Get lookahead for change exceptions
            nextChange = changeIterator.next();
        }

        if (nextCalculated == null && nextChange == null) {
            // Nothing left
            next = null;
            return;
        }

        if (nextChange == null) {
            // No more change exceptions, continue with regular occurrences
            next = nextCalculated;
            nextCalculated = null;
            count++;
            return;
        }

        if (nextCalculated == null) {
            // No more regular occurrences, continue with change exceptions
            Event candidate = changeExceptions.get(nextChange);
            if (end != null && !candidate.getStartDate().before(end.getTime())) {
                // Right boundary reached
                next = null;
                return;
            } else {
                next = candidate;
                nextChange = null;
                count++;
                return;
            }
        }

        // Determine correct order. Change exception wins on equal.
        if (nextCalculated.getStartDate().before(nextChange)) {
            next = nextCalculated;
            nextCalculated = null;
        } else {
            Event candidate = changeExceptions.get(nextChange);
            if (end != null && !candidate.getStartDate().before(end.getTime())) {
                next = nextCalculated;
                nextCalculated = null;
            } else {
                next = candidate;
                nextChange = null;
            }
        }
        count++;
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub

    }

}
