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

package com.openexchange.chronos.impl.groupware;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.DefaultCalendarEvent;
import com.openexchange.chronos.common.DeleteResultImpl;
import com.openexchange.chronos.common.UpdateResultImpl;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarEventNotificationService;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link SimpleResultTracker} - Tracks update and delete operations on calendar events. This tracker
 * is only meant to be used by the {@link CalendarDeleteListener}. For other purposes use {@link com.openexchange.chronos.impl.performer.ResultTracker}.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
class SimpleResultTracker {

    private final List<UpdateResult> updateResults;
    private final List<DeleteResult> deleteResults;
    private final Set<String> affectedFolders;

    /**
     * Initializes a new {@link SimpleResultTracker}.
     */
    public SimpleResultTracker() {
        super();
        this.updateResults = new LinkedList<>();
        this.deleteResults = new LinkedList<>();
        this.affectedFolders = new HashSet<>();
    }

    /**
     * Creates a new {@link CalendarEvent} and calls {@link CalendarHandler#handle(CalendarEvent)}
     *
     * @param session The admin session
     * @param entityResolver The entity resolver, or <code>null</code> if not available
     * @param calendarHandlers The handlers to notify
     * @param parameters Additional calendar parameters, or <code>null</code> if not set
     */
    public void notifyCalenderHandlers(Session session, EntityResolver entityResolver, CalendarEventNotificationService notificationService, CalendarParameters parameters) throws OXException {
        Map<Integer, List<String>> affectedFoldersPerUser = Utils.getAffectedFoldersPerUser(session.getContextId(), entityResolver, affectedFolders);
        if (false == affectedFoldersPerUser.isEmpty()) {
            DefaultCalendarEvent calendarEvent = new DefaultCalendarEvent(
                session.getContextId(), Utils.ACCOUNT_ID, -1, affectedFoldersPerUser, Collections.emptyList(), updateResults, deleteResults, session, entityResolver, parameters);
            notificationService.notifyHandlers(calendarEvent, false);
        }
    }

    /**
     * Add a deleted event as appropriated {@link CalendarEvent}.
     *
     * @param event The {@link Event} to delete
     * @param timestamp The timestamp of the deletion
     */
    public void addDelete(Event event, long timestamp) {
        addFolders(event);
        DeleteResult newResult = new DeleteResultImpl(timestamp, event);
        deleteResults.add(newResult);
    }

    /**
     * Add an updated event as appropriated {@link CalendarEvent}.
     *
     * @param originalEvent The original {@link Event}
     * @param updatedEvent The updated {@link Event}
     * @throws OXException See {@link UpdateResultImpl#UpdateResultImpl(Event, Event)}
     */
    public void addUpdate(Event originalEvent, Event updatedEvent) {
        addFolders(updatedEvent);
        UpdateResult newResult = new UpdateResultImpl(originalEvent, updatedEvent);
        updateResults.add(newResult);
    }

    /**
     * Track the affected folders.
     *
     * @param event The {@link Event}
     */
    private void addFolders(Event event) {
        affectedFolders.addAll(Utils.getPersonalFolderIds(event.getAttendees()));
        if (null != event.getFolderId()) {
            affectedFolders.add(event.getFolderId());
        }
    }

}
