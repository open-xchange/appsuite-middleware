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

import java.util.Arrays;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recurrenceset.RecurrenceSetIterator;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.ChronosLogger;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractRecurrenceIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @param <T> The generic type
 * @since v7.10.0
 */
public abstract class AbstractRecurrenceIterator<T> implements RecurrenceIterator<T> {

    protected final int calculationLimit;
    protected final Calendar start;
    protected final Integer startPosition;
    protected final Calendar end;
    protected final Integer limit;
    protected final RecurrenceData recurrenceData;
    protected final long eventDuration;

    protected DateTime next;
    protected int count;
    protected int position;
    protected int nextPosition;
    protected RecurrenceSetIterator inner;
    protected final long[] exceptionDates;

    private Long lookAhead;

    /**
     * Initializes a new {@link AbstractRecurrenceIterator}.
     *
     * @param config The recurrence configuration to use
     * @param master The master event containing all necessary information like recurrence rule, star and end date, timezones etc.
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     * @param ignoreExceptions Determines if exceptions should be ignored. If true, all occurrences are calculated as if no exceptions exist. Note: This does not add change exceptions. See {@link ChangeExceptionAwareRecurrenceIterator}
     */
    protected AbstractRecurrenceIterator(RecurrenceConfig config, Event master, boolean forwardToOccurrence, Calendar start, Calendar end, Integer limit, boolean ignoreExceptions) throws OXException {
        this(config, new DefaultRecurrenceData(master.getRecurrenceRule(), master.getStartDate(), ignoreExceptions ? null : getExceptionDates(master), CalendarUtils.getExceptionDates(master.getRecurrenceDates())), getEventDuration(master), forwardToOccurrence, start, end, null, limit);
    }

    /**
     * Initializes a new {@link AbstractRecurrenceIterator}.
     *
     * @param config The recurrence configuration to use
     * @param recurrenceData The underlying recurrence data
     * @param eventDuration The duration of the underlying series master event, or <code>0</code> if not considered
     * @param forwardToOccurrence <code>true</code> to fast-forward the iterator to the first occurrence if the recurrence data's start
     *            does not fall into the pattern, <code>false</code> otherwise
     * @param start The left side boundary for the calculation. Optional, can be null.
     * @param end The right side boundary for the calculation. Optional, can be null.
     * @param startPosition The start recurrence position for the calculation. Optional, can be null.
     * @param limit The maximum number of calculated instances. Optional, can be null.
     */
    protected AbstractRecurrenceIterator(RecurrenceConfig config, RecurrenceData recurrenceData, long eventDuration, boolean forwardToOccurrence, Calendar start, Calendar end, Integer startPosition, Integer limit) throws OXException {
        super();
        this.calculationLimit = config.getCalculationLimit();
        this.recurrenceData = recurrenceData;
        this.eventDuration = eventDuration;
        this.start = start;
        this.startPosition = startPosition;
        this.end = end;
        this.limit = limit;
        this.exceptionDates = recurrenceData != null ? recurrenceData.getExceptionDates() : null;
        if (limit != null && limit.intValue() == 0) {
            ChronosLogger.debug("Occurrence limit set to 0, nothing to do.");
        } else {
            inner = recurrenceData != null ? RecurrenceUtils.getRecurrenceIterator(recurrenceData, forwardToOccurrence) : null;
            next = null;
            nextPosition = 0;
            count = 0;
            init();
        }
    }

    private void init() {
        if (null != start || null != startPosition && 1 < startPosition.intValue()) {
            while (_hasNext()) {
                long candidate = inner.next();
                if (isException(candidate)) {
                    nextPosition++;
                    continue;
                }
                if (start != null && candidate + eventDuration > start.getTimeInMillis() ||
                    start != null && 0L == eventDuration && candidate == start.getTimeInMillis() ||
                    startPosition != null && nextPosition + 1 >= startPosition.intValue()) {
                    lookAhead = Long.valueOf(candidate);
                    break;
                }
                nextPosition++;
            }
        }

        innerNext();
    }

    private boolean _hasNext() {
        return inner != null && inner.hasNext();
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

        position = nextPosition;
        T retval = nextInstance();
        innerNext();
        return retval;
    }

    protected abstract T nextInstance();

    private void innerNext() {
        if (count >= calculationLimit) {
            ChronosLogger.debug("Reached internal limit. Stop calculation.");
            next = null;
            return;
        }

        if (limit != null && count >= limit.intValue()) {
            ChronosLogger.debug("Reached given limit. Stop calculation.");
            next = null;
            return;
        }

        if (lookAhead == null) {
            if (!_hasNext()) {
                ChronosLogger.debug("No more instances available.");
                next = null;
                return;
            }
            lookAhead = Long.valueOf(inner.next());
        }

        while (isException(lookAhead.longValue())) {
            ChronosLogger.debug("Next instance is exception.");
            nextPosition++;
            if (_hasNext()) {
                lookAhead = Long.valueOf(inner.next());
            } else {
                next = null;
                return;
            }
        }

        if (this.end != null && lookAhead.longValue() >= this.end.getTimeInMillis()) {
            ChronosLogger.debug("Next instance ({}) reached end boundary ({}).", lookAhead, Long.valueOf(this.end.getTimeInMillis()));
            next = null;
            return;
        }

        next = toDateTime(lookAhead.longValue());
        lookAhead = null;
        count++;
        nextPosition++;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean isFirstOccurrence() {
        return 1 == getPosition();
    }

    @Override
    public boolean isLastOccurrence() {
        return null == lookAhead && false == _hasNext() && 0 < getPosition();
    }

    /**
     * Gets the date-time representation of a timestamp retrieved from the underlying recurrence set iterator.
     * <p/>
     * The actual value type and timezone are taken over from the recurrence data.
     *
     * @param timestamp The timestamp as retrieved from the underlying iterator
     * @return The date-time with suitable type for actual recurrence data
     */
    private DateTime toDateTime(long timestamp) {
        DateTime seriesStart = recurrenceData.getSeriesStart();
        if (null == seriesStart) {
            return new DateTime(timestamp);
        }
        if (seriesStart.isAllDay()) {
            return new DateTime(null, timestamp).toAllDay();
        }
        return new DateTime(seriesStart.getCalendarMetrics(), seriesStart.getTimeZone(), timestamp);
    }

    private boolean isException(long start) {
        return null != exceptionDates && -1 < Arrays.binarySearch(exceptionDates, start);
    }

    /**
     * Builds a sorted array containing the recurrence identifier values from both the change- and delete-exceptions of the supplied
     * series master event.
     *
     * @param seriesMaster The series master event to get all exception dates for
     * @return The timestamps of the exceptions dates in a sorted array, or an <code>null</code> if there are none
     */
    private static long[] getExceptionDates(Event seriesMaster) {
        SortedSet<RecurrenceId> exceptionDates = new TreeSet<RecurrenceId>();
        if (null != seriesMaster.getChangeExceptionDates()) {
            exceptionDates.addAll(seriesMaster.getChangeExceptionDates());
        }
        if (null != seriesMaster.getDeleteExceptionDates()) {
            exceptionDates.addAll(seriesMaster.getDeleteExceptionDates());
        }
        if (exceptionDates.isEmpty()) {
            return null;
        }
        long[] timestamps = new long[exceptionDates.size()];
        int position = 0;
        for (RecurrenceId recurrenceId : exceptionDates) {
            timestamps[position++] = recurrenceId.getValue().getTimestamp();
        }
        return timestamps;
    }

    /**
     * Gets the event's duration in milliseconds.
     *
     * @param event The event
     * @return The duration
     */
    protected static long getEventDuration(Event event) {
        return event.getEndDate().getTimestamp() - event.getStartDate().getTimestamp();
    }

}
