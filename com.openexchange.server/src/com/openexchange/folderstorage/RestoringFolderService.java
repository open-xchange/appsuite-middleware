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

package com.openexchange.folderstorage;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link RestoringFolderService} - Extends the <code>FolderService</code> by {@link #restoreFolderFromTrash(String, List, String, Session, FolderServiceDecorator)} method.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface RestoringFolderService extends FolderService {

    /**
     * Restores a folder from trash to origin location
     *
     * @param tree The tree identifier
     * @param folderIds The identifiers of the folder to restore
     * @param defaultDestFolder The default destination folder
     * @param session The session
     * @return The restored paths mapped by folder id
     * @throws OXException If restore fails
     */
    FolderResponse<Map<String, List<UserizedFolder>>> restoreFolderFromTrash(String tree, List<String> folderIds, UserizedFolder defaultDestFolder, Session session, FolderServiceDecorator decorator) throws OXException;

}
