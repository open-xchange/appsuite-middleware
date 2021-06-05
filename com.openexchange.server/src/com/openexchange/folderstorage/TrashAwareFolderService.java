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

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TrashAwareFolderService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface TrashAwareFolderService extends FolderService {

    /**
     * Deletes the specified folder in given tree.
     * This method tries to move the folder to trash first, before deleting it permanently.
     * <p>
     * The folder is deleted from all trees and its subfolders as well
     *
     * @param treeId The tree identifier
     * @param folderId The folder identifier
     * @param timeStamp The requestor's last-modified time stamp
     * @param session The session
     * @param decorator The folder service decorator or <code>null</code>
     * @return a {@link FolderResponse} which holds a {@link TrashResult}
     * @throws OXException If folder cannot be deleted
     */
    FolderResponse<TrashResult> trashFolder(String treeId, String folderId, Date timeStamp, Session session, FolderServiceDecorator decorator) throws OXException;

}
