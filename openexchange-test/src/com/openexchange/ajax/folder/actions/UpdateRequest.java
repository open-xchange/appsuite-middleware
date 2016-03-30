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
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link UpdateRequest}
 *
 * @author Karsten Will <a href="mailto:karsten.will@open-xchange.com">karsten.will@open-xchange.com</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateRequest extends InsertRequest {

    private final FolderObject folder;
    private boolean handDown;
    private Boolean cascade;
    private Boolean ignoreWarnings;

    public UpdateRequest(API api, FolderObject folder, boolean failOnError) {
        super(api, folder, failOnError);
        this.folder = folder;
    }

    public UpdateRequest(API api, FolderObject folder) {
        this(api, folder, true);
    }

    public UpdateRequest setHandDown(boolean handDown) {
        this.handDown = handDown;
        return this;
    }

    public UpdateRequest setCascadePermissions(boolean cascade) {
        this.cascade = cascade;
        return this;
    }

    public UpdateRequest setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = ignoreWarnings;
        return this;
    }

    @Override
    protected void addParameters(List<Parameter> params) {
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE));
        if (FolderObject.MAIL == folder.getModule() && folder.getFullName() != null) {
            params.add(new Parameter(FolderFields.ID, folder.getFullName()));
        } else {
            params.add(new Parameter(FolderFields.ID, folder.getObjectID()));
            params.add(new Parameter(AJAXServlet.PARAMETER_INFOLDER, String.valueOf(folder.getParentFolderID())));
        }
        params.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(folder.getLastModified().getTime())));
        if (handDown) {
            params.add(new Parameter("permissions", "inherit"));
        }
        if (cascade != null) {
            params.add(new Parameter("cascadePermissions", cascade));
        }
        if (ignoreWarnings != null) {
            params.add(new Parameter("ignoreWarnings", ignoreWarnings));
        }
    }
}
