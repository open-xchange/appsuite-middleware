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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultRecurrenceId;

/**
 * {@link RecurrenceIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceIterator implements Iterator<Event> {

    public static final int MAX = 1000;

    private Event master;
    private Calendar start;
    private Calendar end;
    private Integer limit;
    private Long next;
    private List<Date> exceptionDates;
    private boolean ignoreExceptions;
    private int count;
    private RecurrenceRuleIterator inner;

    /**
     * Initializes a new {@link RecurrenceIterator}.
     *
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @param ignoreExceptions Determines if exceptions should be ignored. If true, all occurrences are calculated as if no exceptions exist. Note: This does not add change exceptions. See {@link ChangeExceptionAwareRecurrenceIterator}
     */
    public RecurrenceIterator(Event master, Calendar start, Calendar end, Integer limit, boolean ignoreExceptions) {
        if (limit != null && limit == 0) {
            // Nothing to do.
            return;
        }
        this.master = master;
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.next = null;
        this.ignoreExceptions = ignoreExceptions;
        this.exceptionDates = new ArrayList<Date>();
        if (master.getDeleteExceptionDates() != null) {
            this.exceptionDates.addAll(master.getDeleteExceptionDates());
        }
        if (master.getChangeExceptionDates() != null) {
            this.exceptionDates.addAll(master.getChangeExceptionDates());
        }
        count = 0;

        init();
    }

    private void init() {
        RecurrenceRule rrule = null;
        try {
            rrule = new RecurrenceRule(master.getRecurrenceRule());
        } catch (InvalidRecurrenceRuleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar seriesStart = master.getStart();
        inner = rrule.iterator(seriesStart.getTimeInMillis(), master.isAllDay() ? null : seriesStart.getTimeZone());

        if (this.start != null) {
            while (inner.hasNext()) {
                long nextMillis = inner.peekMillis();
                if (!ignoreExceptions && isException(nextMillis)) {
                    inner.nextMillis();
                    continue;
                }
                Date nextEnd = calculateEnd(master, new Date(nextMillis));
                if (nextEnd.getTime() > start.getTimeInMillis()) {
                    break;
                } else {
                    inner.nextMillis();
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

        Event retval = getInstance();
        innerNext();
        return retval;
    }

    private void innerNext() {
        if (count >= MAX) {
            // Reached internal limit
            next = null;
            return;
        }

        if (limit != null && count >= limit) {
            // Reached given limit
            next = null;
            return;
        }

        if (!inner.hasNext()) {
            // No more instances
            next = null;
            return;
        }

        long peek = inner.peekMillis();
        if (!ignoreExceptions) {
            // Check for exceptions
            while (isException(peek)) {
                inner.nextMillis();
                if (inner.hasNext()) {
                    peek = inner.peekMillis();
                } else {
                    next = null;
                    return;
                }
            }
        }
        if (this.end != null && peek >= this.end.getTimeInMillis()) {
            // Reached end boundary
            next = null;
            return;
        }

        count++;
        next = inner.nextMillis();
    }

    private Event getInstance() {
        // TODO:
        Event retval = master.clone();

        retval.setRecurrenceId(new DefaultRecurrenceId(next));

        //        retval.removeId();
        //        retval.removeRecurrenceRule();
        retval.removeDeleteExceptionDates();
        retval.removeChangeExceptionDates();
        retval.setStartDate(new Date(next));
        retval.setEndDate(calculateEnd(master, retval.getStartDate()));
        if (master.containsAllDay()) {
            retval.setAllDay(master.getAllDay());
        }
        return retval;
    }

    private Date calculateEnd(Event master, Date start) {
        long startMillis = master.getStartDate().getTime();
        long endMillis = master.getEndDate().getTime();
        long duration = endMillis - startMillis;
        return new Date(start.getTime() + duration);
    }

    private boolean isException(long start) {
        return exceptionDates.contains(new Date(start));
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
