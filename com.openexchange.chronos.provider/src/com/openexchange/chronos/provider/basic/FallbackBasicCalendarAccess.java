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

package com.openexchange.chronos.provider.basic;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.exception.OXException;

/**
 * {@link FallbackBasicCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public abstract class FallbackBasicCalendarAccess implements BasicCalendarAccess {

    protected final CalendarAccount account;

    /**
     * Initializes a new {@link FallbackBasicCalendarAccess}.
     * 
     * @param account The underlying account
     */
    protected FallbackBasicCalendarAccess(CalendarAccount account) {
        super();
        this.account = account;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public Event getEvent(String eventId, RecurrenceId recurrenceId) throws OXException {
        throw CalendarExceptionCodes.EVENT_NOT_FOUND_IN_FOLDER.create(eventId, BasicCalendarAccess.FOLDER_ID);
    }

    @Override
    public List<Event> getEvents() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public List<Event> getChangeExceptions(String seriesId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "FallbackBasicCalendarAccess [account=" + account + "]";
    }

}
