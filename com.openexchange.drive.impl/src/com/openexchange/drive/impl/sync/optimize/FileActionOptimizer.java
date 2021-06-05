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

package com.openexchange.drive.impl.sync.optimize;

import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.comparison.VersionMapper;


/**
 * {@link FileActionOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FileActionOptimizer extends AbstractActionOptimizer<FileVersion> {

    /**
     * Initializes a new {@link FileActionOptimizer}.
     *
     * @param mapper The file version mapper
     */
    public FileActionOptimizer(VersionMapper<FileVersion> mapper) {
        super(mapper);
    }

    protected static boolean matchesByNameAndChecksum(FileVersion v1, FileVersion v2) {
        return matchesByName(v1, v2) && matchesByChecksum(v1, v2);
    }

    protected static boolean matchesByName(FileVersion v1, FileVersion v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return false;
        } else {
            return null == v1.getName() ? null == v2.getName() : v1.getName().equals(v2.getName());
        }
    }

    /**
     * Gets a value indicating whether the supplied file version represents the virtual <code>.drive-meta</code> file or not.
     *
     * @param session The sync session
     * @param fileVersion The file version to check
     * @return <code>true</code> if the file version represents a <code>.drive-meta</code> file, <code>false</code>, otherwise
     */
    protected static boolean isDriveMeta(FileVersion fileVersion) {
        return null != fileVersion && DriveConstants.METADATA_FILENAME.equals(fileVersion.getName());
    }

}