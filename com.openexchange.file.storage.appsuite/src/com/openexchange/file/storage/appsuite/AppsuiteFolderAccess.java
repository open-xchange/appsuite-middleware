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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.appsuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.session.Session;

/**
 * {@link AppsuiteFolderAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class AppsuiteFolderAccess implements FileStorageFolderAccess, UserCreatedFileStorageFolderAccess /* TODO-MW1380 check which marker interface to add here */ {

    private final Session session;
    private final FileStorageAccount account;
    private final ShareClient client;

    /**
     * Initializes a new {@link AppsuiteFolderAccess}.
     *
     * @param accountAccess The {@link AccountAccess}
     * @param ShareClient The {@link ShareClient} for accessing the remote OX
     */
    public AppsuiteFolderAccess(AppsuiteAccountAccess accountAccess, ShareClient client) {
        accountAccess = Objects.requireNonNull(accountAccess, "accountAccess must not be null");
        this.client = Objects.requireNonNull(client, "client must not be null");
        this.session = accountAccess.getSession();
        this.account = accountAccess.getAccount();
    }

    /**
     * Gets a value indicating whether the supplied folder identifier denotes the root folder of the account or not.
     *
     * @param folderId The folder identifier to check
     * @return <code>true</code> if the folder identifier represents the root folder, <code>false</code>, otherwise
     */
    protected static boolean isRoot(String folderId) {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId);
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return client.getFolder(folderId) != null;
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return client.getFolder(folderId);
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        return client.getSubFolders(parentIdentifier);
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return null;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        AppsuiteFolder rootFolder = new AppsuiteFolder(session.getUserId());
        //TODO: created by get from remote
        rootFolder.setRootFolder(true);
        rootFolder.setHoldsFiles(true);
        rootFolder.setHoldsFolders(true);
        rootFolder.setType(FileStorageFolderType.HOME_DIRECTORY);
        rootFolder.setName(account.getDisplayName());
        return rootFolder;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO IMPLEMENT
        return "";
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>();
        FileStorageFolder folder = getFolder(folderId);
        folders.add(folder);
        while (false == isRoot(folder.getId())) {
            folder = getFolder(folder.getParentId());
            folders.add(folder);
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        // TODO: IMPLEMENT
        return Quota.Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        // TODO: IMPLEMENT
        return Quota.Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        // TODO: IMPLEMENT
        List<Quota> ret = new ArrayList<Quota>(types.length);
        for (Type t : types) {
            ret.add(t.getUnlimited());
        }
        return ret.toArray(new Quota[ret.size()]);
    }
}
