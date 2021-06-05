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

package com.openexchange.folderstorage.contact;

import static com.openexchange.contact.common.ContactsParameters.PARAMETER_CONNECTION;
import static com.openexchange.folderstorage.ContactsFolderConverter.getContactsFolder;
import static com.openexchange.folderstorage.ContactsFolderConverter.getStorageFolder;
import static com.openexchange.folderstorage.ContactsFolderConverter.getStorageFolders;
import static com.openexchange.folderstorage.ContactsFolderConverter.optContactsConfig;
import static com.openexchange.folderstorage.ContactsFolderConverter.optContactsProvider;
import static com.openexchange.groupware.contact.ContactUtil.DISTANT_FUTURE;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.contact.common.AccountAwareContactsFolder;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.GroupwareFolderType;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContactsFolderConverter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.SubfolderListingFolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.user.User;

/**
 * {@link ContactsFolderStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsFolderStorage implements SubfolderListingFolderStorage {

    /** The contacts folder type */
    private static final FolderType FOLDER_TYPE = new ContactsFolderType();

    /** The parameter name used to store the {@link IDBasedContactsAccess} reference in the storage parameters */
    private static final String PARAMETER_ACCESS = IDBasedContactsAccess.class.getName();

    private final IDBasedContactsAccessFactory accessFactory;

    /**
     * Initializes a new {@link ContactsFolderStorage}.
     *
     * @param accessFactory The underlying ID-based contacts access factory
     */
    public ContactsFolderStorage(IDBasedContactsAccessFactory accessFactory) {
        super();
        this.accessFactory = accessFactory;
    }

    @Override
    public void clearCache(int userId, int contextId) {
        // no-op
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { ContactContentType.getInstance() };
    }

    @Override
    public FolderType getFolderType() {
        return FOLDER_TYPE;
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public ContentType getDefaultContentType() {
        return ContactContentType.getInstance();
    }

    @Override
    public boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return false;
    }

    @Override
    public boolean containsForeignObjects(User user, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return false;
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public boolean containsFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            return false;
        }
        return null != getFolder(treeId, folderId, storageParameters);
    }

    @Override
    public void checkConsistency(String treeId, StorageParameters storageParameters) throws OXException {
        // no-op
    }

    @Override
    public void restore(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // no-op
    }

    @Override
    public Folder prepareFolder(String treeId, Folder folder, StorageParameters storageParameters) throws OXException {
        return folder;
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        AccountAwareContactsFolder contactsFolder = getContactsAccess(storageParameters).getFolder(folderId);
        ParameterizedFolder result = getStorageFolder(treeId, getDefaultContentType(), contactsFolder, contactsFolder.getAccount());
        return result;
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageType storageType, StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        List<AccountAwareContactsFolder> contactsFolders = getContactsAccess(storageParameters).getFolders(folderIds);
        return getStorageFolders(treeId, getDefaultContentType(), contactsFolders);
    }

    @Override
    public String getDefaultFolderID(User user, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        if (false == ContactContentType.class.isInstance(contentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }
        if (false == GroupwareFolderType.PRIVATE.equals(ContactsFolderConverter.getFolderType(type))) {
            throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(contentType, treeId);
        }
        IDBasedContactsAccess access = getContactsAccess(storageParameters);
        ContactsFolder folder = access.getDefaultFolder();
        if (null == folder) {
            throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(contentType, treeId);
        }
        return folder.getId();
    }

    @Override
    public Type getTypeByParent(User user, String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        return ContactType.getInstance();
    }

    @Override
    public void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        IDBasedContactsAccess access = getContactsAccess(storageParameters);
        long timestamp = null != storageParameters.getTimeStamp() ? storageParameters.getTimeStamp().getTime() : DISTANT_FUTURE;
        access.deleteFolder(folderId, timestamp);
    }

    @Override
    public void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        throw new UnsupportedOperationException("ContactsFolderStorage.clearFolder()");
    }

    @Override
    public void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        IDBasedContactsAccess access = getContactsAccess(storageParameters);

        // Update folder
        long timestamp = null != storageParameters.getTimeStamp() ? storageParameters.getTimeStamp().getTime() : DISTANT_FUTURE;
        ContactsFolder folderToUpdate = getContactsFolder(folder);
        JSONObject userConfig = optContactsConfig(folder);
        String updatedFolderID = access.updateFolder(folder.getID(), folderToUpdate, userConfig, timestamp);

        // Take over updated identifiers in passed folder reference
        folder.setID(updatedFolderID);
        folder.setLastModified(folderToUpdate.getLastModified());
    }

    @Override
    public void updateLastModified(long lastModified, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // no-op
    }

    @Override
    public void createFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        IDBasedContactsAccess access = getContactsAccess(storageParameters);
        ContactsFolder folderToCreate = getContactsFolder(folder);
        String providerId = optContactsProvider(folder);
        JSONObject userConfig = optContactsConfig(folder);
        String newFolderId = access.createFolder(providerId, folderToCreate, userConfig);
        folder.setID(newFolderId);
    }

    @Override
    public SortableId[] getVisibleFolders(String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        if (false == ContactContentType.class.isInstance(contentType)) {
            return new SortableId[0];
        }
        return getSortableIDs(getVisibleFolders(ContactsFolderConverter.getFolderType(type), storageParameters));
    }

    @Override
    public SortableId[] getVisibleFolders(String rootFolderId, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        return getVisibleFolders(treeId, contentType, type, storageParameters);
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        throw new UnsupportedOperationException("ContactsFolderStorage.getUserSharedFolders()");
    }

    @Override
    public SortableId[] getSubfolders(String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        if (PRIVATE_ID.equals(parentId)) {
            return getSortableIDs(getVisibleFolders(GroupwareFolderType.PRIVATE, storageParameters));
        }
        if (SHARED_ID.equals(parentId)) {
            return getSortableIDs(getVisibleFolders(GroupwareFolderType.SHARED, storageParameters));
        }
        if (PUBLIC_ID.equals(parentId)) {
            return getSortableIDs(getVisibleFolders(GroupwareFolderType.PUBLIC, storageParameters));
        }
        return new SortableId[0];
    }

    @Override
    public String[] getModifiedFolderIDs(String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    @Override
    public String[] getDeletedFolderIDs(String treeId, Date timeStamp, StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    @Override
    public boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException {
        // Initialise ID based file access only if necessary
        if (null != parameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS)) {
            return false;
        }
        // Ensure the session is present
        if (null == parameters.getSession()) {
            throw FolderExceptionErrorMessage.MISSING_SESSION.create();
        }
        // Create access via factory
        IDBasedContactsAccess access = accessFactory.createAccess(parameters.getSession());
        if (false == parameters.putParameterIfAbsent(FOLDER_TYPE, PARAMETER_ACCESS, access)) {
            return false;
        }
        // Enqueue in managed transaction if possible, otherwise signal that we started the transaction ourselves
        if (false == TransactionManager.isManagedTransaction(parameters)) {
            return true;
        }
        TransactionManager transactionManager = TransactionManager.getTransactionManager(parameters);
        if (null == transactionManager) {
            return true;
        }
        access.set(PARAMETER_CONNECTION(), transactionManager.getConnection());
        transactionManager.transactionStarted(this);
        return false;
    }

    @Override
    public void commitTransaction(StorageParameters storageParameters) throws OXException {
        IDBasedContactsAccess contactsAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != contactsAccess) {
            try {
                contactsAccess.commit();
            } finally {
                finish(storageParameters);
            }
        }
    }

    @Override
    public void rollback(StorageParameters storageParameters) {
        IDBasedContactsAccess contactsAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != contactsAccess) {
            try {
                contactsAccess.rollback();
            } catch (Exception e) {
                // Ignore
                org.slf4j.LoggerFactory.getLogger(ContactsFolderStorage.class).warn("Unexpected error during rollback: {}", e.getMessage(), e);
            } finally {
                finish(storageParameters);
            }
        }
    }

    @Override
    public Folder[] getSubfolderObjects(String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        if (PRIVATE_ID.equals(parentId)) {
            return getSubFolders(treeId, getVisibleFolders(GroupwareFolderType.PRIVATE, storageParameters));
        }
        if (SHARED_ID.equals(parentId)) {
            return getSubFolders(treeId, getVisibleFolders(GroupwareFolderType.SHARED, storageParameters));
        }
        if (PUBLIC_ID.equals(parentId)) {
            return getSubFolders(treeId, getVisibleFolders(GroupwareFolderType.PUBLIC, storageParameters));
        }
        return new Folder[0];
    }

    /////////////////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Gets all visible folders of the specified type
     *
     * @param type The type of folders to get
     * @param storageParameters The {@link StorageParameters}
     * @return The visible folders
     * @throws OXException if an error is occurred
     */
    private List<AccountAwareContactsFolder> getVisibleFolders(GroupwareFolderType type, StorageParameters storageParameters) throws OXException {
        return getContactsAccess(storageParameters).getVisibleFolders(type);
    }

    /**
     * Gets the ID based contacts access reference from the supplied storage parameters, throwing an appropriate exception in case it is
     * absent.
     *
     * @param storageParameters The storage parameters to get the contacts access from
     * @return The contacts access
     */
    private IDBasedContactsAccess getContactsAccess(StorageParameters storageParameters) throws OXException {
        IDBasedContactsAccess access = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null == access) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAMETER_ACCESS);
        }
        return access;
    }

    /**
     * Retrieves the subfolders for the specified tree identifier
     *
     * @param treeId The tree identifier
     * @param contactsFolders The contacts folders to retrieve
     * @return The array with the retrieved folders
     */
    private Folder[] getSubFolders(String treeId, List<AccountAwareContactsFolder> contactsFolders) {
        if (null == contactsFolders || 0 == contactsFolders.size()) {
            return new Folder[0];
        }
        Folder[] folders = new Folder[contactsFolders.size()];
        for (int i = 0; i < contactsFolders.size(); i++) {
            folders[i] = getStorageFolder(treeId, getDefaultContentType(), contactsFolders.get(i));
        }
        return folders;
    }

    /**
     * Converts the specified list of folders to {@link SortableId}s
     *
     * @param folders The folder id's to convert
     * @return The {@link SortableId}s array
     */
    private SortableId[] getSortableIDs(List<? extends ContactsFolder> folders) {
        if (null == folders || 0 == folders.size()) {
            return new SortableId[0];
        }
        List<SortableId> sortableIds = new ArrayList<>(folders.size());
        for (int i = 0; i < folders.size(); i++) {
            sortableIds.add(new ContactId(folders.get(i).getId(), i, null));
        }
        return sortableIds.toArray(new SortableId[sortableIds.size()]);
    }

    /**
     * Performs possible clean-up operations after a commit/roll-back.
     *
     * @param storageParameters The storage parameters
     */
    private void finish(StorageParameters storageParameters) {
        IDBasedContactsAccess contactsAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != contactsAccess) {
            try {
                contactsAccess.finish();
                for (OXException warning : contactsAccess.getWarnings()) {
                    storageParameters.addWarning(warning);
                }
            } catch (Exception e) {
                // Ignore
                org.slf4j.LoggerFactory.getLogger(ContactsFolderStorage.class).warn("Unexpected error during finish: {}", e.getMessage(), e);
            } finally {
                if (null != storageParameters.putParameter(FOLDER_TYPE, PARAMETER_ACCESS, null)) {
                    contactsAccess.set(PARAMETER_CONNECTION(), null);
                }
            }
        }
    }

}
