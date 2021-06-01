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

package com.openexchange.chronos.provider.google.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import org.dmfs.rfc5545.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.services.calendar.model.Event.Creator;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.Trigger.Related;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;

/**
 * {@link GoogleEventConverter}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleEventConverter {

    private static final GoogleEventConverter INSTANCE = new GoogleEventConverter();
    static final Logger LOG = LoggerFactory.getLogger(GoogleEventConverter.class);

    private Map<EventField, GoogleMapping> mappings = null;

    public static GoogleEventConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link GoogleEventConverter}.
     */
    public GoogleEventConverter() {
        super();
        mappings = createMappings();
    }

    public Event convertToEvent(com.google.api.services.calendar.model.Event from, EventField... fields) throws OXException {
        Event to = new Event();
        if (fields == null || fields.length == 0) {
            fields = EventField.values();
        }
        for (EventField field : fields) {
            GoogleMapping mapping = mappings.get(field);
            if (mapping != null) {
                mapping.serialize(to, from);
            }
        }
        return to;
    }

    public GoogleMapping getMapping(EventField field) {
        return mappings.get(field);
    }

    private Map<EventField, GoogleMapping> createMappings() {
        Map<EventField, GoogleMapping> result = new HashMap<>();

        result.put(EventField.ID, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                to.setFilename(from.getId());
            }

        });
        result.put(EventField.START_DATE, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getStart() != null) {
                    to.setStartDate(convert(from.getStart()));
                }
            }

        });
        result.put(EventField.END_DATE, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getEnd() != null) {
                    to.setEndDate(convert(from.getEnd()));
                }
            }

        });

        result.put(EventField.ALARMS, new GoogleItemMapping<EventReminder, Alarm>() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                Reminders reminders = from.getReminders();
                if (reminders != null) {
                    List<Alarm> alarms = new ArrayList<>();
                    if (reminders.getUseDefault() != null && reminders.getUseDefault().booleanValue()) {
                        return;
                    }
                    if (reminders.getOverrides() != null) {
                        for (EventReminder rem : reminders.getOverrides()) {
                            alarms.add(convert(rem));
                        }
                        to.setAlarms(alarms);
                    }
                }
            }

            @Override
            public Alarm convert(EventReminder rem) {
                Trigger trigger = new Trigger();
                trigger.setRelated(Related.START);
                trigger.setDuration("-PT" + rem.getMinutes() + "M");
                AlarmAction action;
                switch (rem.getMethod()) {
                    case "email":
                        action = AlarmAction.EMAIL;
                        break;
                    case "popup":
                        action = AlarmAction.DISPLAY;
                        break;
                    default:
                        action = new AlarmAction(rem.getMethod());
                }

                Alarm result = new Alarm(trigger, action);
                return result;
            }

        });

        result.put(EventField.ATTENDEES, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {

                List<Attendee> attendees = new ArrayList<>();
                if (from.getAttendees() != null) {
                    for (EventAttendee att : from.getAttendees()) {
                        attendees.add(convert(att));
                    }
                }
                to.setAttendees(attendees);
            }

            public Attendee convert(EventAttendee from) {
                Attendee result = new Attendee();
                convert(from, result);
                if (from.getResource() != null && from.getResource().booleanValue()) {
                    result.setCuType(CalendarUserType.RESOURCE);
                } else {
                    result.setCuType(CalendarUserType.INDIVIDUAL);
                }
                result.setComment(from.getComment());
                switch (from.getResponseStatus()) {
                    case "needsAction":
                        result.setPartStat(ParticipationStatus.NEEDS_ACTION);
                        break;
                    case "declined":
                        result.setPartStat(ParticipationStatus.DECLINED);
                        break;
                    case "tentative":
                        result.setPartStat(ParticipationStatus.TENTATIVE);
                        break;
                    case "accepted":
                        result.setPartStat(ParticipationStatus.ACCEPTED);
                        break;
                }
                return result;
            }

        });
        result.put(EventField.CALENDAR_USER, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getAttendees() != null) {
                    for (EventAttendee att : from.getAttendees()) {
                        if (att.getSelf() != null && att.getSelf().booleanValue()) {
                            to.setCalendarUser(convert(att, new CalendarUser()));
                        }
                    }
                }
            }
        });
        result.put(EventField.ORGANIZER, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getAttendees() != null) {
                    for (EventAttendee att : from.getAttendees()) {
                        if (att.getOrganizer() != null && att.getOrganizer().booleanValue()) {
                            to.setOrganizer(convert(att));
                        }
                    }
                }
            }

            private Organizer convert(EventAttendee att) {
                Organizer org = new Organizer();
                convert(att, org);
                return org;
            }
        });

        result.put(EventField.CATEGORIES, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.CLASSIFICATION, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.COLOR, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getColorId() != null) {
                    to.setColor(from.getColorId());
                }
            }
        });

        result.put(EventField.CREATED, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getCreated() != null) {
                    to.setCreated(new Date(from.getCreated().getValue()));
                }
            }
        });

        result.put(EventField.CREATED_BY, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                Creator creator = from.getCreator();
                if (creator != null) {
                    CalendarUser result = new CalendarUser();
                    result.setCn(creator.getDisplayName());
                    result.setEMail(creator.getEmail());
                    to.setCreatedBy(result);
                }
            }
        });

        result.put(EventField.DELETE_EXCEPTION_DATES, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.DESCRIPTION, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getDescription() != null) {
                    to.setDescription(from.getDescription());
                }
            }
        });

        result.put(EventField.EXTENDED_PROPERTIES, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                com.google.api.services.calendar.model.Event.ExtendedProperties extendedProperties = from.getExtendedProperties();
                if (extendedProperties != null) {
                    to.setExtendedProperties(convert(extendedProperties));
                }

            }

            private ExtendedProperties convert(com.google.api.services.calendar.model.Event.ExtendedProperties from) {
                ExtendedProperties result = new ExtendedProperties();
                if (from.getPrivate() != null) {
                    for (String key : from.getPrivate().keySet()) {
                        ExtendedProperty prop = new ExtendedProperty(key, from.getPrivate().get(key));
                        result.add(prop);
                    }
                }
                if (from.getShared() != null) {
                    for (String key : from.getShared().keySet()) {
                        ExtendedProperty prop = new ExtendedProperty(key, from.getShared().get(key));
                        result.add(prop);
                    }
                }
                return result;
            }
        });

        result.put(EventField.FILENAME, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.FOLDER_ID, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.GEO, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.LOCATION, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getLocation() != null) {
                    to.setLocation(from.getLocation());
                }
            }
        });

        result.put(EventField.LAST_MODIFIED, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getUpdated() != null) {
                    to.setLastModified(new Date(from.getUpdated().getValue()));
                }
            }
        });

        result.put(EventField.MODIFIED_BY, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                // There is no equivalent
            }
        });

        result.put(EventField.RECURRENCE_ID, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getRecurringEventId() != null) {
                    try {
                        if (from.getOriginalStartTime() != null) {
                            com.google.api.client.util.DateTime rfc3339 = from.getOriginalStartTime().getDate() == null ? from.getOriginalStartTime().getDateTime() : from.getOriginalStartTime().getDate();
                            RecurrenceId recId = new DefaultRecurrenceId(new DateTime(rfc3339.getValue()));
                            to.setRecurrenceId(recId);
                        } else {
                            String dateTimeStr = from.getId().substring(from.getRecurringEventId().length() + 1);
                            RecurrenceId recId = new DefaultRecurrenceId(dateTimeStr);
                            to.setRecurrenceId(recId);
                        }
                    } catch (RuntimeException e) {
                        LOG.warn("Error deriving recurrence identifier from Id={}, RecurringEventId={}", from.getId(), from.getRecurringEventId(), e);
                        throw e;
                    }
                } else if (from.getId().indexOf('_') > 0) {
                    /*
                     * Additional check in case recurringEventId isn't set (null)
                     * This check expects that google ids of occurences to be in the following format: [masterId]_[recurrenceid]
                     * E.g.: 4qebqgd7o0nrqdlnqhberc4d3l_20171025T173000Z
                     */
                    String dateTimeStr = from.getId().substring(from.getId().indexOf('_') + 1);
                    try {
                        RecurrenceId recId = new DefaultRecurrenceId(dateTimeStr);
                        to.setRecurrenceId(recId);
                    } catch (IllegalArgumentException e) {
                        LOG.debug("Enexpected id format: '{}'. Unable to parse recurrence id.", dateTimeStr);
                    }
                }
            }
        });

        result.put(EventField.RECURRENCE_RULE, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getRecurrence() != null) {
                    for (String recurrence : from.getRecurrence()) {
                        if (recurrence.startsWith("RRULE:")) {
                            to.setRecurrenceRule(recurrence.substring("RRULE:".length()));
                            continue;
                        }
                        if (recurrence.startsWith("EXDATE:")) {
                            SortedSet<RecurrenceId> deleteExceptionDates = to.getDeleteExceptionDates();
                            if (deleteExceptionDates == null) {
                                deleteExceptionDates = new TreeSet<>();
                                to.setDeleteExceptionDates(deleteExceptionDates);
                            }
                            deleteExceptionDates.add(new DefaultRecurrenceId(recurrence.substring("EXDATE:".length())));
                            continue;
                        }
                    }
                }
            }
        });

        result.put(EventField.SEQUENCE, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getSequence() != null) {
                    to.setSequence(Autoboxing.i(from.getSequence()));
                }
            }
        });

        result.put(EventField.SERIES_ID, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getRecurringEventId() != null) {
                    // Add google id to identify delete exceptions
                    to.setSeriesId(from.getRecurringEventId());
                }
            }
        });

        result.put(EventField.STATUS, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getStatus() != null) {
                    to.setStatus(new EventStatus(from.getStatus().toUpperCase()));
                }
            }
        });

        result.put(EventField.SUMMARY, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getSummary() != null) {
                    to.setSummary(from.getSummary());
                }
            }
        });

        result.put(EventField.TIMESTAMP, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getCreated() != null) {
                    to.setTimestamp(from.getCreated().getValue());
                }
            }
        });

        result.put(EventField.TRANSP, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getTransparency() != null) {
                    to.setTransp(TimeTransparency.valueOf(from.getTransparency().toUpperCase()));
                }
            }
        });

        result.put(EventField.UID, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getICalUID() != null) {
                    to.setUid(from.getICalUID());
                }
            }
        });

        result.put(EventField.URL, new GoogleMapping() {

            @Override
            public void serialize(Event to, com.google.api.services.calendar.model.Event from) {
                if (from.getHtmlLink() != null) {
                    to.setUrl(from.getHtmlLink());
                }
            }
        });

        return result;
    }

    public abstract class GoogleItemMapping<T, S> extends GoogleMapping {

        /**
         * Initializes a new {@link GoogleItemMapping}.
         */
        public GoogleItemMapping() {
            super();
        }

        public abstract S convert(T item);

    }

    abstract class GoogleMapping {

        /**
         * Initializes a new {@link GoogleMapping}.
         */
        public GoogleMapping() {
            super();
        }

        public abstract void serialize(Event to, com.google.api.services.calendar.model.Event from) throws OXException;

        public CalendarUser convert(EventAttendee from, CalendarUser to) {
            to.setEMail(from.getEmail());
            to.setCn(from.getDisplayName());
            to.setUri(CalendarUtils.getURI(from.getEmail()));
            return to;
        }

        public DateTime convert(EventDateTime from) {
            if (from.getDateTime() == null) {
                String timeZoneStr = from.getTimeZone();
                TimeZone tz = Strings.isEmpty(timeZoneStr) ? TimeZone.getDefault() : TimeZone.getTimeZone(timeZoneStr);
                java.util.Calendar cal = java.util.Calendar.getInstance(tz);
                cal.setTimeInMillis(from.getDate().getValue());
                return new DateTime(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
            }
            return new DateTime(from.getDateTime().getValue());
        }

    }

}
