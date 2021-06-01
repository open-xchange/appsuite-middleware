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

package com.openexchange.chronos.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.impl.performer.AdministrativeFreeBusyPerformer;
import com.openexchange.chronos.impl.performer.ConflictCheckPerformer;
import com.openexchange.chronos.impl.performer.FreeBusyPerformer;
import com.openexchange.chronos.impl.performer.HasPerformer;
import com.openexchange.chronos.service.AdministrativeFreeBusyService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;

/**
 * {@link FreeBusyServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class FreeBusyServiceImpl implements FreeBusyService, AdministrativeFreeBusyService {

    /**
     * Initializes a new {@link FreeBusyServiceImpl}.
     */
    public FreeBusyServiceImpl() {
        super();
    }

    @Override
    public boolean[] hasEventsBetween(final CalendarSession session, final Date from, final Date until) throws OXException {
        return new InternalCalendarStorageOperation<boolean[]>(session) {

            @Override
            protected boolean[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new HasPerformer(session, storage).perform(session.getUserId(), from, until);
            }
        }.executeQuery();
    }

    @Override
    public Map<Attendee, FreeBusyResult> getFreeBusy(CalendarSession session, List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException {
        return new InternalCalendarStorageOperation<Map<Attendee, FreeBusyResult>>(session) {

            @Override
            protected Map<Attendee, FreeBusyResult> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new FreeBusyPerformer(session, storage).perform(attendees, from, until, merge);
            }
        }.executeQuery();
    }

    @Override
    public List<EventConflict> checkForConflicts(CalendarSession session, final Event event, final List<Attendee> attendees) throws OXException {
        Boolean oldCheckConflicts = session.get(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.class);
        try {
            session.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.TRUE);
            return new InternalCalendarStorageOperation<List<EventConflict>>(session) {

                @Override
                protected List<EventConflict> execute(CalendarSession session, CalendarStorage storage) throws OXException {
                    return new ConflictCheckPerformer(session, storage).perform(event, attendees);
                }
            }.executeQuery();
        } finally {
            session.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, oldCheckConflicts);
        }
    }

    @Override
    public Map<Attendee, FreeBusyResult> getFreeBusy(int ctxId, List<Attendee> attendees, Date from, Date until, boolean merge) throws OXException {

        return new OSGiCalendarStorageOperation<Map<Attendee, FreeBusyResult>>(Services.getServiceLookup(), ctxId, Utils.ACCOUNT_ID) {

            @Override
            protected Map<Attendee, FreeBusyResult> call(CalendarStorage storage) throws OXException {
                return new AdministrativeFreeBusyPerformer(storage, optEntityResolver(), Optional.empty()).perform(attendees, from, until, merge);
            }}.executeQuery();

    }

}
