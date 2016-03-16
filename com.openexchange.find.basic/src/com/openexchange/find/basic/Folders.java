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
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
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
                FolderResponse<UserizedFolder[]> visibleFolders = Services.getFolderService().getVisibleFolders(
                    FolderStorage.REAL_TREE_ID, contentType, type, false, session, null);
                UserizedFolder[] folders = visibleFolders.getResponse();
                if (null != folders && 0 < folders.length) {
                    for (UserizedFolder folder : folders) {
                        try {
                            if (folder.getOwnPermission().getReadPermission() >= Permission.READ_OWN_OBJECTS) {
                                folderIDs.add(Integer.valueOf(folder.getID()));
                            }
                        } catch (NumberFormatException e) {
                            throw FindExceptionCode.INVALID_FOLDER_ID.create(folder.getID(), Module.CALENDAR.getIdentifier());
                        }
                    }
                }
            }
        } else {
            try {
                folderIDs = Collections.singletonList(Integer.valueOf(folderID));
            } catch (NumberFormatException e) {
                throw FindExceptionCode.INVALID_FOLDER_ID.create(folderID, Module.CALENDAR.getIdentifier());
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
                return CalendarContentType.getInstance();
            case CONTACTS:
                return ContactContentType.getInstance();
            case TASKS:
                return TaskContentType.getInstance();
            default:
                return null;
        }
    }

}
