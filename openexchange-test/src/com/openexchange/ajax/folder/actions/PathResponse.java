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

package com.openexchange.ajax.folder.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonListResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link PathResponse}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PathResponse extends CommonListResponse {

    /**
     * @param response
     */
    public PathResponse(final Response response) {
        super(response);
    }

    public Iterator<FolderObject> getFolder() throws OXException {
        final List<FolderObject> folders = new ArrayList<FolderObject>();
        for (final Object[] rows : this) {
            final FolderObject folder = new FolderObject();
            for (int columnPos = 0; columnPos < getColumns().length; columnPos++) {
                Parser.parse(rows[columnPos], getColumns()[columnPos], folder);
            }
            folders.add(folder);
        }
        return folders.iterator();
    }
}
