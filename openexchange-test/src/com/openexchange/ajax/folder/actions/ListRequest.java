/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    public static final int[] DEFAULT_COLUMNS = {
        FolderObject.OBJECT_ID, FolderObject.FOLDER_ID, FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.SUBFOLDERS, FolderObject.STANDARD_FOLDER,
        FolderObject.CREATED_BY, FolderObject.PERMISSIONS_BITS };

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
