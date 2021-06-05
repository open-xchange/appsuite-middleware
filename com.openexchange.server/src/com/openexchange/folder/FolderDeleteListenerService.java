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

package com.openexchange.folder;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link FolderDeleteListenerService} - A listener for folder delete events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FolderDeleteListenerService {

    /**
     * Invoked on a folder delete event for specified folder.
     * <p>
     * <b>Note</b>: The implementation is supposed to throw a {@link FolderException} only if folder deletion cannot proceed unless event
     * was successfully handled by implementation; e.g. a foreign key reference to folder's tables.
     *
     * @param folderId The ID of the folder which is going to be deleted
     * @param context The folder's context
     * @throws OXException If handling the folder delete event by this listener fails
     */
    public void onFolderDelete(int folderId, Context context) throws OXException;

}
