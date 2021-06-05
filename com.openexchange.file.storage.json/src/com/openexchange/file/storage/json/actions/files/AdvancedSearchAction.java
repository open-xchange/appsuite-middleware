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

package com.openexchange.file.storage.json.actions.files;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AdvancedSearchAction}
 *
 * @author <a href="mailto:alexander.schulze-ardey@open-xchange.com">Alexander Schulze-Ardey</a>
 * @since v7.10.5
 */
public class AdvancedSearchAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(Param.COLUMNS);

        List<Field> columns = request.getFieldsToLoad();
        boolean copy = false;
        if (!columns.contains(File.Field.FOLDER_ID)) {
            columns = new ArrayList<File.Field>(columns);
            columns.add(File.Field.FOLDER_ID);
            copy = true;
        }
        if (!columns.contains(File.Field.ID)) {
            if (!copy) {
                columns = new ArrayList<File.Field>(columns);
                copy = true;
            }
            columns.add(File.Field.ID);
        }

        boolean includeSubfolders = request.getBoolParameter("includeSubfolders");
        Field sortingField = request.getSortingField();
        SortDirection sortingOrder = request.getSortingOrder();
        IDBasedFileAccess fileAccess = request.getFileAccess();

        SearchTerm<?> searchTerm = request.getSearchTerm();

        SearchIterator<File> results = fileAccess.search(request.getSearchFolderId(), includeSubfolders, searchTerm, columns, sortingField, sortingOrder, request.getStart(), request.getEnd());
        try {
            if (Field.CREATED_BY.equals(sortingField)) {
                ServerSession serverSession = request.getSession();
                CreatedByComparator comparator = new CreatedByComparator(serverSession.getUser().getLocale(), serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
                results = CreatedByComparator.resort(results, comparator);
            }
            // limit results if a limit is defined
            int limit = 0;
            if (request.getStart() == 0 && request.getEnd() != 0) {
                limit = request.getEnd() - request.getStart() + 1;
            }
            if (limit != 0 && results.size() > limit) {
                ArrayList<File> resultList = new ArrayList<File>(limit);
                for (int x = 0; x < limit && results.hasNext(); x++) {
                    resultList.add(results.next());
                }
                results = new SearchIteratorAdapter<File>(resultList.iterator());
            }
            AJAXRequestResult requestResult = results(results, 0L, request);
            results = null; // Avoid premature closing
            return requestResult;
        } finally {
            SearchIterators.close(results);
        }
    }
}
