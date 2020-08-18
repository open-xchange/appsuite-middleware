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
