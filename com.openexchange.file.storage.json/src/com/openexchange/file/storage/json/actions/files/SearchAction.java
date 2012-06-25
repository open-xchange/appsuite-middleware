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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
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

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.require(Param.COLUMNS);
        final Field sortingField = request.getSortingField();
        final SortDirection sortingOrder = request.getSortingOrder();

        final IDBasedFileAccess fileAccess = request.getFileAccess();
        SearchIterator<File> results;
        results = fileAccess.search(
            request.getSearchQuery(),
            request.getColumns(),
            request.getSearchFolderId(),
            sortingField,
            sortingOrder,
            request.getStart(),
            request.getEnd());
        if (Field.CREATED_BY.equals(sortingField)) {
            final ServerSession serverSession = request.getSession();
            final CreatedByComparator comparator = new CreatedByComparator(
                serverSession.getUser().getLocale(),
                serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
            results = CreatedByComparator.resort(results, comparator);
        }

        return results(results, 0L, request);
    }
    
    /*
     * TODO: Lots of stuff to do here:
     * 
     *  - if folderId is null, we have to look up all visible infostore folders for this user
     *    and have to search within them parallel. For every folder we have to check if it's
     *    already indexed. If not, we have to index it (we have to build a job for this).
     *    Also we need to know every folders owner. For every owner the according IndexAccess
     *    has to be acquired. 
     *    
     *  - if folderId is not null, we have to check the users read permissions.
     *  
     *  - Maybe we have to introduce an account parameter to allow multiple storage accounts.
     *    In this case we also need a service parameter. Talk with Cisco about this.
     *    
     */
    private void searchInIndex(final InfostoreRequest request) throws OXException {
        final IndexFacadeService indexFacade = Services.getIndexFacade();
        final Field sortingField = request.getSortingField();
        final SortDirection sortingOrder = request.getSortingOrder();
        IndexAccess<File> indexAccess = indexFacade.acquireIndexAccess(Types.INFOSTORE, request.getSession());
        String folderId = request.getSearchFolderId();
        // TODO: No account here. Introduce wildcard?
        if (indexAccess.isIndexed(Integer.toString(0), folderId)) {
            Builder queryBuilder = new QueryParameters.Builder(request.getSearchQuery()).setType(Type.INFOSTORE_DOCUMENT).setHandler(
                SearchHandler.SIMPLE).setFolder(folderId);

            if (sortingField != null) {
                FilestoreIndexField field = FilestoreIndexField.getByFileField(sortingField);
                queryBuilder.setSortField(field);
                if (sortingOrder != null) {
                    queryBuilder.setOrder(sortingOrder == SortDirection.DESC ? Order.DESC : Order.ASC);
                }
            }

            int start = request.getStart();
            int end = request.getEnd();
            if (start != FileStorageFileAccess.NOT_SET) {
                queryBuilder.setOffset(start);
            }
            if (end != FileStorageFileAccess.NOT_SET) {
                queryBuilder.setLength(end - start);
            }
            QueryParameters query = queryBuilder.build();
            List<Field> columns = request.getColumns();
            Set<FilestoreIndexField> indexFields = EnumSet.noneOf(FilestoreIndexField.class);
            for (Field column : columns) {
                indexFields.add(FilestoreIndexField.getByFileField(column));
            }

            IndexResult<File> result = indexAccess.query(query, indexFields);
            List<IndexDocument<File>> documents = result.getResults();
            SearchIterator<File> results = new FilestorageIndexSearchIterator(documents);
        }
    }

}
