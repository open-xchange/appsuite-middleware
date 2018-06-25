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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.optTimeZone;
import static com.openexchange.chronos.compat.Appointment2Event.asString;
import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import static org.slf4j.LoggerFactory.getLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.calendar.json.compat.Appointment;
import com.openexchange.calendar.json.compat.CalendarCollection;
import com.openexchange.calendar.json.compat.CalendarDataObject;
import com.openexchange.calendar.json.compat.RecurrenceChecker;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.DataAwareRecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.PositionAwareRecurrenceId;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.java.util.TimeZones;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link EventConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class EventConverter {

    /**
     * Wraps an exception from <code>com.openexchange.chronos.exception.CalendarExceptionCodes</code> into a similar legacy exception
     * (from <code>com.openexchange.groupware.calendar.OXCalendarExceptionCodes</code> if possible.
     *
     * @param e The exception to wrap
     * @return The wrapped exception if a suitable exception is available, or the passed exception, otherwise
     */
    public static OXException wrapCalendarException(OXException e) {
        if (false == e.isPrefix("CAL")) {
            return e;
        }
        Object firstArg = null != e.getLogArgs() && 0 < e.getLogArgs().length ? e.getLogArgs()[0] : null;
        switch (e.getCode()) {
            case 4224: // com.openexchange.chronos.exception.CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED
                return OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create(e);
            case 4225: // com.openexchange.chronos.exception.CalendarExceptionCodes.MOVE_OCCURRENCE_NOT_SUPPORTED
                return OXCalendarExceptionCodes.RECURRING_EXCEPTION_MOVE_EXCEPTION.create();
            case 4221: // com.openexchange.chronos.exception.CalendarExceptionCodes.END_BEFORE_START
                return OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create(e);
            case 4041: // com.openexchange.chronos.exception.CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER
                return OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.create(e);
            case 4030: // com.openexchange.chronos.exception.CalendarExceptionCodes.NO_READ_PERMISSION
                return OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.create(e, getNumericLogArgument(firstArg));
            case 4040: // com.openexchange.chronos.exception.CalendarExceptionCodes.EVENT_NOT_FOUND
                return OXException.notFound("Object " + firstArg + " in context");
            case 4042: // com.openexchange.chronos.exception.CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND
                return OXCalendarExceptionCodes.UNABLE_TO_CALCULATE_POSITION.create();
            case 4061: // com.openexchange.chronos.exception.CalendarExceptionCodes.INVALID_RECURRENCE_ID
                return OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create(firstArg);
            case 4090: // com.openexchange.chronos.exception.CalendarExceptionCodes.UID_CONFLICT
                return OXCalendarExceptionCodes.APPOINTMENT_UID_ALREDY_EXISTS.create("", firstArg);
            default:
                return e;
        }
    }

    /**
     * Gets the event fields for the supplied column identifiers.
     *
     * @param columns The column identifiers
     * @return The event fields
     */
    public static EventField[] getFields(int[] columns) {
        if (null == columns) {
            return null;
        }
        List<EventField> fields = new ArrayList<EventField>(columns.length);
        for (int column : columns) {
            EventField field = getField(column);
            if (null != field) {
                fields.add(field);
            }
        }
        return fields.toArray(new EventField[fields.size()]);
    }

    /**
     * Gets the event field for the supplied column identifier.
     *
     * @param column The column identifier
     * @return The event field, or <code>null</code> if no appropriate event field is mapped
     */
    public static EventField getField(int column) {
        switch (column) {
            case Appointment.OBJECT_ID:
                return EventField.ID;
            case Appointment.FOLDER_ID:
                return EventField.FOLDER_ID;
            case Appointment.CREATED_BY:
                return EventField.CREATED_BY;
            case Appointment.CREATION_DATE:
                return EventField.CREATED;
            case Appointment.MODIFIED_BY:
                return EventField.MODIFIED_BY;
            case Appointment.LAST_MODIFIED:
            case Appointment.LAST_MODIFIED_UTC:
                return EventField.TIMESTAMP;
            case CalendarObject.TITLE:
                return EventField.SUMMARY;
            case Appointment.SHOWN_AS:
                return EventField.TRANSP;
            case Appointment.LOCATION:
                return EventField.LOCATION;
            case CalendarObject.NOTE:
                return EventField.DESCRIPTION;
            case Appointment.CATEGORIES:
                return EventField.CATEGORIES;
            case Appointment.COLOR_LABEL:
                return EventField.COLOR;
            case Appointment.START_DATE:
                return EventField.START_DATE;
            case Appointment.END_DATE:
                return EventField.END_DATE;
            case Appointment.FULL_TIME:
                return EventField.START_DATE;
            case CalendarObject.RECURRENCE_ID:
                return EventField.SERIES_ID;
            case CalendarObject.RECURRENCE_TYPE:
            case CalendarObject.INTERVAL:
            case CalendarObject.DAYS:
            case CalendarObject.DAY_IN_MONTH:
            case CalendarObject.MONTH:
            case CalendarObject.UNTIL:
            case CalendarObject.RECURRENCE_COUNT:
            case Appointment.RECURRENCE_START:
            case CalendarObject.RECURRENCE_CALCULATOR:
                return EventField.RECURRENCE_RULE;
            case CalendarObject.CHANGE_EXCEPTIONS:
                return EventField.CHANGE_EXCEPTION_DATES;
            case CalendarObject.DELETE_EXCEPTIONS:
                return EventField.DELETE_EXCEPTION_DATES;
            case Appointment.TIMEZONE:
                return EventField.START_DATE;
            case CalendarObject.PARTICIPANTS:
            case CalendarObject.CONFIRMATIONS:
            case CalendarObject.USERS:
                return EventField.ATTENDEES;
            case CalendarObject.ALARM:
                return EventField.ALARMS;
            case CalendarObject.ORGANIZER:
            case CalendarObject.ORGANIZER_ID:
                return EventField.ORGANIZER;
            case CommonObject.UID:
                return EventField.UID;
            case CalendarObject.SEQUENCE:
                return EventField.SEQUENCE;
            case CalendarObject.NUMBER_OF_ATTACHMENTS:
            case CalendarObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT:
                return EventField.ATTACHMENTS;
            case CalendarObject.RECURRENCE_POSITION:
            case CalendarObject.RECURRENCE_DATE_POSITION:
                return EventField.RECURRENCE_ID;
            case CalendarObject.NOTIFICATION:
            default:
                return null;
        }
    }

    protected final ServiceLookup services;
    protected final Session session;

    /**
     * Initializes a new {@link EventConverter}.
     *
     * @param services A service lookup reference
     * @param session The session
     */
    public EventConverter(ServiceLookup services, Session session) {
        super();
        this.services = services;
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    /**
     * Gets a specific event.
     *
     * @param eventID The identifier of the event to get
     * @param fields The event fields to retrieve
     * @return The event
     */
    protected abstract Event getEvent(EventID eventID, EventField... fields) throws OXException;

    protected abstract SortedSet<RecurrenceId> loadChangeExceptionDates(Event event) throws OXException;

    protected abstract RecurrenceData loadRecurrenceData(String seriesId) throws OXException;

    /**
     * Gets the default timezone to use as fallback.
     *
     * @return The default timezone
     */
    public abstract TimeZone getDefaultTimeZone() throws OXException;

    protected RecurrenceService getRecurrenceService() {
        return services.getService(RecurrenceService.class);
    }

    protected abstract CalendarSession getCalendarSession();

    /**
     * Gets the event identifier for the supplied full appointment identifier, optionally resolving a recurrence position to the
     * corresponding recurrence identifier.
     *
     * @param folderID The folder identifier
     * @param objectID The object identifier
     * @param recurrencePosition The recurrence position, or a value <code>< 0</code> if not set
     * @return The event identifier
     */
    public EventID getEventID(String folderID, String objectID, int recurrencePosition) throws OXException {
        EventID eventID = new EventID(folderID, objectID);
        if (0 >= recurrencePosition) {
            return eventID;
        }
        RecurrenceData recurrenceData = new OriginalEventHolder(this, eventID).getRecurrenceData();
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(getRecurrenceService(), recurrenceData, recurrencePosition);
        return new EventID(folderID, objectID, recurrenceID);
    }

    /**
     * Gets the event identifier for the supplied full appointment identifier, optionally resolving a recurrence date position to the
     * corresponding recurrence identifier.
     *
     * @param folderID The folder identifier
     * @param objectID The object identifier
     * @param recurrenceDatePosition The recurrence date position, or <code>null</code> if not set
     * @return The event identifier
     */
    public EventID getEventID(String folderID, String objectID, Date recurrenceDatePosition) throws OXException {
        EventID eventID = new EventID(folderID, objectID);
        if (null == recurrenceDatePosition) {
            return eventID;
        }
        RecurrenceData recurrenceData = new OriginalEventHolder(this, eventID).getRecurrenceData();
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(getRecurrenceService(), recurrenceData, recurrenceDatePosition);
        return new EventID(folderID, objectID, recurrenceID);
    }

    /**
     * Gets a value indicating whether a specific appointment has the <i>all-day</i>-character or not. If the required information is not
     * available in the passed object (e.g. during an update operation), the properties of the existing event are checked.
     *
     * @param appointment The appointment to check
     * @param originalEventHolder The original event holder, or <code>null</code> if not available
     * @return <code>true</code> for an <i>all-day</i> event, <code>false</code>, otherwise
     */
    private boolean isAllDay(Appointment appointment, OriginalEventHolder originalEventHolder) throws OXException {
        if (appointment.containsFullTime()) {
            return appointment.getFullTime();
        }
        if (null != originalEventHolder && null != originalEventHolder.get()) {
            return originalEventHolder.get().getStartDate().isAllDay();
        }
        return false;
    }

    /**
     * Derives the timezone for a specific appointment. If the required information is not available in the passed object (e.g. during an
     * update operation), the properties of the existing event are checked.
     *
     * @param appointment The appointment to check
     * @param originalEventHolder The original event holder, or <code>null</code> if not available
     * @return The timezone, falling back to the request/user timezone, or <code>null</code> for floating events
     */
    private TimeZone getTimeZone(Appointment appointment, OriginalEventHolder originalEventHolder) throws OXException {
        /*
         * check for floating dates
         */
        if (isAllDay(appointment, originalEventHolder)) {
            return null;
        }
        /*
         * derive timezone
         */
        TimeZone defaultTimeZone = getDefaultTimeZone();
        if (appointment.containsTimezone()) {
            return optTimeZone(appointment.getTimezone(), defaultTimeZone);
        }
        if (null != originalEventHolder && null != originalEventHolder.get()) {
            return originalEventHolder.get().getStartDate().getTimeZone();
        }
        return defaultTimeZone;
    }

    /**
     * Converts the supplied appointment into a corresponding event.
     *
     * @param appointment The appointment to convert
     * @param originalEventID The identifier of the original event in case of update operations, or <code>null</code> if unknown
     * @return The event
     */
    public Event getEvent(Appointment appointment, EventID originalEventID) throws OXException {
        /*
         * prepare conversion
         */
        Event event = new Event();
        OriginalEventHolder originalEventHolder = null != originalEventID ? new OriginalEventHolder(this, originalEventID) : null;
        /*
         * convert appointment properties
         */
        if (appointment.containsObjectID()) {
            event.setId(asString(appointment.getObjectID()));
        }
        if (appointment.containsCreatedBy()) {
            if (0 == appointment.getCreatedBy()) {
                event.setCreatedBy(null);
            } else {
                event.setCreatedBy(getCalendarSession().getEntityResolver().applyEntityData(new CalendarUser(), appointment.getCreatedBy()));
            }
        }
        if (appointment.containsModifiedBy()) {
            if (0 == appointment.getModifiedBy()) {
                event.setModifiedBy(null);
            } else {
                event.setModifiedBy(getCalendarSession().getEntityResolver().applyEntityData(new CalendarUser(), appointment.getModifiedBy()));
            }
        }
        if (appointment.containsCreationDate()) {
            event.setCreated(appointment.getCreationDate());
        }
        if (appointment.containsLastModified()) {
            event.setTimestamp(null != appointment.getLastModified() ? appointment.getLastModified().getTime() : 0L);
        }
        //        event.setFolderId(appointment.getParentFolderID());
        //        event.setPublicFolderId(appointment.getParentFolderID());
        //        event.setFolderId(appointment.getPersonalFolderID());
        if (appointment.containsCategories()) {
            event.setCategories(Appointment2Event.getCategories(appointment.getCategories()));
        }
        if (appointment.containsPrivateFlag()) {
            event.setClassification(Appointment2Event.getClassification(appointment.getPrivateFlag()));
        }
        if (appointment.containsLabel()) {
            event.setColor(Appointment2Event.getColor(appointment.getLabel()));
        }
        //appointment.getNumberOfAttachments();
        //appointment.getLastModifiedOfNewestAttachment();
        if (appointment.containsTitle()) {
            event.setSummary(appointment.getTitle());
        }
        if (appointment.containsStartDate() || appointment.containsFullTime() || appointment.containsTimezone()) {
            if (appointment.containsStartDate() && null == appointment.getStartDate()) {
                event.setStartDate(null);
            } else {
                long timestamp;
                if (null != appointment.getStartDate()) {
                    timestamp = appointment.getStartDate().getTime();
                } else if (null != originalEventHolder) {
                    if (null != originalEventHolder.get()) {
                        timestamp = originalEventHolder.get().getStartDate().getTimestamp();
                    } else {
                        throw AjaxExceptionCodes.MISSING_FIELD.create(CalendarFields.START_DATE); // no other chance
                    }
                } else {
                    throw AjaxExceptionCodes.MISSING_FIELD.create(CalendarFields.START_DATE); // no other chance
                }
                if (isAllDay(appointment, originalEventHolder)) {
                    event.setStartDate(new DateTime(timestamp).toAllDay());
                } else {
                    event.setStartDate(new DateTime(getTimeZone(appointment, originalEventHolder), timestamp));
                }
            }
        }
        if (appointment.containsEndDate() || appointment.containsFullTime() || appointment.containsTimezone()) {
            if (appointment.containsEndDate() && null == appointment.getEndDate()) {
                event.setEndDate(null);
            } else {
                long timestamp;
                if (null != appointment.getEndDate()) {
                    timestamp = appointment.getEndDate().getTime();
                } else {
                    if (null != originalEventHolder) {
                        if (null != originalEventHolder.get()) {
                            timestamp = originalEventHolder.get().getEndDate().getTimestamp();
                        } else {
                            throw AjaxExceptionCodes.MISSING_FIELD.create(CalendarFields.END_DATE); // no other chance
                        }
                    } else {
                        throw AjaxExceptionCodes.MISSING_FIELD.create(CalendarFields.END_DATE); // no other chance
                    }
                }
                if (isAllDay(appointment, originalEventHolder)) {
                    event.setEndDate(new DateTime(timestamp).toAllDay());
                } else {
                    event.setEndDate(new DateTime(getTimeZone(appointment, originalEventHolder), timestamp));
                }
            }
        }
        if (appointment.containsNote()) {
            event.setDescription(appointment.getNote());
        }
        if (appointment.containsRecurrenceID()) {
            event.setSeriesId(asString(appointment.getRecurrenceID()));
        }
        if (appointment.containsRecurrenceDatePosition()) {
            if (null == appointment.getRecurrenceDatePosition()) {
                event.setRecurrenceId(null);
            } else {
                if (null != originalEventHolder) {
                    RecurrenceData recurrenceData = originalEventHolder.getRecurrenceData();
                    event.setRecurrenceId(Appointment2Event.getRecurrenceID(getRecurrenceService(), recurrenceData, appointment.getRecurrenceDatePosition()));
                }
            }
        }
        if (appointment.containsRecurrencePosition()) {
            if (0 >= appointment.getRecurrencePosition()) {
                event.setRecurrenceId(null);
            } else {
                if (null != originalEventHolder) {
                    RecurrenceData recurrenceData = originalEventHolder.getRecurrenceData();
                    event.setRecurrenceId(Appointment2Event.getRecurrenceID(getRecurrenceService(), recurrenceData, appointment.getRecurrencePosition()));
                }
            }
        }
        if (appointment.containsRecurrenceType()) {
            if (0 == appointment.getRecurrenceType()) {
                event.setRecurrenceRule(null);
            } else {
                RecurrenceData recurrenceData = getRecurrenceData(appointment, originalEventHolder);
                event.setRecurrenceRule(null != recurrenceData ? recurrenceData.getRecurrenceRule() : null);
            }
        }
        if (appointment.containsChangeExceptions()) {
            if (null == appointment.getChangeException()) {
                event.setChangeExceptionDates(null);
            } else {
                if (null != originalEventHolder) {
                    RecurrenceData recurrenceData = originalEventHolder.getRecurrenceData();
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(getRecurrenceService(), recurrenceData, Arrays.asList(appointment.getChangeException())));
                }
            }
        }
        if (appointment.containsDeleteExceptions()) {
            if (null == appointment.getDeleteException()) {
                event.setDeleteExceptionDates(null);
            } else {
                if (null != originalEventHolder) {
                    RecurrenceData recurrenceData = originalEventHolder.getRecurrenceData();
                    event.setDeleteExceptionDates(Appointment2Event.getRecurrenceIDs(getRecurrenceService(), recurrenceData, Arrays.asList(appointment.getDeleteException())));
                }
            }
        }
        //appointment.getNotification();
        //appointment.getRecurrenceCalculator();
        if (appointment.containsParticipants() || appointment.containsUserParticipants()) {
            UserParticipant[] users = appointment.containsUserParticipants() ? appointment.getUsers() : null;
            Participant[] participants = appointment.containsParticipants() ? appointment.getParticipants() : null;
            event.setAttendees(getAttendees(participants, users));
        }
        if (appointment.containsOrganizerId() || appointment.containsOrganizer() || appointment.containsPrincipal() || appointment.containsPrincipalId()) {
            event.setOrganizer(getOrganizer(appointment.getOrganizerId(), appointment.getOrganizer(), appointment.getPrincipalId(), appointment.getPrincipal()));
        }
        if (appointment.containsUid()) {
            event.setUid(appointment.getUid());
        }
        if (appointment.containsSequence()) {
            event.setSequence(appointment.getSequence());
        }
        if (appointment.containsLocation()) {
            event.setLocation(appointment.getLocation());
        }
        if (appointment.containsShownAs()) {
            event.setTransp(Appointment2Event.getTransparency(appointment.getShownAs()));
        }
        if (appointment.containsAlarm()) {
            if (-1 == appointment.getAlarm()) {
                event.setAlarms(null); // "-1" means alarm removal
            } else {
                event.setAlarms(Collections.singletonList(Appointment2Event.getAlarm(appointment.getAlarm())));
            }
        }
        return event;
    }

    /**
     * Converts the supplied event into a corresponding appointment.
     *
     * @param event The event to convert
     * @return The appointment
     */
    public CalendarDataObject getAppointment(Event event) throws OXException {
        CalendarDataObject appointment = new CalendarDataObject();
        RecurrenceData recurrenceData = null;
        if (event.containsId()) {
            appointment.setObjectID(asInt(event.getId()));
        }
        if (event.containsCreatedBy()) {
            appointment.setCreatedBy(null == event.getCreatedBy() ? 0 : event.getCreatedBy().getEntity());
        }
        if (event.containsModifiedBy()) {
            appointment.setModifiedBy(null == event.getModifiedBy() ? 0 : event.getModifiedBy().getEntity());
        }
        if (event.containsCreated()) {
            appointment.setCreationDate(event.getCreated());
        }
        if (event.containsTimestamp()) {
            appointment.setLastModified(new Date(event.getTimestamp()));
        }
        if (event.containsFolderId()) {
            appointment.setParentFolderID(asInt(event.getFolderId()));
            //        appointment.setParentFolderID(event.getPublicFolderId());
            //        appointment.setPersonalFolderID(event.getFolderId());
        }
        if (event.containsCategories()) {
            appointment.setCategories(Event2Appointment.getCategories(event.getCategories()));
        }
        if (event.containsClassification()) {
            appointment.setPrivateFlag(Event2Appointment.getPrivateFlag(event.getClassification()));
        }
        if (event.containsColor()) {
            appointment.setLabel(Event2Appointment.getColorLabel(event.getColor()));
        } else {
            appointment.setLabel(0);
        }
        if (event.containsAttachments()) {
            List<Attachment> attachments = event.getAttachments();
            if (null != attachments) {
                appointment.setNumberOfAttachments(attachments.size());
                Date lastModifiedOfNewestAttachment = null;
                for (Attachment attachment : attachments) {
                    if (null != attachment.getCreated() &&
                        (null == lastModifiedOfNewestAttachment || attachment.getCreated().after(lastModifiedOfNewestAttachment))) {
                        lastModifiedOfNewestAttachment = attachment.getCreated();
                    }
                }
                appointment.setLastModifiedOfNewestAttachment(lastModifiedOfNewestAttachment);
            } else {
                appointment.setNumberOfAttachments(0);
                appointment.setLastModifiedOfNewestAttachment(null);
            }
        }
        if (event.containsSummary()) {
            appointment.setTitle(event.getSummary());
        }
        if (isSeriesMaster(event)) {
            if (event.containsStartDate() || event.containsEndDate()) {
                // prefer start/end date of first occurrence for the series master event
                recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
                RecurrenceId firstRecurrenceId = null;
                RecurrenceIterator<RecurrenceId> iterator = getRecurrenceService().iterateRecurrenceIds(recurrenceData);
                if (iterator.hasNext()) {
                    firstRecurrenceId = iterator.next();
                }
                if (event.containsStartDate()) {
                    DateTime seriesStart = recurrenceData.getSeriesStart();
                    appointment.setFullTime(seriesStart.isAllDay());
                    appointment.setTimezone(seriesStart.isFloating() ? null : seriesStart.getTimeZone().getID());
                    long timestamp = null != firstRecurrenceId ? firstRecurrenceId.getValue().getTimestamp() : event.getStartDate().getTimestamp();
                    appointment.setStartDate(new Date(timestamp));
                }
                if (event.containsEndDate()) {
                    long timestamp;
                    if (null != firstRecurrenceId) {
                        long duration = event.getEndDate().getTimestamp() - event.getStartDate().getTimestamp();
                        timestamp = firstRecurrenceId.getValue().getTimestamp() + duration;
                    } else {
                        timestamp = event.getEndDate().getTimestamp();
                    }
                    appointment.setEndDate(new Date(timestamp));
                }
            }
        } else {
            if (event.containsStartDate()) {
                DateTime startDate = event.getStartDate();
                if (null == startDate) {
                    appointment.setStartDate(null);
                } else {
                    appointment.setFullTime(startDate.isAllDay());
                    appointment.setTimezone(null == startDate.getTimeZone() ? null : startDate.getTimeZone().getID());
                    appointment.setStartDate(new Date(startDate.getTimestamp()));
                }
            }
            if (event.containsEndDate()) {
                DateTime endDate = event.getEndDate();
                appointment.setEndDate(null == endDate ? null : new Date(endDate.getTimestamp()));
            }
        }
        if (event.containsDescription()) {
            appointment.setNote(event.getDescription());
        }
        if (event.containsAlarms()) {
            List<Alarm> alarms = event.getAlarms();
            if (false == isSeriesMaster(event) && null != alarms && 1 == alarms.size() && AlarmUtils.isAcknowledged(alarms.get(0), event, getDefaultTimeZone())) {
                // don't apply single dismissed reminders
            } else {
                Integer reminder = Event2Appointment.getReminder(event.getAlarms());
                if (null == reminder) {
                    // don't apply "-1" reminder minutes when converting to appointment
                } else {
                    appointment.setAlarmFlag(true);
                    appointment.setAlarm(reminder.intValue());
                }
            }
        }
        if (event.containsSeriesId()) {
            appointment.setRecurrenceID(asInt(event.getSeriesId(), true));
        }
        if (event.containsRecurrenceId()) {
            if (null == event.getRecurrenceId()) {
                appointment.setRecurrencePosition(0);
                appointment.setRecurrenceDatePosition(null);
            } else {
                if (PositionAwareRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                    appointment.setRecurrenceDatePosition(((PositionAwareRecurrenceId) event.getRecurrenceId()).getRecurrenceDatePosition());
                    appointment.setRecurrencePosition(((PositionAwareRecurrenceId) event.getRecurrenceId()).getRecurrencePosition());
                } else {
                    if (null == recurrenceData) {
                        if (DataAwareRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                            recurrenceData = (RecurrenceData) event.getRecurrenceId();
                        } else {
                            recurrenceData = loadRecurrenceData(event);
                        }
                    }
                    appointment.setRecurrenceDatePosition(Event2Appointment.getRecurrenceDatePosition(event.getRecurrenceId()));
                    if (null != event.getId()) {
                        appointment.setRecurrencePosition(Event2Appointment.getRecurrencePosition(getRecurrenceService(), recurrenceData, event.getRecurrenceId()));
                    }
                }
            }
        }
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule() && false == isSeriesException(event)) {

            // series pattern seems to be added in response for recurrence master and "regular" occurrences, but not for change exceptions

            if (null == recurrenceData) {
                if (isSeriesMaster(event) || null == event.getSeriesId() && null == event.getId()) {
                    recurrenceData = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
                } else if (null != event.getRecurrenceId() && DataAwareRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                    recurrenceData = (RecurrenceData) event.getRecurrenceId();
                } else {
                    recurrenceData = loadRecurrenceData(event);
                }
            }
            SeriesPattern pattern = Event2Appointment.getSeriesPattern(getRecurrenceService(), recurrenceData);
            if (SeriesPattern.MONTHLY_2.equals(pattern.getType())) {
                appointment.setRecurrenceType(SeriesPattern.MONTHLY_1.intValue());
            } else if (SeriesPattern.YEARLY_2.equals(pattern.getType())) {
                appointment.setRecurrenceType(SeriesPattern.YEARLY_1.intValue());
            } else {
                appointment.setRecurrenceType(pattern.getType().intValue());
            }
            if (null != pattern.getSeriesStart()) {
                appointment.setRecurringStart(pattern.getSeriesStart().longValue());
            }
            if (null != pattern.getDaysOfWeek()) {
                appointment.setDays(pattern.getDaysOfWeek());
            }
            if (null != pattern.getDayOfMonth()) {
                appointment.setDayInMonth(pattern.getDayOfMonth());
            }
            if (null != pattern.getMonth()) {
                appointment.setMonth(pattern.getMonth());
            }
            if (null != pattern.getInterval()) {
                appointment.setInterval(pattern.getInterval());
            }
            appointment.setUntil(null != pattern.getSeriesEnd() ? new Date(pattern.getSeriesEnd().longValue()) : null);
            if (null != pattern.getOccurrences()) {
                appointment.setOccurrence(pattern.getOccurrences());
            }
        }
        if (null != event.getChangeExceptionDates()) {
            appointment.setChangeExceptions(Event2Appointment.getRecurrenceDatePositions(event.getChangeExceptionDates()));
        }
        if (null != event.getDeleteExceptionDates()) {
            appointment.setDeleteExceptions(Event2Appointment.getRecurrenceDatePositions(event.getDeleteExceptionDates()));
        }
        if (isSeriesMaster(event)) {
            SortedSet<RecurrenceId> changeExceptionDates = loadChangeExceptionDates(event);
            if (0 < changeExceptionDates.size()) {
                appointment.setChangeExceptions(Event2Appointment.getRecurrenceDatePositions(changeExceptionDates));
            }
        }

        //appointment.setNotification(false);
        //appointment.setRecurrenceCalculator(0);
        if (event.containsAttendees()) {
            if (null == event.getAttendees()) {
                appointment.setParticipants((Participant[]) null);
                appointment.setUsers((UserParticipant[]) null);
                appointment.setConfirmations((ConfirmableParticipant[]) null);
            } else {
                List<Participant> participants = new ArrayList<Participant>();
                List<UserParticipant> users = new ArrayList<UserParticipant>();
                List<ConfirmableParticipant> confirmations = new ArrayList<ConfirmableParticipant>();
                for (Attendee attendee : event.getAttendees()) {
                    convertAttendee(attendee, participants, users, confirmations);
                }
                appointment.setParticipants(participants);
                appointment.setUsers(users);
                appointment.setConfirmations(confirmations);
            }

        }
        if (event.containsOrganizer()) {
            Organizer organizer = event.getOrganizer();
            if (null == organizer) {
                appointment.setOrganizer(null);
                appointment.setOrganizerId(0);
            } else if (null != organizer.getSentBy()) {
                appointment.setOrganizer(Event2Appointment.getEMailAddress(organizer.getSentBy().getUri()));
                appointment.setOrganizerId(organizer.getSentBy().getEntity());
                appointment.setPrincipal(Event2Appointment.getEMailAddress(organizer.getUri()));
                appointment.setPrincipalId(organizer.getEntity());
            } else {
                appointment.setOrganizer(Event2Appointment.getEMailAddress(organizer.getUri()));
                appointment.setOrganizerId(organizer.getEntity());
            }
        }
        if (event.containsUid()) {
            appointment.setUid(event.getUid());
        }
        if (event.containsSequence()) {
            appointment.setSequence(event.getSequence());
        } else {
            //            appointment.setSequence(0);
        }
        if (event.containsLocation()) {
            appointment.setLocation(event.getLocation());
        }
        if (event.containsTransp()) {
            appointment.setShownAs(null == event.getTransp() ? 0 : Event2Appointment.getShownAs(event.getTransp()));
        }
        return appointment;
    }

    /**
     * Converts the supplied participant into a corresponding attendee.
     *
     * @param participant The participant to convert
     * @return The attendee
     */
    public static Attendee getAttendee(Participant participant) {
        Attendee attendee = new Attendee();
        if (0 < participant.getIdentifier()) {
            attendee.setEntity(participant.getIdentifier());
        }
        if (0 < participant.getType()) {
            attendee.setCuType(Appointment2Event.getCalendarUserType(participant.getType()));
        }
        if (null != participant.getEmailAddress()) {
            attendee.setUri(Appointment2Event.getURI(participant.getEmailAddress()));
        }
        if (null != participant.getDisplayName()) {
            attendee.setCn(participant.getDisplayName());
        }
        if (ConfirmableParticipant.class.isInstance(participant)) {
            ConfirmableParticipant confirmableParticipant = (ConfirmableParticipant) participant;
            if (confirmableParticipant.containsStatus()) {
                attendee.setPartStat(Appointment2Event.getParticipationStatus(confirmableParticipant.getConfirm()));
            }
            if (confirmableParticipant.containsMessage()) {
                attendee.setComment(confirmableParticipant.getMessage());
            }
        }
        if (UserParticipant.class.isInstance(participant)) {
            UserParticipant userParticipant = (UserParticipant) participant;
            if (userParticipant.containsConfirm()) {
                attendee.setPartStat(Appointment2Event.getParticipationStatus(userParticipant.getConfirm()));
            }
            if (userParticipant.containsConfirmMessage()) {
                attendee.setComment(userParticipant.getConfirmMessage());
            }
        }
        return attendee;
    }

    /**
     * Converts an array of participants into their corresponding attendees.
     *
     * @param participants The participants to convert, or <code>null</code> if not set
     * @param users The user participant data, or <code>null</code> if not set
     * @return The attendees
     */
    private static List<Attendee> getAttendees(Participant[] participants, UserParticipant[] users) {
        if (null == participants && null == users) {
            return null;
        }
        List<Attendee> attendees = new ArrayList<Attendee>();
        if (null != participants) {
            for (Participant participant : participants) {
                attendees.add(getAttendee(participant));
            }
        }
        if (null != users) {
            for (UserParticipant user : users) {
                Attendee existingAttendee = find(attendees, user.getIdentifier());
                if (null != existingAttendee) {
                    copyProperties(getAttendee(user), existingAttendee);
                } else {
                    attendees.add(getAttendee(user));
                }
            }
        }
        return attendees;
    }

    private static Organizer getOrganizer(int organizerId, String organizerMail, int principalId, String principalMail) {
        if (null == organizerMail && 0 == organizerId && 0 == principalId && null == principalMail) {
            return null;
        }
        Organizer organizer = new Organizer();
        if (0 == principalId && null == principalMail) {
            organizer.setEntity(organizerId);
            organizer.setUri(Appointment2Event.getURI(organizerMail));
        } else {
            organizer.setEntity(principalId);
            organizer.setUri(Appointment2Event.getURI(principalMail));
            CalendarUser sentBy = new CalendarUser();
            sentBy.setEntity(organizerId);
            sentBy.setUri(Appointment2Event.getURI(organizerMail));
            organizer.setSentBy(sentBy);
        }
        return organizer;
    }

    /**
     * Extracts the series pattern from the supplied appointment data, optionally merging with the previous series pattern in case of
     * update operations.
     *
     * @param appointment The appointment to extract the series pattern from
     * @param originalEventHolder The original event holder, or <code>null</code> if not available
     * @return The series pattern, or <code>null</code> if not set
     */
    protected RecurrenceData getRecurrenceData(Appointment appointment, OriginalEventHolder originalEventHolder) throws OXException {
        /*
         * prepare series pattern & take over original pattern data if available
         */
        boolean fulltime = false;
        TimeZone timeZone = null;
        RecurrenceData originalRecurrenceData = null != originalEventHolder ? originalEventHolder.getRecurrenceData() : null;
        SeriesPattern pattern = new SeriesPattern();
        if (null != originalRecurrenceData) {
            DateTime seriesStart = originalRecurrenceData.getSeriesStart();
            fulltime = seriesStart.isAllDay();
            pattern.setSeriesStart(Long.valueOf(seriesStart.getTimestamp()));
            timeZone = null != seriesStart.getTimeZone() ? seriesStart.getTimeZone() : TimeZones.UTC;
        }
        if (appointment.containsRecurrenceType()) {
            if (0 == appointment.getRecurrenceType()) {
                return null;
            } else if (SeriesPattern.YEARLY_1.intValue() == appointment.getRecurrenceType() && appointment.containsDays()) {
                pattern.setType(SeriesPattern.YEARLY_2);
            } else if (SeriesPattern.MONTHLY_1.intValue() == appointment.getRecurrenceType() && appointment.containsDays()) {
                pattern.setType(SeriesPattern.MONTHLY_2);
            } else {
                pattern.setType(appointment.getRecurrenceType());
            }
        }
        if (appointment.containsDays()) {
            pattern.setDaysOfWeek(appointment.getDays());
        }
        if (appointment.containsDayInMonth()) {
            pattern.setDayOfMonth(appointment.getDayInMonth());
        }
        if (appointment.containsMonth()) {
            pattern.setMonth(appointment.getMonth());
        }
        if (appointment.containsInterval()) {
            pattern.setInterval(appointment.getInterval());
        }
        if (appointment.containsUntil()) {
            pattern.setSeriesEnd(null != appointment.getUntil() ? appointment.getUntil().getTime() : null);
        }
        if (appointment.containsOccurrence()) {
            pattern.setOccurrences(appointment.getOccurrence());
        }
        if (appointment.containsFullTime()) {
            fulltime = appointment.getFullTime();
        }
        if (appointment.containsTimezone() && null != appointment.getTimezone()) {
            timeZone = TimeZone.getTimeZone(appointment.getTimezone());
        }
        if (null == timeZone) {
            timeZone = getDefaultTimeZone();
        }

        if (appointment.containsRecurringStart() && appointment.containsStartDate()) {
            /*
             * recurring start and appointment start are both set; prefer the time fraction from start date
             */
            Calendar seriesStartCalendar = initCalendar(timeZone, appointment.getRecurringStart());
            Calendar startDateCalendar = initCalendar(timeZone, appointment.getStartDate());
            seriesStartCalendar.set(Calendar.HOUR_OF_DAY, startDateCalendar.get(Calendar.HOUR_OF_DAY));
            seriesStartCalendar.set(Calendar.MINUTE, startDateCalendar.get(Calendar.MINUTE));
            seriesStartCalendar.set(Calendar.SECOND, startDateCalendar.get(Calendar.SECOND));
            seriesStartCalendar.set(Calendar.MILLISECOND, startDateCalendar.get(Calendar.MILLISECOND));
            pattern.setSeriesStart(Long.valueOf(seriesStartCalendar.getTimeInMillis()));
        } else if (appointment.containsRecurringStart()) {
            /*
             * take over recurring start
             */
            pattern.setSeriesStart(Long.valueOf(appointment.getRecurringStart()));
        } else if (null == pattern.getSeriesStart() && null != appointment.getStartDate()) {
            /*
             * use appointment start as recurring start if missing
             */
            pattern.setSeriesStart(Long.valueOf(appointment.getStartDate().getTime()));
        }
        return Appointment2Event.getRecurrenceData(validate(pattern), timeZone, fulltime);
    }

    private SeriesPattern validate(SeriesPattern pattern) throws OXException {
        CalendarDataObject cdo = new CalendarDataObject();
        if (null != pattern.getType()) {
            if (SeriesPattern.MONTHLY_2.equals(pattern.getType())) {
                cdo.setRecurrenceType(SeriesPattern.MONTHLY_1.intValue());
            } else if (SeriesPattern.YEARLY_2.equals(pattern.getType())) {
                cdo.setRecurrenceType(SeriesPattern.YEARLY_1.intValue());
            } else {
                cdo.setRecurrenceType(pattern.getType().intValue());
            }
        }
        if (null != pattern.getInterval()) {
            cdo.setInterval(pattern.getInterval().intValue());
        }
        if (null != pattern.getSeriesEnd()) {
            cdo.setUntil(new Date(pattern.getSeriesEnd().longValue()));
        }
        if (null != pattern.getOccurrences()) {
            cdo.setOccurrence(pattern.getOccurrences().intValue());
        }
        if (null != pattern.getDayOfMonth()) {
            cdo.setDayInMonth(pattern.getDayOfMonth());
        }
        if (null != pattern.getDaysOfWeek()) {
            cdo.setDays(pattern.getDaysOfWeek());
        }
        if (null != pattern.getMonth()) {
            cdo.setMonth(pattern.getMonth());
        }
        RecurrenceChecker.check(cdo);
        new CalendarCollection().checkRecurring(cdo);
        return pattern;
    }

    public void convertAttendee(Attendee attendee, List<Participant> participants, List<UserParticipant> users, List<ConfirmableParticipant> confirmations) {
        if (CalendarUserType.GROUP.equals(attendee.getCuType())) {
            participants.add(new GroupParticipant(attendee.getEntity()));
        } else if (null == attendee.getCuType() || CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
            if (isInternal(attendee)) {
                UserParticipant userParticipant = new UserParticipant(attendee.getEntity());
                userParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                userParticipant.setConfirmMessage(attendee.getComment());
                users.add(userParticipant);
                if (null == attendee.getMember()) {
                    participants.add(new UserParticipant(attendee.getEntity()));
                }
            } else {
                ExternalUserParticipant externalParticipant = new ExternalUserParticipant(Event2Appointment.getEMailAddress(attendee.getUri()));
                externalParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                externalParticipant.setMessage(attendee.getComment());
                externalParticipant.setDisplayName(attendee.getCn());
                participants.add(externalParticipant);
                confirmations.add(externalParticipant);
            }
        } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) || CalendarUserType.ROOM.equals(attendee.getCuType())) {
            participants.add(new ResourceParticipant(attendee.getEntity()));
        }
    }

    /**
     * Loads the recurrence data for an event. Exception dates are excluded implicitly.
     *
     * @param event The event to get the recurrence data for
     * @return The recurrence data, or <code>null</code> if not set
     */
    protected RecurrenceData loadRecurrenceData(Event event) throws OXException {
        if (null != event.getId() && false == event.getId().equals(event.getSeriesId())) {
            if (null == event.getSeriesId()) {
                // no recurrence (yet)
                return new DefaultRecurrenceData(null, event.getStartDate(), null);
            }
            RecurrenceData recurrenceData = loadRecurrenceData(event.getSeriesId());
            return new DefaultRecurrenceData(recurrenceData.getRecurrenceRule(), recurrenceData.getSeriesStart(), null);
        }
        return new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), null);
    }

    /**
     * Copies all <i>set</i> properties from one attendee to another.
     *
     * @param from The source attendee to copy the <i>set</i> properties from
     * @param to The target attendee to copy the properties to
     * @return The passed target attendee reference
     */
    private static Attendee copyProperties(Attendee from, Attendee to) {
        if (from.containsCn()) {
            to.setCn(from.getCn());
        }
        if (from.containsComment()) {
            to.setComment(from.getComment());
        }
        if (from.containsCuType()) {
            to.setCuType(from.getCuType());
        }
        if (from.containsEntity()) {
            to.setEntity(from.getEntity());
        }
        if (from.containsFolderID()) {
            to.setFolderId(from.getFolderId());
        }
        if (from.containsMember()) {
            to.setMember(from.getMember());
        }
        if (from.containsPartStat()) {
            to.setPartStat(from.getPartStat());
        }
        if (from.containsRole()) {
            to.setRole(from.getRole());
        }
        if (from.containsRsvp()) {
            to.setRsvp(from.getRsvp());
        }
        if (from.containsSentBy()) {
            to.setSentBy(from.getSentBy());
        }
        if (from.containsUri()) {
            to.setUri(from.getUri());
        }
        return to;
    }

    private static Number getNumericLogArgument(Object arg) {
        if (null != arg) {
            if (Number.class.isInstance(arg)) {
                return (Number) arg;
            }
            try {
                return Long.valueOf(String.valueOf(arg));
            } catch (NumberFormatException e) {
                getLogger(EventConverter.class).warn("Error parsing numeric log argument {}.", arg, e);
            }
        }
        return null;
    }

}
