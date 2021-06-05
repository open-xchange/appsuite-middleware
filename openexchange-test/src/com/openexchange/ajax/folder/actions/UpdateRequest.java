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

import static com.openexchange.java.Autoboxing.B;
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

    private static final Long FAR_FUTURE = Long.valueOf(Long.MAX_VALUE);

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
        this.cascade = B(cascade);
        return this;
    }

    public UpdateRequest setIgnoreWarnings(boolean ignoreWarnings) {
        this.ignoreWarnings = B(ignoreWarnings);
        return this;
    }

    public UpdateRequest setIgnorePermission(boolean ignorePermissions) {
        this.ignorePermissions = ignorePermissions;
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
        params.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(folder.getMeta() != null ? folder.getMeta().getOrDefault("timestamp", FAR_FUTURE) : FAR_FUTURE)));
        if (handDown) {
            params.add(new Parameter("permissions", "inherit"));
        }
        if (cascade != null) {
            params.add(new Parameter("cascadePermissions", cascade.booleanValue()));
        }
        if (ignoreWarnings != null) {
            params.add(new Parameter("ignoreWarnings", ignoreWarnings.booleanValue()));
        }
    }
}
