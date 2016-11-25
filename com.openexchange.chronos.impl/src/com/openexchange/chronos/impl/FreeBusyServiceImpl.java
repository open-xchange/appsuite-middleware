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

package com.openexchange.chronos.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.performer.ConflictCheckPerformer;
import com.openexchange.chronos.impl.performer.FreeBusyPerformer;
import com.openexchange.chronos.impl.performer.HasPerformer;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FreeBusyServiceImpl implements FreeBusyService {

    /**
     * Initializes a new {@link FreeBusyServiceImpl}.
     */
    public FreeBusyServiceImpl() {
        super();
    }

    @Override
    public boolean[] hasEventsBetween(final CalendarSession session, final Date from, final Date until) throws OXException {
        return new StorageOperation<boolean[]>(session) {

            @Override
            protected boolean[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new HasPerformer(session, storage).perform(session.getUser().getId(), from, until);
            }
        }.executeQuery();
    }

    @Override
    public Map<Attendee, List<Event>> getFreeBusy(CalendarSession session, final List<Attendee> attendees, final Date from, final Date until) throws OXException {
        return new StorageOperation<Map<Attendee, List<Event>>>(session) {

            @Override
            protected Map<Attendee, List<Event>> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new FreeBusyPerformer(session, storage).perform(attendees, from, until);
            }
        }.executeQuery();
    }

    @Override
    public List<EventConflict> checkForConflicts(CalendarSession session, final Event event, final List<Attendee> attendees) throws OXException {
        return new StorageOperation<List<EventConflict>>(session) {

            @Override
            protected List<EventConflict> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ConflictCheckPerformer(session, storage).perform(event, attendees);
            }
        }.executeQuery();
    }

}
