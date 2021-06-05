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
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link VisibleFoldersRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VisibleFoldersRequest extends AbstractFolderRequest<VisibleFoldersResponse> {

    public static final int[] DEFAULT_COLUMNS = { FolderObject.OBJECT_ID, FolderObject.MODULE, FolderObject.TYPE, FolderObject.FOLDER_NAME, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER, FolderObject.CREATED_BY, FolderObject.PERMISSIONS_BITS };

    private final String contentType;

    private final int[] columns;

    private final boolean failOnError;

    private boolean altNames = false;

    /**
     * Initializes a new {@link VisibleFoldersRequest}.
     *
     * @param api The API version to use
     * @param contentType The content type as a string; e.g. "calendar", "contacts", or "tasks"
     * @param columns The columns which shall be available in returned folder objects
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    public VisibleFoldersRequest(final API api, final String contentType, final int[] columns, boolean failOnError) {
        super(api);
        this.contentType = contentType;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    /**
     * Initializes a new {@link VisibleFoldersRequest}.
     *
     * @param api The API version to use
     * @param contentType The content type as a string; e.g. "calendar", "contacts", or "tasks"
     * @param columns The columns which shall be available in returned folder objects
     */
    public VisibleFoldersRequest(final API api, final String contentType, final int[] columns) {
        this(api, contentType, columns, true);
    }

    /**
     * Initializes a new {@link VisibleFoldersRequest} with default columns.
     *
     * @param api The API version to use
     * @param contentType The content type as a string; e.g. "calendar", "contacts", or "tasks"
     */
    public VisibleFoldersRequest(final API api, final String contentType) {
        this(api, contentType, DEFAULT_COLUMNS);
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
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "allVisible"));
        params.add(new Parameter(Folder.PARAMETER_CONTENT_TYPE, contentType));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (altNames) {
            params.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
    }

    @Override
    public VisibleFoldersParser getParser() {
        return new VisibleFoldersParser(columns, failOnError);
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }
}
