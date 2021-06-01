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

package com.openexchange.chronos.common;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;

/**
 * {@link SelfProtectionFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class SelfProtectionFactory {

    static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SelfProtectionFactory.class);

    public static final Property PROPERTY_EVENT_LIMIT = DefaultProperty.valueOf("com.openexchange.calendar.maxEventResults", I(1000));
    public static final Property PROPERTY_ATTENDEE_LIMIT = DefaultProperty.valueOf("com.openexchange.calendar.maxAttendeesPerEvent", I(1000));
    public static final Property PROPERTY_ALARM_LIMIT = DefaultProperty.valueOf("com.openexchange.calendar.maxAlarmsPerEvent", I(100));
    public static final Property PROPERTY_CONFERENCE_LIMIT = DefaultProperty.valueOf("com.openexchange.calendar.maxConferencesPerEvent", I(100));

    /**
     * Initializes a new {@link SelfProtectionFactory}.
     */
    private SelfProtectionFactory() {
        super();
    }

    public static SelfProtection createSelfProtection(LeanConfigurationService leanConfigurationService) {
        return new SelfProtection(leanConfigurationService);
    }

    public static class SelfProtection {

        private final int eventLimit;
        private final int attendeeLimit;
        private final int alarmLimit;
        private final int conferenceLimit;

        /**
         * Initializes a new {@link SelfProtectionFactory.SelfProtection}.
         * @param leanConfigurationService The {@link LeanConfigurationService}
         */
        public SelfProtection(LeanConfigurationService leanConfigurationService) {
            super();
            if (leanConfigurationService == null){
                // Log warning and use defaults
                LOG.warn("Missing LeanConfigurationService. Going to use default values for self protection.");
                eventLimit = 1000;
                attendeeLimit = 1000;
                alarmLimit = 100;
                conferenceLimit = 100;
                return;
            }

            eventLimit = leanConfigurationService.getIntProperty(PROPERTY_EVENT_LIMIT);
            attendeeLimit = leanConfigurationService.getIntProperty(PROPERTY_ATTENDEE_LIMIT);
            alarmLimit = leanConfigurationService.getIntProperty(PROPERTY_ALARM_LIMIT);
            conferenceLimit = leanConfigurationService.getIntProperty(PROPERTY_CONFERENCE_LIMIT);
        }

        /**
         * Checks if a {@link Collection} of events contains too many events
         *
         * @param collection A collections of {@link Event} objects or similar (e.g. EventIds)
         * @throws OXException if the collection contains too many {@link Event}s
         */
        public void checkEventCollection(Collection<?> collection) throws OXException {
            if (collection.size() > eventLimit) {
                throw CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create();
            }
        }

        /**
         * Checks if a {@link Collection} of events contains too many events
         *
         * @param collection A collections of {@link Event} objects or similar (e.g. EventIds)
         * @param requestedFields The requested fields, or <code>null</code> if all event fields were requested
         * @throws OXException if the collection contains too many {@link Event}s
         */
        public void checkEventCollection(Collection<?> collection, EventField[] requestedFields) throws OXException {
            if (null != collection && collection.size() > eventLimit && false == CalendarUtils.containsOnlyIdentifyingFields(requestedFields)) {
                throw CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create();
            }
        }

        /**
         * Checks if a map of event results contains too many events.
         *
         * @param eventsResults A map of events results
         * @param requestedFields The requested fields, or <code>null</code> if all event fields were requested
         * @throws OXException if the collections contain too many {@link Event}s
         */
        public void checkEventResults(Map<?, ? extends EventsResult> eventsResults, EventField[] requestedFields) throws OXException {
            if (countEventsResults(eventsResults) > eventLimit && false == CalendarUtils.containsOnlyIdentifyingFields(requestedFields)) {
                throw CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create();
            }
        }

        /**
         * Checks if an {@link Event} contains too many {@link Alarm}s, {@link Conference}s, or too many {@link Attendee}s
         *
         * @param event The event to check
         * @throws OXException if the event contains too many {@link Attendee}s, {@link Conference}s or {@link Alarm}s
         */
        public void checkEvent(Event event) throws OXException {
            if (event.getAlarms() != null && event.getAlarms().size() > alarmLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ALARMS.create();
            }
            if (event.getAttendees() != null && event.getAttendees().size() > attendeeLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ATTENDEES.create();
            }
            checkConferenceCollection(event.getConferences());
        }

        /**
         * Checks if a {@link Collection} of {@link Attendee}s contains too many elements.
         *
         * @param attendees The {@link Collection} to check
         * @throws OXException if the {@link Collection} contains too many {@link Attendee}s
         */
        public void checkAttendeeCollection(Collection<Attendee> attendees) throws OXException {
            if (attendees != null && attendees.size() > attendeeLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ATTENDEES.create();
            }
        }

        /**
         * Checks if a collection of conferences of contains too many elements.
         *
         * @param attendees The conferences to check
         * @throws OXException - {@link CalendarExceptionCodes#TOO_MANY_CONFERENCES}
         */
        public void checkConferenceCollection(Collection<Conference> conferences) throws OXException {
            if (null != conferences && conferenceLimit < conferences.size()) {
                throw CalendarExceptionCodes.TOO_MANY_CONFERENCES.create(I(conferenceLimit), I(conferences.size()));
            }
        }

        public void checkMap(Map<?, ? extends Collection<Event>> map) throws OXException {
            int sum = 0;
            for (Collection<Event> collection : map.values()) {
                if (collection == null) {
                    continue;
                }
                sum += collection.size();
            }

            if (sum > eventLimit) {
                throw CalendarExceptionCodes.TOO_MANY_EVENT_RESULTS.create();
            }
        }

    }

    static int countEventsResults(Map<?, ? extends EventsResult> eventsResults) {
        int count = 0;
        if (null != eventsResults) {
            for (EventsResult value : eventsResults.values()) {
                if (null != value && null != value.getEvents()) {
                    count += value.getEvents().size();
                }
            }
        }
        return count;
    }

}
