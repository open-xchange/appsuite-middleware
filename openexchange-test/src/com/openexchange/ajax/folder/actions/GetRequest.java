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
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.container.FolderObject;

/**
 * Request to get a folder from the server.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GetRequest extends AbstractFolderRequest<GetResponse> {

    class GetParser extends AbstractAJAXParser<GetResponse> {

        GetParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected GetResponse createResponse(final Response response) {
            return new GetResponse(response);
        }
    }

    private final boolean failOnError;

    private final String folderIdentifier;

    private final int[] columns;

    private boolean altNames = false;

    /**
     * Initializes a new {@link GetRequest} for specified columns
     */
    public GetRequest(final API api, final String folderIdentifier, final int[] columns, final boolean failOnError) {
        super(api);
        this.folderIdentifier = folderIdentifier;
        this.columns = columns;
        this.failOnError = failOnError;
    }

    public GetRequest(final API api, final String folderIdentifier, final boolean failOnError) {
        this(api, folderIdentifier, FolderObject.ALL_COLUMNS, failOnError);
    }

    public GetRequest(final API api, final String folderIdentifier) {
        this(api, folderIdentifier, FolderObject.ALL_COLUMNS, true);
    }

    public GetRequest(final API api, final int folderId, final int[] columns) {
        this(api, Integer.toString(folderId), columns, true);
    }

    public GetRequest(final API api, final String folderId, final int[] columns) {
        this(api, folderId, columns, true);
    }

    public GetRequest(final API api, final int folderId) {
        this(api, Integer.toString(folderId), FolderObject.ALL_COLUMNS, true);
    }

    public GetRequest(final API api, final int folderId, final boolean failOnError) {
        this(api, Integer.toString(folderId), FolderObject.ALL_COLUMNS, failOnError);
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
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_GET));
        params.add(new Parameter(AJAXServlet.PARAMETER_ID, folderIdentifier));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (altNames) {
            params.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
    }

    @Override
    public GetParser getParser() {
        return new GetParser(failOnError);
    }

    protected String getFolderIdentifier() {
        return folderIdentifier;
    }

    protected int[] getColumns() {
        return columns;
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }
}
