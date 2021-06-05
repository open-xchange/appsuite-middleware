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

import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;


/**
 * {@link DirectoryActionOptimizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DirectoryActionOptimizer extends AbstractActionOptimizer<DirectoryVersion> {

    /**
     * Initializes a new {@link DirectoryActionOptimizer}.
     *
     * @param mapper The file version mapper
     */
    public DirectoryActionOptimizer(VersionMapper<DirectoryVersion> mapper) {
        super(mapper);
    }

    protected static boolean matchesByPathAndChecksum(DirectoryVersion v1, DirectoryVersion v2) {
        return matchesByPath(v1, v2) && matchesByChecksum(v1, v2);
    }

    protected static boolean matchesByPath(DirectoryVersion v1, DirectoryVersion v2) {
        if (null == v1) {
            return null == v2;
        } else if (null == v2) {
            return false;
        } else {
            return null == v1.getPath() ? null == v2.getPath() : v1.getPath().equals(v2.getPath());
        }
    }

}