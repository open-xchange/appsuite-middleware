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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.file.storage.composition.internal.IDManglingFolder.withRelativeID;
import static com.openexchange.file.storage.composition.internal.IDManglingFolder.withUniqueID;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;
import com.openexchange.tx.AbstractService;
import com.openexchange.tx.TransactionException;

/**
 * {@link AbstractCompositingIDBasedFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractCompositingIDBasedFolderAccess extends AbstractService<Transaction> implements IDBasedFolderAccess {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AbstractCompositingIDBasedFolderAccess.class);

    private final ThreadLocal<Map<String, FileStorageAccountAccess>> connectedAccounts = new ThreadLocal<Map<String, FileStorageAccountAccess>>();
    private final ThreadLocal<List<FileStorageAccountAccess>> accessesToClose = new ThreadLocal<List<FileStorageAccountAccess>>();
    private final Session session;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedFolderAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedFolderAccess(final Session session) {
        super();
        this.session = session;
        connectedAccounts.set(new HashMap<String, FileStorageAccountAccess>());
        accessesToClose.set(new LinkedList<FileStorageAccountAccess>());
    }

    /**
     * Gets the {@link EventAdmin} service.
     *
     * @return The event admin service
     */
    protected abstract EventAdmin getEventAdmin();

    @Override
    public boolean exists(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        try {
            return getFolderAccess(folderID).exists(folderID.getFolderId());
        } catch (final OXException e) {
            if (FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder folder = getFolderAccess(folderID).getFolder(folderID.getFolderId());
        return withUniqueID(folder, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        FolderID folderID = new FolderID(parentIdentifier);
        FileStorageFolder[] folders = getFolderAccess(folderID).getSubfolders(folderID.getFolderId(), all);
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        FolderID parentFolderID = new FolderID(toCreate.getParentId());
        FileStorageFolderAccess folderAccess = getFolderAccess(parentFolderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(parentFolderID.getFolderId());
        String newID = folderAccess.createFolder(withRelativeID(toCreate));
        FolderID newFolderID = new FolderID(parentFolderID.getService(), parentFolderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        FolderID folderID = new FolderID(identifier);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        String newID = folderAccess.updateFolder(folderID.getFolderId(), withRelativeID(toUpdate));
        FolderID newFolderID = new FolderID(folderID.getService(), folderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        FolderID newParentID = new FolderID(newParentId);
        String newID;
        Event deleteEvent = new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(folderID, path));
        if (folderID.getAccountId().equals(newParentID.getAccountId()) && folderID.getService().equals(newParentID.getService())) {
            newID = folderAccess.moveFolder(folderID.getFolderId(), newParentID.getFolderId());
        } else {
            FileStorageFolder sourceFolder = folderAccess.getFolder(folderID.getFolderId());
            FileStorageFolder toCreate = new IDManglingFolder(sourceFolder, null, newParentID.getFolderId());
            FileStorageFolderAccess targetFolderAccess = getFolderAccess(newParentID);
            path = targetFolderAccess.getPath2DefaultFolder(newParentID.getFolderId());
            newID = targetFolderAccess.createFolder(toCreate);
        }
        FolderID newFolderID = new FolderID(newParentID.getService(), newParentID.getAccountId(), newID);
        fire(deleteEvent);
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        String newID = folderAccess.renameFolder(folderID.getFolderId(), newName);
        FolderID newFolderID =new FolderID(folderID.getService(), folderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, true);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        folderAccess.deleteFolder(folderID.getFolderId(), hardDelete);
        fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(folderID, path)));
        return folderID.toUniqueID();
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, true);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        FolderID folderID = new FolderID(folderId);
        getFolderAccess(folderID).clearFolder(folderID.getFolderId());
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder[] folders = getFolderAccess(folderID).getPath2DefaultFolder(folderID.getFolderId());
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return getFolderAccess(folderID).getStorageQuota(folderID.getFolderId());
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return getFolderAccess(folderID).getFileQuota(folderID.getFolderId());
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        FolderID folderID = new FolderID(folder);
        return getFolderAccess(folderID).getQuotas(folderID.getFolderId(), types);
    }

    /**
     * Gets the folder access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The folder access
     * @throws OXException If an error occurs
     */
    protected FileStorageFolderAccess getFolderAccess(String serviceId, String accountId) throws OXException {
        FileStorageAccountAccess accountAccess = connectedAccounts.get().get(serviceId + '/' + accountId);
        if (null == accountAccess) {
            FileStorageService fileStorage = getFileStorageService(serviceId);
            accountAccess = fileStorage.getAccountAccess(accountId, session);
            connect(accountAccess);
        }
        return accountAccess.getFolderAccess();
    }

    protected FileStorageFolderAccess getFolderAccess(FolderID folderID) throws OXException {
        return getFolderAccess(folderID.getService(), folderID.getAccountId());
    }

    private void connect(FileStorageAccountAccess accountAccess) throws OXException {
        String id = accountAccess.getService().getId() + '/' + accountAccess.getAccountId();
        Map<String, FileStorageAccountAccess> accounts = connectedAccounts.get();
        if (false == accounts.containsKey(id)) {
            accounts.put(id, accountAccess);
            accountAccess.connect();
            accessesToClose.get().add(accountAccess);
        }
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        for (AccessWrapper accessWrapper : getAllAccountAccesses()) {
            FileStorageAccountAccess accountAccess = accessWrapper.accountAccess;
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            FileStorageFolder personalFolder = folderAccess.getPersonalFolder();
            if (null != personalFolder) {
                return IDManglingFolder.withUniqueID(personalFolder, accountAccess.getService().getId(), accountAccess.getAccountId());
            }
        }
        return null;
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        List<AccessWrapper> accessWrappers = getAllAccountAccesses();
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(accessWrappers.size());
        for (AccessWrapper accessWrapper : accessWrappers) {
            FileStorageAccountAccess accountAccess = accessWrapper.accountAccess;
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            FileStorageFolder[] publicFolders = folderAccess.getPublicFolders();
            if (null != publicFolders && 0 < publicFolders.length) {
                folders.addAll(Arrays.asList(IDManglingFolder.withUniqueID(
                    publicFolders, accountAccess.getService().getId(), accountAccess.getAccountId())));
            }
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    @Override
    public FileStorageFolder[] getRootFolders(final Locale locale) throws OXException {
        final List<AccessWrapper> accessWrappers = getAllAccountAccesses();
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(accessWrappers.size());
        // Sort according to account name
        Collections.sort(accessWrappers, new AccessWrapperComparator(locale == null ? Locale.US : locale));
        for (AccessWrapper accessWrapper : accessWrappers) {
            FileStorageAccountAccess accountAccess = accessWrapper.accountAccess;
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            FileStorageFolder rootFolder = folderAccess.getRootFolder();
            if (null != rootFolder) {
                folders.add(IDManglingFolder.withUniqueID(rootFolder, accountAccess.getService().getId(), accountAccess.getAccountId()));
            }
        }
        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    protected List<AccessWrapper> getAllAccountAccesses() throws OXException {
        List<FileStorageService> allFileStorageServices = getAllFileStorageServices();
        List<AccessWrapper> accountAccesses = new ArrayList<AccessWrapper>(allFileStorageServices.size());
        for (FileStorageService fsService : allFileStorageServices) {
            List<FileStorageAccount> accounts = null;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware)fsService).getAccounts(session);
            }
            if (null == accounts) {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount fileStorageAccount : accounts) {
                FileStorageAccountAccess accountAccess = fsService.getAccountAccess(fileStorageAccount.getId(), session);
                connect(accountAccess);
                accountAccesses.add(new AccessWrapper(accountAccess, fileStorageAccount.getDisplayName()));
            }
        }
        return accountAccesses;
    }

    protected abstract FileStorageService getFileStorageService(String serviceId) throws OXException;

    protected abstract List<FileStorageService> getAllFileStorageServices() throws OXException;

    // Transaction Handling

    @Override
    protected void commit(final Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    protected Transaction createTransaction() throws TransactionException {
        return null;
    }

    @Override
    protected void rollback(final Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nothing
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing
    }

    @Override
    public void setTransactional(final boolean transactional) {
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
        for(final FileStorageAccountAccess acc : accessesToClose.get()) {
            acc.close();
        }
        accessesToClose.get().clear();
        super.finish();
    }

//    private Dictionary<String, Object> getEventProperties(FolderID folderID) {
//        Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
//        properties.put(FileStorageEventConstants.SESSION, session);
//        properties.put(FileStorageEventConstants.FOLDER_ID, folderID.getFolderId());
//        properties.put(FileStorageEventConstants.ACCOUNT_ID, folderID.getAccountId());
//        properties.put(FileStorageEventConstants.SERVICE, folderID.getService());
//        return properties;
//    }

    private Dictionary<String, Object> getEventProperties(FolderID folderID, FileStorageFolder[] path) {
        Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FileStorageEventConstants.SESSION, session);
        properties.put(FileStorageEventConstants.ACCOUNT_ID, folderID.getAccountId());
        properties.put(FileStorageEventConstants.SERVICE, folderID.getService());
        properties.put(FileStorageEventConstants.FOLDER_ID, folderID.getFolderId());
        if (null != path) {
            String[] parentFolderIDs = new String[path.length];
            for (int i = 0; i < path.length; i++) {
                parentFolderIDs[i] = path[i].getId();
            }
            properties.put(FileStorageEventConstants.FOLDER_PATH, parentFolderIDs);
        }
        return properties;
    }

//    private Dictionary<String, Object> getEventProperties(FolderID folderID, FolderID parentFolderID) {
//        Dictionary<String, Object> properties = getEventProperties(folderID);
//        properties.put(FileStorageEventConstants.PARENT_FOLDER_ID, parentFolderID.getFolderId());
//        return properties;
//    }

    private void fire(Event event) {
        EventAdmin eventAdmin = getEventAdmin();
        if (null != eventAdmin) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Publishing: " + dump(event));
            }
            eventAdmin.postEvent(event);
        } else if (LOG.isWarnEnabled()) {
            LOG.warn("Unable to access event admin, unable to publish event " + dump(event));
        }
    }

    private static String dump(Event event) {
        if (null != event) {
            return new StringAllocator().append(event.getTopic())
                .append(": folderId=").append(event.getProperty(FileStorageEventConstants.FOLDER_ID))
                .append(": folderPath=").append(event.getProperty(FileStorageEventConstants.FOLDER_PATH))
                .append(", service=").append(event.getProperty(FileStorageEventConstants.SERVICE))
                .append(", accountId=").append(event.getProperty(FileStorageEventConstants.ACCOUNT_ID))
                .append(", session=").append(event.getProperty(FileStorageEventConstants.SESSION))
                .toString();
        }
        return null;
    }

    private static final class AccessWrapper {

        final FileStorageAccountAccess accountAccess;
        final String displayName;

        AccessWrapper(FileStorageAccountAccess accountAccess, String displayName) {
            super();
            this.accountAccess = accountAccess;
            this.displayName = displayName;
        }


    }

    private static final class AccessWrapperComparator implements Comparator<AccessWrapper> {

        private final Collator collator;

        AccessWrapperComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        @Override
        public int compare(final AccessWrapper o1, final AccessWrapper o2) {
            return collator.compare(o1.displayName, o2.displayName);
        }

    } // End of FileStorageAccountComparator

}
