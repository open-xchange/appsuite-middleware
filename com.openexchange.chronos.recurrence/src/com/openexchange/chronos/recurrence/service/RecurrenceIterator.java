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
import java.util.NoSuchElementException;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Event;

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
    private int count;
    private RecurrenceRuleIterator inner;

    public RecurrenceIterator(Event master, Calendar start, Calendar end, Integer limit) {
        this.master = master;
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.next = null;
        count = 0;

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

    private void innerNext() {
        if (count >= MAX) {
            return;
        }

        if (limit != null && count >= limit) {
            next = null;
            return;
        }

        if (!inner.hasNext()) {
            next = null;
            return;
        }

        if (this.end != null && inner.peekMillis() >= this.end.getTimeInMillis()) {
            next = null;
            return;
        }

        count++;
        next = inner.nextMillis();
    }

    private Date calculateEnd(Event master, Date start) {
        long startMillis = master.getStartDate().getTime();
        long endMillis = master.getEndDate().getTime();
        long duration = endMillis - startMillis;
        return new Date(start.getTime() + duration);
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
        Event retval = master.clone();
        retval.removeId();
        retval.removeRecurrenceRule();
        retval.removeDeleteExceptionDates();
        retval.removeChangeExceptionDates();
        retval.setStartDate(new Date(next));
        retval.setEndDate(calculateEnd(master, retval.getStartDate()));
        innerNext();
        return retval;
    }

}
