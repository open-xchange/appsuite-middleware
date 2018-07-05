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

import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link RecurrenceServiceImpl}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RecurrenceServiceImpl implements RecurrenceService {

    private final RecurrenceConfig config;

    /**
     * Initializes a new {@link RecurrenceServiceImpl}.
     *
     * @param config The recurrence configuration to use
     */
    public RecurrenceServiceImpl(RecurrenceConfig config) {
        super();
        this.config = config;
    }

    @Override
    public Iterator<Event> calculateInstances(Event master, final Calendar start, final Calendar end, Integer limit) throws OXException {
        return new RecurrenceIterator(config, master, true, start, end, limit, true);
    }

    @Override
    public Iterator<Event> calculateInstancesRespectExceptions(Event master, Calendar start, Calendar end, Integer limit, List<Event> changeExceptions) throws OXException {
        return new ChangeExceptionAwareRecurrenceIterator(config, master, false, start, end, limit, changeExceptions);
    }

    @Override
    public Calendar calculateRecurrenceDatePosition(Event master, int position) throws OXException {
        if (!master.containsRecurrenceRule()) {
            return null;
        }
        if (position <= 0) {
            return null;
        }
        int counter = 1;
        RecurrenceData recurrenceData = new DefaultRecurrenceData(master.getRecurrenceRule(), master.getStartDate(), null);
        RecurrenceSetIterator iterator = RecurrenceUtils.getRecurrenceIterator(recurrenceData);
        while (iterator.hasNext()) {
            long nextMillis = iterator.next();
            if (counter++ == position) {
                Calendar retval = GregorianCalendar.getInstance(master.getStartDate().getTimeZone());
                retval.setTimeInMillis(nextMillis);
                return retval;
            }
        }

        return null;
    }

    @Override
    public int calculateRecurrencePosition(Event master, Calendar datePosition) throws OXException {
        if (!master.containsRecurrenceRule()) {
            return 0;
        }
        if (datePosition.getTimeInMillis() < master.getStartDate().getTimestamp()) {
            return 0;
        }
        int position = 1;
        RecurrenceData recurrenceData = new DefaultRecurrenceData(master.getRecurrenceRule(), master.getStartDate(), null);
        RecurrenceSetIterator iterator = RecurrenceUtils.getRecurrenceIterator(recurrenceData);
        while (iterator.hasNext()) {
            long nextMillis = iterator.next();
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
    public com.openexchange.chronos.service.RecurrenceIterator<Event> iterateEventOccurrences(Event seriesMaster, Date from, Date until) throws OXException {
        Calendar fromCalendar = null != from ? initCalendar(TimeZones.UTC, from) : null;
        Calendar untilCalendar = null != until ? initCalendar(TimeZones.UTC, until) : null;
        return new RecurrenceIterator(config, seriesMaster, true, fromCalendar, untilCalendar, null, false);
    }

    @Override
    public com.openexchange.chronos.service.RecurrenceIterator<RecurrenceId> iterateRecurrenceIds(RecurrenceData recurrenceData) throws OXException {
        return new RecurrenceIdIterator(config, recurrenceData, true, null, null, null, null);
    }

    @Override
    public com.openexchange.chronos.service.RecurrenceIterator<RecurrenceId> iterateRecurrenceIds(RecurrenceData recurrenceData, Date from, Date until) throws OXException {
        Calendar fromCalendar = null != from ? initCalendar(TimeZones.UTC, from) : null;
        Calendar untilCalendar = null != until ? initCalendar(TimeZones.UTC, until) : null;
        return new RecurrenceIdIterator(config, recurrenceData, true, fromCalendar, untilCalendar, null, null);
    }

    @Override
    public com.openexchange.chronos.service.RecurrenceIterator<RecurrenceId> iterateRecurrenceIds(RecurrenceData recurrenceData, Integer startPosition, Integer limit) throws OXException {
        return new RecurrenceIdIterator(config, recurrenceData, true, null, null, startPosition, limit);
    }

    @Override
    public void validate(RecurrenceData recurrenceData) throws OXException {
        /*
         * initializing the iterator implicitly checks the rule within the constraints of the corresponding recurrence data
         */
        RecurrenceUtils.getRecurrenceIterator(recurrenceData);
    }

    @Override
    public boolean isUnlimited(String recurrenceRule) throws OXException {
        RecurrenceRule rule = initRecurrenceRule(recurrenceRule);
        return null == rule.getCount() && null == rule.getUntil();
    }

    @Override
    public RecurrenceId getLastOccurrence(RecurrenceData recurrenceData) throws OXException {
        RecurrenceRule rule = initRecurrenceRule(recurrenceData.getRecurrenceRule());
        if (null == rule.getCount() && null == rule.getUntil()) {
            return null;
        }
        RecurrenceSetIterator iterator = RecurrenceUtils.getRecurrenceIterator(recurrenceData, true);
        if (false == iterator.hasNext()) {
            return null != recurrenceData.getSeriesStart() ? new DefaultRecurrenceId(recurrenceData.getSeriesStart()) : null;
        }
        DateTime dateTime;
        do {
            dateTime = new DateTime(iterator.next());
        } while (iterator.hasNext());
        return new DefaultRecurrenceId(dateTime);
    }

}
