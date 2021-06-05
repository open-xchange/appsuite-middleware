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

package com.openexchange.file.storage.infostore.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.search.ComparablePattern;
import com.openexchange.groupware.infostore.search.ComparisonType;
import com.openexchange.groupware.infostore.search.LastModifiedUtcTerm;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TrashCleanupHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TrashCleaner implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrashCleaner.class);

    private final ServerSession session;
    final Date maxLastModified;

    private FolderService folderService;

    /**
     * Initializes a new {@link TrashCleaner}.
     *
     * @param session The session
     * @param retentionDays The number of days after which to permanently delete items in trash
     */
    public TrashCleaner(ServerSession session, int retentionDays) {
        super();
        this.session = session;
        this.maxLastModified = getMaxLastModified(retentionDays);
    }

    private FolderService getFolderService() {
        if (null == folderService) {
            folderService = Services.getService(FolderService.class);
        }
        return folderService;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        int deletedFiles = 0;
        int deletedFolders = 0;
        LOG.debug("{} starting, purging items older than {}.", this, maxLastModified);
        try {
            UserizedFolder trashFolder = getTrashFolder();
            if (null == trashFolder) {
                LOG.debug("No default trash folder found for user {} in context {}, aborting.", I(session.getUserId()), Integer.valueOf(session.getContextId()));
                return;
            }
            /*
             * delete contained files and folders
             */
            deletedFiles = cleanupFiles(trashFolder);
            deletedFolders = cleanupFolders(trashFolder);
        } catch (Exception e) {
            LOG.warn("Unexpected error during trash cleanup run for user {} in context {}:", I(session.getUserId()), Integer.valueOf(session.getContextId()), e.getMessage(), e);
        }
        LOG.debug("{} finished after {}ms, purged {} folders and {} files.", this, L(System.currentTimeMillis() - start), I(deletedFolders), I(deletedFiles));
    }

    private int cleanupFolders(UserizedFolder folder) throws OXException {
        /*
         * search deletable folders
         */
        List<UserizedFolder> deletableFolders = getDeletableFolders(folder);
        if (0 == deletableFolders.size()) {
            return 0;
        }
        FolderServiceDecorator decorator = new FolderServiceDecorator().put("hardDelete", Boolean.TRUE.toString());
        for (UserizedFolder deletableFolder : deletableFolders) {
            getFolderService().deleteFolder(FolderStorage.REAL_TREE_ID, deletableFolder.getID(), folder.getLastModifiedUTC(), session, decorator);
        }
        return deletableFolders.size();
    }

    private List<UserizedFolder> getDeletableFolders(UserizedFolder parentFolder) throws OXException {
        String[] subfolderIDs = parentFolder.getSubfolderIDs();
        if (null == subfolderIDs || 0 == subfolderIDs.length) {
            return Collections.emptyList();
        }
        List<UserizedFolder> deletableFolders = new ArrayList<>();
        FolderResponse<UserizedFolder[]> folderResponse = getFolderService().getSubfolders(FolderStorage.REAL_TREE_ID, parentFolder.getID(), true, session, null);
        if (null != folderResponse) {
            UserizedFolder[] subfolders = folderResponse.getResponse();
            if (null != subfolders && 0 < subfolders.length) {
                for (UserizedFolder folder : subfolders) {
                    if (null != folder.getLastModifiedUTC() && maxLastModified.after(folder.getLastModifiedUTC())) {
                        deletableFolders.add(folder);
                    }
                }
            }
        }
        return deletableFolders;
    }

    private int cleanupFiles(UserizedFolder folder) throws OXException {
        /*
         * search deletable documents
         */
        SearchIterator<DocumentMetadata> searchIterator = null;
        long sequenceNumber = 0;
        List<IDTuple> deletableDocuments = new ArrayList<>();
        try {
            searchIterator = searchDeletableFiles(folder);
            while (searchIterator.hasNext()) {
                DocumentMetadata document = searchIterator.next();
                if (null != document.getLastModified() && maxLastModified.after(document.getLastModified())) {
                    deletableDocuments.add(new IDTuple(folder.getID(), String.valueOf(document.getId())));
                    sequenceNumber = Math.max(sequenceNumber, document.getSequenceNumber());
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        /*
         * permanently delete them
         */
        if (0 < deletableDocuments.size()) {
            InfostoreFacade infostore = Services.getService(InfostoreFacade.class);
            List<IDTuple> notRemoved = infostore.removeDocument(deletableDocuments, sequenceNumber, session);
            if (null != notRemoved && 0 < notRemoved.size()) {
                LOG.debug("Failed to cleanup the following files for user {} in context {}: {}",
                    I(session.getUserId()), I(session.getContextId()), notRemoved);
                return deletableDocuments.size() - notRemoved.size();
            }
            return deletableDocuments.size();
        }
        return 0;
    }

    private SearchIterator<DocumentMetadata> searchDeletableFiles(UserizedFolder folder) throws OXException {
        int[] folderIDs = { Utils.parseUnsignedInt(folder.getID()) };
        SearchTerm<?> searchTerm = buildSearchTerm();
        Metadata[] columns = { Metadata.LAST_MODIFIED_LITERAL, Metadata.ID_LITERAL, Metadata.SEQUENCE_NUMBER_LITERAL };
        InfostoreSearchEngine searchEngine = Services.getService(InfostoreSearchEngine.class);
        return searchEngine.search(session, searchTerm, folderIDs, columns, null, InfostoreSearchEngine.NOT_SET, InfostoreSearchEngine.NOT_SET, InfostoreSearchEngine.NOT_SET);
    }

    private SearchTerm<?> buildSearchTerm() {
        return new LastModifiedUtcTerm(new ComparablePattern<Date>() {

            @Override
            public ComparisonType getComparisonType() {
                return ComparisonType.LESS_THAN;
            }

            @Override
            public Date getPattern() {
                return maxLastModified;
            }
        });
    }

    private UserizedFolder getTrashFolder() throws OXException {
        try {
            return getFolderService().getDefaultFolder(
                session.getUser(), FolderStorage.REAL_TREE_ID, InfostoreContentType.getInstance(), TrashType.getInstance(), session, null);
        } catch (OXException e) {
            if (false == FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.equals(e)) {
                throw e;
            }
        }
        return null;
    }

    private static Date getMaxLastModified(int retentionDays) {
        Calendar calendar = Calendar.getInstance(TimeZones.UTC);
        calendar.add(Calendar.DAY_OF_YEAR, -1 * retentionDays);
        return calendar.getTime();
    }

    @Override
    public String toString() {
        return "TrashCleaner [user=" + session.getUserId() + ", context=" + session.getContextId() + "]";
    }
}
