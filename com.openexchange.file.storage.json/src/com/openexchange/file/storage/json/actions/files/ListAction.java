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
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.groupware.results.FilteringTimedResult;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link ListAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ListAction extends AbstractListingAction {

    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.requireBody();

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

        final List<String> ids = request.getIds();
        if (ids.stream().anyMatch((x) -> x == null)) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }

        // This is too complicated. We'd rather have layers below here aggressively check folders.

        final TimedResult<File> documents = new FilteringTimedResult<File>(fileAccess.getDocuments(ids, columns)) {
            private int threshhold = 0;

            @Override
            protected boolean accept(File thing) throws OXException {
                int i = threshhold;
                while (i < ids.size()) {
                    FileID fileID = new FileID(ids.get(i));
                    if (fileID.toUniqueID().equals(thing.getId())) {
                        threshhold = i+1;
                        break;
                    }
                    i++;
                }
                String folderForID = request.getFolderAt(i);
                return null != folderForID && folderForID.equals(thing.getFolderId());
            }

        };

        return result(documents, request);
    }

}
