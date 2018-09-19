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

package com.openexchange.chronos.common;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Map;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
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

    private static final String PROPERTY_EVENT_LIMIT = "com.openexchange.calendar.maxEventResults";
    private static final String PROPERTY_ATTENDEE_LIMIT = "com.openexchange.calendar.maxAttendeesPerEvent";
    private static final String PROPERTY_ALARM_LIMIT = "com.openexchange.calendar.maxAlarmsPerEvent";

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

        /**
         * Initializes a new {@link SelfProtectionFactory.SelfProtection}.
         * @param leanConfigurationService The {@link LeanConfigurationService}
         */
        public SelfProtection(LeanConfigurationService leanConfigurationService) {
            super();
            if(leanConfigurationService == null){
                // Log warning and use defaults
                LOG.warn("Missing LeanConfigurationService. Going to use default values for self protection.");
                eventLimit = 1000;
                attendeeLimit = 1000;
                alarmLimit = 100;
                return;
            }

            Property prop = DefaultProperty.valueOf(PROPERTY_EVENT_LIMIT, I(1000));
            eventLimit = leanConfigurationService.getIntProperty(prop);

            prop = DefaultProperty.valueOf(PROPERTY_ATTENDEE_LIMIT, I(1000));
            attendeeLimit = leanConfigurationService.getIntProperty(prop);

            prop = DefaultProperty.valueOf(PROPERTY_ALARM_LIMIT, I(100));
            alarmLimit = leanConfigurationService.getIntProperty(prop);
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
         * Checks if an {@link Event} contains too many {@link Alarm}s or too many {@link Attendee}s
         *
         * @param event The event to check
         * @throws OXException if the event contains too many {@link Attendee}s or {@link Alarm}s
         */
        public void checkEvent(Event event) throws OXException {
            if (event.getAlarms() != null && event.getAlarms().size() > alarmLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ALARMS.create();
            }
            if (event.getAttendees() != null && event.getAttendees().size() > attendeeLimit) {
                throw CalendarExceptionCodes.TOO_MANY_ATTENDEES.create();
            }
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
