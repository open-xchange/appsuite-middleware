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
import com.openexchange.java.Strings;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ListRequest extends AbstractFolderRequest<ListResponse> {

    public static final int[] DEFAULT_COLUMNS = { FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER, FolderObject.CREATED_BY, FolderObject.PERMISSIONS_BITS };

    private final String parentFolder;

    private final int[] columns;

    private final boolean ignoreMail;

    private final Modules[] allowedModules;

    private final boolean failOnError;

    private boolean altNames = false;

    public ListRequest(final API api, final String parentFolder, final int[] columns, final boolean ignoreMail, final Modules[] allowedModules, final boolean failOnError) {
        super(api);
        this.parentFolder = parentFolder;
        this.columns = columns;
        this.ignoreMail = ignoreMail;
        this.allowedModules = allowedModules;
        this.failOnError = failOnError;
    }

    public ListRequest(final API api, final String parentFolder, final int[] columns, final boolean ignoreMail, final boolean failOnError) {
        this(api, parentFolder, columns, ignoreMail, null, failOnError);
    }

    public ListRequest(final API api, final String parentFolder, final int[] columns, final boolean ignoreMail) {
        this(api, parentFolder, columns, ignoreMail, null, true);
    }

    public ListRequest(final API api, final String parentFolder) {
        this(api, parentFolder, DEFAULT_COLUMNS, false);
    }

    public ListRequest(final API api, final int parentFolder) {
        this(api, Integer.toString(parentFolder), false);
    }

    public ListRequest(final API api, final String parentFolder, final boolean ignoreMail) {
        this(api, parentFolder, DEFAULT_COLUMNS, ignoreMail);
    }

    public ListRequest(final API api, final String parentFolder, final boolean ignoreMail, final boolean failOnError) {
        this(api, parentFolder, DEFAULT_COLUMNS, ignoreMail, failOnError);
    }

    public ListRequest(final API api, final String parentFolder, final Modules[] allowedModules) {
        this(api, parentFolder, DEFAULT_COLUMNS, false, allowedModules, true);
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
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        params.add(new Parameter(Folder.PARAMETER_PARENT, parentFolder));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (ignoreMail) {
            params.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, "mailfolder"));
        }
        if (null != allowedModules && allowedModules.length > 0) {
            params.add(new Parameter("allowed_modules", Strings.join(allowedModules, ",")));
        }
        if (altNames) {
            params.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
    }

    @Override
    public ListParser getParser() {
        return new ListParser(columns, failOnError);
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }
}
