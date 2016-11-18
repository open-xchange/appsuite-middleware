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
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;

/**
 * {@link RecurrenceServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceServiceImpl implements RecurrenceService {

    @Override
    public Iterator<Event> calculateInstances(Event master, final Calendar start, final Calendar end, Integer limit) throws OXException {
        return new RecurrenceIterator(master, start, end, limit, true);
    }

    @Override
    public Iterator<Event> calculateInstancesRespectExceptions(Event master, Calendar start, Calendar end, Integer limit, List<Event> changeExceptions) throws OXException {
        return new ChangeExceptionAwareRecurrenceIterator(master, start, end, limit, changeExceptions);
    }

    @Override
    public Calendar calculateRecurrenceDatePosition(Event master, int position) {
        if (!master.containsRecurrenceRule()) {
            return null;
        }
        if (position <= 0) {
            return null;
        }
        RecurrenceRule rrule = null;
        try {
            rrule = new RecurrenceRule(master.getRecurrenceRule());
        } catch (InvalidRecurrenceRuleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int counter = 1;
        RecurrenceRuleIterator iterator = rrule.iterator(new DateTime(TimeZone.getTimeZone(master.getStartTimeZone()), master.getStartDate().getTime()));
        while (iterator.hasNext()) {
            long nextMillis = iterator.nextMillis();
            if (counter++ == position) {
                Calendar retval = GregorianCalendar.getInstance(TimeZone.getTimeZone(master.getStartTimeZone()));
                retval.setTimeInMillis(nextMillis);
                return retval;
            }
        }

        return null;
    }

    @Override
    public int calculateRecurrencePosition(Event master, Calendar datePosition) {
        if (!master.containsRecurrenceRule()) {
            return 0;
        }
        if (datePosition.compareTo(master.getStart()) < 0) {
            return 0;
        }
        RecurrenceRule rrule = null;
        try {
            rrule = new RecurrenceRule(master.getRecurrenceRule());
        } catch (InvalidRecurrenceRuleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int position = 1;
        RecurrenceRuleIterator iterator = rrule.iterator(new DateTime(TimeZone.getTimeZone(master.getStartTimeZone()), master.getStartDate().getTime()));
        while (iterator.hasNext()) {
            long nextMillis = iterator.nextMillis();
            if (nextMillis > datePosition.getTimeInMillis()) {
                break;
            }
            if (nextMillis == datePosition.getTimeInMillis()) {
                return position;
            }
            position++;
        }
        return 0;
    }

    @Override
    public Iterator<RecurrenceId> getRecurrenceIterator(Event master, Calendar start, Calendar end, boolean ignoreExceptions) throws OXException {
        return new RecurrenceIdIterator(master, start, end, null, ignoreExceptions);
    }

}
