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

package com.openexchange.chronos.provider.groupware;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.folder.FallbackFolderCalendarAccess;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.exception.OXException;

/**
 * {@link FallbackFolderCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public abstract class FallbackGroupwareCalendarAccess extends FallbackFolderCalendarAccess implements GroupwareCalendarAccess {

    /**
     * Initializes a new {@link FallbackGroupwareCalendarAccess}.
     * 
     * @param account The underlying account
     */
    protected FallbackGroupwareCalendarAccess(CalendarAccount account) {
        super(account);
    }

    @Override
    public CalendarResult createEvent(String folderId, Event event) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folderId);
    }

    @Override
    public CalendarResult putResource(String folderId, CalendarObjectResource resource, boolean replace) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folderId);
    }

    @Override
    public CalendarResult updateEvent(EventID eventID, Event event, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(eventID.getFolderID());
    }

    @Override
    public CalendarResult moveEvent(EventID eventID, String folderId, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folderId);
    }

    @Override
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, List<Alarm> alarms, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(eventID.getFolderID());
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(eventID.getFolderID());
    }

    @Override
    public CalendarResult splitSeries(EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(eventID.getFolderID());
    }

    @Override
    public List<ImportResult> importEvents(String folderId, List<Event> events) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(folderId);
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(I(managedId), eventID.getObjectID(), eventID.getFolderID());
    }

    @Override
    public CalendarResult changeOrganizer(EventID eventID, Organizer organizer, long clientTimestamp) throws OXException {
        throw CalendarExceptionCodes.NO_WRITE_PERMISSION.create(eventID.getFolderID());
    }

    @Override
    protected DefaultGroupwareCalendarFolder prepareFallbackFolder(String folderId) {
        return new DefaultGroupwareCalendarFolder(super.prepareFallbackFolder(folderId));
    }

}
