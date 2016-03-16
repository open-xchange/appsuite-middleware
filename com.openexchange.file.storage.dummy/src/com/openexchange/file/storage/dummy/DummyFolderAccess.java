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

package com.openexchange.file.storage.dummy;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolder;


/**
 * {@link DummyFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DummyFolderAccess extends AbstractFileStorageFolderAccess {

    @Override
    public boolean exists(String folderId) throws OXException {
        return folderId.equals(getRootFolder().getId());
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        System.out.println("GET FOLDER: " + folderId);
        if (folderId.equals(getRootFolder().getId())) {
            return getRootFolder();
        }
        return null;
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        System.out.println("GET PERSONAL FOLDER");
        return getRootFolder();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        System.out.println("GET TRASH FOLDER");
        return getRootFolder();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        System.out.println("GET PUBLIC FOLDERS");
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        System.out.println("GET SUBFOLDERS: " + parentIdentifier+ ", " + all);
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        System.out.println("GET ROOT FOLDER");
        return RootFolder.getRootFolder();
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return null;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        return null;
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return null;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return null;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        return null;
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPath2DefaultFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
