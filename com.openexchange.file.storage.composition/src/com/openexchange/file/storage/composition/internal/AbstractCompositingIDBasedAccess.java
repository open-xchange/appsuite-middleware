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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
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
public abstract class AbstractCompositingIDBasedAccess extends AbstractService<Transaction> {

    private final ThreadLocal<Map<String, FileStorageAccountAccess>> connectedAccounts = new ThreadLocal<Map<String, FileStorageAccountAccess>>();
    private final ThreadLocal<List<FileStorageAccountAccess>> accessesToClose = new ThreadLocal<List<FileStorageAccountAccess>>();

    protected final Session session;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedAccess(Session session) {
        super();
        this.session = session;
        connectedAccounts.set(new HashMap<String, FileStorageAccountAccess>());
        accessesToClose.set(new LinkedList<FileStorageAccountAccess>());
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
    }

    @Override
    public void finish() throws TransactionException {
        connectedAccounts.get().clear();
        List<FileStorageAccountAccess> accesses = accessesToClose.get();
        for (FileStorageAccountAccess access : accesses) {
            access.close();
        }
        accesses.clear();
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
     * Gets the folder access.
     *
     * @param folderID The folder identifier to get the file access for
     * @return The folder access
     */
    protected FileStorageFileAccess getFileAccess(FolderID folderID) throws OXException {
        return getFileAccess(folderID.getService(), folderID.getAccountId());
    }

    /**
     * Gets the account access for a specific account in a service.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The account access
     */
    protected FileStorageAccountAccess getAccountAccess(String serviceId, String accountId) throws OXException {
        FileStorageAccountAccess accountAccess = connectedAccounts.get().get(serviceId + '/' + accountId);
        if (null == accountAccess) {
            FileStorageService fileStorage = getFileStorageServiceRegistry().getFileStorageService(serviceId);
            accountAccess = fileStorage.getAccountAccess(accountId, session);
            connect(accountAccess);
        }
        return accountAccess;
    }

    /**
     * Gets a list of all file storage account accesses.
     * 
     * @return The account accesses.
     */
    protected List<FileStorageFileAccess> getAllFileStorageAccesses() throws OXException {
        List<FileStorageService> allFileStorageServices = getFileStorageServiceRegistry().getAllServices();
        List<FileStorageFileAccess> retval = new ArrayList<FileStorageFileAccess>(allFileStorageServices.size());
        for (FileStorageService fsService : getFileStorageServiceRegistry().getAllServices()) {
            List<FileStorageAccount> accounts = null;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware) fsService).getAccounts(session);
            }
            if (null == accounts) {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount fileStorageAccount : accounts) {
                FileStorageAccountAccess accountAccess = fsService.getAccountAccess(fileStorageAccount.getId(), session);
                connect(accountAccess);
                retval.add(accountAccess.getFileAccess());
            }
        }
        return retval;
    }

    private void connect(FileStorageAccountAccess accountAccess) throws OXException {
        String id = accountAccess.getService().getId() + '/' + accountAccess.getAccountId();
        Map<String, FileStorageAccountAccess> accounts = connectedAccounts.get();
        if (false == accounts.containsKey(id)) {
            try {
                accountAccess.connect();
            } catch (OXException e) {
                // OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA -- 'OAUTH-0004'
                if (e.equalsCode(4, "OAUTH") || OXExceptions.containsCommunicationError(e)) {
                    throw FileStorageExceptionCodes.ACCOUNT_NOT_ACCESSIBLE.create(e, accountAccess.getAccountId(), accountAccess.getService().getId(), session.getUserId(), session.getContextId());
                }
                throw e;
            }            
            accounts.put(id, accountAccess);
            accessesToClose.get().add(accountAccess);
        }
    }

}
