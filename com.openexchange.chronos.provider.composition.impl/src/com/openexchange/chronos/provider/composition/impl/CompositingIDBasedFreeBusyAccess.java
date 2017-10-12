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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.chronos.provider.composition.impl.idmangling.IDMangling.withUniqueIDs;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.common.FreeBusyUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.common.SelfProtectionFactory.SelfProtection;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.FreeBusyAwareCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedFreeBusyAccess;
import com.openexchange.chronos.provider.composition.impl.idmangling.IDManglingEventConflict;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedFreeBusyAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CompositingIDBasedFreeBusyAccess extends AbstractCompositingIDBasedCalendarAccess implements IDBasedFreeBusyAccess {

    private SelfProtection protection;

    /**
     * Initializes a new {@link CompositingIDBasedFreeBusyAccess}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     */
    public CompositingIDBasedFreeBusyAccess(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super(session, providerRegistry, services);
    }

    private SelfProtection getSelfProtection() throws OXException {
        if (protection == null) {
            LeanConfigurationService leanConfigurationService = services.getService(LeanConfigurationService.class);
            protection = SelfProtectionFactory.createSelfProtection(this.session, leanConfigurationService);
        }
        return protection;
    }

    @Override
    public boolean[] hasEventsBetween(Date from, Date until) throws OXException {

        boolean[] result = null;
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                boolean[] hasEventsBetween = ((FreeBusyAwareCalendarAccess) access).hasEventsBetween(from, until);
                if (result == null) {
                    result = hasEventsBetween;
                } else {
                    mergeEventsBetween(result, hasEventsBetween);
                }
            }
        }

        return result;
    }

    private void mergeEventsBetween(boolean[] result, boolean[] newValues) throws OXException {
        if (result.length != newValues.length) {
            // Should never occur
            throw new OXException(new InvalidParameterException("The two boolean arrays must not have different sizes!"));
        }

        for (int x = 0; x < result.length; x++) {
            result[x] |= newValues[x];
        }
    }

    @Override
    public Map<Attendee, List<Event>> getFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<Event>> result = null;

        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                Map<Attendee, List<Event>> eventsPerAttendee = ((FreeBusyAwareCalendarAccess) access).getFreeBusy(attendees, from, until);
                if (result == null) {
                    result = eventsPerAttendee;
                    for (Attendee att : result.keySet()) {
                        result.put(att, withUniqueIDs(result.get(att), account.getAccountId()));
                        getSelfProtection().checkEventCollection(result.get(att));
                    }
                } else {
                    for (Attendee att : eventsPerAttendee.keySet()) {
                        result.get(att).addAll(withUniqueIDs(eventsPerAttendee.get(att), account.getAccountId()));
                        getSelfProtection().checkEventCollection(result.get(att));
                    }
                }
                getSelfProtection().checkMap(eventsPerAttendee);
            }
        }

        //TODO properly sort events or remove sorting?

        return result;
    }

    @Override
    public Map<Attendee, List<FreeBusyTime>> getMergedFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, List<FreeBusyTime>> result = null;
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                Map<Attendee, List<FreeBusyTime>> freeBusyTimesPerAttendee = ((FreeBusyAwareCalendarAccess) access).getMergedFreeBusy(attendees, from, until);
                if (result == null) {
                    result = freeBusyTimesPerAttendee;
                } else {
                    for (Attendee att : freeBusyTimesPerAttendee.keySet()) {
                        result.get(att).addAll(freeBusyTimesPerAttendee.get(att));
                    }
                }
            }
        }

        if (result == null) {
            return new HashMap<>();
        }

        // Merge results
        for (Attendee att : result.keySet()) {
            List<FreeBusyTime> freeBusyTimes = result.get(att);
            result.put(att, FreeBusyUtils.mergeFreeBusy(freeBusyTimes));
        }

        return result;
    }

    @Override
    public Map<Attendee, List<FreeBusyTime>> calculateFreeBusyTime(List<Attendee> attendees, Date from, Date until) throws OXException {
        Map<Attendee, FreeBusyResult> temp = new HashMap<>(0);
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                Map<Attendee, FreeBusyResult> freeBusyTimesPerAttendee = ((FreeBusyAwareCalendarAccess) access).calculateFreeBusyTime(attendees, from, until);
                if (temp == null) {
                    temp = freeBusyTimesPerAttendee;
                } else {
                    for (Attendee att : freeBusyTimesPerAttendee.keySet()) {
                        List<FreeBusyTime> freeBusyTimes = freeBusyTimesPerAttendee.get(att).getFreeBusyTimes();
                        FreeBusyResult freeBusyResult = temp.get(att);
                        if (freeBusyResult == null) {
                            freeBusyResult = new FreeBusyResult();
                            freeBusyResult.setFreeBusyTimes(freeBusyTimes);
                            temp.put(att, freeBusyResult);
                        } else {
                            freeBusyResult.getFreeBusyTimes().addAll(freeBusyTimes);
                        }
                    }
                }
            }
        }

        // Merge results
        Map<Attendee, List<FreeBusyTime>> result = new HashMap<>();
        for (Attendee att : temp.keySet()) {
            FreeBusyResult freeBusyResult = temp.get(att);
            List<FreeBusyTime> freeBusyTimes = freeBusyResult.getFreeBusyTimes();
            result.put(att, FreeBusyUtils.mergeFreeBusy(freeBusyTimes));
        }

        return result;
    }

    @Override
    public List<EventConflict> checkForConflicts(Event event, List<Attendee> attendees) throws OXException {
        List<EventConflict> result = null;
        for (CalendarAccount account : getAccounts()) {
            CalendarAccess access = getAccess(account);
            if (access instanceof FreeBusyAwareCalendarAccess) {
                List<EventConflict> eventConflicts = ((FreeBusyAwareCalendarAccess) access).checkForConflicts(event, attendees);
                if (result == null) {
                    result = new ArrayList<>(eventConflicts.size());
                }
                for (EventConflict conflict : eventConflicts) {
                    result.add(new IDManglingEventConflict(conflict, account.getAccountId()));
                }
            }
            getSelfProtection().checkEventCollection(result);
        }

        //TODO sort results?

        return result;
    }

}
