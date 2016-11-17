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
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.ChronosLogger;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractRecurrenceIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public abstract class AbstractRecurrenceIterator<T> implements Iterator<T> {

    public static final int MAX = 1000;

    protected final Event master;
    protected final Calendar start;
    protected final Calendar end;
    protected final Integer limit;
    protected final List<Date> exceptionDates;
    protected final boolean ignoreExceptions;
    protected final RecurrenceData recurrenceData;

    protected Long next;
    protected int count;
    protected RecurrenceRuleIterator inner;

    /**
     * Initializes a new {@link AbstractRecurrenceIterator}.
     *
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @param ignoreExceptions Determines if exceptions should be ignored. If true, all occurrences are calculated as if no exceptions exist. Note: This does not add change exceptions. See {@link ChangeExceptionAwareRecurrenceIterator}
     */
    public AbstractRecurrenceIterator(Event master, Calendar start, Calendar end, Integer limit, boolean ignoreExceptions) throws OXException {
        this.master = master;
        this.recurrenceData = new DefaultRecurrenceData(master);
        this.start = start;
        this.end = end;
        this.limit = limit;
        this.ignoreExceptions = ignoreExceptions;
        this.exceptionDates = new ArrayList<Date>();
        if (master.getDeleteExceptionDates() != null) {
            this.exceptionDates.addAll(master.getDeleteExceptionDates());
        }
        if (master.getChangeExceptionDates() != null) {
            this.exceptionDates.addAll(master.getChangeExceptionDates());
        }
        if (limit != null && limit == 0) {
            ChronosLogger.debug("Occurrence limit set to 0, nothing to do.");
            return;
        }
        inner = Recurrence.getRecurrenceIterator(recurrenceData, true);
        next = null;
        count = 0;
        init();
    }

    private void init() {
        if (this.start != null) {
            while (inner.hasNext()) {
                long nextMillis = inner.peekMillis();
                if (!ignoreExceptions && isException(nextMillis)) {
                    inner.nextMillis();
                    count++;
                    continue;
                }
                Date nextEnd = calculateEnd(master, new Date(nextMillis));
                if (nextEnd.getTime() > start.getTimeInMillis()) {
                    break;
                } else {
                    inner.nextMillis();
                    count++;
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
    public T next() {
        if (next == null) {
            throw new NoSuchElementException();
        }

        T retval = nextInstance();
        innerNext();
        return retval;
    }

    protected abstract T nextInstance();

    private void innerNext() {
        if (count >= MAX) {
            ChronosLogger.debug("Reached internal limit. Stop calculation.");
            next = null;
            return;
        }

        if (limit != null && count >= limit) {
            ChronosLogger.debug("Reached given limit. Stop calculation.");
            next = null;
            return;
        }

        if (!inner.hasNext()) {
            ChronosLogger.debug("No more instances available.");
            next = null;
            return;
        }

        long peek = inner.peekMillis();
        if (!ignoreExceptions) {
            while (isException(peek)) {
                ChronosLogger.debug("Next instance is exception.");
                inner.nextMillis();
                count++;
                if (inner.hasNext()) {
                    peek = inner.peekMillis();
                } else {
                    next = null;
                    return;
                }
            }
        }
        if (this.end != null && peek >= this.end.getTimeInMillis()) {
            ChronosLogger.debug("Next instance ({}) reached end boundary ({}).", peek, this.end.getTimeInMillis());
            next = null;
            return;
        }

        next = inner.nextMillis();
        count++;
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
