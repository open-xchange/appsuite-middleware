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

package com.openexchange.chronos.impl.osgi.event;

import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.Types;
import com.openexchange.session.Session;

/**
 * {@link ChronosCommonEvent} - The {@link CommonEvent} to thrown on {@link Event} changes
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosCommonEvent implements CommonEvent {

    private final Session session;
    private final int actionID;
    private final Event actionEvent;
    private final Event oldEvent;
    private final Map<Integer, Set<Integer>> affectedUsersWithFolders;

    /**
     * Initializes a new {@link ChronosCommonEvent}.
     *
     * @param session The associated session
     * @param actionID The common event action identifier to take over
     * @param actionEvent The new, updated or deleted event
     * @param oldEvent The original event in case of updates
     * @param affectedUsersWithFolders A map containing the affected user identifiers associated with the corresponding folder identifiers
     */
    public ChronosCommonEvent(Session session, int actionID, Event actionEvent, Event oldEvent, Map<Integer, Set<Integer>> affectedUsersWithFolders) {
        super();
        this.session = session;
        this.actionID = actionID;
        this.actionEvent = actionEvent;
        this.oldEvent = oldEvent;
        this.affectedUsersWithFolders = affectedUsersWithFolders;
    }

    @Override
    public int getContextId() {
        return session.getContextId();
    }

    @Override
    public int getUserId() {
        return session.getUserId();
    }

    @Override
    public int getModule() {
        return Types.APPOINTMENT;
    }

    @Override
    public Object getActionObj() {
        return actionEvent;
    }

    @Override
    public Object getOldObj() {
        return oldEvent;
    }

    /**
     * Get the old {@link Event} in case of an update
     *
     * @return The old event or <code>null</code>
     */
    public Event getOldEvent() {
        return oldEvent;
    }

    @Override
    public Object getSourceFolder() {
        return null == oldEvent ? null : oldEvent.getFolderId();
    }

    @Override
    public Object getDestinationFolder() {
        return null == actionEvent ? null : actionEvent.getFolderId();
    }

    @Override
    public int getAction() {
        return actionID;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Map<Integer, Set<Integer>> getAffectedUsersWithFolder() {
        return affectedUsersWithFolders;
    }

}
