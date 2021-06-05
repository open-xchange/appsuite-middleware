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

package com.openexchange.groupware.infostore.database.impl.versioncontrol;

import com.openexchange.filestore.QuotaFileStorage;

/**
 * {@link VersionControlResult}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class VersionControlResult {

    private final QuotaFileStorage srcFs;
    private final QuotaFileStorage destFs;

    private final int versionId;

    private final String sourceLocation;
    private final String destLocation;

    /**
     * Initializes a new {@link VersionControlResult}.
     *
     * @param srcFs The source file storage
     * @param destFs The destination file storage
     * @param versionId The affected version identifier
     * @param sourceLocation The location in the source file storage
     * @param destLocation The location in the destination file storage
     */
    public VersionControlResult(QuotaFileStorage srcFs, QuotaFileStorage destFs, int versionId, String sourceLocation, String destLocation) {
        super();
        this.srcFs = srcFs;
        this.destFs = destFs;
        this.versionId = versionId;
        this.sourceLocation = sourceLocation;
        this.destLocation = destLocation;
    }

    /**
     * Gets the source file storage
     *
     * @return The source file storage
     */
    public QuotaFileStorage getSourceFileStorage() {
        return srcFs;
    }

    /**
     * Gets the destination file storage
     *
     * @return The destination file storage
     */
    public QuotaFileStorage getDestFileStorage() {
        return destFs;
    }

    /**
     * Gets the version identifier
     *
     * @return The version identifier
     */
    public int getVersion() {
        return versionId;
    }

    /**
     * Gets the source location
     *
     * @return The source location
     */
    public String getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Gets the destination location
     *
     * @return The destination location
     */
    public String getDestLocation() {
        return destLocation;
    }

}
