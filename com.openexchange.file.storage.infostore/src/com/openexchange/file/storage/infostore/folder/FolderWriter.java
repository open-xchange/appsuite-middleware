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

package com.openexchange.file.storage.infostore.folder;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderWriter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderWriter {

    /**
     * Initializes a new {@link FolderWriter}.
     */
    private FolderWriter() {
        super();
    }

    /**
     * Writes a folder.
     *
     * @param folder The folder
     * @return The written folder
     */
    public static FileStorageFolder writeFolder(UserizedFolder folder) throws OXException {
        if (null == folder) {
            return null;
        }
        try {
            return new UserizedFileStorageFolder(folder);
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Converts an array of userized folders into their file storage folder equivalents.
     *
     * @param folders The userized folders to convert
     * @return The file storage folders
     */
    public static FileStorageFolder[] writeFolders(UserizedFolder[] folders) throws OXException {
        return writeFolders(folders, true);
    }
    
    /**
     * Converts an array of userized folders into their file storage folder equivalents.
     *
     * @param folders The userized folders to convert
     * @param infostoreOnly <code>true</code> to exclude folders from other modules, <code>false</code>, otherwise 
     * @return The file storage folders
     */
    public static FileStorageFolder[] writeFolders(UserizedFolder[] folders, boolean infostoreOnly) throws OXException {
        if (null == folders) {
            return null;
        }
        List<FileStorageFolder> fileStorageFolders = new ArrayList<FileStorageFolder>(folders.length);
        for (UserizedFolder folder : folders) {
            if (false == infostoreOnly || false == isNotInfostore(folder)) {
                fileStorageFolders.add(writeFolder(folder));
            }
        }
        return fileStorageFolders.toArray(new FileStorageFolder[fileStorageFolders.size()]);
    }

    /**
     * Gets a value indicating whether the supplied folder is no infostore or system folder. 
     * 
     * @param folder The folder to check
     * @return <code>true</code> if the folder is no infostore-, file- or system-folder, <code>false</code>, otherwise
     */
    private static boolean isNotInfostore(UserizedFolder folder) {
        if (null != folder) {
            ContentType contentType = folder.getContentType();
            if (null != contentType) {
                int module = contentType.getModule();
                if (FolderObject.INFOSTORE == module || FolderObject.FILE == module) {
                    return false;
                }
                if (FolderObject.SYSTEM_MODULE == module) {
                    try {
                        int numericalID = Integer.parseInt(folder.getID());
                        if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == numericalID || FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == numericalID || 
                            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == numericalID) {
                            return false;
                        }   
                    } catch (NumberFormatException e) {
                        // no numerical identifier
                    }
                }
            }
        }
        return true;
    }

}
