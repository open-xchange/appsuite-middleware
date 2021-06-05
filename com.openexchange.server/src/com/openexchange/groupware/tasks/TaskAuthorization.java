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

package com.openexchange.groupware.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.tools.session.ServerSession;

/**
 * This class implements authorization checks for attachments.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TaskAuthorization implements AttachmentAuthorization {

    /**
     * Default constructor.
     */
    public TaskAuthorization() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayAttach(ServerSession session, int folderId, int taskId) throws OXException {
        TaskStorage storage = TaskStorage.getInstance();
        FolderStorage foldStor = FolderStorage.getInstance();
        Task task = storage.selectTask(session.getContext(), taskId, StorageType.ACTIVE);
        task.setParentFolderID(folderId);
        FolderObject folder = Tools.getFolder(session.getContext(), folderId);
        Permission.checkWriteInFolder(session.getContext(), session.getUser(), session.getUserPermissionBits(), folder, task);
        // Check if task appears in folder.
        Folder matchingFolder = foldStor.selectFolderById(session.getContext(), taskId, folderId, StorageType.ACTIVE);
        if (null == matchingFolder || task.getPrivateFlag() && Tools.isFolderShared(folder, session.getUser())) {
            throw TaskExceptionCode.NO_PERMISSION.create(I(taskId), I(folderId));
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayDetach(ServerSession session, int folderId, int taskId) throws OXException {
        checkMayAttach(session, folderId, taskId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkMayReadAttachments(ServerSession session, int folderId, int taskId) throws OXException {
        final TaskStorage storage = TaskStorage.getInstance();
        final FolderObject folder = Tools.getFolder(session.getContext(), folderId);
        
        Permission.canReadInFolder(session.getContext(), session.getUser(), session.getUserPermissionBits(), folder);

        final Task task = storage.selectTask(session.getContext(), taskId, StorageType.ACTIVE);
        Set<Folder> folderMappings = FolderStorage.getInstance().selectFolder(session.getContext(), taskId, StorageType.ACTIVE);
        Folder matchingFolder = FolderStorage.getFolder(folderMappings, folderId);
        if (null == matchingFolder || (Tools.isFolderShared(folder, session.getUser()) && task.getPrivateFlag())) {
            throw TaskExceptionCode.NO_PERMISSION.create(Autoboxing.I(taskId), Autoboxing.I(folderId));
        }
    }
}
