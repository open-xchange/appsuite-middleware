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

package com.openexchange.share.impl.groupware;

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.oxfolder.OXFolderAccess;


/**
 * {@link FolderTools}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class FolderTools {

    /**
     * Determines if the given folder is public folder based on its id, type and parent hierarchy.
     * @param folder The folder
     * @param folderAccess The folder access
     * @return <code>true</code> if the folder is public.
     */
    public static boolean isPublicFolder(FolderObject folder, OXFolderAccess folderAccess) throws OXException {
        int fid = folder.getObjectID();
        int pfid = folder.getParentFolderID();
        if (folder.getType() != FolderObject.PUBLIC || fid == FolderObject.SYSTEM_ROOT_FOLDER_ID || fid == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID || fid == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            return false;
        }

        if (fid == FolderObject.SYSTEM_PUBLIC_FOLDER_ID || fid == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID || pfid == FolderObject.SYSTEM_PUBLIC_FOLDER_ID || pfid == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID) {
            return true;
        }

        if (folder.getModule() == FolderObject.INFOSTORE) {
            // Need not to be public, we have to check the parents
            return isPublicFolder(folderAccess.getFolderObject(pfid), folderAccess);
        }

        return false;
    }

    /**
     * Determines if the leaf folder of the given folder path is a public folder or not.
     * @param folderPath The path from the leaf folder (inclusive, index 0) to the root folder (exclusive)
     * @return <code>true</code> if the folder is public.
     */
    public static boolean isPublicFolder(UserizedFolder[] folderPath) {
        for (int i = 0; i < folderPath.length; i++) {
            UserizedFolder f = folderPath[i];
            if (isPublic(f)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Don't call this directly, use {@link #isPublicFolder(UserizedFolder[])}!
     */
    private static boolean isPublic(Folder f) {
        String privateInfostore = Integer.toString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        String publicInfostore = Integer.toString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
        String publicRoot = Integer.toString(FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
        if (privateInfostore.equals(f.getID())) {
            return false;
        }

        if (publicInfostore.equals(f.getID())) {
            return true;
        }

        if (publicRoot.equals(f.getID())) {
            return true;
        }

        return false;
    }

}
