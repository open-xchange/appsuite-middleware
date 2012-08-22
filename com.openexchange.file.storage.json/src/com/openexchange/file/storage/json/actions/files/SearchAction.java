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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.filestorage.contentType.FileStorageContentType;
import com.openexchange.folderstorage.type.FileStorageType;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.QueryParameters.Order;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.filestore.FilestoreIndexField;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchAction}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Action(method = RequestMethod.PUT, name = "search", description = "Search infoitems", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields as per tables Common object data and Detailed infoitem data."),
    @Parameter(name = "sort", optional = true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional = true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "start", optional = true, description = "The start index (inclusive) in the ordered search, that is requested."),
    @Parameter(name = "end", optional = true, description = "The last index (inclusive) from the ordered search, that is requested.") }, requestBody = "An Object as described in Search contacts.", responseDescription = "")
public class SearchAction extends AbstractFileAction {
    
    private static final Log LOG = com.openexchange.log.Log.loggerFor(SearchAction.class);

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.require(Param.COLUMNS);        

        final Field sortingField = request.getSortingField();
        final SortDirection sortingOrder = request.getSortingOrder();
        final IndexFacadeService indexFacade = Services.getIndexFacade();
        SearchIterator<File> results;
        if (true) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using fallback search.");
            }
            results = searchInFileAccess(request, sortingField, sortingOrder);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using index search.");
            }
            results = searchInIndex(request, sortingField, sortingOrder);
        }        
        
        if (Field.CREATED_BY.equals(sortingField)) {
            final ServerSession serverSession = request.getSession();
            final CreatedByComparator comparator = new CreatedByComparator(
                serverSession.getUser().getLocale(),
                serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
            results = CreatedByComparator.resort(results, comparator);
        }

        return results(results, 0L, request);
    }
    
    private SearchIterator<File> searchInFileAccess(final InfostoreRequest request, Field sortingField, SortDirection sortingOrder) throws OXException {
        final IDBasedFileAccess fileAccess = request.getFileAccess();
        return fileAccess.search(
            request.getSearchQuery(),
            request.getColumns(),
            request.getSearchFolderId(),
            sortingField,
            sortingOrder,
            request.getStart(),
            request.getEnd());
    }
    
    private SearchIterator<File> searchInIndex(final InfostoreRequest request, Field sortingField, SortDirection sortingOrder) throws OXException {
        final IndexFacadeService indexFacade = Services.getIndexFacade();
        final int start = request.getStart();
        final int end = request.getEnd();        
        final ServerSession session = request.getSession();
        int contextId = session.getContextId();
        
        final List<Field> columns = request.getColumns();
        Set<FilestoreIndexField> indexFields = EnumSet.noneOf(FilestoreIndexField.class);
        for (Field column : columns) {
            indexFields.add(FilestoreIndexField.getByFileField(column));
        }
        
        
        String folderId = request.getSearchFolderId();
        List<IndexDocument<File>> documents;
        if (folderId == FileStorageFileAccess.ALL_FOLDERS) {            
            FolderService folderService = Services.getFolderService();
            // TODO: use users default folder tree
            Map<Integer, Set<String>> searchDestinations = new HashMap<Integer, Set<String>>();
            FolderResponse<UserizedFolder[]> visibleFolders = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, FileStorageContentType.getInstance(), FileStorageType.getInstance(), true, session, null);
            for (UserizedFolder folder :visibleFolders.getResponse()) {
                // TODO: do we have to check every returned element within a folder if it's readable by the searching user?
                int createdBy = folder.getCreatedBy();
                String id = folder.getID();
                Set<String> folders = searchDestinations.get(createdBy);
                if (folders == null) {
                    folders = new HashSet<String>();
                    searchDestinations.put(createdBy, folders);
                }
                
                folders.add(id);
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Going to search in multiple Indices: " + searchDestinations.toString());
            }            
            documents = new ArrayList<IndexDocument<File>>();
            boolean folderNotIndexed = false;
            outer: for (int owner : searchDestinations.keySet()) {
                Set<String> folders = searchDestinations.get(owner);
                IndexAccess<File> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, owner, contextId);
                Set<String> notIndexed = new HashSet<String>();
                for (String folder : folders) {
                    if (!indexAccess.isIndexed(Integer.toString(0), folder)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Folder " + folder + " is not indexed yet. Using fallback search.");
                        }
                        folderNotIndexed = true;
                        break outer;
                        // TODO: schedule job
                    }
                }
                
                folders.remove(notIndexed);
                QueryParameters query = buildQuery(request.getSearchQuery(), 
                    sortingField, 
                    folders, 
                    sortingOrder, 
                    start, 
                    end);
                
                IndexResult<File> result = indexAccess.query(query, indexFields);
                documents.addAll(result.getResults());
            }
            
            if (folderNotIndexed) {
                documents.clear();
                return searchInFileAccess(request, sortingField, sortingOrder);
            }
        } else {
            IndexAccess<File> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, request.getSession());
            QueryParameters query = buildQuery(request.getSearchQuery(), 
                sortingField, 
                Collections.singleton(folderId), 
                sortingOrder, 
                start, 
                end);
            
            IndexResult<File> result = indexAccess.query(query, indexFields);
            documents = result.getResults();            
        }
        
        return new FilestorageIndexSearchIterator(documents);        
    }
    
    private QueryParameters buildQuery(String searchTerm, Field sortingField, Set<String> folders, SortDirection sortingOrder, int start, int end) {
        Builder queryBuilder = new QueryParameters.Builder(searchTerm).setHandler(
            SearchHandler.SIMPLE).setFolders(folders);

        if (sortingField != null) {
            FilestoreIndexField field = FilestoreIndexField.getByFileField(sortingField);
            queryBuilder.setSortField(field);
            if (sortingOrder != null) {
                queryBuilder.setOrder(sortingOrder == SortDirection.DESC ? Order.DESC : Order.ASC);
            }
        }

        if (start != FileStorageFileAccess.NOT_SET) {
            queryBuilder.setOffset(start);
        }
        if (end != FileStorageFileAccess.NOT_SET) {
            queryBuilder.setLength(end - start);
        }
        
        QueryParameters query = queryBuilder.build();        
        return query;
    }

}
