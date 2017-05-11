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

package com.openexchange.calendar.json.actions.chronos;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.provider.composition.CompositeEventID;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link IDBasedEventConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDBasedEventConverter extends EventConverter {

    private final IDBasedCalendarAccess access;

    /**
     * Initializes a new {@link IDBasedEventConverter}.
     *
     * @param services A service lookup reference
     * @param access The calendar access
     */
    public IDBasedEventConverter(ServiceLookup services, IDBasedCalendarAccess access) {
        super(services, access.getSession());
        this.access = access;
    }

    @Override
    protected TimeZone getDefaultTimeZone() throws OXException {
        TimeZone timeZone = access.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class);
        if (null == timeZone) {
            return TimeZone.getTimeZone(ServerSessionAdapter.valueOf(access.getSession()).getUser().getTimeZone());
        }
        return timeZone;
    }

    /**
     * Gets the event identifier for the supplied full appointment identifier, optionally resolving a recurrence position to the
     * corresponding recurrence identifier.
     *
     * @param objectID The unique event identifier
     * @param recurrencePosition The recurrence position, or a value <code>< 0</code> if not set
     * @return The event identifier
     */
    public CompositeEventID getEventID(String objectID, int recurrencePosition) throws OXException {
        CompositeEventID compositeID = CompositeEventID.parse(objectID);
        if (0 >= recurrencePosition) {
            return compositeID;
        }
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(getRecurrenceService(), loadRecurrenceData(compositeID), recurrencePosition);
        return new CompositeEventID(compositeID, recurrenceID);
    }

    /**
     * Gets the event identifier for the supplied full appointment identifier, optionally resolving a recurrence date position to the
     * corresponding recurrence identifier.
     *
     * @param objectID The object identifier
     * @param recurrenceDatePosition The recurrence date position, or <code>null</code> if not set
     * @return The event identifier
     */
    public CompositeEventID getEventID(String objectID, Date recurrenceDatePosition) throws OXException {
        CompositeEventID eventID = CompositeEventID.parse(objectID);
        if (null == recurrenceDatePosition) {
            return eventID;
        }
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(getRecurrenceService(), loadRecurrenceData(eventID), recurrenceDatePosition);
        return new CompositeEventID(eventID, recurrenceID);
    }

    @Override
    protected Event getEvent(EventID eventID, EventField... fields) throws OXException {
        return getEvent(CompositeEventID.parse(eventID.getObjectID()), fields);
    }

    protected Event getEvent(CompositeEventID eventID, EventField... fields) throws OXException {
        EventField[] oldFields = access.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        Boolean oldRecurrenceMaster = access.get(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.class);
        Date oldRangeStart = access.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date oldRangeEnd = access.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        try {
            access.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            access.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.TRUE);
            access.set(CalendarParameters.PARAMETER_RANGE_START, null);
            access.set(CalendarParameters.PARAMETER_RANGE_END, null);
            return access.getEvent(eventID);
        } finally {
            access.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            access.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, oldRecurrenceMaster);
            access.set(CalendarParameters.PARAMETER_RANGE_START, oldRangeStart);
            access.set(CalendarParameters.PARAMETER_RANGE_END, oldRangeEnd);
        }
    }

    /**
     * Loads the recurrence data for an event.
     *
     * @param eventID The identifier of the event to get the recurrence data for
     * @return The series pattern, or <code>null</code> if not set
     */
    protected RecurrenceData loadRecurrenceData(CompositeEventID eventID) throws OXException {
        EventField [] recurrenceFields = {
            EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.ALL_DAY,
            EventField.START_DATE, EventField.START_TIMEZONE, EventField.END_DATE, EventField.END_TIMEZONE
        };
        Event event = getEvent(eventID, recurrenceFields);
        if (false == event.getId().equals(event.getSeriesId())) {
            if (null == event.getSeriesId()) {
                // no recurrence (yet)
                return new DefaultRecurrenceData(null, event.isAllDay(), event.getStartTimeZone(), event.getStartDate().getTime());
            }
            event = getEvent(CompositeEventID.parse(event.getSeriesId()), recurrenceFields);
        }
        return new DefaultRecurrenceData(event);
    }

    @Override
    protected RecurrenceData loadRecurrenceData(Event event) throws OXException {
        if (ConvertibleEvent.class.isInstance(event)) {
            event = ((ConvertibleEvent) event).getDelegate();
        }
        return super.loadRecurrenceData(event);
    }

    @Override
    public CalendarDataObject getAppointment(Event event) throws OXException {
        ConvertibleEvent convertibleEvent = new ConvertibleEvent(event);
        CalendarDataObject appointment = super.getAppointment(convertibleEvent);
        if (event.containsId()) {
            appointment.setProperty("com.openexchange.chronos.provider.composition.EventID", event.getId());
        }
        if (event.containsFolderId()) {
            appointment.setProperty("com.openexchange.chronos.provider.composition.FolderID", event.getFolderId());
        }
        if (event.containsSeriesId()) {
            appointment.setProperty("com.openexchange.chronos.provider.composition.SeriesID", event.getSeriesId());
        }
        return appointment;
    }

    private static class ConvertibleEvent extends DelegatingEvent {

        ConvertibleEvent(Event delegate) {
            super(delegate);
        }

        Event getDelegate() {
            return delegate;
        }

        @Override
        public String getId() {
            return "0";
        }

        @Override
        public String getFolderId() {
            return "0";
        }

        @Override
        public String getSeriesId() {
            return "0";
        }

    }

}
