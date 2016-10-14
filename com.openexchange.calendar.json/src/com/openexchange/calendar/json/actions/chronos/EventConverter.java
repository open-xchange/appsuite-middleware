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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.calendar.RecurrenceChecker;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DataAwareRecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.UserizedEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import com.openexchange.server.ServiceLookup;

/**
 * {@link EventConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventConverter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link EventConverter}.
     *
     * @param services A service lookup reference
     */
    public EventConverter(ServiceLookup services) {
        super();
        this.services = services;
    }

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
        switch (e.getCode()) {
            case 4224: // com.openexchange.chronos.exception.CalendarExceptionCodes.MOVE_SERIES_NOT_SUPPORTED
                return OXCalendarExceptionCodes.RECURRING_FOLDER_MOVE.create(e);
            case 4221: // com.openexchange.chronos.exception.CalendarExceptionCodes.END_BEFORE_START
                return OXCalendarExceptionCodes.END_DATE_BEFORE_START_DATE.create(e);
            case 4041: // com.openexchange.chronos.exception.CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER
                return OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_2.create(e, e.getLogArgs()[1]);
            case 4030: // com.openexchange.chronos.exception.CalendarExceptionCodes.NO_READ_PERMISSION
                return OXCalendarExceptionCodes.LOAD_PERMISSION_EXCEPTION_5.create(e, e.getLogArgs()[0]);
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
                return EventField.PUBLIC_FOLDER_ID;
            case Appointment.CREATED_BY:
                return EventField.CREATED_BY;
            case Appointment.CREATION_DATE:
                return EventField.CREATED;
            case Appointment.MODIFIED_BY:
                return EventField.MODIFIED_BY;
            case Appointment.LAST_MODIFIED:
            case Appointment.LAST_MODIFIED_UTC:
                return EventField.LAST_MODIFIED;
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
                return EventField.ALL_DAY;
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
            case CalendarObject.DELETE_EXCEPTIONS:
                return EventField.DELETE_EXCEPTION_DATES;
            case CalendarObject.CHANGE_EXCEPTIONS:
                return EventField.CHANGE_EXCEPTION_DATES;
            case Appointment.TIMEZONE:
                return EventField.START_TIMEZONE;
            case CalendarObject.PARTICIPANTS:
            case CalendarObject.CONFIRMATIONS:
            case CalendarObject.USERS:
            case CalendarObject.ALARM:
                return EventField.ATTENDEES;
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

    public EventID getEventID(CalendarSession session, int folderID, int objectID, int recurrencePosition) throws OXException {
        EventID eventID = new EventID(folderID, objectID);
        if (0 >= recurrencePosition) {
            return eventID;
        }
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(loadRecurrenceData(session, eventID), recurrencePosition);
        return new EventID(folderID, objectID, recurrenceID);
    }

    public EventID getEventID(CalendarSession session, int folderID, int objectID, Date recurrenceDatePosition) throws OXException {
        EventID eventID = new EventID(folderID, objectID);
        if (null == recurrenceDatePosition) {
            return eventID;
        }
        RecurrenceId recurrenceID = Appointment2Event.getRecurrenceID(loadRecurrenceData(session, eventID), recurrenceDatePosition);
        return new EventID(folderID, objectID, recurrenceID);
    }

    /**
     * Converts the supplied appointment into a corresponding userized event.
     *
     * @param session The calendar session
     * @param appointment The appointment to convert
     * @param originalEventID The identifier of the original event in case of update operations, or <code>null</code> if unknwon
     * @return The userized event
     */
    public UserizedEvent getEvent(CalendarSession session, Appointment appointment, EventID originalEventID) throws OXException {
        Event event = new Event();
        if (appointment.containsObjectID()) {
            event.setId(appointment.getObjectID());
        }
        if (appointment.containsCreatedBy()) {
            event.setCreatedBy(appointment.getCreatedBy());
        }
        if (appointment.containsModifiedBy()) {
            event.setModifiedBy(appointment.getModifiedBy());
        }
        if (appointment.containsCreationDate()) {
            event.setCreated(appointment.getCreationDate());
        }
        if (appointment.containsLastModified()) {
            event.setLastModified(appointment.getLastModified());
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
        if (appointment.containsStartDate()) {
            event.setStartDate(appointment.getStartDate());
        }
        if (appointment.containsEndDate()) {
            event.setEndDate(appointment.getEndDate());
        }
        if (appointment.containsNote()) {
            event.setDescription(appointment.getNote());
        }
        if (appointment.containsRecurrenceID()) {
            event.setSeriesId(appointment.getRecurrenceID());
        }
        RecurrenceData recurrenceData = null;
        //        SeriesPattern seriesPattern = null;
        if (appointment.containsRecurrenceType()) {
            if (0 == appointment.getRecurrenceType()) {
                event.setRecurrenceRule(null);
            } else {
                RecurrenceData originalRecurrenceData = null != originalEventID ? loadRecurrenceData(session, originalEventID) : null;
                recurrenceData = getRecurrenceData(session, appointment, originalRecurrenceData);
                event.setRecurrenceRule(recurrenceData.getRecurrenceRule());
            }
        }
        if (appointment.containsRecurrenceDatePosition()) {
            if (null == appointment.getRecurrenceDatePosition()) {
                event.setRecurrenceId(null);
            } else {
                if (null == recurrenceData) {
                    recurrenceData = loadRecurrenceData(session, originalEventID);
                }
                event.setRecurrenceId(Appointment2Event.getRecurrenceID(recurrenceData, appointment.getRecurrenceDatePosition()));
            }
        }
        if (appointment.containsRecurrencePosition()) {
            if (0 >= appointment.getRecurrencePosition()) {
                event.setRecurrenceId(null);
            } else {
                if (null == recurrenceData) {
                    recurrenceData = loadRecurrenceData(session, originalEventID);
                }
                event.setRecurrenceId(Appointment2Event.getRecurrenceID(recurrenceData, appointment.getRecurrencePosition()));
            }
        }
        if (appointment.containsChangeExceptions()) {
            //TODO - UTC dates -> original start time of occurrence?
            event.setChangeExceptionDates(null != appointment.getChangeException() ? Arrays.asList(appointment.getChangeException()) : null);
        }
        if (appointment.containsDeleteExceptions()) {
            //TODO - UTC dates -> original start time of occurrence?
            event.setDeleteExceptionDates(null != appointment.getDeleteException() ? Arrays.asList(appointment.getDeleteException()) : null);
        }
        //appointment.getNotification();
        //appointment.getRecurrenceCalculator();
        if (appointment.containsParticipants()) {
            UserParticipant[] users = appointment.containsUserParticipants() ? appointment.getUsers() : null;
            event.setAttendees(getAttendees(appointment.getParticipants(), users));
        }
        if (appointment.containsOrganizerId() || appointment.containsOrganizer()) {
            event.setOrganizer(getOrganizer(appointment.getOrganizerId(), appointment.getOrganizer()));
        }
        //appointment.getPrincipal();
        //appointment.getPrincipalId();
        if (appointment.containsUid()) {
            event.setUid(appointment.getUid());
        }
        if (appointment.containsSequence()) {
            event.setSequence(appointment.getSequence());
        }
        if (appointment.containsLocation()) {
            event.setLocation(appointment.getLocation());
        }
        if (appointment.containsFullTime()) {
            event.setAllDay(appointment.getFullTime());
        }
        if (appointment.containsShownAs()) {
            event.setTransp(Appointment2Event.getTransparency(appointment.getShownAs()));
        }
        if (appointment.containsTimezone()) {
            event.setStartTimeZone(appointment.getTimezone());
        }

        UserizedEvent userizedEvent = new UserizedEvent(session.getSession(), event);
        if (appointment.containsAlarm()) {
            userizedEvent.setAlarms(Collections.singletonList(Appointment2Event.getAlarm(appointment.getAlarm())));
        }
        if (appointment.containsParentFolderID()) {
            userizedEvent.setFolderId(appointment.getParentFolderID());
        }
        return userizedEvent;
    }

    /**
     * Converts the supplied userized event into a corresponding appointment.
     *
     * @param session The calendar session
     * @param userizedEvent The userized event to convert
     * @return The appointment
     */
    public CalendarDataObject getAppointment(CalendarSession session, UserizedEvent userizedEvent) throws OXException {
        Event event = userizedEvent.getEvent();
        CalendarDataObject appointment = new CalendarDataObject();
        if (event.containsId()) {
            appointment.setObjectID(event.getId());
        }
        if (event.containsCreatedBy()) {
            appointment.setCreatedBy(event.getCreatedBy());
        }
        if (event.containsModifiedBy()) {
            appointment.setModifiedBy(event.getModifiedBy());
        }
        if (event.containsCreated()) {
            appointment.setCreationDate(event.getCreated());
        }
        if (event.containsLastModified()) {
            appointment.setLastModified(event.getLastModified());
        }
        if (userizedEvent.containsFolderId()) {
            appointment.setParentFolderID(userizedEvent.getFolderId());
        }
        //        appointment.setParentFolderID(event.getPublicFolderId());
        //        appointment.setPersonalFolderID(event.getFolderId());
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
                    if (null != attachment.getLastModified() &&
                        (null == lastModifiedOfNewestAttachment || attachment.getLastModified().after(lastModifiedOfNewestAttachment))) {
                        lastModifiedOfNewestAttachment = attachment.getLastModified();
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
        if (event.containsStartDate()) {
            appointment.setStartDate(event.getStartDate());
        }
        if (event.containsEndDate()) {
            appointment.setEndDate(event.getEndDate());
        }
        if (event.containsDescription()) {
            appointment.setNote(event.getDescription());
        }
        if (userizedEvent.containsAlarms()) {
            Integer reminder = Event2Appointment.getReminder(userizedEvent.getAlarms());
            appointment.setAlarm(null == reminder ? -1 : reminder.intValue());
        }
        if (event.containsSeriesId()) {
            appointment.setRecurrenceID(event.getSeriesId());
        }
        if (event.containsRecurrenceId() && null != event.getRecurrenceId()) {
            RecurrenceData recurrenceData;
            if (DataAwareRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                recurrenceData = (RecurrenceData) event.getRecurrenceId();
            } else {
                EventID masterID = new EventID(userizedEvent.getFolderId(), event.getSeriesId());
                recurrenceData = loadRecurrenceData(session, masterID);
            }
            // TODO:
            // provide master event in resolved recurrence?
            // provide needed info for calculation in resolved recurrence?
            appointment.setRecurrenceDatePosition(Event2Appointment.getRecurrenceDatePosition(event.getRecurrenceId()));
            appointment.setRecurrencePosition(Event2Appointment.getRecurrencePosition(recurrenceData, event.getRecurrenceId()));
        }
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule() && CalendarUtils.isSeriesMaster(event)) {
            SeriesPattern pattern = Event2Appointment.getSeriesPattern(new DefaultRecurrenceData(event));
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

        //appointment.setNotification(false);
        //appointment.setRecurrenceCalculator(0);
        if (event.containsAttendees()) {
            List<Attendee> attendees = event.getAttendees();
            if (null == attendees) {
                appointment.setParticipants((Participant[]) null);
                appointment.setUsers((UserParticipant[]) null);
                appointment.setConfirmations((ConfirmableParticipant[]) null);
            } else {
                List<Participant> participants = new ArrayList<Participant>();
                List<UserParticipant> users = new ArrayList<UserParticipant>();
                List<ConfirmableParticipant> confirmations = new ArrayList<ConfirmableParticipant>();
                for (Attendee attendee : attendees) {
                    convertAttendee(attendee, participants, users, confirmations);
                }
                appointment.setParticipants(participants);
                appointment.setUsers(users);
                appointment.setConfirmations(confirmations);
            }

        }
        if (event.containsOrganizer()) {
            appointment.setOrganizer(null != event.getOrganizer() ? Event2Appointment.getEMailAddress(event.getOrganizer().getUri()) : null);
            appointment.setOrganizerId(null != event.getOrganizer() ? event.getOrganizer().getEntity() : 0);
        }
        //appointment.setPrincipal(null);
        //appointment.setPrincipalId(0);
        if (event.containsUid()) {
            appointment.setUid(event.getUid());
        }
        if (event.containsSequence()) {
            appointment.setSequence(event.getSequence());
        } else {
            appointment.setSequence(0);
        }
        if (event.containsLocation()) {
            appointment.setLocation(event.getLocation());
        }
        if (event.containsAllDay()) {
            appointment.setFullTime(event.isAllDay());
        } else {
            appointment.setFullTime(false);
        }
        if (event.containsTransp()) {
            appointment.setShownAs(Event2Appointment.getShownAs(event.getTransp()));
        }
        if (event.containsStartTimeZone()) {
            appointment.setTimezone(event.getStartTimeZone());
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
     * @param participants The participants to convert
     * @param users The optionally defined user participant data, or <code>null</code> if not set
     * @return The attendees
     */
    private static List<Attendee> getAttendees(Participant[] participants, UserParticipant[] users) {
        if (null == participants) {
            return null;
        }
        List<Attendee> attendees = new ArrayList<Attendee>(participants.length);
        for (Participant participant : participants) {
            attendees.add(getAttendee(participant));
        }
        if (null != users) {
            for (UserParticipant user : users) {
                Attendee existingAttendee = find(attendees, user.getIdentifier());
                if (null != existingAttendee) {
                    copyProperties(getAttendee(user), existingAttendee);
                } else {
                    //TODO: add from users array or not?
                    // needs to be ignored for
                    // - com.openexchange.ajax.appointment.bugtests.Bug15903Test.testUpdatedParticipants()
                    //                     attendees.add(getAttendee(user));
                }
            }
        }
        return attendees;
    }

    private static Organizer getOrganizer(int organizerId, String organizerMail) {
        if (null == organizerMail && 0 == organizerId) {
            return null;
        }
        Organizer organizer = new Organizer();
        organizer.setEntity(organizerId);
        organizer.setUri(Appointment2Event.getURI(organizerMail));
        return organizer;
    }

    /**
     * Extracts the series pattern from the supplied appointment data, optionally merging with the previous series pattern in case of
     * update operations.
     *
     * @param appointment The appointment to extract the series pattern from
     * @param originalPattern The original pattern, or <code>null</code> if not available
     * @return The series pattern, or <code>null</code> if not set
     */
    private RecurrenceData getRecurrenceData(CalendarSession session, Appointment appointment, RecurrenceData originalRecurrenceData) throws OXException {
        /*
         * prepare series pattern & take over original pattern data if available
         */
        SeriesPattern pattern = new SeriesPattern();
        if (null != originalRecurrenceData) {
            if (null != originalRecurrenceData.getRecurrenceRule()) {
                SeriesPattern originalPattern = Event2Appointment.getSeriesPattern(originalRecurrenceData);
                pattern.setDayOfMonth(originalPattern.getDayOfMonth());
                pattern.setDaysOfWeek(originalPattern.getDaysOfWeek());
                pattern.setFullTime(originalPattern.isFullTime());
                pattern.setInterval(originalPattern.getInterval());
                pattern.setMonth(originalPattern.getMonth());
                if (null != originalPattern.getOccurrences()) {
                    pattern.setOccurrences(originalPattern.getOccurrences());
                } else {
                    pattern.setSeriesEnd(originalPattern.getSeriesEnd());
                }
                pattern.setSeriesStart(originalPattern.getSeriesStart());
                pattern.setType(originalPattern.getType());
                pattern.setTz(originalPattern.getTimeZone());
            } else {
                pattern.setFullTime(Boolean.valueOf(originalRecurrenceData.isAllDay()));
                pattern.setSeriesStart(Long.valueOf(originalRecurrenceData.getSeriesStart()));
                pattern.setTz(null != originalRecurrenceData.getTimeZoneID() ? TimeZone.getTimeZone(originalRecurrenceData.getTimeZoneID()) : null);
            }
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
        if (appointment.containsRecurringStart()) {
            pattern.setSeriesStart(Long.valueOf(appointment.getRecurringStart()));
        } else if (null == pattern.getSeriesStart() && null != appointment.getStartDate()) {
            pattern.setSeriesStart(Long.valueOf(appointment.getStartDate().getTime()));
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
            pattern.setFullTime(Boolean.valueOf(appointment.getFullTime()));
        } else if (null == pattern.isFullTime()) {
            pattern.setFullTime(Boolean.FALSE);
        }
        if (appointment.containsTimezone()) {
            pattern.setTz(TimeZone.getTimeZone(appointment.getTimezone()));
        } else if (null == pattern.getTimeZone()) {
            pattern.setTz(session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class, TimeZone.getTimeZone(session.getUser().getTimeZone())));
        }
        return Appointment2Event.getRecurrenceData(validate(pattern));
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
        services.getService(CalendarCollectionService.class).checkRecurring(cdo);
        return pattern;
    }

    private static void convertAttendee(Attendee attendee, List<Participant> participants, List<UserParticipant> users, List<ConfirmableParticipant> confirmations) {
        if (null == attendee.getCuType()) {
            return;
        }
        switch (attendee.getCuType()) {
            case GROUP:
                GroupParticipant groupParticipant = new GroupParticipant(attendee.getEntity());
                // display name is not expected for groups participants
                //                groupParticipant.setDisplayName(attendee.getCommonName());
                participants.add(groupParticipant);
                break;
            case INDIVIDUAL:
                if (CalendarUtils.isInternal(attendee)) {
                    UserParticipant userParticipant = new UserParticipant(attendee.getEntity());
                    userParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                    userParticipant.setConfirmMessage(attendee.getComment());
                    userParticipant.setDisplayName(attendee.getCn());
                    if (null != attendee.getUri() && attendee.getUri().toLowerCase().startsWith("mailto:")) {
                        userParticipant.setEmailAddress(Event2Appointment.getEMailAddress(attendee.getUri()));
                    }
                    users.add(userParticipant);
                    if (null == attendee.getMember()) {
                        participants.add(userParticipant);
                    }
                } else {
                    ExternalUserParticipant externalParticipant = new ExternalUserParticipant(Event2Appointment.getEMailAddress(attendee.getUri()));
                    externalParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                    externalParticipant.setMessage(attendee.getComment());
                    externalParticipant.setDisplayName(attendee.getCn());
                    participants.add(externalParticipant);
                    confirmations.add(externalParticipant);
                }
                break;
            case RESOURCE:
            case ROOM:
                ResourceParticipant resourceParticipant = new ResourceParticipant(attendee.getEntity());
                resourceParticipant.setDisplayName(attendee.getCn());
                participants.add(resourceParticipant);
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
    }

    /**
     * Loads the recurrence data for an event.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to get the recurrence data for
     * @return The series pattern, or <code>null</code> if not set
     */
    private RecurrenceData loadRecurrenceData(CalendarSession session, EventID eventID) throws OXException {
        EventField [] recurrenceFields = {
            EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.ALL_DAY,
            EventField.START_DATE, EventField.START_TIMEZONE, EventField.END_DATE, EventField.END_TIMEZONE
        };
        Event event = getEvent(session, eventID, recurrenceFields);
        if (event.getSeriesId() != event.getId()) {
            if (0 == event.getSeriesId()) {
                // no recurrence (yet)
                return new DefaultRecurrenceData(null, event.isAllDay(), event.getStartTimeZone(), event.getStartDate().getTime());
            }
            event = getEvent(session, new EventID(eventID.getFolderID(), event.getSeriesId()), recurrenceFields);
        }
        return new DefaultRecurrenceData(event);
    }

    /**
     * Gets a specific event.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to get
     * @param fields The event fields to retrieve
     * @return The event
     */
    private Event getEvent(CalendarSession session, EventID eventID, EventField... fields) throws OXException {
        EventField[] oldFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        Boolean oldRecurrenceMaster = session.get(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.class);
        Date oldRangeStart = session.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date oldRangeEnd = session.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        try {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            session.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, Boolean.TRUE);
            session.set(CalendarParameters.PARAMETER_RANGE_START, null);
            session.set(CalendarParameters.PARAMETER_RANGE_END, null);
            UserizedEvent event = session.getCalendarService().getEvent(session, eventID.getFolderID(), eventID.getObjectID());
            return null != event ? event.getEvent() : null;
        } finally {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            session.set(CalendarParameters.PARAMETER_RECURRENCE_MASTER, oldRecurrenceMaster);
            session.set(CalendarParameters.PARAMETER_RANGE_START, oldRangeStart);
            session.set(CalendarParameters.PARAMETER_RANGE_END, oldRangeEnd);
        }
    }

    /**
     * Looks up a specific internal attendee in a collection of attendees based on its entity identifier.
     *
     * @param attendees The attendees to search
     * @param entity The entity identifier to lookup
     * @return The matching attendee, or <code>null</code> if not found
     */
    private static Attendee find(List<Attendee> attendees, int entity) {
        if (null != attendees && 0 < attendees.size()) {
            for (Attendee attendee : attendees) {
                if (entity == attendee.getEntity()) {
                    return attendee;
                }
            }
        }
        return null;
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
            to.setFolderID(from.getFolderID());
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

}
