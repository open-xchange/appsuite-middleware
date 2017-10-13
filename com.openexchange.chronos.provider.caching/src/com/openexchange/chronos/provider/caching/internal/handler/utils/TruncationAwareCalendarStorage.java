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

package com.openexchange.chronos.provider.caching.internal.handler.utils;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.AttachmentStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageUtilities;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TruncationAwareCalendarStorage}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class TruncationAwareCalendarStorage implements CalendarStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TruncationAwareCalendarStorage.class);

    private static final int MAX_RETRIES = 3;

    private final CalendarStorage calendarStorage;
    private final Session session;

    public TruncationAwareCalendarStorage(CalendarStorage calendarStorage, Session session) {
        this.calendarStorage = calendarStorage;
        this.session = session;
    }

    public void updateAttendees(String eventId, List<Attendee> attendees) throws OXException {
        int retryCount = 0;
        do {
            try {
                calendarStorage.getAttendeeStorage().updateAttendees(eventId, attendees);
                return;
            } catch (OXException e) {
                if (++retryCount < MAX_RETRIES) {
                    Boolean handled = handle(attendees, e);
                    if (Boolean.TRUE.equals(handled)) {
                        continue;
                    } else if (Boolean.FALSE.equals(handled)) {
                        return;
                    }
                }
                throw e;
            }
        } while (true);
    }

    public void updateEvent(Event event) throws OXException {
        int retryCount = 0;
        do {
            try {
                calendarStorage.getEventStorage().updateEvent(event);
                return;
            } catch (OXException e) {
                if (++retryCount < MAX_RETRIES) {
                    Boolean handled = handle(event, e);
                    if (Boolean.TRUE.equals(handled)) {
                        continue;
                    } else if (Boolean.FALSE.equals(handled)) {
                        return;
                    }
                }
                throw e;
            }
        } while (true);
    }

    public void insertEvent(Event event) throws OXException {
        int retryCount = 0;
        do {
            try {
                calendarStorage.getEventStorage().insertEvent(event);
                return;
            } catch (OXException e) {
                if (++retryCount < MAX_RETRIES) {
                    Boolean handled = handle(event, e);
                    if (Boolean.TRUE.equals(handled)) {
                        continue;
                    } else if (Boolean.FALSE.equals(handled)) {
                        return;
                    }
                }
                throw e;
            }
        } while (true);
    }

    public void insertAttendees(String id, List<Attendee> attendees) throws OXException {
        int retryCount = 0;
        do {
            try {
                calendarStorage.getAttendeeStorage().insertAttendees(id, attendees);
                return;
            } catch (OXException e) {
                if (++retryCount < MAX_RETRIES) {
                    Boolean handled = handle(attendees, e);
                    if (Boolean.TRUE.equals(handled)) {
                        continue;
                    } else if (Boolean.FALSE.equals(handled)) {
                        return;
                    }
                }
                throw e;
            }
        } while (true);
    }

    private <T> Boolean handle(T obj, OXException e) {
        try {
            switch (e.getErrorCode()) {
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return handleDataTruncation(obj, e);
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
        }
        return null;
    }

    private <T> Boolean handleDataTruncation(T obj, OXException e) throws Exception {
        CalendarUtilities calendarUtilities = Services.getService(CalendarUtilities.class);
        /*
         * trim mapped data truncations, indicate "try again" if possible
         */
        LOG.info("Data truncation detected, trimming problematic fields and trying again.");
        boolean hasTrimmed = false;
        if (Event.class.isInstance(obj)) {
            hasTrimmed = calendarUtilities.handleDataTruncation(e, (Event) obj);
        } else if (List.class.isInstance(obj)) {
            hasTrimmed = calendarUtilities.handleDataTruncation(e, (List<Attendee>) obj);
        }
        return hasTrimmed ? Boolean.TRUE : null;
    }

    @Override
    public EventStorage getEventStorage() {
        return calendarStorage.getEventStorage();
    }

    @Override
    public AlarmStorage getAlarmStorage() {
        return calendarStorage.getAlarmStorage();
    }

    @Override
    public AttachmentStorage getAttachmentStorage() {
        return calendarStorage.getAttachmentStorage();
    }

    @Override
    public AttendeeStorage getAttendeeStorage() {
        return calendarStorage.getAttendeeStorage();
    }

    @Override
    public AlarmTriggerStorage getAlarmTriggerStorage() {
        return calendarStorage.getAlarmTriggerStorage();
    }

    @Override
    public CalendarStorageUtilities getUtilities() {
        return calendarStorage.getUtilities();
    }

    @Override
    public Map<String, List<OXException>> getWarnings() {
        return calendarStorage.getWarnings();
    }

    @Override
    public Map<String, List<OXException>> getAndFlushWarnings() {
        return calendarStorage.getAndFlushWarnings();
    }

    @Override
    public CalendarAccountStorage getAccountStorage() {
        return calendarStorage.getAccountStorage();
    }
}
