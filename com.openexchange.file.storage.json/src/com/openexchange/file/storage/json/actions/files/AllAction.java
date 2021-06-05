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

import static com.openexchange.file.storage.json.actions.files.AbstractFileAction.Param.FOLDER_ID;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.groupware.results.Results;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AllAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AllAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.require(FOLDER_ID);
        IDBasedFileAccess fileAccess = request.getFileAccess();
        String folderId = request.getFolderId();
        if (Strings.isEmpty(folderId)) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(Param.FOLDER_ID.getName());
        }
        Field sortingField = request.getSortingField();
        SortDirection sortingOrder = request.getSortingOrder();

        int[] indexes = AJAXRequestDataTools.parseFromToIndexes(request.getRequestData());

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

        TimedResult<File> documents;
        if ((null == indexes) || Field.CREATED_BY.equals(sortingField)) {
            documents = fileAccess.getDocuments(folderId, columns, sortingField, sortingOrder);

            if (Field.CREATED_BY.equals(sortingField)) {
                ServerSession serverSession = request.getSession();
                CreatedByComparator comparator = new CreatedByComparator(serverSession.getUser().getLocale(), serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
                SearchIterator<File> iter = CreatedByComparator.resort(documents.results(), comparator);
                documents = new DelegatingTimedResult(iter, documents.sequenceNumber());
            }

            if (null != indexes) {
                documents = slice(documents, indexes);
            }
        } else {
            documents = fileAccess.getDocuments(folderId, columns, sortingField, sortingOrder, Range.valueOf(indexes));
        }

        return result( documents, request );
    }

    private TimedResult<File> slice(TimedResult<File> documents, int[] indexes) throws OXException {
        if (null == indexes) {
            return documents;
        }

        int from = indexes[0];
        int to = indexes[1];
        if (from >= to) {
            return Results.emptyTimedResult();
        }

        SearchIterator<File> iter = documents.results();
        try {
            int index = 0;
            while (index < from) {
                if (false == iter.hasNext()) {
                    return Results.emptyTimedResult();
                }
                iter.next();
                index++;
            }

            List<File> files = new LinkedList<File>();
            while (index < to && iter.hasNext()) {
                files.add(iter.next());
                index++;
            }

            return new ListBasedTimedResult(files, documents.sequenceNumber());
        } finally {
            SearchIterators.close(iter);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static final class ListBasedTimedResult implements TimedResult<File> {

        private final long sequenceNumber;
        private final SearchIterator<File> results;

        /**
         * Initializes a new {@link TimedResultImplementation}.
         */
        ListBasedTimedResult(List<File> files, long sequenceNumber) {
            super();
            this.results = new SearchIteratorDelegator<>(files);
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            return results;
        }
    }

    private static final class DelegatingTimedResult implements TimedResult<File> {

        private final long sequenceNumber;
        private final SearchIterator<File> results;

        /**
         * Initializes a new {@link TimedResultImplementation}.
         */
        DelegatingTimedResult(SearchIterator<File> results, long sequenceNumber) {
            super();
            this.results = results;
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            return results;
        }
    }

}
