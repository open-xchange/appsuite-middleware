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

package com.openexchange.file.storage.infostore.internal;

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
    private final Date maxLastModified;

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
                LOG.debug("No default trash folder found for user {} in context {}, aborting.", session.getUserId(), session.getContextId());
                return;
            }
            /*
             * delete contained files and folders
             */
            deletedFiles = cleanupFiles(trashFolder);
            deletedFolders = cleanupFolders(trashFolder);
        } catch (Exception e) {
            LOG.warn("Unexpected error during trash cleanup run for user {} in context {}:", session.getUserId(), session.getContextId(), e.getMessage(), e);
        }
        LOG.debug("{} finished after {}ms, purged {} folders and {} files.", this, (System.currentTimeMillis() - start), deletedFolders, deletedFiles);
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
        List<UserizedFolder> deletableFolders = new ArrayList<UserizedFolder>();
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
        List<IDTuple> deletableDocuments = new ArrayList<IDTuple>();
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
                    session.getUserId(), session.getContext(), notRemoved);
                return deletableDocuments.size() - notRemoved.size();
            }
            return deletableDocuments.size();
        }
        return 0;
    }

    private SearchIterator<DocumentMetadata> searchDeletableFiles(UserizedFolder folder) throws OXException {
        int[] folderIDs = { Integer.parseInt(folder.getID()) };
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
