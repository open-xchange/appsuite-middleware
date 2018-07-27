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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.compare;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.splitExceptionDates;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.injectRecurrenceData;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.UUID;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RelatedTo;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.TimeZones;

/**
 * {@link SplitPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SplitPerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link SplitPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public SplitPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Initializes a new {@link SplitPerformer}, taking over the settings from another update performer.
     *
     * @param updatePerformer The update performer to take over the settings from
     */
    protected SplitPerformer(AbstractUpdatePerformer updatePerformer) {
        super(updatePerformer);
    }

    /**
     * Performs the split operation.
     *
     * @param objectId The identifier of the event to split
     * @param splitPoint The (minimum inclusive) date or date-time where the split is to occur
     * @param uid A new unique identifier to assign to the new part of the series, or <code>null</code> if not set
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The split result
     */
    public InternalCalendarResult perform(String objectId, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        /*
         * load original event data & check permissions
         */
        Event originalEvent = requireUpToDateTimestamp(loadEventData(objectId), clientTimestamp);
        if (false == isSeriesMaster(originalEvent)) {
            throw CalendarExceptionCodes.INVALID_SPLIT.create(originalEvent.getId(), splitPoint);
        }
        Check.eventIsInFolder(originalEvent, folder);
        requireWritePermissions(originalEvent);
        Map<Integer, List<Alarm>> originalAlarmsByUserId = storage.getAlarmStorage().loadAlarms(originalEvent);
        List<Event> originalChangeExceptions = loadExceptionData(originalEvent);
        /*
         * check the supplied split point for validity & derive next recurrence
         */
        if (splitPoint.before(originalEvent.getStartDate())) {
            throw CalendarExceptionCodes.INVALID_SPLIT.create(originalEvent.getId(), splitPoint);
        }
        TimeZone timeZone = isFloating(originalEvent) ? TimeZones.UTC : originalEvent.getStartDate().getTimeZone();
        DefaultRecurrenceData originalRecurrenceData = new DefaultRecurrenceData(originalEvent.getRecurrenceRule(), originalEvent.getStartDate(), null);
        RecurrenceIterator<RecurrenceId> iterator = session.getRecurrenceService().iterateRecurrenceIds(originalRecurrenceData, new Date(splitPoint.getTimestamp()), null);
        if (false == iterator.hasNext()) {
            throw CalendarExceptionCodes.INVALID_SPLIT.create(originalEvent.getId(), splitPoint);
        }
        RecurrenceId nextRecurrenceId = iterator.next();
        /*
         * prepare common related-to value to link the splitted series
         */
        RelatedTo relatedTo = new RelatedTo("X-CALENDARSERVER-RECURRENCE-SET", UUID.randomUUID().toString());
        /*
         * prepare a new series event representing the 'detached' part prior to the split time, based on the original series master
         */
        Event detachedSeriesMaster = EventMapper.getInstance().copy(originalEvent, null, true, (EventField[]) null);
        detachedSeriesMaster.setId(storage.getEventStorage().nextId());
        detachedSeriesMaster.setSeriesId(detachedSeriesMaster.getId());
        detachedSeriesMaster.setUid(Strings.isNotEmpty(uid) ? Check.uidIsUnique(session, storage, uid) : UUID.randomUUID().toString());
        detachedSeriesMaster.setFilename(null);
        detachedSeriesMaster.setRelatedTo(relatedTo);
        Consistency.setCreated(session, timestamp, detachedSeriesMaster, session.getUserId());
        Consistency.setModified(session, timestamp, detachedSeriesMaster, session.getUserId());
        /*
        * prepare event update for the original series for the part after on or after the split time;
         */
        Event updatedSeriesMaster = EventMapper.getInstance().copy(
            originalEvent, null, EventField.ID, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE);
        updatedSeriesMaster.setRelatedTo(relatedTo);
        Consistency.setModified(session, timestamp, updatedSeriesMaster, session.getUserId());
        /*
         * distribute recurrence dates prior / on or after the split time
         */
        if (false == isNullOrEmpty(originalEvent.getRecurrenceDates())) {
            Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splittedRecurrenceDates = splitExceptionDates(originalEvent.getRecurrenceDates(), splitPoint);
            detachedSeriesMaster.setRecurrenceDates(splittedRecurrenceDates.getKey());
            updatedSeriesMaster.setRecurrenceDates(splittedRecurrenceDates.getValue());
        }
        /*
         * distribute delete exception dates prior / on or after the split time
         */
        if (false == isNullOrEmpty(originalEvent.getDeleteExceptionDates())) {
            Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splittedExceptionDates = splitExceptionDates(originalEvent.getDeleteExceptionDates(), splitPoint);
            detachedSeriesMaster.setDeleteExceptionDates(splittedExceptionDates.getKey());
            updatedSeriesMaster.setDeleteExceptionDates(splittedExceptionDates.getValue());
        }
        /*
         * distribute change exception dates prior / on or after the split time
         */
        if (false == isNullOrEmpty(originalEvent.getChangeExceptionDates())) {
            Entry<SortedSet<RecurrenceId>, SortedSet<RecurrenceId>> splittedExceptionDates = splitExceptionDates(originalEvent.getChangeExceptionDates(), splitPoint);
            detachedSeriesMaster.setChangeExceptionDates(splittedExceptionDates.getKey());
            updatedSeriesMaster.setChangeExceptionDates(splittedExceptionDates.getValue());
        }
        /*
         * adjust recurrence rule for the detached series to have a fixed UNTIL one second or day prior the split point
         */
        RecurrenceRule detachedRule = initRecurrenceRule(originalEvent.getRecurrenceRule());
        DateTime until = splitPoint.addDuration(splitPoint.isAllDay() ? new Duration(-1, 1, 0) : new Duration(-1, 0, 1));
        detachedRule.setUntil(until);
        detachedSeriesMaster.setRecurrenceRule(detachedRule.toString());
        if (detachedSeriesMaster.getStartDate().after(until) || false == hasFurtherOccurrences(detachedSeriesMaster, null)) {
            /*
             * no occurrences in 'detached' series, so no split is needed
             */
            return resultTracker.getResult();
        }
        /*
         * adjust recurrence rule, start- and end-date for the updated event series to begin on or after the split point
         */
        RecurrenceRule updatedRule = initRecurrenceRule(originalEvent.getRecurrenceRule());
        if (null != updatedRule.getCount()) {
            DefaultRecurrenceData detachedRecurrenceData = new DefaultRecurrenceData(detachedSeriesMaster.getRecurrenceRule(), originalEvent.getStartDate(), null);
            for (iterator = session.getRecurrenceService().iterateRecurrenceIds(detachedRecurrenceData); iterator.hasNext(); iterator.next()) {
                ;
            }
            updatedRule.setCount(i(updatedRule.getCount()) - iterator.getPosition());
            updatedSeriesMaster.setRecurrenceRule(updatedRule.toString());
        }
        updatedSeriesMaster.setStartDate(CalendarUtils.calculateStart(originalEvent, nextRecurrenceId));
        updatedSeriesMaster.setEndDate(CalendarUtils.calculateEnd(originalEvent, nextRecurrenceId));
        if (false == hasFurtherOccurrences(updatedSeriesMaster, null)) {
            /*
             * no occurrences in updated series, so no split is needed
             */
            return resultTracker.getResult();
        }
        /*
         * insert the new detached series event, taking over any auxiliary data from the original series
         */
        Check.quotaNotExceeded(storage, session);
        storage.getEventStorage().insertEvent(detachedSeriesMaster);
        storage.getAttendeeStorage().insertAttendees(detachedSeriesMaster.getId(), originalEvent.getAttendees());
        storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), detachedSeriesMaster.getId(), originalEvent.getAttachments());
        Map<Integer, List<Alarm>> newAlarmsByUserId = insertAlarms(detachedSeriesMaster, originalAlarmsByUserId, true);
        storage.getAlarmTriggerStorage().insertTriggers(detachedSeriesMaster, newAlarmsByUserId);
        resultTracker.trackCreation(loadEventData(detachedSeriesMaster.getId()));
        /*
         * assign existing change exceptions to new detached event series, if prior split time
         */
        for (Event originalChangeException : originalChangeExceptions) {
            if (0 > compare(originalChangeException.getRecurrenceId().getValue(), splitPoint, timeZone)) {
                Event exceptionUpdate = EventMapper.getInstance().copy(originalChangeException, null, EventField.ID);
                EventMapper.getInstance().copy(detachedSeriesMaster, exceptionUpdate, EventField.SERIES_ID, EventField.UID, EventField.FILENAME, EventField.RELATED_TO);
                Consistency.setModified(session, timestamp, exceptionUpdate, session.getUserId());
                storage.getEventStorage().updateEvent(exceptionUpdate);
                resultTracker.trackUpdate(originalChangeException, loadEventData(originalChangeException.getId()));
            }
        }
        /*
         * update the original event series; also decorate original change exceptions on or after the split with the related-to marker
         */
        storage.getEventStorage().updateEvent(updatedSeriesMaster);
        resultTracker.trackUpdate(originalEvent, loadEventData(originalEvent.getId()));
        for (Event originalChangeException : originalChangeExceptions) {
            if (0 <= compare(originalChangeException.getRecurrenceId().getValue(), splitPoint, timeZone)) {
                Event exceptionUpdate = EventMapper.getInstance().copy(originalChangeException, null, EventField.ID);
                exceptionUpdate.setRelatedTo(relatedTo);
                // workaround to hide a possibly incorrect recurrence position in passed recurrence id for legacy storage
                // TODO: remove once no longer needed
                injectRecurrenceData(exceptionUpdate, new DefaultRecurrenceData(updatedSeriesMaster.getRecurrenceRule(), updatedSeriesMaster.getStartDate()));
                Consistency.setModified(session, timestamp, exceptionUpdate, session.getUserId());
                storage.getEventStorage().updateEvent(exceptionUpdate);
                resultTracker.trackUpdate(originalChangeException, loadEventData(originalChangeException.getId()));
            }
        }
        return resultTracker.getResult();
    }

}
