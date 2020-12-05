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

package com.openexchange.file.storage.xox;

import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageResult;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.SearchableFolderNameFolderAccess;
import com.openexchange.file.storage.SetterAwareFileStorageFolder;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.java.Strings;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.QuotaType;
import com.openexchange.session.Session;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * {@link XOXFolderAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXFolderAccess implements FileStorageFolderAccess, UserCreatedFileStorageFolderAccess, PermissionAware, SearchableFolderNameFolderAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(XOXFolderAccess.class);

    /** The identifier of the root folder on the remote account */
    private static final String ROOT_FOLDER_ID = "9"; // SYSTEM_INFOSTORE_FOLDER_ID

    private final XOXAccountAccess accountAccess;
    private final ShareClient client;
    private final XOXFileAccess fileAccess;
    private final Session session;

    /**
     * Initializes a new {@link XOXFolderAccess}.
     *
     * @param accountAccess The {@link XOXAccountAccess}
     * @param fileAccess The {@link XOXFileAccess}
     * @param client The {@link ShareClient} for accessing the remote OX
     * @param session the {@link Session}
     */
    public XOXFolderAccess(XOXAccountAccess accountAccess, XOXFileAccess fileAccess, ShareClient client, Session session) {
        this.fileAccess = fileAccess;
        this.session = session;
        this.accountAccess = Objects.requireNonNull(accountAccess, "accountAccess must not be null");
        this.client = Objects.requireNonNull(client, "client must not be null");
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
     * @param folderId The ID of the folder to get the quota for
     * @return The {@link AccountQuota} for module "filestorage" and account "infostore"
     * @throws OXException
     */
    private AccountQuota getInfostoreAccountQuota(String folderId) throws OXException {
        return client.getInfostoreQuota(folderId);
    }

    /**
     * Returns a list of visible/subscribed root-subfolders
     *
     * @return A list of subscribed root subfolders
     * @throws OXException
     */
    private List<FileStorageFolder> getVisibleFolders() throws OXException {
        List<FileStorageFolder> ret = new ArrayList<>();
        FileStorageFolder[] f1 = getSubfolders("10", false);
        if (f1 != null) {
            Collections.addAll(ret, f1);
        }
        FileStorageFolder[] f2 = getSubfolders("15", false);
        if (f2 != null) {
            Collections.addAll(ret, f2);
        }
        return ret;
    }

    /**
     * Remembers the subfolders of a certain parent folder within the account configuration.
     * <p/>
     * Previously remembered folders of this parent are purged implicitly, so that the passed collection of folders will effectively
     * replace the last known state for this folder type afterwards.
     *
     * @param subfolders The subfolders to remember
     * @param parentId The identifier of the parent folder to remember the subfolders for
     * @return The passed folders after they were remembered
     */
    private DefaultFileStorageFolder[] rememberSubfolders(String parentId, DefaultFileStorageFolder[] subfolders) {
        try {
            new AccountMetadataHelper(accountAccess.getAccount(), session).storeSubFolders(subfolders, parentId);
        } catch (Exception e) {
            LOGGER.warn("Error remembering subfolders of {} in account config", parentId, e);
        }
        return subfolders;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        try {
            getFolder(folderId);
            return true;
        } catch (OXException e) {
            if (e.similarTo(OXFolderExceptionCode.NOT_EXISTS)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return accountAccess.getSubscribedHelper().addSubscribed(client.getFolder(folderId));
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
        DefaultFileStorageFolder[] subfolders = client.getSubFolders(parentIdentifier);
        subfolders = accountAccess.getSubscribedHelper().addSubscribed(subfolders, false == all);
        if (parentIdentifier.equals("10") || parentIdentifier.equals("15")) {
            //Set the last known root folders
            rememberSubfolders(parentIdentifier, subfolders);
        }
        return subfolders;
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return null;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return fileAccess.createFolder(toCreate, true);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        FileStorageResult<String> result = updateFolder(identifier, toUpdate, Boolean.TRUE, null, Boolean.TRUE);
        return result.getResponse();
    }

    @Override
    public FileStorageResult<String> updateFolder(String identifier, boolean ignoreWarnings, FileStorageFolder toUpdate) throws OXException {
        return updateFolder(identifier, toUpdate, Boolean.TRUE, null, B(ignoreWarnings));
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        FileStorageResult<String> result = updateFolder(identifier, toUpdate, null, B(cascadePermissions), Boolean.TRUE);
        return result.getResponse();
    }

    @Override
    public FileStorageResult<String> updateFolder(boolean ignoreWarnings, String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        return updateFolder(identifier, toUpdate, null, B(cascadePermissions), B(ignoreWarnings));
    }

    private FileStorageResult<String> updateFolder(String folderId, FileStorageFolder folderUpdate, Boolean autoRename, Boolean cascadePermissions, Boolean ignoreWarnings) throws OXException {
        /*
         * pass-through update to remote server & handle changed "subscribed" flag internally
         */
        String result = client.updateFolder(folderId, folderUpdate, FileStorageFileAccess.DISTANT_FUTURE, autoRename, cascadePermissions);
        if (SetterAwareFileStorageFolder.class.isInstance(folderUpdate) && ((SetterAwareFileStorageFolder) folderUpdate).containsSubscribed()) {
            if(false == folderUpdate.isSubscribed()) {
                //Transition to  'unsubscribed', check if everything else is already unsubscribed
                List<FileStorageFolder> subscribedFolders = getVisibleFolders();
                Optional<FileStorageFolder> folderToUnsubscibe =  subscribedFolders.stream().filter(f -> f.getId().equals(folderId)).findFirst();
                if(folderToUnsubscibe.isPresent()) {
                    subscribedFolders.removeIf(f -> f == folderToUnsubscibe.get());
                    if(subscribedFolders.isEmpty()) {
                        //The last folder is going to be unsubscribed
                        if (ignoreWarnings == Boolean.TRUE) {
                            //Delete
                            accountAccess.getService().getAccountManager().deleteAccount(accountAccess.getAccount(), session);
                        }
                        else {

                            //Throw a warning
                            String folderName = folderToUnsubscibe.get().getName();
                            String accountName = accountAccess.getAccount().getDisplayName();
                            return FileStorageResult.newFileStorageResult(null, Arrays.asList(ShareSubscriptionExceptions.ACCOUNT_WILL_BE_REMOVED.create(folderName, accountName)));
                        }
                        return FileStorageResult.newFileStorageResult(null, null);
                    }
                }
            }
            FileStorageFolder folder = client.getFolder(result);
            accountAccess.getSubscribedHelper().setSubscribed(accountAccess.getSession(), folder, B(folderUpdate.isSubscribed()));
        }
        return FileStorageResult.newFileStorageResult(result, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return fileAccess.moveFolder(folderId, newParentId, newName, true);
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return fileAccess.moveFolder(folderId, null, newName, true);
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
        fileAccess.removeDocument(folderId, FileStorageFileAccess.DISTANT_FUTURE, hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>();
        FileStorageFolder folder = getFolder(folderId);
        folders.add(folder);
        while (Strings.isNotEmpty(folder.getParentId()) && false == ROOT_FOLDER_ID.equals(folder.getParentId())) {
            folder = getFolder(folder.getParentId());
            if (null == folder) {
                break;
            }
            folders.add(folder);
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return getStorageQuota(getInfostoreAccountQuota(folderId));
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return getFileQuota(getInfostoreAccountQuota(folderId));
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
            final AccountQuota accountQuota = getInfostoreAccountQuota(folder);
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

    @Override
    public FileStorageFolder[] searchFolderByName(String query, String folderId, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        List<XOXFolder> folders = client.searchByFolderName("0", folderId, "infostore", null, query, date, includeSubfolders, all, start, end);
        return folders.toArray(new XOXFolder[folders.size()]);
    }

}
