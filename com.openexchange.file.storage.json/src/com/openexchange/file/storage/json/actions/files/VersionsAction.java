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
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link VersionsAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class VersionsAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(Param.ID);

        IDBasedFileAccess fileAccess = request.getFileAccess();

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
        if (!columns.contains(File.Field.VERSION)) {
            if (!copy) {
                columns = new ArrayList<File.Field>(columns);
                copy = true;
            }
            columns.add(File.Field.VERSION);
        }

        final Field sortingField = request.getSortingField();
        final SortDirection sortingOrder = request.getSortingOrder();
        TimedResult<File> versions = fileAccess.getVersions(request.getId(), columns, sortingField, sortingOrder);

        if (Field.CREATED_BY.equals(sortingField)) {
            final ServerSession serverSession = request.getSession();
            final CreatedByComparator comparator = new CreatedByComparator(serverSession.getUser().getLocale(), serverSession.getContext()).setDescending(SortDirection.DESC.equals(sortingOrder));
            final SearchIterator<File> iter = CreatedByComparator.resort(versions.results(), comparator);
            final TimedResult<File> delegate = versions;
            versions = new TimedResult<File>() {

                @Override
                public long sequenceNumber() throws OXException {
                    return delegate.sequenceNumber();
                }

                @Override
                public SearchIterator<File> results() throws OXException {
                    return iter;
                }
            };
        }

        return result( skipVersion0( versions ), request);
    }

    /**
     * Skips the version 0 of the given versions
     *
     * @param versions The versions as a {@link TimedResult}
     * @return A {@link TimedResult} not containing the version 0
     */
    private TimedResult<File> skipVersion0(final TimedResult<File> versions) {

        return new TimedResult<File>() {

            @Override
            public SearchIterator<File> results() throws OXException {
                return new FilteringSearchIterator<File>(versions.results()) {

                    @Override
                    public boolean accept(final File thing) throws OXException {
                        final String version = thing.getVersion();
                        return version == null || !version.equals("0");
                    }
                };
            }

            @Override
            public long sequenceNumber() throws OXException {
                return versions.sequenceNumber();
            }

        };
    }

}
