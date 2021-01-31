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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.marker.OXThreadMarkers.unrememberCloseable;
import static org.slf4j.LoggerFactory.getLogger;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultWarningsAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.file.storage.WarningsAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tx.AbstractService;
import com.openexchange.tx.TransactionException;

/**
 * {@link AbstractCompositingIDBasedAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractCompositingIDBasedAccess extends AbstractService<Transaction> implements WarningsAware, Closeable {

    /** The identifier of the shared infostore root folder */
    static final String SHARED_INFOSTORE_ID = "10"; // com.openexchange.groupware.container.FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID

    /** The identifier of the public infostore root folder */
    static final String PUBLIC_INFOSTORE_ID = "15"; // com.openexchange.groupware.container.FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID

    private final ThreadLocal<Map<String, FileStorageAccountAccess>> connectedAccounts = new ThreadLocal<Map<String, FileStorageAccountAccess>>();
    private final ThreadLocal<List<FileStorageAccountAccess>> accessesToClose = new ThreadLocal<List<FileStorageAccountAccess>>();

    protected final Session session;
    private final WarningsAware warningsAware;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedAccess(Session session) {
        super();
        this.session = session;
        this.warningsAware = new DefaultWarningsAware();
        connectedAccounts.set(new HashMap<String, FileStorageAccountAccess>());
        accessesToClose.set(new LinkedList<FileStorageAccountAccess>());
    }

    // --------------------------------------------------- Closeable stuff -----------------------------------------------------------------

    @Override
    public void close() throws IOException {
        try {
            this.finish0(false);
        } catch (Exception e) {
            // Ignore
        }
    }

    // --------------------------------------------------- IDBasedAccess stuff -------------------------------------------------------------

    @Override
    public List<OXException> getWarnings() {
        return warningsAware.getWarnings();
    }

    @Override
    public List<OXException> getAndFlushWarnings() {
        return warningsAware.getAndFlushWarnings();
    }

    @Override
    public void addWarning(OXException warning) {
        warningsAware.addWarning(warning);
    }

    @Override
    public void removeWarning(OXException warning) {
        warningsAware.removeWarning(warning);
    }

    @Override
    protected void commit(Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    protected Transaction createTransaction() throws TransactionException {
        return null;
    }

    @Override
    protected void rollback(Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        // Nothing
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        // Nothing
    }

    @Override
    public void setTransactional(boolean transactional) {
        // Nothing
    }

    @Override
    public void startTransaction() throws TransactionException {
        super.startTransaction();
        connectedAccounts.get().clear();
        accessesToClose.get().clear();
        warningsAware.getAndFlushWarnings();
    }

    @Override
    public void finish() throws TransactionException {
        finish0(true);
    }

    private void finish0(boolean unremember) throws TransactionException {
        connectedAccounts.get().clear();
        List<FileStorageAccountAccess> accesses = accessesToClose.get();
        for (FileStorageAccountAccess access : accesses) {
            if (WarningsAware.class.isInstance(access)) {
                addWarnings(((WarningsAware) access).getAndFlushWarnings());
            }
            access.close();
        }
        accesses.clear();
        if (unremember) {
            unrememberCloseable(this);
        }
        super.finish();
    }

    @Override
    public String toString() {
        return new StringBuilder("IDBasedAccess ")
            .append("[user=").append(session.getUserId()).append(", context=").append(session.getContextId())
            .append(", connectedAccounts=").append(connectedAccounts.get().keySet()).append(']')
            .toString();
    }

    /**
     * Gets a reference to the {@link EventAdmin} service.
     *
     * @return The event admin service
     */
    protected abstract EventAdmin getEventAdmin();

    /**
     * Gets a reference to the {@link FileStorageServiceRegistry} service.
     *
     * @return The file storage service registry
     */
    protected abstract FileStorageServiceRegistry getFileStorageServiceRegistry();

    /**
     * Gets the associated session
     *
     * @return The session
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Adds multiple warnings.
     *
     * @param warnings The warnings to add
     */
    protected void addWarnings(Collection<OXException> warnings) {
        if (null != warnings && 0 < warnings.size()) {
            for (OXException warning : warnings) {
                warningsAware.addWarning(warning);
            }
        }
    }

    /**
     * Gets the folder access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The folder access
     */
    protected FileStorageFolderAccess getFolderAccess(String serviceId, String accountId) throws OXException {
        return getAccountAccess(serviceId, accountId).getFolderAccess();
    }
    /**
     * Gets the folder access.
     *
     * @param folderID The folder identifier to get the folder access for
     * @return The folder access
     */
    protected FileStorageFolderAccess getFolderAccess(FolderID folderID) throws OXException {
        return getFolderAccess(folderID.getService(), folderID.getAccountId());
    }

    /**
     * Optionally gets the folder access.
     *
     * @param folderID The folder identifier to get the file access for
     * @return The folder access or <code>null</code> if not already initialized before
     */
    protected FileStorageFolderAccess optFolderAccess(FolderID folderID) throws OXException {
        FileStorageAccountAccess accountAccess = getAccountAccess(folderID.getService(), folderID.getAccountId());
        return null == accountAccess ? null : accountAccess.getFolderAccess();
    }

    /**
     * Gets the file access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The file access
     */
    protected FileStorageFileAccess getFileAccess(String serviceId, String accountId) throws OXException {
        return getAccountAccess(serviceId, accountId).getFileAccess();
    }

    /**
     * Gets the file access implementing a specific extension for a specific account. The account is connected implicitly and
     * remembered to be closed during {@link #finish()} implicitly, if not already done.
     * <p/>
     * If the extension is not provided by the account's file access, an appropriate exception is thrown.
     *
     * @param <T> The required interface for the targeted file storage file access implementation
     * @param serviceId The identifier of the service to get the file access for
     * @param accountId The identifier of the account to get the file access for
     * @param extensionClass The interface to cast the file access reference
     * @return The file storage file access for the specified account
     */
    protected <T extends FileStorageFileAccess> T getFileAccess(String serviceId, String accountId, Class<T> extensionClass) throws OXException {
        FileStorageFileAccess access = getFileAccess(serviceId, accountId);
        try {
            return extensionClass.cast(access);
        } catch (ClassCastException e) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(serviceId);
        }
    }

    /**
     * Gets the file access implementing a specific extension for a specific account. The account is connected implicitly and
     * remembered to be closed during {@link #finish()} implicitly, if not already done.
     * <p/>
     * If the extension is not provided by the account's file access, an appropriate exception is thrown.
     *
     * @param folderID The folder identifier to get the file access for
     * @param extensionClass The interface to cast the file access reference
     * @return The file storage file access for the specified account
     */
    protected <T extends FileStorageFileAccess> T getFileAccess(FolderID folderID, Class<T> extensionClass) throws OXException {
        return getFileAccess(folderID.getService(), folderID.getAccountId(), extensionClass);
    }

    /**
     * Gets the file access implementing a specific extension for a specific account. The account is connected implicitly and
     * remembered to be closed during {@link #finish()} implicitly, if not already done.
     * <p/>
     * If the extension is not provided by the account's file access, an appropriate exception is thrown.
     *
     * @param folderID The folder identifier to get the file access for
     * @return The file storage file access for the specified account
     */
    protected FileStorageFileAccess getFileAccess(FolderID folderID) throws OXException {
        return getFileAccess(folderID.getService(), folderID.getAccountId());
    }

    /**
     * Optionally gets the file access.
     *
     * @param folderID The folder identifier to get the file access for
     * @return The file access or <code>null</code> if not already initialized before
     */
    protected FileStorageFileAccess optFileAccess(FolderID folderID) throws OXException {
        FileStorageAccountAccess accountAccess = getAccountAccess(folderID.getService(), folderID.getAccountId());
        return null == accountAccess ? null : accountAccess.getFileAccess();
    }

    /**
     * Gets the account access for a specific account in a service.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The account access
     */
    protected FileStorageAccountAccess getAccountAccess(String serviceId, String accountId) throws OXException {
        String id = new StringBuilder(serviceId).append('/').append(accountId).toString();
        FileStorageAccountAccess accountAccess = connectedAccounts.get().get(id);
        if (null == accountAccess) {
            FileStorageService fileStorage = getFileStorageServiceRegistry().getFileStorageService(serviceId);
            accountAccess = fileStorage.getAccountAccess(accountId, session);
            return connect(accountAccess, id);
        }
        return accountAccess;
    }

    /**
     * Optionally gets the account access for a specific account in a service.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The account access or <code>null</code>
     */
    protected FileStorageAccountAccess optAccountAccess(String serviceId, String accountId) {
        String id = new StringBuilder(serviceId).append('/').append(accountId).toString();
        return connectedAccounts.get().get(id);
    }

    /**
     * Gets a list of all file storage account accesses.
     *
     * @return The connected account accesses.
     */
    protected List<FileStorageFileAccess> getAllFileStorageAccesses() throws OXException {
        return getAllFileStorageAccesses(null);
    }

    /**
     * Gets a list of all file storage account accesses.
     *
     * @param filter A predicate which defines the {@link FileStorageService}s which should only be returned, or <code>null</code> to return all services.
     * @return The account accesses.
     */
    protected List<FileStorageFileAccess> getAllFileStorageAccesses(Predicate<FileStorageService> filter) throws OXException {
        List<FileStorageService> allFileStorageServices = getFileStorageServiceRegistry().getAllServices();
        List<FileStorageFileAccess> retval = new ArrayList<FileStorageFileAccess>(allFileStorageServices.size());
        for (FileStorageService fsService : getFileStorageServiceRegistry().getAllServices()) {

            if (filter != null && false == filter.test(fsService)) {
                //ignore not matching service
                continue;
            }

            List<FileStorageAccount> accounts = null;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware) fsService).getAccounts(session);
            }
            if (null == accounts) {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount fileStorageAccount : accounts) {
                FileStorageAccountAccess accountAccess = fsService.getAccountAccess(fileStorageAccount.getId(), session);
                retval.add(connect(accountAccess).getFileAccess());
            }
        }
        return retval;
    }

    /**
     * Constructs the unique folder identifier for the supplied storage-relative folder identifier in a specific file storage account.
     *
     * @param relativeId The relative folder identifier to get the unique composite identifier for
     * @param serviceId The file storage service identifier the referenced folder originates in
     * @param accountId The identifier of the account the referenced folder originates in
     * @return The unique folder identifier
     */
    protected String getUniqueFolderId(String relativeId, String serviceId, String accountId) {
        if (null == relativeId || FileID.INFOSTORE_SERVICE_ID.equals(serviceId) && FileID.INFOSTORE_ACCOUNT_ID.equals(accountId)) {
            return relativeId;
        }
        /*
         * check if special handling of shared root folders for federated shares applies
         */
        if ((SHARED_INFOSTORE_ID.equals(relativeId) || PUBLIC_INFOSTORE_ID.equals(relativeId)) && false == isSeparateFederatedShares()) {
            FileStorageService fileStorageService = null;
            try {
                FileStorageAccountAccess accountAccess = optAccountAccess(serviceId, accountId);
                fileStorageService = null == accountAccess ? getFileStorageServiceRegistry().getFileStorageService(serviceId) : accountAccess.getService();
            } catch (OXException e) {
                getLogger(AbstractCompositingIDBasedAccess.class).warn(
                    "Unexpected error determining file storage service for {} / {}, falling back to static ID mangling", serviceId, accountId, e);
            }
            if (null != fileStorageService && SharingFileStorageService.class.isInstance(fileStorageService)) {
                /*
                 * do not mangle shared/public root folders of integrated federated shares
                 */
                return relativeId;
            }
        }
        return new FolderID(serviceId, accountId, relativeId).toUniqueID();
    }

    private static final String INFOSTORE_ID = "com.openexchange.infostore/infostore";
    private static final String INFOSTORE_DEFAULT_ACCOUNT_ID = "infostore";

    /**
     * Connects the supplied account access if not already done, remembering the account for closing during the {@link #finish()}.
     *
     * @param accountAccess The account access to connect
     * @return The account access, or a previously connected account access.
     */
    private FileStorageAccountAccess connect(FileStorageAccountAccess accountAccess) throws OXException {
        return connect(accountAccess, INFOSTORE_DEFAULT_ACCOUNT_ID.equals(accountAccess.getAccountId()) ? INFOSTORE_ID : new StringBuilder(accountAccess.getService().getId()).append('/').append(accountAccess.getAccountId()).toString());
    }

    /**
     * Connects the supplied account access if not already done, remembering the account for closing during the {@link #finish()}.
     *
     * @param accountAccess The account access to connect
     * @param id The account identifier for look-up purpose
     * @return The account access, or a previously connected account access.
     */
    private FileStorageAccountAccess connect(FileStorageAccountAccess accountAccess, String id) throws OXException {
        Map<String, FileStorageAccountAccess> accounts = connectedAccounts.get();
        FileStorageAccountAccess connectedAccountAccess = accounts.get(id);
        if (null != connectedAccountAccess) {
            return connectedAccountAccess;
        }
        try {
            accountAccess.connect();
        } catch (OXException e) {
            // OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA -- 'OAUTH-0004'
            if (e.equalsCode(4, "OAUTH") || OXExceptions.containsCommunicationError(e)) {
                throw FileStorageExceptionCodes.ACCOUNT_NOT_ACCESSIBLE.create(e, accountAccess.getAccountId(), accountAccess.getService().getId(), I(session.getUserId()), I(session.getContextId()));
            }
            throw e;
        }
        accounts.put(id, accountAccess);
        accessesToClose.get().add(accountAccess);
        return accountAccess;
    }

    /**
     * Gets a value indicating whether <i>federated</i> shares from other servers/contexts are mounted at a separate location in the
     * folder tree, or if they're integrated into the common system folders "Shared Files" / "Public Files".
     *
     * @return <code>true</code> if federated shares appear as separate account, <code>false</code> if they're integrated into the default folders
     */
    protected boolean isSeparateFederatedShares() {
        return false; // not separated for now
    }

}
