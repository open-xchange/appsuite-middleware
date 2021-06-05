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

import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractColumnsParser;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link PathRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PathRequest extends AbstractFolderRequest<PathResponse> {

    private static final int[] DEFAULT_COLUMNS = { FolderObject.OBJECT_ID, FolderObject.MODULE, FolderObject.FOLDER_NAME, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER, FolderObject.CREATED_BY, FolderObject.PERMISSIONS_BITS };

    private final String folder;
    private final int[] columns;
    private final boolean failOnError;
    private boolean altNames = false;

    public PathRequest(final API api, final String folderId, final int[] columns, final boolean failOnError) {
        super(api);
        this.folder = folderId;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    public PathRequest(final API api, final String folderId, final int[] columns) {
        this(api, folderId, columns, true);
    }

    public PathRequest(final API api, final String folderId) {
        this(api, folderId, DEFAULT_COLUMNS, true);
    }

    public PathRequest(final API api, final int folderId, final int[] columns, final boolean failOnError) {
        this(api, Integer.toString(folderId), columns, failOnError);
    }

    public PathRequest(final API api, final int folderId, final int[] columns) {
        this(api, Integer.toString(folderId), columns, true);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    protected void addParameters(final List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_PATH));
        params.add(new Parameter(Folder.PARAMETER_ID, folder));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (altNames) {
            params.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
    }

    @Override
    public PathParser getParser() {
        return new PathParser(columns, failOnError);
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }

    private static class PathParser extends AbstractColumnsParser<PathResponse> {

        public PathParser(final int[] columns, final boolean failOnError) {
            super(failOnError, columns);
        }

        @Override
        protected PathResponse instantiateResponse(final Response response) {
            return new PathResponse(response);
        }
    }
}
