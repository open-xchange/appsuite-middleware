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

package com.openexchange.file.storage.oxshare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.QuotaType;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.session.Session;

/**
 * {@link OXShareFolderAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class OXShareFolderAccess implements FileStorageFolderAccess, UserCreatedFileStorageFolderAccess /* TODO-MW1380 check which marker interface to add here */ {

    private final Session session;
    private final OXShareAccountAccess accountAccess;
    private final FileStorageAccount account;
    private final ShareClient client;

    /**
     * Initializes a new {@link OXShareFolderAccess}.
     *
     * @param accountAccess The {@link OXShareAccountAccess}
     * @param client The {@link ShareClient} for accessing the remote OX
     */
    public OXShareFolderAccess(OXShareAccountAccess accountAccess, ShareClient client) {
        this.accountAccess = Objects.requireNonNull(accountAccess, "accountAccess must not be null");
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

    /**
     * Internal method to extract the storage quota from the given remote quota data.
     *
     * @param accountQuota The {@link AccountQuota} fetched from the remote
     * @return The {@link Quota} of type {@link Quota.Type.STORAGE} for the given account quota data
     */
    private Quota getStorageQuota(AccountQuota quota) {
        com.openexchange.quota.Quota storageQuota = quota.getQuota(QuotaType.SIZE);
        if (storageQuota != null) {
            return new Quota(storageQuota.getLimit(), storageQuota.getUsage(), Quota.Type.STORAGE);
        }
        return Quota.Type.STORAGE.getUnlimited();
    }

    /**
     * Internal method to extract the file quota from the given remote quota data.
     *
     * @param accountQuota The {@link AccountQuota} fetched from the remote
     * @return The {@link Quota} of type {@link Quota.Type.File} for the given account quota data
     */
    private Quota getFileQuota(AccountQuota accountQuota) {
        com.openexchange.quota.Quota fileQuota = accountQuota.getQuota(QuotaType.AMOUNT);
        if (fileQuota != null) {
            return new Quota(fileQuota.getLimit(), fileQuota.getUsage(), Quota.Type.FILE);
        }
        return Quota.Type.FILE.getUnlimited();
    }

    /**
     * Internal method to get the remote infostore {@link AccountQuota}
     *
     * @return The {@link AccountQuota} for module "filestorage" and account "infostore"
     * @throws OXException
     */
    private AccountQuota getInfostoreAccountQuota() throws OXException {
        return client.getInfostoreQuota();
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
        OXShareFolder rootFolder = new OXShareFolder(session.getUserId());
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
        return ((OXShareFileAccess) accountAccess.getFileAccess()).createFolder(toCreate, true);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        return client.updateFolder(identifier, toUpdate, FileStorageFileAccess.DISTANT_FUTURE, true);
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return ((OXShareFileAccess) accountAccess.getFileAccess()).moveFolder(folderId, newParentId, newName, true);
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return ((OXShareFileAccess) accountAccess.getFileAccess()).moveFolder(folderId, null, newName, true);
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        return client.deleteFolder(folderId, hardDelete);
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        ((OXShareFileAccess) accountAccess.getFileAccess()).removeDocument(folderId, FileStorageFileAccess.DISTANT_FUTURE, hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>();
        FileStorageFolder folder = getFolder(folderId);
        folders.add(folder);
        while (false == isRoot(folder.getId())) {
            folder = isRoot(folder.getParentId()) ? getRootFolder() : getFolder(folder.getParentId());
            folders.add(folder);
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return getStorageQuota(getInfostoreAccountQuota());
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return getFileQuota(getInfostoreAccountQuota());
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        final List<Quota> ret = new ArrayList<Quota>(types.length);
        final List<Type> quotaTypes = Arrays.asList(types);
        if (quotaTypes.contains(Type.FILE) || quotaTypes.contains(Type.STORAGE)) {
            //Fetch quota information from remote
            final AccountQuota accountQuota = getInfostoreAccountQuota();
            for (Type t : quotaTypes) {
                switch (t) {
                    case FILE:
                        ret.add(getFileQuota(accountQuota));
                        break;
                    case STORAGE:
                        ret.add(getStorageQuota(accountQuota));
                        break;
                    default:
                        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + t);
                }
            }
        }
        return ret.toArray(new Quota[ret.size()]);
    }
}
