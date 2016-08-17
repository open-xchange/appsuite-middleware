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
