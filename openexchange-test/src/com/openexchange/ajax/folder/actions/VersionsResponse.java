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
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * 
 * {@link VersionsResponse}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class VersionsResponse extends AbstractAJAXResponse {

    private List<com.openexchange.file.storage.File> versions = new ArrayList<>();

    public VersionsResponse(final Response response) {
        super(response);
    }

    public List<com.openexchange.file.storage.File> getVersions() {
        return versions;
    }

    public void setVersions(List<com.openexchange.file.storage.File> versions) {
        this.versions = versions;
    }

    public void addVersion(com.openexchange.file.storage.File version) {
        this.versions.add(version);
    }
}
