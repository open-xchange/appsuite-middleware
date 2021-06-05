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
