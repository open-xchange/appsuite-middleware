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

package com.openexchange.file.storage.xctx;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.SetterAwareFileStorageFolder;
import com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.share.core.subscription.AccountMetadataHelper;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XctxFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFolderAccess extends AbstractInfostoreFolderAccess {

    private static final Logger LOG = LoggerFactory.getLogger(XctxFolderAccess.class);

    /** Identifiers of those system folders that are shared with the default infostore, hence served by the cross-context file storage provider */
    static final Set<String> UNHANDLED_FOLDER_IDS = Collections.unmodifiableSet(INFOSTORE_FOLDER_ID, PUBLIC_INFOSTORE_FOLDER_ID);

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
        if (UNHANDLED_FOLDER_IDS.contains(folderId)) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId, accountAccess.getAccountId(), accountAccess.getService().getId(), I(localSession.getUserId()), I(localSession.getContextId()));
        }
        DefaultFileStorageFolder folder = super.getFolder(folderId);
        return accountAccess.getSubscribedHelper().addSubscribed(folder);
    }

    @Override
    public DefaultFileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        final FileStorageAccount account = accountAccess.getAccount();
        DefaultFileStorageFolder[] ret = null;
        try {
            DefaultFileStorageFolder[] subfolders = super.getSubfolders(parentIdentifier, true);
            ret = accountAccess.getSubscribedHelper().addSubscribed(subfolders, false == all);
            if (parentIdentifier.equals("10") || parentIdentifier.equals("15")) {
                //Set the last known folders
                new AccountMetadataHelper(accountAccess.getAccount(), session).storeSubFolders(ret, parentIdentifier);
            }
        } catch (Exception e) {
            if (parentIdentifier.equals("10") || parentIdentifier.equals("15")) {
                try {
                    //Error: We do return the last known sub folders in case of an error.
                    //Those folders get decorated with an error when loaded later and thus can be displayed by the client
                    LOG.debug("Unable to load federate sharing folders for account " + account.getId() + ": " + e.getMessage());
                    ret = new AccountMetadataHelper(account, session).getLastKnownFolders(parentIdentifier);
                    if (ret.length == 0) {
                        LOG.debug("There are no last known federated sharing folders for account " + account.getId());
                    }
                } catch (Exception e2) {
                    LOG.error("Unable to load last known federate sharing folders for account " + account.getId(), e2);
                }
            } else {
                throw e;
            }
        }
        return ret;
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
        String result = super.updateFolder(identifier, toUpdate, cascadePermissions);
        if (SetterAwareFileStorageFolder.class.isInstance(toUpdate) && ((SetterAwareFileStorageFolder) toUpdate).containsSubscribed()) {
            DefaultFileStorageFolder folder = super.getFolder(result);
            accountAccess.getSubscribedHelper().setSubscribed(localSession, folder, B(toUpdate.isSubscribed()));
        }
        return result;
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

    @Override
    public String toString() {
        return "XctxFolderAccess [accountId=" + accountAccess.getAccountId() +
            ", localUser=" + localSession.getUserId() + '@' + localSession.getContextId() +
            ", guestUser=" + super.session.getUserId() + '@' + super.session.getContextId() + ']';
    }

}
