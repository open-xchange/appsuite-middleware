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

package com.openexchange.find.basic;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.common.FolderType;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.tools.session.ServerSession;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class Folders {

    /**
     * Gets all folder IDs to search in for the given parameters. Supports only calendar, contacts and tasks!
     *
     * @return A list of IDs or <code>null</code>, if {@link SearchRequest} didn't provide
     * a folder id or type.
     */
    public static List<Integer> getIDs(SearchRequest searchRequest, Module module, ServerSession session) throws OXException {
        String folderID = searchRequest.getFolderId();
        FolderType folderType = searchRequest.getFolderType();
        List<Integer> folderIDs = null;
        if (null == folderID) {
            Type type = getFolderType(folderType);
            ContentType contentType = getContentType(module);
            if (type != null && contentType != null) {
                folderIDs = new LinkedList<Integer>();
                FolderResponse<UserizedFolder[]> visibleFolders = Services.getFolderService().getVisibleFolders(FolderStorage.REAL_TREE_ID, contentType, type, false, session, null);
                UserizedFolder[] folders = visibleFolders.getResponse();
                if (null != folders && 0 < folders.length) {
                    for (UserizedFolder folder : folders) {
                        try {
                            if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                                folderIDs.add(Integer.valueOf(folder.getID()));
                            }
                        } catch (NumberFormatException e) {
                            throw FindExceptionCode.INVALID_FOLDER_ID.create(folder.getID(), Module.CALENDAR.getIdentifier(), e);
                        }
                    }
                }
            }
        } else {
            try {
                folderIDs = Collections.singletonList(Integer.valueOf(folderID));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(folderID, Module.CALENDAR.getIdentifier(), e);
            }
        }

        return folderIDs;
    }

    /**
     * Gets all folder IDs to search in for the given parameters. Supports only calendar, contacts and tasks!
     *
     * @return A list of IDs or <code>null</code>, if {@link SearchRequest} didn't provide
     * a folder id or type.
     */
    public static List<String> getStringIDs(SearchRequest searchRequest, Module module, ServerSession session) throws OXException {
        String folderID = searchRequest.getFolderId();
        FolderType folderType = searchRequest.getFolderType();
        List<String> folderIDs = null;
        if (null == folderID) {
            Type type = getFolderType(folderType);
            ContentType contentType = getContentType(module);
            if (type != null && contentType != null) {
                folderIDs = new LinkedList<String>();
                FolderResponse<UserizedFolder[]> visibleFolders = Services.getFolderService().getVisibleFolders(
                    FolderStorage.REAL_TREE_ID, contentType, type, false, session, null);
                UserizedFolder[] folders = visibleFolders.getResponse();
                if (null != folders && 0 < folders.length) {
                    for (UserizedFolder folder : folders) {
                        try {
                            if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                                folderIDs.add(folder.getID());
                            }
                        } catch (NumberFormatException e) {
                            throw FindExceptionCode.INVALID_FOLDER_ID.create(folder.getID(), Module.CALENDAR.getIdentifier());
                        }
                    }
                }
            }
        } else {
            try {
                folderIDs = Collections.singletonList(folderID);
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(folderID, Module.CALENDAR.getIdentifier());
            }
        }

        return folderIDs;
    }

    private static Type getFolderType(FolderType folderType) {
        if (folderType == null) {
            return null;
        }

        Type type = null;
        if (FolderType.PRIVATE == folderType) {
            type = PrivateType.getInstance();
        } else if (FolderType.PUBLIC == folderType) {
            type = PublicType.getInstance();
        } else if (FolderType.SHARED == folderType) {
            type = SharedType.getInstance();
        }

        return type;
    }

    private static ContentType getContentType(Module module) {
        switch (module) {
            case CALENDAR:
                return com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance();
            case CONTACTS:
                return ContactsContentType.getInstance();
            case TASKS:
                return TaskContentType.getInstance();
            default:
                return null;
        }
    }

}
