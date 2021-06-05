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
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.groupware.results.Delta;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdatesAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(Param.FOLDER_ID, Param.COLUMNS);

        IDBasedFileAccess fileAccess = request.getFileAccess();

        long timestamp = request.getTimestamp();
        Field sortingField = request.getSortingField();
        SortDirection sortingOrder = request.getSortingOrder();

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

        Delta<File> delta = fileAccess.getDelta(
            request.getFolderId(),
            timestamp == FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER ? FileStorageFileAccess.DISTANT_PAST : timestamp,
            columns,
            sortingField,
            sortingOrder,
            request.getIgnore().contains("deleted"));

        if (Field.CREATED_BY.equals(sortingField)) {
            final ServerSession serverSession = request.getSession();
            final CreatedByComparator comparator = new CreatedByComparator(serverSession.getUser().getLocale(), serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
            final SearchIterator<File> iter = CreatedByComparator.resort(delta.results(), comparator);
            final SearchIterator<File> niter = CreatedByComparator.resort(delta.getNew(), comparator);
            final SearchIterator<File> miter = CreatedByComparator.resort(delta.getModified(), comparator);
            final SearchIterator<File> diter = CreatedByComparator.resort(delta.getDeleted(), comparator);
            final Delta<File> delegate = delta;
            delta = new Delta<File>() {

                @Override
                public SearchIterator<File> results() throws OXException {
                    return iter;
                }

                @Override
                public long sequenceNumber() throws OXException {
                    return delegate.sequenceNumber();
                }

                @Override
                public SearchIterator<File> getNew() {
                    return niter;
                }

                @Override
                public SearchIterator<File> getModified() {
                    return miter;
                }

                @Override
                public SearchIterator<File> getDeleted() {
                    return diter;
                }

            };
        }

        return result(delta, request);
    }

}
