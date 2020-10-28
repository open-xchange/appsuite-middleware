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

package com.openexchange.chronos.provider.groupware;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
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
