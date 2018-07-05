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

package com.openexchange.folderstorage.internal.performers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.RestoringFolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RestorePerformer}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class RestorePerformer extends AbstractUserizedFolderPerformer {

    /**
     * Initializes a new {@link RestorePerformer}.
     *
     * @param session The session
     * @param decorator The decorator
     * @throws OXException If passed session is invalid
     */
    public RestorePerformer(ServerSession session, FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Performs the <code>RESTORE</code> request.
     *
     * @param treeId The tree identifier
     * @param folderIds The identifiers of the folders to restore
     * @param defaultDestFolder The identifier of the default destination folder
     * @return The paths of the restored folders mapped by their identifier
     * @throws OXException If restore fails
     */
    public Map<String, List<UserizedFolder>> doRestore(String treeId, List<String> folderIds, UserizedFolder defaultDestFolder) throws OXException {
        FolderStorage storage = folderStorageDiscoverer.getFolderStorage(treeId, defaultDestFolder.getID());
        if (false == RestoringFolderStorage.class.isInstance(storage)) {
            throw FolderExceptionErrorMessage.NO_RESTORE_SUPPORT.create();
        }

        Map<String, String> restoredFromTrash = null;
        RestoringFolderStorage restoringFolderStorage = (RestoringFolderStorage) storage;
        restoredFromTrash = restoringFolderStorage.restoreFromTrash(treeId, folderIds, defaultDestFolder.getID(), storageParameters);

        FolderServiceDecorator decorator = getDecorator();
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setLocale(session.getUser().getLocale());
        PathPerformer pathPerformer = new PathPerformer(session, decorator);
        Map<String, List<UserizedFolder>> retval = new LinkedHashMap<>(restoredFromTrash.size());
        for (Map.Entry<String, String> restoredEntry : restoredFromTrash.entrySet()) {
            String folderId = restoredEntry.getKey();
            String newParentFolderId = restoredEntry.getValue();
            UserizedFolder[] userizedPath = pathPerformer.doPath(treeId, newParentFolderId, true);
            retval.put(folderId, preparePath(userizedPath, defaultDestFolder.getContentType()));
        }
        return retval;
    }

    private List<UserizedFolder> preparePath(UserizedFolder[] userizedPath, ContentType acceptedContentType) {
        List<UserizedFolder> tmp = null;
        int acceptedModule = acceptedContentType.getModule();
        for (int i = 0; i < userizedPath.length; i++) {
            UserizedFolder userizedFolder = userizedPath[i];
            int module = userizedFolder.getContentType().getModule();
            if (acceptedModule != module) {
                if (FolderObject.INFOSTORE == acceptedModule && FolderObject.SYSTEM_MODULE == module && isSystemInfoStoreFolder(userizedFolder)) {
                    if (null != tmp) {
                        tmp.add(userizedFolder);
                    }
                } else {
                    if (null == tmp) {
                        tmp = new ArrayList<UserizedFolder>(userizedPath.length);
                        if (i > 0) {
                            for (int k = 0; k < i; k++) {
                                tmp.add(userizedPath[k]);
                            }
                        }
                    }
                }
            } else {
                if (null != tmp) {
                    tmp.add(userizedFolder);
                }
            }
        }
        return null == tmp ? Arrays.asList(userizedPath) : tmp;
    }

    private boolean isSystemInfoStoreFolder(UserizedFolder userizedFolder) {
        try {
            int numericalID = Integer.parseInt(userizedFolder.getID());
            return (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == numericalID || FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == numericalID || FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == numericalID);
        } catch (NumberFormatException e) {
            // no numerical identifier
        }
        return false;
    }

}
