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

package com.openexchange.event;

import java.util.Map;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link EventFactoryService} - Factory for events, e.g instances of {@link CommonEvent}, {@link RemoteEvent}, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface EventFactoryService {

    /**
     * Creates a new common event from specified arguments.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @param affectedUsersWithFolder a map containing the affected users as keys and a set of folders to refresh as values.
     * @param action The action constant (one of {@link GenericEvent#INSERT}, {@link GenericEvent#UPDATE}, etc.)
     * @param module The module
     * @param actionObj The action object
     * @param oldObj The old object
     * @param sourceFolder The source folder
     * @param destinationFolder The destination folder (on move)
     * @param session The session
     * @return A new common event ready for being distributed
     */
    public CommonEvent newCommonEvent(int contextId, int userId, Map<Integer, Set<Integer>> affectedUsersWithFolder, int action, int module, Object actionObj, Object oldObj, Object sourceFolder, Object destinationFolder, Session session);

    /**
     * Creates a new remote event from specified arguments.
     *
     * @param folderId The folder ID
     * @param userId The user ID
     * @param contextId The context ID
     * @param action The action; either {@link RemoteEvent#FOLDER_CHANGED} or {@link RemoteEvent#FOLDER_CONTENT_CHANGED}
     * @param module The module
     * @param timestamp The time stamp of the modification or <code>0</code> if not available
     * @return A new common event ready for being distributed
     */
    public RemoteEvent newRemoteEvent(int folderId, int userId, int contextId, int action, int module, long timestamp);

}
