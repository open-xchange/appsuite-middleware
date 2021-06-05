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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link SequenceNumberPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SequenceNumberPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link SequenceNumberPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public SequenceNumberPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the folder to determine the sequence number for
     * @return The sequence number
     */
    public long perform(String folderId) throws OXException {
        CalendarFolder folder = getFolder(session, folderId);
        requireCalendarPermission(folder, READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        long timestamp = 0L;//TODO? folder.getLastModifiedUTC().getTime();
        SearchTerm<?> searchTerm = getFolderIdTerm(session, folder);
        SearchOptions sortOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.TIMESTAMP, SortOrder.Order.DESC)).setLimits(0, 1);
        EventField[] fields = { EventField.TIMESTAMP };
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, sortOptions, fields);
        if (0 < events.size() && timestamp < events.get(0).getTimestamp()) {
            timestamp = events.get(0).getTimestamp();
        }
        List<Event> deletedEvents = storage.getEventStorage().searchEventTombstones(searchTerm, sortOptions, fields);
        if (0 < deletedEvents.size() && timestamp < deletedEvents.get(0).getTimestamp()) {
            timestamp = deletedEvents.get(0).getTimestamp();
        }
        return timestamp;
    }

}
