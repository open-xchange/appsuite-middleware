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

package com.openexchange.gdpr.dataexport;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 * {@link FileLocations}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class FileLocations {

    private final List<FileLocation> fileLocations;
    private final long lastAccessed;

    /**
     * Initializes a new {@link FileLocations}.
     *
     * @param fileLocations The file locations
     * @param lastAccessed The last-accessed time stamp
     */
    public FileLocations(List<FileLocation> fileLocations, long lastAccessed) {
        super();
        this.fileLocations = fileLocations == null ? ImmutableList.of() : ImmutableList.copyOf(fileLocations);
        this.lastAccessed = lastAccessed;
    }

    /**
     * Gets the file locations.
     *
     * @return The file locations as an immutable list
     */
    public List<FileLocation> getLocations() {
        return fileLocations;
    }

    /**
     * Gets the last-accessed time stamp as number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The last-accessed time stamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

}
