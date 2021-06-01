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

package com.openexchange.file.storage.xctx;

import static com.openexchange.file.storage.xctx.XctxAccountAccess.XCTX_PARENT_FOLDER_IDS;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageResult;
import com.openexchange.file.storage.SearchableFolderNameFolderAccess;
import com.openexchange.file.storage.SetterAwareFileStorageFolder;
import com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.share.subscription.ShareSubscriptionExceptions;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XctxFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFolderAccess extends AbstractInfostoreFolderAccess implements SearchableFolderNameFolderAccess {

    private static final Logger LOG = LoggerFactory.getLogger(XctxFolderAccess.class);

    private final XctxAccountAccess accountAccess;
    private final XctxFolderConverter folderConverter;
    private final ServerSession localSession;

    /**
     * Initializes a new {@link XctxFolderAccess}.
     *
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFolderAccess(XctxAccountAccess accountAccess, ServerSession localSession, ServerSession guestSession) {
        super(guestSession);
        this.localSession = localSession;
        this.accountAccess = accountAccess;
        this.folderConverter = new XctxFolderConverter(accountAccess, localSession, guestSession);
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
            java.util.Collections.addAll(ret, f1);
        }
        FileStorageFolder[] f2 = getSubfolders("15", false);
        if (f2 != null) {
            java.util.Collections.addAll(ret, f2);
        }
        return ret;
    }

    @Override
    protected FolderService getFolderService() throws OXException {
        return accountAccess.getServiceSafe(FolderService.class);
    }

    @Override
    protected InfostoreFacade getInfostore() throws OXException {
        return accountAccess.getServiceSafe(InfostoreFacade.class);
    }

    @Override
    protected FolderConverter getConverter() {
        return folderConverter;
    }

    @Override
    public DefaultFileStorageFolder getFolder(String folderId) throws OXException {
        if (XCTX_PARENT_FOLDER_IDS.contains(folderId)) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId, accountAccess.getAccountId(), accountAccess.getService().getId(), I(localSession.getUserId()), I(localSession.getContextId()));
        }
        DefaultFileStorageFolder folder = super.getFolder(folderId);
        return accountAccess.getSubscribedHelper().addSubscribed(folder);
    }

    @Override
    public DefaultFileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        DefaultFileStorageFolder[] subfolders = super.getSubfolders(parentIdentifier, true);
        subfolders = accountAccess.getSubscribedHelper().addSubscribed(subfolders, false == all);
        if (XCTX_PARENT_FOLDER_IDS.contains(parentIdentifier)) {
            rememberSubfolders(parentIdentifier, subfolders);
        }
        return subfolders;
    }

    @Override
    public DefaultFileStorageFolder[] getPublicFolders() throws OXException {
        DefaultFileStorageFolder[] publicFolders = super.getPublicFolders();
        return accountAccess.getSubscribedHelper().addSubscribed(publicFolders, true);
    }

    @Override
    public DefaultFileStorageFolder[] getUserSharedFolders() throws OXException {
        DefaultFileStorageFolder[] userSharedFolders = super.getUserSharedFolders();
        return accountAccess.getSubscribedHelper().addSubscribed(userSharedFolders, false);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        FileStorageResult<String> result = updateFolder(true, identifier, toUpdate, cascadePermissions);
        return result.getResponse();
    }

    @Override
    public FileStorageResult<String> updateFolder(String identifier, boolean ignoreWarnings, FileStorageFolder toUpdate) throws OXException {
        return updateFolder(ignoreWarnings, identifier, toUpdate, false);
    }

    @Override
    public FileStorageResult<String> updateFolder(boolean ignoreWarnings, String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        String result = super.updateFolder(identifier, toUpdate, cascadePermissions);
        if (SetterAwareFileStorageFolder.class.isInstance(toUpdate) && ((SetterAwareFileStorageFolder) toUpdate).containsSubscribed()) {

            if(false == toUpdate.isSubscribed()) {
                //Transition to  'unsubscribed', check if everything else is already unsubscribed
                List<FileStorageFolder> subscribedFolders = getVisibleFolders();
                Optional<FileStorageFolder> folderToUnsubscibe =  subscribedFolders.stream().filter(f -> f.getId().equals(identifier)).findFirst();
                if(folderToUnsubscibe.isPresent()) {
                    subscribedFolders.removeIf(f -> f == folderToUnsubscibe.get());
                    if(subscribedFolders.isEmpty()) {
                        //The last folder is going to be unsubscribed
                        if(ignoreWarnings) {
                            //Delete
                            accountAccess.getService().getAccountManager().deleteAccount(accountAccess.getAccount(), localSession);
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

            DefaultFileStorageFolder folder = super.getFolder(result);
            accountAccess.getSubscribedHelper().setSubscribed(localSession, folder, B(toUpdate.isSubscribed()));
        }
        return FileStorageResult.newFileStorageResult(result, null);
    }

    @Override
    public DefaultFileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        DefaultFileStorageFolder[] path2DefaultFolder = super.getPath2DefaultFolder(folderId);
        return accountAccess.getSubscribedHelper().addSubscribed(path2DefaultFolder, false);
    }

    @Override
    protected DefaultFileStorageFolder getDefaultFolder(com.openexchange.folderstorage.Type type) throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public DefaultFileStorageFolder getRootFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
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
            new AccountMetadataHelper(accountAccess.getAccount(), localSession).storeSubFolders(subfolders, parentId);
        } catch (Exception e) {
            LOG.warn("Error remembering subfolders of {} in account config", parentId, e);
        }
        return subfolders;
    }

    @Override
    public String toString() {
        return "XctxFolderAccess [accountId=" + accountAccess.getAccountId() +
            ", localUser=" + localSession.getUserId() + '@' + localSession.getContextId() +
            ", guestUser=" + super.session.getUserId() + '@' + super.session.getContextId() + ']';
    }

}
