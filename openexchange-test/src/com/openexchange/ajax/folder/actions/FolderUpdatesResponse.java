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
import java.util.List;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesResponse;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderUpdatesResponse}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class FolderUpdatesResponse extends CommonUpdatesResponse {

    private List<FolderObject> folders = new ArrayList<FolderObject>();

    public FolderUpdatesResponse(Response response) {
        super(response);
    }

    public void setFolders(List<FolderObject> folders) {
        this.folders = folders;
    }

    public void addFolder(FolderObject folder) {
        this.folders.add(folder);
    }

    public List<FolderObject> getFolders() {
        return this.folders;
    }
}
