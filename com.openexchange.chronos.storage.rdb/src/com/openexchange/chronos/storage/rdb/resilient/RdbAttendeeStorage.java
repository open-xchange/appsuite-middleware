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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb.resilient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.CalendarStorageWarnings;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttendeeStorage extends CalendarStorageWarnings implements AttendeeStorage {

    private static final int MAX_RETRIES = 5;

    private final AttendeeStorage delegate;
    private final ServiceLookup services;
    private final boolean handleTruncations;
    private final boolean handleIncorrectStrings;

    /**
     * Initializes a new {@link RdbAttendeeStorage}.
     *
     * @param services A service lookup reference
     * @param delegate The delegate storage
     * @param handleTruncations <code>true</code> to automatically handle data truncation warnings, <code>false</code>, otherwise
     * @param handleIncorrectStrings <code>true</code> to automatically handle incorrect string warnings, <code>false</code>, otherwise
     */
    public RdbAttendeeStorage(ServiceLookup services, AttendeeStorage delegate, boolean handleTruncations, boolean handleIncorrectStrings) {
        super();
        this.services = services;
        this.delegate = delegate;
        this.handleIncorrectStrings = handleIncorrectStrings;
        this.handleTruncations = handleTruncations;
    }

    @Override
    public List<Attendee> loadAttendees(String eventId) throws OXException {
        return delegate.loadAttendees(eventId);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds) throws OXException {
        return delegate.loadAttendees(eventIds);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal) throws OXException {
        return delegate.loadAttendees(eventIds, internal);
    }

    @Override
    public Map<String, ParticipationStatus> loadPartStats(String[] eventIds, Attendee attendee) throws OXException {
        return delegate.loadPartStats(eventIds, attendee);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendeeTombstones(String[] eventIds) throws OXException {
        return delegate.loadAttendeeTombstones(eventIds);
    }

    @Override
    public void deleteAttendees(String eventId) throws OXException {
        delegate.deleteAttendees(eventId);
    }

    @Override
    public void deleteAttendees(List<String> eventIds) throws OXException {
        delegate.deleteAttendees(eventIds);
    }

    @Override
    public void deleteAttendees(String eventId, List<Attendee> attendees) throws OXException {
        delegate.deleteAttendees(eventId, attendees);
    }

    @Override
    public void deleteAllAttendees() throws OXException {
        delegate.deleteAllAttendees();
    }

    @Override
    public void insertAttendees(String eventId, List<Attendee> attendees) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(eventId, attendees, f)))
            .run(() -> delegate.insertAttendees(eventId, attendees))
        ;
    }

    @Override
    public void insertAttendees(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(attendeesByEventId, f)))
            .run(() -> delegate.insertAttendees(attendeesByEventId))
        ;
    }

    @Override
    public void updateAttendees(String eventId, List<Attendee> attendees) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(eventId, attendees, f)))
            .run(() -> delegate.updateAttendees(eventId, attendees))
        ;
    }

    @Override
    public void updateAttendee(String eventId, Attendee attendee) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(eventId, Collections.singletonList(attendee), f)))
            .run(() -> delegate.updateAttendee(eventId, attendee))
        ;
    }

    @Override
    public void insertAttendeeTombstone(String eventId, Attendee attendee) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(eventId, Collections.singletonList(attendee), f)))
            .run(() -> delegate.insertAttendeeTombstone(eventId, attendee))
        ;
    }

    @Override
    public void insertAttendeeTombstones(String eventId, List<Attendee> attendees) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(eventId, attendees, f)))
            .run(() -> delegate.insertAttendeeTombstones(eventId, attendees))
        ;
    }

    @Override
    public void insertAttendeeTombstones(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        Failsafe
            .with(new RetryPolicy().withMaxRetries(MAX_RETRIES).retryOn(f -> handle(attendeesByEventId, f)))
            .run(() -> delegate.insertAttendeeTombstones(attendeesByEventId))
        ;
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param attendeesByEventId The attendees being stored, mapped to the corresponding event identifier
     * @param failure The exception
     * @return <code>true</code> if the attendees data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(Map<String, List<Attendee>> attendeesByEventId, Throwable failure) {
        if (false == OXException.class.isInstance(failure)) {
            return false;
        }
        OXException e = (OXException) failure;
        try {
            switch (e.getErrorCode()) {
                case "CAL-5071": // Incorrect string [string %1$s, field %2$s, column %3$s]
                    return handleIncorrectStrings && handleIncorrectString(e, attendeesByEventId);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return handleTruncations && handleTruncation(e, attendeesByEventId);
                default:
                    return false;
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
            return false;
        }
    }

    /**
     * Tries to handle an exception that occurred during inserting data automatically.
     *
     * @param eventId The identifier of the event the attendees are stored for
     * @param attendees The attendees being stored
     * @param failure The exception
     * @return <code>true</code> if the attendees data was adjusted so that the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(String eventId, List<Attendee> attendees, Throwable failure) {
        return handle(Collections.singletonMap(eventId, attendees), failure);
    }

    private boolean handleIncorrectString(OXException e, Map<String, List<Attendee>> attendeesByEventId) {
        LOG.debug("Incorrect string detected while storing calendar data, replacing problematic characters and trying again.", e);
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null == calendarUtilities) {
            return false;
        }
        for (Map.Entry<String, List<Attendee>> entry : attendeesByEventId.entrySet()) {
            if (calendarUtilities.handleIncorrectString(e, entry.getValue())) {
                addWarning(entry.getKey(), e);
                return true;
            }
        }
        return false;
    }

    private boolean handleTruncation(OXException e, Map<String, List<Attendee>> attendeesByEventId) {
        LOG.debug("Data truncation detected while storing calendar data, trimming problematic fields and trying again.");
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null == calendarUtilities) {
            return false;
        }
        for (Map.Entry<String, List<Attendee>> entry : attendeesByEventId.entrySet()) {
            if (calendarUtilities.handleDataTruncation(e, entry.getValue())) {
                addWarning(entry.getKey(), e);
                return true;
            }
        }
        return false;
    }

}
