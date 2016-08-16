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
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarParameters;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.exception.OXException;
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
        String recurrenceRule = null;
        if (appointment.containsRecurrenceType()) {
            if (0 == appointment.getRecurrenceType()) {
                event.setRecurrenceRule(null);
            } else {
                SeriesPattern originalPattern = null != originalEventID ? loadSeriesPattern(session, originalEventID) : null;
                recurrenceRule = Appointment2Event.getRecurrenceRule(getSeriesPattern(session, appointment, originalPattern));
                event.setRecurrenceRule(recurrenceRule);
            }
        }
        if (appointment.containsRecurrenceDatePosition()) {
            //TODO
            event.setRecurrenceId(Appointment2Event.getRecurrenceId(recurrenceRule, appointment.getRecurrenceDatePosition()));
        }
        if (appointment.containsRecurrencePosition()) {
            //TODO
            event.setRecurrenceId(Appointment2Event.getRecurrenceId(recurrenceRule, appointment.getRecurrencePosition()));
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
            event.setAttendees(getAttendees(appointment.getParticipants()));
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
            event.setStartTimezone(appointment.getTimezone());
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
    public static Appointment getAppointment(UserizedEvent userizedEvent) {
        Event event = userizedEvent.getEvent();
        Appointment appointment = new Appointment();
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
        if (event.containsRecurrenceId()) {
            //TODO
            appointment.setRecurrenceDatePosition(Event2Appointment.getRecurrenceDatePosition(event.getRecurrenceRule(), event.getRecurrenceId()));
            appointment.setRecurrencePosition(Event2Appointment.getRecurrencePosition(event.getRecurrenceRule(), event.getRecurrenceId()));
        }
        SeriesPattern pattern = Event2Appointment.getSeriesPattern(event.getRecurrenceRule(), event.getStartDate(), event.getStartTimezone(), event.isAllDay());
        if (null != pattern) {
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
            if (null != event.getChangeExceptionDates()) {
                // TODO: original start date -> utc date
                appointment.setChangeExceptions(event.getChangeExceptionDates());
            }
            if (null != event.getDeleteExceptionDates()) {
                // TODO: original start date -> utc date
                appointment.setDeleteExceptions(event.getDeleteExceptionDates());
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
        if (event.containsStartTimezone()) {
            appointment.setTimezone(event.getStartTimezone());
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
            int confirm = ((ConfirmableParticipant) participant).getConfirm();
            if (0 < confirm) {
                attendee.setPartStat(Appointment2Event.getParticipationStatus(confirm));
            }
            String message = ((ConfirmableParticipant) participant).getMessage();
            if (null != message) {
                attendee.setComment(message);
            }
        }
        if (UserParticipant.class.isInstance(participant)) {
            String message = ((UserParticipant) participant).getConfirmMessage();
            if (null != message) {
                attendee.setComment(message);
            }
        }
        return attendee;
    }

    private static List<Attendee> getAttendees(Participant[] participants) {
        if (null == participants) {
            return null;
        }
        List<Attendee> attendees = new ArrayList<Attendee>(participants.length);
        for (Participant participant : participants) {
            attendees.add(getAttendee(participant));
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
    private static SeriesPattern getSeriesPattern(CalendarSession session, Appointment appointment, SeriesPattern originalPattern) {
        /*
         * prepare series pattern & take over original pattern data if available
         */
        SeriesPattern pattern = new SeriesPattern();
        if (null != originalPattern) {
            pattern.setDayOfMonth(originalPattern.getDayOfMonth());
            pattern.setDaysOfWeek(originalPattern.getDaysOfWeek());
            pattern.setFullTime(originalPattern.isFullTime());
            pattern.setInterval(originalPattern.getInterval());
            pattern.setMonth(originalPattern.getMonth());
            pattern.setOccurrences(originalPattern.getOccurrences());
            pattern.setSeriesEnd(originalPattern.getSeriesEnd());
            pattern.setSeriesStart(originalPattern.getSeriesStart());
            pattern.setType(originalPattern.getType());
            pattern.setTz(originalPattern.getTimeZone());
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
                if (0 < attendee.getEntity()) {
                    UserParticipant userParticipant = new UserParticipant(attendee.getEntity());
                    userParticipant.setConfirm(Event2Appointment.getConfirm(attendee.getPartStat()));
                    userParticipant.setConfirmMessage(attendee.getComment());
                    userParticipant.setDisplayName(attendee.getCn());
                    userParticipant.setEmailAddress(Event2Appointment.getEMailAddress(attendee.getUri()));
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
     * Loads an event and extract the event's series pattern.
     *
     * @param session The calendar session
     * @param eventID The identifier of the event to get the pattern for
     * @return The series pattern, or <code>null</code> if not set
     */
    private SeriesPattern loadSeriesPattern(CalendarSession session, EventID eventID) throws OXException {
        EventField [] recurrenceFields = {
            EventField.RECURRENCE_RULE, EventField.ALL_DAY, EventField.START_DATE, EventField.START_TIMEZONE,
            EventField.END_DATE, EventField.END_TIMEZONE
        };
        Event event = getEvent(session, eventID, recurrenceFields);
        return Event2Appointment.getSeriesPattern(event.getRecurrenceRule(), event.getStartDate(), event.getStartTimezone(), event.isAllDay());
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
        try {
            session.set(CalendarParameters.PARAMETER_FIELDS, fields);
            UserizedEvent event = services.getService(CalendarService.class).getEvent(session, eventID.getFolderID(), eventID.getObjectID());
            return null != event ? event.getEvent() : null;
        } finally {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
        }
    }

}
