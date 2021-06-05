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
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link RootRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RootRequest extends AbstractFolderRequest<ListResponse> {

    private static final int[] DEFAULT_COLUMNS = { FolderObject.OBJECT_ID, FolderObject.MODULE, FolderObject.FOLDER_NAME, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER, FolderObject.CREATED_BY };

    private final int[] columns;

    private final boolean ignoreMail;

    private boolean altNames = true;

    public RootRequest(final API api, final int[] columns, final boolean ignoreMail) {
        super(api);
        this.columns = columns;
        this.ignoreMail = ignoreMail;
    }

    public RootRequest(final API api) {
        this(api, DEFAULT_COLUMNS, false);
    }

    public RootRequest(final API api, final boolean ignoreMail) {
        this(api, DEFAULT_COLUMNS, ignoreMail);
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
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ROOT));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (ignoreMail) {
            params.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, "mailfolder"));
        }
        if (altNames) {
            params.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
    }

    @Override
    public ListParser getParser() {
        return new ListParser(columns, true);
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }
}
