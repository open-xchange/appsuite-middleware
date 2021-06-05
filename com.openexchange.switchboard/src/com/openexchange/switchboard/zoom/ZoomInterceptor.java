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

package com.openexchange.switchboard.zoom;

import static com.openexchange.chronos.common.CalendarUtils.calculateEnd;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.initRecurrenceRule;
import static com.openexchange.chronos.common.CalendarUtils.isAllDay;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.switchboard.zoom.Utils.getZoomConferences;
import static com.openexchange.switchboard.zoom.Utils.matches;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.service.CalendarInterceptor;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.switchboard.SwitchboardProperties;
import com.openexchange.switchboard.exception.ZoomExceptionCodes;

/**
 * {@link ZoomInterceptor}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class ZoomInterceptor implements CalendarInterceptor {

    private static final Set<EventField> RELEVANT_FIELDS = com.openexchange.tools.arrays.Collections.unmodifiableSet(EventField.CONFERENCES, EventField.START_DATE, EventField.END_DATE);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ZoomInterceptor}.
     *
     * @param services A service lookup reference
     */
    public ZoomInterceptor(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Set<EventField> getRelevantFields() {
        return RELEVANT_FIELDS;
    }

    @Override
    public void onBeforeUpdate(CalendarSession session, String folderId, Event originalEvent, Event updatedEvent) throws OXException {
        if (false == isEnabled(session) || hasExternalOrganizer(updatedEvent)) {
            return;
        }
        /*
         * check general integrity of updated event if applicable
         */
        List<Conference> updatedConferences = getZoomConferences(updatedEvent);
        if (updatedConferences.isEmpty() || EventMapper.getInstance().equalsByFields(originalEvent, updatedEvent, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE)) {
            return;
        }
        checkIntegrity(session, updatedEvent, updatedConferences);
        /*
         * perform further checks based on conferences in original event
         */
        if (isSeriesMaster(originalEvent) != isSeriesMaster(updatedEvent)) {
            /*
             * series <-> single event, ensure that all original conferences are not re-used
             */
            for (Conference originalConference : getZoomConferences(originalEvent)) {
                if (updatedConferences.stream().anyMatch(c -> matches(c, originalConference))) {
                    throw ZoomExceptionCodes.NO_SWITCH_TO_OR_FROM_SERIES.create(originalEvent.getId());
                }
            }
        }
    }

    @Override
    public void onBeforeCreate(CalendarSession session, String folderId, Event newEvent) throws OXException {
        if (false == isEnabled(session) || hasExternalOrganizer(newEvent)) {
            return;
        }
        /*
         * check integrity if applicable
         */
        List<Conference> zoomConferences = getZoomConferences(newEvent);
        if (zoomConferences.isEmpty()) {
            return;
        }
        checkIntegrity(session, newEvent, zoomConferences);
    }

    @Override
    public void onBeforeDelete(CalendarSession session, String folderId, Event deletedEvent) throws OXException {
        // nothing to do
    }

    private boolean isEnabled(CalendarSession session) throws OXException {
        return services.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(session.getUserId(), session.getContextId(), SwitchboardProperties.enableZoomInterceptor);
    }

    private static void checkIntegrity(CalendarSession session, Event event, List<Conference> zoomConferences) throws OXException {
        if (null == zoomConferences || zoomConferences.isEmpty()) {
            return;
        }
        /*
         * no zoom meetings with floating dates
         */
        if (isAllDay(event)) {
            throw ZoomExceptionCodes.NO_ALL_DAY_APPOINTMENTS.create(event.getId());
        } else if (isFloating(event)) {
            throw ZoomExceptionCodes.NO_FLOATING_APPOINTMENTS.create(event.getId());
        }
        /*
         * no recurring zoom meeting that spans over more than a year
         */
        if (isSeriesMaster(event)) {
            DefaultRecurrenceData recurrenceData = new DefaultRecurrenceData(event);
            RecurrenceRule rule = initRecurrenceRule(recurrenceData.getRecurrenceRule());
            if (null == rule.getCount() && null == rule.getUntil()) {
                throw ZoomExceptionCodes.NO_SERIES_LONGER_THAN_A_YEAR.create(event.getId(), String.valueOf(recurrenceData));
            }
            DateTime rangeStart = event.getStartDate();
            RecurrenceIterator<RecurrenceId> iterator = session.getRecurrenceService().iterateRecurrenceIds(recurrenceData);
            long maxDuration = TimeUnit.DAYS.toMillis(365L);
            DateTime rangeEnd;
            while (iterator.hasNext()) {
                rangeEnd = calculateEnd(event, iterator.next());
                if (maxDuration < rangeEnd.getTimestamp() - rangeStart.getTimestamp()) {
                    throw ZoomExceptionCodes.NO_SERIES_LONGER_THAN_A_YEAR.create(event.getId(), String.valueOf(recurrenceData));
                }
            }
        }
    }

}
