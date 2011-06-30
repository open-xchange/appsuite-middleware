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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.file.storage.smartDrive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.smartdrive.client.SmartDriveDirectory;
import com.openexchange.smartdrive.client.SmartDriveException;
import com.openexchange.smartdrive.client.SmartDriveResource;
import com.openexchange.smartdrive.client.SmartDriveResponse;
import com.openexchange.smartdrive.client.SmartDriveStatefulAccess;
import static com.openexchange.file.storage.smartDrive.Helpers.*;

/**
 * {@link UISDFolderAccess}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UISDFolderAccess implements FileStorageFolderAccess {

    private UISDAccountAccess accountAccess;

    /**
     * Initializes a new {@link UISDFolderAccess}.
     * 
     * @param accountAccess
     */
    public UISDFolderAccess(UISDAccountAccess accountAccess) {
        this.accountAccess = accountAccess;
    }

    public void clearFolder(String folderId) throws FileStorageException {

    }

    public void clearFolder(String folderId, boolean hardDelete) throws FileStorageException {

    }

    public String createFolder(FileStorageFolder toCreate) throws FileStorageException {
        return null;
    }

    public String deleteFolder(String folderId) throws FileStorageException {
        return null;
    }

    public String deleteFolder(String folderId, boolean hardDelete) throws FileStorageException {
        return null;
    }

    public boolean exists(String folderId) throws FileStorageException {
        return false;
    }

    public Quota getFileQuota(String folderId) throws FileStorageException {
        return null;
    }

    public FileStorageFolder getFolder(String folderId) throws FileStorageException {
        if (isRootFolder(folderId)) {
            return getRootFolder();
        }
        SmartDriveStatefulAccess statefulAccess = accountAccess.getStatefulAccess();
        try {
            SmartDriveResponse<List<SmartDriveResource>> response = statefulAccess.propget(folderId, new int[0]);
            checkResponse(response);
            List<FileStorageFolder> asFolders = getFolders(null, response.getResponse());
            if (!asFolders.isEmpty()) {
                DefaultFileStorageFolder f = (DefaultFileStorageFolder) asFolders.get(0);
                f.setId(folderId);
                return f;
            }
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }
        return null;
    }

    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws FileStorageException {
        return new FileStorageFolder[0];
    }

    public Quota[] getQuotas(String folder, Type[] types) throws FileStorageException {
        return new Quota[0];
    }

    public FileStorageFolder getRootFolder() throws FileStorageException {
        DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
        folder.setId("");
        folder.setName(accountAccess.getAccount().getDisplayName());
        folder.setSubfolders(true);
        folder.setRootFolder(true);
        
        folder.setExists(true);
        folder.setHoldsFiles(true);
        folder.setHoldsFolders(true);
        folder.setLastModifiedDate(new Date());
        folder.setParentId("1");
        folder.setCreationDate(new Date());
        folder.setSubscribed(true);
        folder.setSubscribedSubfolders(true);
        folder.setFileCount(0);
        
        FileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(accountAccess.getUserId());
        folder.setPermissions(Arrays.asList(permission));
        
        return folder;
    }

    public Quota getStorageQuota(String folderId) throws FileStorageException {
        return null;
    }

    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws FileStorageException {
        if (isRootFolder(parentIdentifier)) {
            parentIdentifier = "";
        }
        SmartDriveStatefulAccess statefulAccess = accountAccess.getStatefulAccess();
        try {
            SmartDriveResponse<List<SmartDriveResource>> response = statefulAccess.list(parentIdentifier);
            checkResponse(response);
            List<FileStorageFolder> asFolders = getFolders(parentIdentifier, response.getResponse());
            return asFolders.toArray(new FileStorageFolder[asFolders.size()]);

        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }
    }

    public String moveFolder(String folderId, String newParentId) throws FileStorageException {
        return null;
    }

    public String renameFolder(String folderId, String newName) throws FileStorageException {
        return null;
    }

    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws FileStorageException {
        return null;
    }

    private List<FileStorageFolder> getFolders(String parentId, List<SmartDriveResource> response) throws FileStorageException {
        List<FileStorageFolder> retval = new ArrayList<FileStorageFolder>(response.size());
        try {
            for (SmartDriveResource resource : response) {
                if (resource.isDirectory()) {
                    SmartDriveDirectory directory = resource.toDirectory();
                    DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
                    if(parentId != null) {
                        folder.setId(parentId + "/" + directory.getName());
                    }
                    folder.setName(directory.getName());

                    final FileStoragePermission permission = DefaultFileStoragePermission.newInstance();
                    permission.setEntity(accountAccess.getUserId());
                    folder.setPermissions(Arrays.asList(permission));
                    folder.setSubfolders(true);

                    folder.setExists(true);
                    folder.setHoldsFiles(true);
                    folder.setHoldsFolders(true);
                    folder.setLastModifiedDate(new Date());
                    folder.setParentId(parentId);
                    folder.setCreationDate(new Date());
                    
                    folder.setSubscribed(true);
                    folder.setSubscribedSubfolders(true);
                    folder.setFileCount(0);
                    

                    retval.add(folder);
                }
            }
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }

        return retval;
    }

    private boolean isRootFolder(String folderId) {
        return "".equals(folderId);
    }

}
