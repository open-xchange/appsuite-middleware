///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the OX Software GmbH group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2016-2020 OX Software GmbH
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.caldav;
//
//import static com.openexchange.chronos.common.CalendarUtils.getDateInTimeZone;
//import static com.openexchange.chronos.common.CalendarUtils.getTriggerDuration;
//import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
//import static com.openexchange.chronos.common.CalendarUtils.isFloating;
//import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//import java.util.TimeZone;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//import com.openexchange.caldav.resources.EventResource;
//import com.openexchange.chronos.Alarm;
//import com.openexchange.chronos.AlarmAction;
//import com.openexchange.chronos.Attendee;
//import com.openexchange.chronos.Event;
//import com.openexchange.chronos.Trigger;
//import com.openexchange.chronos.common.CalendarUtils;
//import com.openexchange.chronos.ical.AlarmComponent;
//import com.openexchange.chronos.ical.CalendarExport;
//import com.openexchange.chronos.ical.DefaultICalProperty;
//import com.openexchange.chronos.ical.EventComponent;
//import com.openexchange.chronos.ical.ICalParameters;
//import com.openexchange.chronos.ical.ICalProperty;
//import com.openexchange.chronos.ical.ICalService;
//import com.openexchange.chronos.service.CalendarSession;
//import com.openexchange.dav.DAVUserAgent;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.Types;
//import com.openexchange.groupware.reminder.ReminderExceptionCode;
//import com.openexchange.groupware.reminder.ReminderHandler;
//import com.openexchange.groupware.reminder.ReminderObject;
//import com.openexchange.java.Strings;
//
///**
// * {@link CalDAVExport}
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// * @since v7.10.0
// */
//public class CalDAVExport {
//
//    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalDAVExport.class);
//
//    private final EventResource resource;
//    private final Event event;
//
//    /**
//     * Initializes a new {@link CalDAVExport}.
//     *
//     * @param resource The event resource
//     * @param event The event to export
//     */
//    public CalDAVExport(EventResource resource, Event event) {
//        super();
//        this.resource = resource;
//        this.event = event;
//    }
//
//    public InputStream export() throws OXException {
//        /*
//         * init export
//         */
//        ICalService iCalService = resource.getFactory().requireService(ICalService.class);
//        ICalParameters parameters = iCalService.initParameters();
//        CalendarExport calendarExport = iCalService.exportICal(parameters);
//        List<Event> changeExceptions = null;
//        if (PhantomMaster.class.isInstance(event)) {
//            /*
//             * no access to parent recurring master, use detached occurrences as exceptions
//             */
//            changeExceptions = ((PhantomMaster) event).getDetachedOccurrences();
//        } else {
//            /*
//             * add (master) event to export
//             */
//            calendarExport.add(prepareForExport(event));
//            if (CalendarUtils.isSeriesMaster(event) && null != event.getChangeExceptionDates()) {
//                CalendarSession calendarSession = resource.getCalendarSession();
//                changeExceptions = calendarSession.getCalendarService().getChangeExceptions(calendarSession, event.getFolderId(), event.getSeriesId());
//            }
//        }
//        /*
//         * add change exceptions to export
//         */
//        if (null != changeExceptions && 0 < changeExceptions.size()) {
//            for (Event changeException : changeExceptions) {
//                calendarExport.add(prepareForExport(changeException));
//            }
//        }
//        return calendarExport.getClosingStream();
//    }
//
//    private EventComponent prepareForExport(Event event) {
//        EventComponent eventComponent = new EventComponent(event, null);
//        /*
//         * apply outgoing patches
//         */
//        Patches.Outgoing.adjustProposedTimePrefixes(eventComponent);
//
//        applyReminderProperties(eventComponent);
//
//        /*
//         * apply additional properties
//         */
//        List<ICalProperty> extraProperties = getExtraProperties(eventComponent);
//        if (null != extraProperties && 0 < extraProperties.size()) {
//            eventComponent.setProperties(extraProperties);
//        }
//        return eventComponent;
//    }
//
//    private List<ICalProperty> getExtraProperties(Event event) {
//        List<ICalProperty> extraProperties = new ArrayList<ICalProperty>();
//        /*
//         * "caldav-privatecomments" for Apple clients
//         * https://github.com/apple/ccs-calendarserver/blob/master/doc/Extensions/caldav-privatecomments.txt
//         */
//        if (null != event.getAttendees() && (DAVUserAgent.IOS.equals(resource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent()))) {
//            if (CalendarUtils.isOrganizer(event, resource.getFactory().getUser().getId())) {
//                /*
//                 * provide all attendee comments for organizer
//                 */
//                for (Attendee attendee : event.getAttendees()) {
//                    if (Strings.isNotEmpty(attendee.getComment())) {
//                        Map<String, String> parameters = Collections.singletonMap("X-CALENDARSERVER-ATTENDEE-REF", attendee.getUri());
//                        extraProperties.add(new DefaultICalProperty("X-CALENDARSERVER-ATTENDEE-COMMENT", attendee.getComment(), parameters));
//                    }
//                }
//            } else {
//                /*
//                 * provide the current user's confirmation message
//                 */
//                Attendee attendee = CalendarUtils.find(event.getAttendees(), resource.getFactory().getUser().getId());
//                if (null != attendee && Strings.isNotEmpty(attendee.getComment())) {
//                    extraProperties.add(new DefaultICalProperty("X-CALENDARSERVER-PRIVATE-COMMENT", attendee.getComment(), Collections.<String, String> emptyMap()));
//                }
//            }
//        }
//
//        return extraProperties;
//    }
//
//    private void applyReminderProperties(EventComponent event) {
//        if (1 == 1)
//            return;
//
//        try {
//            if (null == event.getAlarms() || 1 != event.getAlarms().size()) {
//                /*
//                 * insert a dummy alarm to prevent Apple clients from adding their own default alarms
//                 */
//                if (DAVUserAgent.IOS.equals(resource.getUserAgent()) || DAVUserAgent.MAC_CALENDAR.equals(resource.getUserAgent())) {
//                    event.setAlarms(Collections.singletonList(getEmptyDefaultAlarm()));
//                }
//                return;
//            }
//
//            List<ICalProperty> extraAlarmProperties = new ArrayList<ICalProperty>();
//            List<ICalProperty> extraEventProperties = new ArrayList<ICalProperty>();
//
//            AlarmComponent alarm = new AlarmComponent(event.getAlarms().get(0), null);
//            if (null == alarm.getUid()) {
//                alarm.setUid(UUID.randomUUID().toString());
//            }
//            /*
//             * take over "acknowledged" also in "X-MOZ-LASTACK"
//             */
//            if (null != alarm.getAcknowledged()) {
//                extraAlarmProperties.add(new DefaultICalProperty("X-MOZ-LASTACK", formatAsUTC(alarm.getAcknowledged())));
//            }
//
//            //                /*
//            //                 * set last acknowledged date one minute prior next trigger time
//            //                 */
//            //                String timeZone = null != event.getStartTimeZone() ? event.getStartTimeZone() : resource.getFactory().getUser().getTimeZone();
//            //                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
//            //                calendar.setTime(reminder.getDate());
//            //                calendar.add(Calendar.MINUTE, -1);
//            //                extraProperties.add(new DefaultICalProperty("ACKNOWLEDGED", formatAsUTC(calendar.getTime()), null));
//            //                extraProperties.add(new DefaultICalProperty("X-MOZ-LASTACK", formatAsUTC(calendar.getTime()), null));
//
//            /*
//             * check if reminder is a 'snoozed' one (by checking against the next regular trigger time)
//             */
//
//
//            //                appointment.setProperty("com.openexchange.data.conversion.ical.alarm.acknowledged", calendar.getTime());
//            /*
//             * check if reminder is a 'snoozed' one (by checking against the next regular trigger time)
//             */
//            ReminderObject reminder = optReminder(event);
//            Date now = new Date();
//            Date rangeStart = now.after(reminder.getDate()) ? now : reminder.getDate();
//            Date nextTrigger = calculateNextTrigger(event, alarm, rangeStart, TimeZone.getTimeZone(resource.getFactory().getUser().getTimeZone()));
//            if (null != nextTrigger && nextTrigger.before(reminder.getDate())) {
//                /*
//                 * consider reminder as 'snoozed', apply corresponding client-specific data
//                 */
//                switch (resource.getUserAgent()) {
//                    case THUNDERBIRD_LIGHTNING:
//                        /*
//                         * Thunderbird/Lightning likes to have a custom "X-MOZ-SNOOZE-TIME-<timestamp_of_recurrence>" property for recurring
//                         * events, and a custom "X-MOZ-SNOOZE-TIME" property for non-recurring ones
//                         */
//                        if (isSeriesMaster(event) && null != alarm.getTrigger() && null != alarm.getTrigger().getDuration()) {
//                            Calendar calendar = initCalendar(TimeZone.getDefault(), nextTrigger);
//                            calendar.add(Calendar.SECOND, -1 * ((int) getTriggerDuration(alarm.getTrigger().getDuration()) / 1000));
//                            Date relatedDate = calendar.getTime();
//                            extraEventProperties.add(new DefaultICalProperty("X-MOZ-SNOOZE-TIME-" + String.valueOf(relatedDate.getTime()) + "000", formatAsUTC(nextTrigger)));
//                        } else {
//                            extraEventProperties.add(new DefaultICalProperty("X-MOZ-SNOOZE-TIME", formatAsUTC(nextTrigger)));
//                        }
//                        break;
//                    case IOS:
//                    case MAC_CALENDAR:
//                        /*
//                         * Apple clients prefer a relative snooze time duration in the trigger of appointment series
//                         */
//                        if (isSeriesMaster(event)) {
//                            Calendar calendar = initCalendar(TimeZone.getDefault(), nextTrigger);
//                            calendar.add(Calendar.SECOND, -1 * ((int) getTriggerDuration(alarm.getTrigger().getDuration()) / 1000));
//                            Date relatedDate = calendar.getTime();
//                            long diff = relatedDate.getTime() - reminder.getDate().getTime();
//                            if (diff >= 0) {
//
//                                Trigger snoozeTrigger = new Trigger();
//                                snoozeTrigger.setDuration("-PT" + TimeUnit.MILLISECONDS.toMinutes(diff) + "M");
//                                snoozeTrigger.setDateTime(nextTrigger);
//                                AlarmComponent snoozeAlarm = new AlarmComponent();
//                                snoozeAlarm.setTrigger(snoozeTrigger);
//                                snoozeAlarm.setAction(AlarmAction.DISPLAY);
//                                snoozeAlarm.setDescription(alarm.getDescription());
//                                snoozeAlarm.setRelatedTo(new com.openexchange.chronos.RelatedTo("SNOOZE", alarm.getUid()));
//                                List<Alarm> newAlarms = new ArrayList<Alarm>(event.getAlarms());
//                                newAlarms.add(snoozeAlarm);
//                                event.setAlarms(newAlarms);
//                                break;
//                            }
//                        }
//                        // fall through, otherwise
//                    default:
//                        /*
//                         * apply default snooze handling and insert additional VALARM for snoozed alarm, linked to default alarm via UID and RELTYPE=SNOOZE
//                         */
//                        Trigger snoozeTrigger = new Trigger();
//                        snoozeTrigger.setDateTime(nextTrigger);
//                        AlarmComponent snoozeAlarm = new AlarmComponent();
//                        snoozeAlarm.setTrigger(snoozeTrigger);
//                        snoozeAlarm.setAction(AlarmAction.DISPLAY);
//                        snoozeAlarm.setDescription(alarm.getDescription());
//                        snoozeAlarm.setRelatedTo(new com.openexchange.chronos.RelatedTo("SNOOZE", alarm.getUid()));
//                        List<Alarm> newAlarms = new ArrayList<Alarm>(event.getAlarms());
//                        newAlarms.add(snoozeAlarm);
//                        event.setAlarms(newAlarms);
//                        break;
//                }
//            }
//
//            if (0 < extraAlarmProperties.size()) {
//                alarm.setProperties(extraAlarmProperties);
//            }
//            if (0 < extraEventProperties.size()) {
//                List<ICalProperty> properties = event.getProperties();
//                if (null == properties) {
//                    event.setProperties(extraEventProperties);
//                } else {
//                    properties.addAll(extraEventProperties);
//                }
//            }
//
//        } catch (OXException e) {
//
//        }
//    }
//
//    /**
//     * Calculates the next time an alarm for an event is triggered after a specific start date.
//     *
//     * @param event The event the alarm is associated with
//     * @param alarm The alarm
//     * @param startDate The (exclusive) start date for the trigger date to consider
//     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
//     * @return The next trigger time, or <code>null</code> if there is none
//     */
//    private Date calculateNextTrigger(Event event, Alarm alarm, Date startDate, TimeZone timeZone) {
//        if (null == alarm || null == alarm.getTrigger()) {
//            return null;
//        }
//        Date acknowledged = alarm.getAcknowledged();
//        Date triggerDate = alarm.getTrigger().getDateTime();
//        if (null != triggerDate) {
//            return triggerDate.after(startDate) && (null == acknowledged || acknowledged.before(triggerDate)) ? triggerDate : null;
//        }
//        long triggerDuration = getTriggerDuration(alarm.getTrigger().getDuration());
//        if (isSeriesMaster(event)) {
//
//        } else {
//            Date relatedDate;
//            if (Trigger.Related.END.equals(alarm.getTrigger().getRelated())) {
//                relatedDate = isFloating(event) ? getDateInTimeZone(event.getEndDate(), timeZone) : event.getEndDate();
//            } else {
//                relatedDate = isFloating(event) ? getDateInTimeZone(event.getStartDate(), timeZone) : event.getStartDate();
//            }
//            Calendar calendar = initCalendar(timeZone, relatedDate);
//            calendar.add(Calendar.SECOND, -1 * ((int) triggerDuration / 1000));
//            triggerDate = calendar.getTime();
//            return triggerDate.after(startDate) && (null == acknowledged || acknowledged.before(triggerDate)) ? triggerDate : null;
//        }
//
//        return null;
//    }
//
//    private ReminderObject calculateNextReminder(Event event, Date startDate, Alarm alarm, ReminderObject existingReminder) throws OXException {
//        if (null == alarm) {
//            return null;
//        }
//        if (CalendarUtils.isSeriesMaster(event)) {
//            //            resource.getFactory().requireService(RecurrenceService.class).calculateInstancesRespectExceptions(master, start, end, limit, changeExceptions)
//            return null;
//        }
//
//        Trigger trigger = alarm.getTrigger();
//
//        return null;
//
//    }
//
//    /**
//     * Optionally gets the current user's reminder associated with the supplied event.
//     *
//     * @param event The event to get the reminder for
//     * @return The reminder, or <code>null</code> if there is none
//     */
//    private ReminderObject optReminder(Event event) throws OXException {
//        if (null != event) {
//            try {
//                return new ReminderHandler(resource.getFactory().getContext()).loadReminder(event.getId(), resource.getFactory().getUser().getId(), Types.APPOINTMENT);
//            } catch (OXException e) {
//                if (false == ReminderExceptionCode.NOT_FOUND.equals(e)) {
//                    throw e;
//                }
//            }
//        }
//        return null;
//    }
//
//    private static Alarm getEmptyDefaultAlarm() {
//        Trigger trigger = new Trigger();
//        trigger.setDateTime(new Date(197168145000L)); // 19760401T005545Z
//        AlarmComponent alarm = new AlarmComponent();
//        alarm.setTrigger(trigger);
//        alarm.setUid(UUID.randomUUID().toString().toUpperCase());
//        alarm.setAction(AlarmAction.NONE);
//        List<ICalProperty> extraProperties = new ArrayList<ICalProperty>();
//        extraProperties.add(new DefaultICalProperty("X-WR-ALARMUID", alarm.getUid()));
//        extraProperties.add(new DefaultICalProperty("X-APPLE-LOCAL-DEFAULT-ALARM", "TRUE"));
//        extraProperties.add(new DefaultICalProperty("X-APPLE-DEFAULT-ALARM", "TRUE"));
//        alarm.setProperties(extraProperties);
//        return alarm;
//    }
//
//    /**
//     * Converts a date into the textual iCal 'UTC' representation (with the <code>Z</code> prefix) appended to the time value.
//     *
//     * @param date The date to format
//     * @return The formatted date string
//     */
//    private static String formatAsUTC(Date date) {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        return dateFormat.format(date);
//    }
//
//}
