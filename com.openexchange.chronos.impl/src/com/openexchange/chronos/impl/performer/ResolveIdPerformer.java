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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.Utils.applyExceptionDates;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link ResolveIdPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ResolveIdPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link ResolveIdPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public ResolveIdPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param eventId The identifier of the event to resolve
     * @return The resolved event, or <code>null</code> if not found
     */
    public Event perform(String eventId) throws OXException {
        /*
         * load event data, check permissions & apply folder identifier
         */
        Event event = storage.getEventStorage().loadEvent(eventId, null);
        if (null == event) {
            return null;
        }
        int calendarUserId = session.getUserId();
        event = storage.getUtilities().loadAdditionalEventData(calendarUserId, event, null);
        String folderId = CalendarUtils.getFolderView(event, calendarUserId);
        if (false == hasReadPermission(event)) {
            CalendarFolder folder = getFolder(session, folderId);
            if (false == matches(event.getCreatedBy(), session.getUserId())) {
                requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
            } else {
                requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
            }
        }
        event.setFolderId(folderId);
        event.setFlags(getFlags(event, calendarUserId));
        /*
         * return event, anonymized as needed
         */
        if (isSeriesMaster(event)) {
            event = applyExceptionDates(storage, event, calendarUserId);
        }
        return anonymizeIfNeeded(session, event);
    }

}
