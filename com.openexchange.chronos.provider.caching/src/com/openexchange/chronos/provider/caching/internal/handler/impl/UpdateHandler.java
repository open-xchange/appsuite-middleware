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

package com.openexchange.chronos.provider.caching.internal.handler.impl;

import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import org.apache.commons.lang3.ArrayUtils;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.utils.TruncationAwareCalendarStorage;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.session.Session;

/**
 * {@link UpdateHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UpdateHandler extends AbstractHandler {

    public UpdateHandler(BasicCachingCalendarAccess cachedCalendarAccess) {
        super(cachedCalendarAccess);
    }

    private void processDiff(TruncationAwareCalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        delete(calendarStorage, diff);
        create(calendarStorage, diff);
        update(calendarStorage, diff);
    }

    private void delete(TruncationAwareCalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        if (diff.getRemovedItems().isEmpty()) {
            return;
        }

        for (Event event : diff.getRemovedItems()) {
            delete(calendarStorage, event);
        }
    }

    protected void delete(TruncationAwareCalendarStorage calendarStorage, Event originalEvent) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            deleteExceptions(calendarStorage, originalEvent.getSeriesId(), getChangeExceptionDates(calendarStorage, originalEvent.getSeriesId()));
        }
        /*
         * delete event data from storage
         */
        String id = originalEvent.getId();
        calendarStorage.getEventStorage().insertEventTombstone(calendarStorage.getUtilities().getTombstone(originalEvent, new Date(), getCalendarUser()));
        calendarStorage.getAttendeeStorage().insertAttendeeTombstones(id, calendarStorage.getUtilities().getTombstones(originalEvent.getAttendees()));
        calendarStorage.getAlarmStorage().deleteAlarms(id);
        calendarStorage.getEventStorage().deleteEvent(id);
        calendarStorage.getAttendeeStorage().deleteAttendees(id, originalEvent.getAttendees());
    }

    protected SortedSet<RecurrenceId> getChangeExceptionDates(TruncationAwareCalendarStorage calendarStorage, String seriesId) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(CalendarUtils.getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, seriesId)).addSearchTerm(CalendarUtils.getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)));
        List<Event> changeExceptions = calendarStorage.getEventStorage().searchEvents(searchTerm, null, new EventField[] { EventField.RECURRENCE_ID });
        return CalendarUtils.getRecurrenceIds(changeExceptions);
    }

    protected void deleteExceptions(TruncationAwareCalendarStorage calendarStorage, String seriesID, Collection<RecurrenceId> exceptionDates) throws OXException {
        for (Event originalExceptionEvent : loadExceptionData(calendarStorage, seriesID, exceptionDates)) {
            delete(calendarStorage, originalExceptionEvent);
        }
    }

    protected List<Event> loadExceptionData(TruncationAwareCalendarStorage calendarStorage, String seriesID, Collection<RecurrenceId> recurrenceIDs) throws OXException {
        List<Event> exceptions = new ArrayList<Event>();
        if (null != recurrenceIDs && 0 < recurrenceIDs.size()) {
            for (RecurrenceId recurrenceID : recurrenceIDs) {
                Event exception = calendarStorage.getEventStorage().loadException(seriesID, recurrenceID, null);
                if (null == exception) {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(seriesID, String.valueOf(recurrenceID));
                }
                exceptions.add(exception);
            }
        }
        return calendarStorage.getUtilities().loadAdditionalEventData(this.cachedCalendarAccess.getSession().getUserId(), exceptions, getFields());
    }

    private void create(TruncationAwareCalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        if (diff.getAddedItems().isEmpty()) {
            return;
        }
        create(calendarStorage, diff.getAddedItems());
    }

    private void update(TruncationAwareCalendarStorage calendarStorage, EventUpdates diff) throws OXException {
        if (diff.getUpdatedItems().isEmpty()) {
            return;
        }

        for (EventUpdate eventUpdate : diff.getUpdatedItems()) {
            Event persistedEvent = eventUpdate.getOriginal();
            Event updatedEvent = eventUpdate.getUpdate();

            updatedEvent.setId(persistedEvent.getId());
            calendarStorage.updateEvent(updatedEvent);

            CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = eventUpdate.getAttendeeUpdates();
            if (!attendeeUpdates.isEmpty()) {
                updateAttendees(calendarStorage, updatedEvent.getId(), attendeeUpdates);
            }

            CollectionUpdate<Alarm, AlarmField> alarmUpdates = eventUpdate.getAlarmUpdates();
            if (!alarmUpdates.isEmpty()) {
                updateAlarms(calendarStorage, updatedEvent, alarmUpdates);
            }
        }
    }

    private void updateAlarms(TruncationAwareCalendarStorage calendarStorage, Event event, CollectionUpdate<Alarm, AlarmField> alarmUpdates) throws OXException {
        if (!alarmUpdates.isEmpty()) {
            int userId = cachedCalendarAccess.getSession().getUserId();
            AlarmStorage alarmStorage = calendarStorage.getAlarmStorage();
            if (!alarmUpdates.getAddedItems().isEmpty()) {
                for (Alarm alarm : alarmUpdates.getAddedItems()) {
                    alarm.setId(alarmStorage.nextId());
                }
                alarmStorage.insertAlarms(event, userId, alarmUpdates.getAddedItems());
            }
            if (!alarmUpdates.getRemovedItems().isEmpty()) {
                List<Integer> removedAlarms = new ArrayList<>(alarmUpdates.getRemovedItems().size());
                for (Alarm alarm : alarmUpdates.getRemovedItems()) {
                    removedAlarms.add(I(alarm.getId()));
                }
                alarmStorage.deleteAlarms(event.getId(), userId, ArrayUtils.toPrimitive(removedAlarms.toArray(new Integer[removedAlarms.size()])));
            }
            List<? extends ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
            if (!updatedItems.isEmpty()) {
                List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
                for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                    Alarm update = itemUpdate.getUpdate();
                    update.setId(itemUpdate.getOriginal().getId());
                    update.setUid(itemUpdate.getOriginal().getUid());
                    alarms.add(update);
                }
                alarmStorage.updateAlarms(event, userId, alarms);
            }
        }
    }

    private void updateAttendees(TruncationAwareCalendarStorage calendarStorage, String eventId, CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) throws OXException {
        if (!attendeeUpdates.isEmpty()) {
            AttendeeStorage attendeeStorage = calendarStorage.getAttendeeStorage();
            if (!attendeeUpdates.getAddedItems().isEmpty()) {
                calendarStorage.insertAttendees(eventId, attendeeUpdates.getAddedItems());
            }
            if (!attendeeUpdates.getRemovedItems().isEmpty()) {
                attendeeStorage.deleteAttendees(eventId, attendeeUpdates.getRemovedItems());
            }
            if (!attendeeUpdates.getUpdatedItems().isEmpty()) {
                List<Attendee> updatedAttendees = new ArrayList<>();
                for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates.getUpdatedItems()) {
                    Attendee updated = attendeeUpdate.getUpdate();
                    Attendee original = attendeeUpdate.getOriginal();
                    Attendee newUpdatedAttendee = AttendeeMapper.getInstance().copy(original, updated, AttendeeField.URI);
                    updatedAttendees.add(newUpdatedAttendee);
                }
                EntityResolver entityResolver = optEntityResolver(this.cachedCalendarAccess.getSession().getContextId());
                calendarStorage.updateAttendees(eventId, entityResolver != null ? entityResolver.prepare(updatedAttendees) : updatedAttendees);
            }
        }
    }

    @Override
    public ExternalCalendarResult getExternalEvents() throws OXException {
        return getAndPrepareExtEvents();
    }

    @Override
    public List<Event> getExistingEvents() throws OXException {
        return getExistingEventsForAccount();
    }

    @Override
    public void persist(final EventUpdates diff) throws OXException {
        if (diff.isEmpty()) {
            return;
        }
        final Session session = this.cachedCalendarAccess.getSession();
        new OSGiCalendarStorageOperation<Void>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                processDiff(new TruncationAwareCalendarStorage(storage, session), diff);

                return null;
            }

        }.executeUpdate();
    }
}
