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

package com.openexchange.folderstorage.calendar;

import static com.openexchange.chronos.common.CalendarUtils.DISTANT_FUTURE;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import static com.openexchange.folderstorage.CalendarFolderConverter.getCalendarFolder;
import static com.openexchange.folderstorage.CalendarFolderConverter.getCalendarType;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolder;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolders;
import static com.openexchange.folderstorage.CalendarFolderConverter.optCalendarConfig;
import static com.openexchange.folderstorage.CalendarFolderConverter.optCalendarProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.chronos.provider.AccountAwareCalendarFolder;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.SubfolderListingFolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.calendar.contentType.CalendarContentType;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.folderstorage.type.CalendarType;
import com.openexchange.user.User;

/**
 * {@link CalendarFolderStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarFolderStorage implements SubfolderListingFolderStorage {

    /** The calendar folder type */
    private static final FolderType FOLDER_TYPE = new CalendarFolderType();

    /** The parameter name used to store the {@link IDBasedCalendarAccess} reference in the storage parameters */
    private static final String PARAMETER_ACCESS = IDBasedCalendarAccess.class.getName();

    private final IDBasedCalendarAccessFactory accessFactory;

    /**
     * Initializes a new {@link CalendarFolderStorage}.
     *
     * @param accessFactory The underlying ID-based calendar access factory
     */
    public CalendarFolderStorage(IDBasedCalendarAccessFactory accessFactory) {
        super();
        this.accessFactory = accessFactory;
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
    public boolean startTransaction(StorageParameters parameters, boolean modify) throws OXException {
        /*
         * initialize ID based calendar access if necessary
         */
        if (null == parameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS)) {
            /*
             * ensure the session is present
             */
            if (null == parameters.getSession()) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create();
            }
            /*
             * create access via factory
             */
            IDBasedCalendarAccess calendarAccess = accessFactory.createAccess(parameters.getSession());
            if (parameters.putParameterIfAbsent(FOLDER_TYPE, PARAMETER_ACCESS, calendarAccess)) {
                /*
                 * transfer storage- to calendar parameters
                 */
                if (StorageParametersUtility.getBoolParameter("ignoreWarnings", parameters)) {
                    calendarAccess.set(CalendarParameters.PARAMETER_IGNORE_STORAGE_WARNINGS, Boolean.TRUE);
                }
                /*
                 * enqueue in managed transaction if possible, otherwise signal that we started the transaction ourselves
                 */
                if (false == TransactionManager.isManagedTransaction(parameters)) {
                    return true;
                }
                TransactionManager transactionManager = TransactionManager.getTransactionManager(parameters);
                if (null == transactionManager) {
                    return true;
                }
                calendarAccess.set(PARAMETER_CONNECTION(), transactionManager.getConnection());
                transactionManager.transactionStarted(this);
            }
        }
        return false;
    }

    @Override
    public void rollback(StorageParameters storageParameters) {
        IDBasedCalendarAccess calendarAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != calendarAccess) {
            try {
                calendarAccess.rollback();
            } catch (Exception e) {
                // Ignore
                org.slf4j.LoggerFactory.getLogger(CalendarFolderStorage.class).warn("Unexpected error during rollback: {}", e.getMessage(), e);
            } finally {
                finish(storageParameters);
            }
        }
    }

    @Override
    public void commitTransaction(StorageParameters storageParameters) throws OXException {
        IDBasedCalendarAccess calendarAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != calendarAccess) {
            try {
                calendarAccess.commit();
            } finally {
                finish(storageParameters);
            }
        }
    }

    /**
     * Performs possible clean-up operations after a commit/roll-back.
     *
     * @param storageParameters The storage parameters
     */
    private void finish(StorageParameters storageParameters) {
        IDBasedCalendarAccess calendarAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null != calendarAccess) {
            try {
                calendarAccess.finish();
                for (OXException warning : calendarAccess.getWarnings()) {
                    storageParameters.addWarning(warning);
                }
            } catch (Exception e) {
                // Ignore
                org.slf4j.LoggerFactory.getLogger(CalendarFolderStorage.class).warn("Unexpected error during finish: {}", e.getMessage(), e);
            } finally {
                if (null != storageParameters.putParameter(FOLDER_TYPE, PARAMETER_ACCESS, null)) {
                    calendarAccess.set(PARAMETER_CONNECTION(), null);
                }
            }
        }
    }

    @Override
    public void clearCache(int userId, int contextId) {
        // unused
    }

    @Override
    public void restore(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        // unused
    }

    @Override
    public Folder prepareFolder(String treeId, Folder folder, StorageParameters storageParameters) throws OXException {
        // unused
        return folder;
    }

    @Override
    public void checkConsistency(String treeId, StorageParameters storageParameters) throws OXException {
        // unused
    }

    @Override
    public SortableId[] getVisibleFolders(String rootFolderId, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        return getVisibleFolders(treeId, contentType, type, storageParameters);
    }

    @Override
    public SortableId[] getVisibleFolders(String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        if (false == CalendarContentType.class.isInstance(contentType)) {
            return new SortableId[0];
        }
        return getSortableIDs(getVisibleFolders(getCalendarType(type), storageParameters));
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        throw new UnsupportedOperationException("CalendarFolderStorage.getUserSharedFolders()");
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { CalendarContentType.getInstance() };
    }

    @Override
    public ContentType getDefaultContentType() {
        return CalendarContentType.getInstance();
    }

    @Override
    public void createFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        IDBasedCalendarAccess calendarAccess = getCalendarAccess(storageParameters);
        CalendarFolder folderToCreate = getCalendarFolder(folder);
        String providerId = optCalendarProvider(folder);
        JSONObject userConfig = optCalendarConfig(folder);
        String newFolderId = calendarAccess.createFolder(providerId, folderToCreate, userConfig);
        folder.setID(newFolderId);
    }

    @Override
    public void updateFolder(Folder folder, StorageParameters storageParameters) throws OXException {
        /*
         * update folder
         */
        IDBasedCalendarAccess calendarAccess = getCalendarAccess(storageParameters);
        long timestamp = null != storageParameters.getTimeStamp() ? storageParameters.getTimeStamp().getTime() : DISTANT_FUTURE;
        CalendarFolder folderToUpdate = getCalendarFolder(folder);
        JSONObject userConfig = optCalendarConfig(folder);
        String updatedFolderID = calendarAccess.updateFolder(folder.getID(), folderToUpdate, userConfig, timestamp);
        /*
         * take over updated identifiers in passed folder reference
         */
        folder.setID(updatedFolderID);
        folder.setLastModified(folderToUpdate.getLastModified());
    }

    @Override
    public void clearFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        throw new UnsupportedOperationException("CalendarFolderStorage.clearFolder()");
    }

    @Override
    public void deleteFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        IDBasedCalendarAccess calendarAccess = getCalendarAccess(storageParameters);
        long timestamp = null != storageParameters.getTimeStamp() ? storageParameters.getTimeStamp().getTime() : DISTANT_FUTURE;
        calendarAccess.deleteFolder(folderId, timestamp);
    }

    @Override
    public String getDefaultFolderID(User user, String treeId, ContentType contentType, Type type, StorageParameters storageParameters) throws OXException {
        if (false == CalendarContentType.class.isInstance(contentType)) {
            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentType.toString());
        }
        if (false == GroupwareFolderType.PRIVATE.equals(getCalendarType(type))) {
            throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(contentType, treeId);
        }
        IDBasedCalendarAccess calendarAccess = getCalendarAccess(storageParameters);
        CalendarFolder folder = calendarAccess.getDefaultFolder();
        if (null == folder) {
            throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(contentType, treeId);
        }
        return folder.getId();
    }

    @Override
    public Type getTypeByParent(User user, String treeId, String parentId, StorageParameters storageParameters) throws OXException {
        return CalendarType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(User user, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        //TODO
        return false;
    }

    @Override
    public boolean isEmpty(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        //TODO
        return false;
    }

    @Override
    public void updateLastModified(long lastModified, String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        //
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIds, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(String treeId, List<String> folderIds, StorageType storageType, StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        List<AccountAwareCalendarFolder> calendarFolders = getCalendarAccess(storageParameters).getFolders(folderIds);
        return getStorageFolders(treeId, getDefaultContentType(), calendarFolders);
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    @Override
    public Folder getFolder(String treeId, String folderId, StorageType storageType, StorageParameters storageParameters) throws OXException {
        if (StorageType.BACKUP.equals(storageType)) {
            throw FolderExceptionErrorMessage.UNSUPPORTED_STORAGE_TYPE.create(storageType);
        }
        AccountAwareCalendarFolder calendarFolder = getCalendarAccess(storageParameters).getFolder(folderId);
        return getStorageFolder(treeId, getDefaultContentType(), calendarFolder, calendarFolder.getAccount());
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
    public String[] getDeletedFolderIDs(String treeId, Date timeStamp, StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    @Override
    public String[] getModifiedFolderIDs(String treeId, Date timeStamp, ContentType[] includeContentTypes, StorageParameters storageParameters) throws OXException {
        return new String[0];
    }

    private List<AccountAwareCalendarFolder> getVisibleFolders(GroupwareFolderType type, StorageParameters storageParameters) throws OXException {
        return getCalendarAccess(storageParameters).getVisibleFolders(type);
    }

    /**
     * Gets the ID based calendar access reference from the supplied storage parameters, throwing an appropriate exception in case it is
     * absent.
     *
     * @param storageParameters The storage parameters to get the calendar access from
     * @return The calendar access
     */
    private static IDBasedCalendarAccess getCalendarAccess(StorageParameters storageParameters) throws OXException {
        IDBasedCalendarAccess calendarAccess = storageParameters.getParameter(FOLDER_TYPE, PARAMETER_ACCESS);
        if (null == calendarAccess) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(PARAMETER_ACCESS);
        }
        return calendarAccess;
    }

    private Folder[] getSubFolders(String treeId, List<AccountAwareCalendarFolder> calendarFolders) {
        if (null == calendarFolders || 0 == calendarFolders.size()) {
            return new Folder[0];
        }
        Folder[] folders = new Folder[calendarFolders.size()];
        for (int i = 0; i < calendarFolders.size(); i++) {
            folders[i] = getStorageFolder(treeId, getDefaultContentType(), calendarFolders.get(i));
        }
        return folders;
    }

    private static SortableId[] getSortableIDs(List<? extends CalendarFolder> folders) {
        if (null == folders || 0 == folders.size()) {
            return new SortableId[0];
        }
        List<SortableId> sortableIds = new ArrayList<SortableId>(folders.size());
        for (int i = 0; i < folders.size(); i++) {
            sortableIds.add(new CalendarId(folders.get(i).getId(), i, null));
        }
        return sortableIds.toArray(new SortableId[sortableIds.size()]);
    }

}
