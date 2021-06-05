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

package com.openexchange.drive.impl.comparison;

import java.util.Collection;
import com.openexchange.drive.DirectoryVersion;


/**
 * {@link DirectoryVersionMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryVersionMapper extends VersionMapper<DirectoryVersion> {

    /**
     * Initializes a new {@link VersionMapper} using collections of original-, client- and server directories.
     *
     * @param originalVersions The original, i.e. previously known versions
     * @param clientVersions The current client versions
     * @param serverVersions The current server versions
     */
    public DirectoryVersionMapper(Collection<? extends DirectoryVersion> originalVersions,
        Collection<? extends DirectoryVersion> clientVersions, Collection<? extends DirectoryVersion> serverVersions) {
        super(originalVersions, clientVersions, serverVersions);
    }

    @Override
    protected String getKey(DirectoryVersion version) {
        return version.getPath();
    }

}
